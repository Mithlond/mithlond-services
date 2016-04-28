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
import se.mithlond.services.organisation.api.transport.AdmissionDetails;
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
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.shared.authorization.api.RequireAuthorization;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;
import se.mithlond.services.shared.spi.jpa.JpaUtilities;
import se.mithlond.services.shared.spi.jpa.PersistenceOperationFailedException;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    @RequireAuthorization(authorizationPatterns = "/Mithlond/Inbyggare/,/Forodrim/Allmoge/")
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
            final Set<AdmissionDetails> admissions,
            final boolean isOpenToGeneralPublic) {

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

        // #1) Find the Category with the supplied 'category'
        final Category locationCategory = getAddressLocationCategory(addressCategory, entityManager)
                .orElseThrow(() -> new IllegalArgumentException("Address location category ["
                        + addressCategory + "] not found within persistent storage."));

        // #2) Find the owner/responsible for the Activity.
        final Group responsibleGroup = responsibleGroupName != null && !responsibleGroupName.isEmpty()
                ? getGroup(responsibleGroupName, organisationName, entityManager).orElse(null)
                : null;
        final List<AdmissionDetails> responsibleAdmissions = admissions.stream()
                .filter(AdmissionDetails::isResponsible)
                .collect(Collectors.toList());

        // Check sanity
        if(responsibleGroup == null && responsibleAdmissions == null || responsibleAdmissions.isEmpty()) {
            throw new IllegalArgumentException(
                    "Cannot create an Activity without having a responsible Group or Membership.");
        }

        // #3) Get the Organisation
        final Organisation owningOrganisation = getOrganisation(organisationName, entityManager)
                .orElseThrow(() -> new RuntimeException("Cannot find an Organisation with the name ["
                        + organisationName + "]"));

        // #4) Create the Activity instance
        Activity toCreate = new Activity(shortDesc,
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


        final Map<ProtoAdmission, Membership> admission2Membership = wrapAdmissions(admissions);

        Activity toCreate = null;
        try {

            // First, create the activity object.
            toCreate = new Activity(shortDesc,
                    fullDesc,
                    startTime,
                    endTime,
                    cost,
                    lateAdmissionCost,
                    lateAdmissionDate,
                    lastAdmissionDate,
                    cancelled,
                    dressCode,
                    category,
                    location,
                    addressShortDescription,
                    organisation,
                    responsibleGuildOrNull,
                    isOpenToGeneralPublic);
            final Set<Admission> admissionSet = toCreate.getAdmissions();

            // Now add the Admissions given.
            for (Map.Entry<ProtoAdmission, Membership> current : admission2Membership.entrySet()) {

                final ProtoAdmission protoAdmission = current.getKey();
                Admission admission = new Admission(toCreate,
                        current.getValue(),
                        DateTime.now(),
                        protoAdmission.getNote(),
                        protoAdmission.isResponsible());
                admissionSet.add(admission);
            }

            // ... and persist the Activity.
            create(toCreate);

        } catch (PersistenceOperationFailedException e) {
            log.error("Could not create Activity [" + shortDesc + "]", e);
            throw e;
        }

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
            final String responsibleGuildName,
            final Set<AdmissionDetails> admissions,
            final boolean isOpenToGeneralPublic) {

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

        if (responsibleGuildName != null && !responsibleGuildName.equals("")) {
            final Guild responsibleGuild = getGuildByNameAndOrganisation(responsibleGuildName, organisationName);
            toUpdate.setResponsible(responsibleGuild);
        }

        // Update the Admissions.
        final Set<Admission> admissionSet = toUpdate.getAdmissions();
        final Map<String, AdmissionDelta> admissionDeltaMap = new TreeMap<String, AdmissionDelta>();
        final Map<ProtoAdmission, Membership> admission2Membership = wrapAdmissions(admissions);

        for (Admission current : admissionSet) {
            AdmissionDelta delta = new AdmissionDelta();
            delta.admission = current;
            admissionDeltaMap.put(current.getAdmitted().getMember().getAlias(), delta);
        }
        for (ProtoAdmission current : admissions) {
            final String alias = current.getAlias();

            AdmissionDelta delta = admissionDeltaMap.get(alias);
            if (delta == null) {
                delta = new AdmissionDelta();
            }
            delta.protoAdmission = current;
        }

        for (Map.Entry<String, AdmissionDelta> current : admissionDeltaMap.entrySet()) {

            final String alias = current.getKey();
            final AdmissionDelta delta = current.getValue();

            if (delta.protoAdmission == null && delta.admission != null) {

                // Remove the admission altogether.
                admissionSet.remove(delta.admission);

            } else if (delta.protoAdmission != null && delta.admission == null) {
                // Create an Admission for this ProtoAdmission, and add it.
                admissionSet.add(
                        new Admission(
                                toUpdate,
                                admission2Membership.get(delta.protoAdmission),
                                DateTime.now(),
                                delta.protoAdmission.getNote(),
                                delta.protoAdmission.isResponsible()
                        ));
            } else if (delta.protoAdmission != null && delta.admission != null) {
                if (!areEqual(delta.protoAdmission, delta.admission)) {

                    // Update the current admission with the data in the protoAdmission.
                    // Only update the admission note and the isResponsible flag.
                    if (!delta.admission.getAdmissionNote().equals(delta.protoAdmission.getNote())) {
                        delta.admission.setAdmissionNote(delta.protoAdmission.getNote());
                    }
                    if (!delta.protoAdmission.isResponsible() == delta.admission.isResponsible()) {
                        delta.admission.setResponsible(delta.protoAdmission.isResponsible());
                    }
                }
            } else {
                // This should never happen.
                log.error("Null admission *and* null protoadmission for [" + alias + "] and activity ["
                        + toUpdate.getShortDesc() + "]");
            }
        }

        // We are inside a transaction (scoped by this method), implying that we can return here
        // without explicitly calling merge in the EntityManager. This is caused by:
        //
        // a) The toUpdate instance is managed (was retrieved by a find call)
        // b) The EntityManager will flush() at the end of this method, updating DB state.
        return toUpdate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addOrAlterAdmission(final long activityId,
            final Membership membership,
            final boolean responsible,
            final String admissionNote,
            final boolean onlyUpdateAdmissionNote) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Category, List<CategorizedAddress>> getActivityLocationAddresses(final String organisationName) {
        return null;
    }

    //
    // Private helpers
    //

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
            final Set<AdmissionDetails> admissions) {

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
