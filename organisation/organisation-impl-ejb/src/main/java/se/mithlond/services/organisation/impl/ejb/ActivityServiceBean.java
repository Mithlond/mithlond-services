/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-impl-ejb
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.mithlond.services.organisation.impl.ejb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.organisation.api.ActivityService;
import se.mithlond.services.organisation.api.parameters.ActivitySearchParameters;
import se.mithlond.services.organisation.api.transport.activity.AdmissionVO;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.activity.Admission;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.finance.Amount;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;
import se.mithlond.services.shared.spi.jpa.JpaUtilities;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * ActivityService Stateless EJB implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Stateless
public class ActivityServiceBean extends AbstractJpaService implements ActivityService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(ActivityServiceBean.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Activity> getActivities(final ActivitySearchParameters parameters) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Activity getActivity(final long activityID) {

        final Activity activity = findByPrimaryKey(Activity.class, activityID);

        // Ensure that all lazily loaded collections are loaded.
        final Set<Admission> admissions = activity.getAdmissions();
        if (admissions.size() < 1) {
            log.warn("No admissions found for activity [" + activity.getShortDesc()
                    + "] with id [" + activityID + "]");
        }

        // All done.
        return activity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    public Activity createActivity(final String organisationName,
            final String shortDesc,
            final String fullDesc,
            final ZonedDateTime startTime,
            final ZonedDateTime endTime,
            final Amount cost,
            final Amount lateAdmissionCost,
            final LocalDate lateAdmissionDate,
            final LocalDate lastAdmissionDate,
            final boolean cancelled,
            final String dressCode,
            final String addressCategory,
            final Address location,
            final String addressShortDescription,
            final String responsibleGroupName,
            final Set<AdmissionVO> admissions,
            final boolean isOpenToGeneralPublic,
            final Membership activeMembership) {

        // Check sanity
        validateActivityData(organisationName,
                shortDesc,
                fullDesc,
                startTime,
                endTime,
                addressCategory,
                location,
                addressShortDescription,
                admissions);
        Validate.notNull(activeMembership, "activeMembership");

        // #1) Find the Category with the supplied 'category'
        final Category locationCategory = getAddressLocationCategory(addressCategory, entityManager)
                .orElseThrow(() -> new IllegalArgumentException("Address location category ["
                        + addressCategory + "] not found within persistent storage."));

        // #2) Find the owner/responsible for the Activity.
        final Group responsibleGroup = responsibleGroupName != null && !responsibleGroupName.isEmpty()
                ? getGroup(responsibleGroupName, organisationName, entityManager).orElse(null)
                : null;
        final List<AdmissionVO> responsibleAdmissions = admissions.stream()
                .filter(AdmissionVO::isResponsible)
                .collect(Collectors.toList());

        // Check sanity
        if (responsibleGroup == null && responsibleAdmissions == null || responsibleAdmissions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot create an Activity without having a responsible Group or Membership.");
        }

        // #3) Get the Organisation
        final Organisation owningOrganisation = getOrganisation(organisationName, entityManager)
                .orElseThrow(() -> new RuntimeException("Cannot find an Organisation with the name ["
                        + organisationName + "]"));

        // #4) Create and persist the Activity instance
        final Activity toCreate = new Activity(shortDesc,
                fullDesc,
                startTime,
                endTime,
                cost,
                lateAdmissionCost,
                lateAdmissionDate,
                lastAdmissionDate,
                cancelled,
                dressCode,
                locationCategory,
                location,
                addressShortDescription,
                owningOrganisation,
                responsibleGroup,
                isOpenToGeneralPublic);

        entityManager.persist(toCreate);
        entityManager.flush();

        // #5) Create the Admissions for the Activity
        updateAdmissions(admissions, toCreate, entityManager, activeMembership);

        // All done.
        return toCreate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Activity updateActivity(final long activityId,
            final String organisationName,
            final String shortDesc,
            final String fullDesc,
            final ZonedDateTime startTime,
            final ZonedDateTime endTime,
            final Amount cost,
            final Amount lateAdmissionCost,
            final LocalDate lateAdmissionDate,
            final LocalDate lastAdmissionDate,
            final boolean cancelled,
            final String dressCode,
            final String addressCategory,
            final Address location,
            final String addressShortDescription,
            final String responsibleGroupName,
            final Set<AdmissionVO> admissions,
            final boolean isOpenToGeneralPublic,
            final Membership activeMembership) {

        // Check sanity
        validateActivityData(organisationName,
                shortDesc,
                fullDesc,
                startTime,
                endTime,
                addressCategory,
                location,
                addressShortDescription,
                admissions);
        Validate.notNull(activeMembership, "activeMembership");

        // Find the Activity to update.
        final Activity toUpdate = findByPrimaryKey(Activity.class, activityId);
        Validate.notNull(toUpdate, "Found no activity with id [" + activityId + "]");

        // Find the Category of the Address
        final List<Category> categories = JpaUtilities.findEntities(
                Category.class,
                Category.NAMEDQ_GET_BY_ID_CLASSIFICATION,
                true,
                entityManager,
                q -> q.setParameter(
                        OrganisationPatterns.PARAM_CLASSIFICATION,
                        CategorizedAddress.ACTIVITY_CLASSIFICATION));
        if (categories.size() != 1) {
            throw new IllegalArgumentException("Expected exactly 1 Category [" + addressCategory + "], but got ["
                    + categories.size() + "]");
        }
        final Category category = categories.get(0);

        // Update the activity state
        toUpdate.setCancelled(cancelled);
        toUpdate.setCost(cost);
        toUpdate.setDressCode(dressCode);
        toUpdate.setEndTime(endTime);
        toUpdate.setFullDesc(fullDesc);
        toUpdate.setLastAdmissionDate(lastAdmissionDate);
        toUpdate.setLateAdmissionCost(lateAdmissionCost);
        toUpdate.setLateAdmissionDate(lateAdmissionDate);
        toUpdate.setLocation(location);
        toUpdate.setAddressCategory(category);
        toUpdate.setStartTime(startTime);
        toUpdate.setShortDesc(shortDesc);
        toUpdate.setAddressShortDescription(addressShortDescription);
        toUpdate.setOpenToGeneralPublic(isOpenToGeneralPublic);

        if (responsibleGroupName != null && !responsibleGroupName.equals("")) {
            final Optional<Group> group = getGroup(responsibleGroupName, organisationName, entityManager);
            if (group.isPresent()) {
                toUpdate.setResponsible(group.get());
            }
        }

        // Update the admissions.
        updateAdmissions(admissions, toUpdate, entityManager, activeMembership);

        // All Done.
        return toUpdate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean modifyAdmission(
            final ChangeType changeType,
            final AdmissionVO admissionDetails,
            final Membership actingMembership) {

        // Check sanity
        Validate.notNull(changeType, "changeType");
        Validate.notNull(admissionDetails, "admissionDetails");
        Validate.notNull(actingMembership, "actingMembership");

        final String alias = Validate.notEmpty(admissionDetails.getAlias(), "admissionDetails.getAlias()");
        final String organisation = Validate.notEmpty(admissionDetails.getOrganisation(),
                "admissionDetails.getOrganisation()");
        boolean toReturn = false;

        // Find the Activity
        final Activity activity = entityManager.find(Activity.class, admissionDetails.getActivityID());

        // Are we still permitted to change the admissions?
        final ZonedDateTime now = ZonedDateTime.now(activity.getStartTime().getZone());
        if(now.isAfter(activity.getLastAdmissionDate().atStartOfDay(activity.getStartTime().getZone()).plusDays(1))) {

            log.info("Cannot modify admissions to ");
        }

        // Find the admitted Membership
        final Membership admitted = entityManager.createNamedQuery(
                Membership.NAMEDQ_GET_BY_ALIAS_ORGANISATION, Membership.class)
                .setParameter(OrganisationPatterns.PARAM_ALIAS, alias)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisation)
                .getSingleResult();

        switch (changeType) {
            case DELETE:
                final Optional<Admission> existingAdmission = activity.getAdmissions().stream()
                        .filter(c -> Membership.ALIAS_AND_ORG_COMPARATOR.compare(admitted, c.getAdmitted()) == 0)
                        .findFirst();

                if(existingAdmission.isPresent() && isAdmissionRemovable(existingAdmission.get(), activity)) {

                    // We can safely remove the supplied Admission.
                }

                if(!existingAdmission.isPresent()) {
                    log.warn("No admission for " + admitted + " found in Activity [JpaID: " + activity.getId()
                            + ", ShortDesc: " + activity.getShortDesc() + "]. Ignoring delete request.");
                } else {

                    // Remove the Admission
                    final Admission toRemove = existingAdmission.get();

                    activity.getAdmissions().remove(toRemove);

                    // Delete the Admission record.
                    entityManager.remove(toRemove);

                    // update

                    // All Done.
                    toReturn = true;
                }
        }

        // All Done.
        return toReturn;
    }

    /**
     * Checks if the supplied Admission can be removed from the given Activity.
     *
     * @param toRemove
     * @param activity
     * @return
     */
    public static boolean isAdmissionRemovable(final Admission toRemove, final Activity activity) {

        // Check sanity
        Validate.notNull(toRemove, "toRemove");
        Validate.notNull(activity, "activity");

        // If we have a Group as responsible, any Admission can be removed.
        if(activity.getResponsible() != null) {
            return true;
        }

        // Otherwise, at least 1 other responsible Admission must be present.
        final List<Admission> otherResponsibleAdmissions = activity.getAdmissions().stream()
                .filter(Admission::isResponsible)
                .filter(c -> Membership.ALIAS_AND_ORG_COMPARATOR.compare(c.getAdmitted(), toRemove.getAdmitted()) != 0)
                .collect(Collectors.toList());

        // All Done.
        return !otherResponsibleAdmissions.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    public boolean modifyAdmission(final long activityId,
            final String alias,
            final String organisation,
            final boolean responsible,
            final String admissionNote,
            final boolean onlyUpdateAdmissionNote,
            final Membership activeMembership) {

        // Check sanity
        Validate.notEmpty(alias, "alias");
        Validate.notEmpty(organisation, "organisation");
        Validate.notNull(activeMembership, "activeMembership");

        // #1) Find the activity to update
        final Activity toUpdate = entityManager.find(Activity.class, activityId);
        if (toUpdate == null) {
            log.warn("Could not find an activity with JPAID [" + activityId + "]");
            return false;
        }

        // TODO: Determine if the activeMembership is permitted to alter the admission status of the supplied Membership

        // #2) Extract the admitted membership
        final Membership admitted = getMembership(alias, organisation, entityManager);
        final List<Admission> relevantAdmissions = toUpdate.getAdmissions().stream()
                .filter(admission ->
                        Membership.ALIAS_AND_ORG_COMPARATOR.compare(admission.getAdmitted(), admitted) == 0)
                .collect(Collectors.toList());

        if (relevantAdmissions == null || relevantAdmissions.isEmpty()) {

            // Add a new Admission
            final ZonedDateTime now = ZonedDateTime.now(toUpdate.getStartTime().getZone());
            final Admission newAdmission = new Admission(toUpdate,
                    admitted,
                    now,
                    now,
                    admissionNote,
                    responsible,
                    activeMembership);

            toUpdate.getAdmissions().add(newAdmission);
            entityManager.persist(newAdmission);
            entityManager.flush();

        } else {

            final Admission adm = relevantAdmissions.get(0);

            if (relevantAdmissions.size() > 1) {
                final List<Admission> toRemove = relevantAdmissions.stream()
                        .filter(admission -> !adm.getAdmissionId().equals(admission.getAdmissionId()))
                        .collect(Collectors.toList());

                if (log.isWarnEnabled()) {
                    log.warn("Found surplus admissions for Membership [" + admitted + "] to " + toUpdate
                            + ". Removing all but 1.");
                }
                toUpdate.getAdmissions().removeAll(toRemove);
            }

            // Update the Admission's data.
            adm.setResponsible(responsible);
            adm.setAdmissionNote(admissionNote);
            adm.setAdmittedBy(activeMembership);
        }

        // All Done
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Category, List<CategorizedAddress>> getActivityLocationAddresses(final String organisationName) {
        return null;
    }

    /**
     * Updates the Admissions within the supplied {@link Activity} originating from the supplied Set of
     * {@link AdmissionVO} harvested by a client.
     *
     * @param admissions          The non-null Set of {@link AdmissionVO} to use when updating the Activity.
     * @param activity            A non-null {@link Activity} whose Admissions should be updated.
     * @param entityManager       An Active {@link EntityManager}.
     * @param admittingMembership The active Membership executing this method.
     */
    public static void updateAdmissions(final Set<AdmissionVO> admissions,
            final Activity activity,
            final EntityManager entityManager,
            final Membership admittingMembership) {

        // Check sanity
        Validate.notNull(admissions, "Cannot handle null 'admissions' argument.");
        Validate.notNull(activity, "Cannot handle null 'activity' argument.");
        Validate.notNull(entityManager, "Cannot handle null 'entityManager' argument.");

        final SortedMap<Membership, Admission> toReturn = new TreeMap<>();
        final List<Admission> toDelete = new ArrayList<>();
        final List<Admission> toPersist = new ArrayList<>();

        // #1) Extract any existing admissions from the supplied Activity
        activity.getAdmissions().stream().forEach(c -> toReturn.put(c.getAdmitted(), c));

        // #2) Convert any inbound admissions, and add to (or overwrite) existing ones.
        admissions.stream()
                .map(c -> {

                    // #2.1) Find details from the current AdmissionDetails.
                    final String alias = c.getAlias();
                    final String organisation = c.getOrganisation();
                    final String note = c.getNote().orElse("");

                    // #2.2) Does an admission from the supplied Membership already exist?
                    final Optional<Admission> existingAdmission = toReturn.entrySet().stream()
                            .filter(ship -> {

                                final Membership currentMembership = ship.getKey();
                                final String currentAlias = currentMembership.getAlias();
                                final String currentOrg = currentMembership.getOrganisation().getOrganisationName();

                                // All Done.
                                return currentAlias.equals(alias) && currentOrg.equals(organisation);
                            })
                            .map(Map.Entry::getValue)
                            .findFirst();

                    if (existingAdmission.isPresent()) {

                        // #2.3) We should simply update the current Admission with any data received.
                        final Admission admission = existingAdmission.get();

                        // Update the Admission state
                        admission.setAdmissionNote(note);
                        admission.setAdmittedBy(admittingMembership);

                        // TODO: Update the responsible field? If ....
                        // TODO: 1) At least one other responsible party exists.
                        // TODO: 2) activeMembership can change perms

                    } else {

                        // #2.4) Create a new Admission for the supplied membership data
                        final List<Membership> memberships = JpaUtilities.findEntities(Membership.class,
                                Membership.NAMEDQ_GET_BY_ALIAS_ORGANISATION,
                                true,
                                entityManager,
                                aQuery -> {
                                    aQuery.setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisation);
                                    aQuery.setParameter(OrganisationPatterns.PARAM_ALIAS, alias);
                                });

                        // Check sanity
                        final Optional<Membership> membership = JpaUtilities.getSingleInstance(
                                memberships,
                                "Membership",
                                Membership::toString);

                        if (membership.isPresent()) {

                            // #2.5) Found a proper Membership; create a new Admission
                            final ZonedDateTime now = ZonedDateTime.now(activity.getStartTime().getZone());
                            final Admission newAdmission = new Admission(activity,
                                    membership.get(),
                                    now,
                                    now,
                                    note,
                                    c.isResponsible(),
                                    admittingMembership);

                            toPersist.add(newAdmission);
                            activity.getAdmissions().add(newAdmission);
                        } else {

                            log.warn("Found no MemberShip for [Org: " + organisation + ", Alias: " + alias + "]");
                        }
                    }

                    // Nopes.
                    return null;
                })
                .filter(c -> c != null)
                .collect(Collectors.toMap(Admission::getAdmitted, c -> c));

        // Persist and delete the corresponding Admissions.
        toPersist.stream().forEach(admission -> {
            entityManager.persist(admission);
            entityManager.flush();
        });
        toDelete.stream().forEach(admission -> {
            entityManager.remove(admission);
            entityManager.flush();
        });
    }

    //
    // Private helpers
    //

    private static Membership getMembership(final String alias,
            final String organisationName,
            final EntityManager entityManager) {

        final List<Membership> memberShips = JpaUtilities.findEntities(
                Membership.class,
                Membership.NAMEDQ_GET_BY_ALIAS_ORGANISATION,
                true,
                entityManager,
                aQuery -> {
                    aQuery.setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisationName);
                    aQuery.setParameter(OrganisationPatterns.PARAM_ALIAS, alias);
                });

        return JpaUtilities.getSingleInstance(memberShips, "Membership", null);
    }

    private static Optional<Organisation> getOrganisation(final String organisationName,
            final EntityManager entityManager) {

        final List<Organisation> organisations = JpaUtilities.findEntities(Organisation.class,
                Organisation.NAMEDQ_GET_BY_NAME,
                true,
                entityManager,
                aQuery -> aQuery.setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisationName));

        // All Done.
        return JpaUtilities.getSingleInstance(organisations, "Organisation", Organisation::getOrganisationName);
    }

    private static Optional<Group> getGroup(final String groupName,
            final String organisationName,
            final EntityManager entityManager) {

        final List<Group> groups = JpaUtilities.findEntities(Group.class,
                Group.NAMEDQ_GET_BY_NAME_ORGANISATION,
                true,
                entityManager,
                aQuery -> {
                    aQuery.setParameter(OrganisationPatterns.PARAM_GROUP_NAME, groupName);
                    aQuery.setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisationName);
                });

        // All Done.
        return JpaUtilities.getSingleInstance(groups, "Group", Group::getGroupName);
    }

    /**
     * Retrieves a persisted Category corresponding to the supplied categoryID.
     *
     * @param categoryID    The non-empty category ID for which to retrieve the (persisted) address-location Category.
     * @param entityManager The non-null {@link EntityManager}
     * @return an Optional and persisted address location Category.
     */
    private static Optional<Category> getAddressLocationCategory(final String categoryID,
            final EntityManager entityManager) {

        final List<Category> categories = JpaUtilities.findEntities(Category.class,
                Category.NAMEDQ_GET_BY_ID_CLASSIFICATION,
                true,
                entityManager,
                aQuery -> {
                    aQuery.setParameter(OrganisationPatterns.PARAM_CATEGORY_ID, categoryID);
                    aQuery.setParameter(OrganisationPatterns.PARAM_CLASSIFICATION,
                            CategorizedAddress.ACTIVITY_CLASSIFICATION);
                });

        // All Done.
        return JpaUtilities.getSingleInstance(categories, "Category", Category::getCategoryID);
    }

    @SuppressWarnings("all")
    private void validateActivityData(final String organisationName,
            final String shortDesc,
            final String fullDesc,
            final ZonedDateTime startTime,
            final ZonedDateTime endTime,
            final String addressCategory,
            final Address location,
            final String addressShortDescription,
            final Set<AdmissionVO> admissions) {

        Validate.notEmpty(organisationName, "organisationName");
        Validate.notEmpty(shortDesc, "shortDesc");
        Validate.notEmpty(fullDesc, "fullDesc");
        Validate.notNull(startTime, "Cannot handle null startTime argument.");
        Validate.notNull(endTime, "Cannot handle null endTime argument.");
        Validate.notEmpty(addressCategory, "Cannot handle null or empty addressCategory argument.");
        Validate.notNull(location, "Cannot handle null location/address argument.");
        Validate.isTrue(startTime.isBefore(endTime), "startTime must be before endTime.");
        Validate.notNull(admissions, "Cannot handle null admissions argument.");
        Validate.notEmpty(addressShortDescription, "Cannot handle null or empty addressShortDescription argument.");

        Validate.isTrue(endTime.isAfter(startTime), "endTime must be after startTime");
    }
}
