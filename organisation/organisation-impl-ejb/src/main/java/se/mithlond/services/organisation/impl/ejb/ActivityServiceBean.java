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
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.mithlond.services.organisation.api.ActivityService;
import se.mithlond.services.organisation.api.OrganisationService;
import se.mithlond.services.organisation.api.parameters.ActivitySearchParameters;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.activity.Admission;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.transport.activity.Activities;
import se.mithlond.services.organisation.model.transport.activity.ActivityVO;
import se.mithlond.services.organisation.model.transport.activity.AdmissionVO;
import se.mithlond.services.organisation.model.transport.activity.Admissions;
import se.mithlond.services.organisation.model.transport.address.CategoriesAndAddresses;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.algorithms.introspection.SimpleIntrospector;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;
import se.mithlond.services.shared.spi.jpa.JpaUtilities;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TimeZone;
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
     * The category name of Home addresses.
     */
    public static final String HOMEADRESS_CATEGORY = "Hemadress";

    @EJB
    private OrganisationService organisationServiceBean;

    /**
     * Default constructor.
     */
    public ActivityServiceBean() {
    }

    /**
     * Injectable, test-friendly, constructor.
     *
     * @param organisationService An OrganisatinoService to inject.
     */
    public ActivityServiceBean(final OrganisationService organisationService) {
        this.organisationServiceBean = organisationService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Activities getActivities(final ActivitySearchParameters parameters, final Membership activeMembership) {

        // Check sanity
        Validate.notNull(parameters, "parameters");
        Validate.notNull(activeMembership, "activeMembership");

        if (log.isDebugEnabled()) {
            log.debug("Received " + parameters + " and activeMembership: " + activeMembership);
        }

        // Pad the ID Lists.
        final int organisationIDsSize = AbstractJpaService.padAndGetSize(parameters.getOrganisationIDs(), 0L);
        final int activityIDsSize = AbstractJpaService.padAndGetSize(parameters.getActivityIDs(), 0L);
        // final int membershipIDsSize = AbstractJpaService.padAndGetSize(parameters.getMembershipIDs(), 0L);

        // Acquire the padded lists.
        final List<Long> organisationIDs = parameters.getOrganisationIDs();
        final List<Long> activityIDs = parameters.getActivityIDs();
        // final List<Long> membershipIDs = parameters.getMembershipIDs();
        final SortedMap<Organisation, Boolean> adminMap = new TreeMap<>();

        organisationIDs.stream().sorted().forEach(orgID -> {

            final Organisation org = entityManager.find(Organisation.class, orgID);
            if (!adminMap.containsKey(org)) {
                adminMap.put(org, organisationServiceBean.isAdministratorFor(activeMembership, org));
            }
        });

        if (log.isDebugEnabled()) {

            final String preamble = "Caller [" + activeMembership.getAlias() + " @ "
                    + activeMembership.getOrganisation().getOrganisationName() + "] is Admin within ...\n";

            final StringBuilder builder = new StringBuilder(preamble);
            adminMap.forEach((k, v) -> builder.append(" [" + k.getOrganisationName() + "]: " + v + "\n"));
            log.debug(builder.toString());
        }

        final TypedQuery<Activity> query = entityManager.createNamedQuery(
                Activity.NAMEDQ_GET_BY_SEARCH_PARAMETERS, Activity.class)
                .setParameter(OrganisationPatterns.PARAM_NUM_ACTIVITYIDS, activityIDsSize)
                .setParameter(OrganisationPatterns.PARAM_IDS, activityIDs)
                // .setParameter(OrganisationPatterns.PARAM_NUM_MEMBERSHIPIDS, membershipIDsSize)
                // .setParameter(OrganisationPatterns.PARAM_MEMBERSHIP_IDS, membershipIDs)
                .setParameter(OrganisationPatterns.PARAM_NUM_ORGANISATIONIDS, organisationIDsSize)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_IDS, organisationIDs)
                .setParameter(OrganisationPatterns.PARAM_START_TIME, parameters.getStartPeriod())
                .setParameter(OrganisationPatterns.PARAM_END_TIME, parameters.getEndPeriod());

        /*
        if (parameters.getStartPeriod() != null) {
            query.setParameter(OrganisationPatterns.PARAM_START_TIME,
                    Timestamp.valueOf(parameters.getStartPeriod()),
                    TemporalType.TIMESTAMP);
        }

        if (parameters.getEndPeriod() != null) {
            query.setParameter(OrganisationPatterns.PARAM_START_TIME,
                    Timestamp.valueOf(parameters.getEndPeriod()),
                    TemporalType.TIMESTAMP);
        }
        */

        final List<Activity> activities = query.getResultList();

        final Activities toReturn = new Activities();
        if (parameters.isDetailedResponsePreferred()) {

            toReturn.getActivities().addAll(activities);

            if (log.isDebugEnabled()) {
                log.debug("Added [" + toReturn.getActivities().size() + "] Activity objects to result.");
            }

        } else {

            toReturn.getActivityVOs().addAll(activities
                    .stream()
                    .map(ActivityVO::new)
                    .collect(Collectors.toList()));

            if (log.isDebugEnabled()) {
                log.debug("Added [" + toReturn.getActivityVOs().size() + "] ActivityVO objects to result.");
            }
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Activity createActivity(final ActivityVO activityVO, final Membership activeMembership)
            throws RuntimeException {

        // Check sanity
        Validate.notNull(activeMembership, "activeMembership");
        Validate.notNull(activityVO, "activityVO");

        Validate.notNull(activityVO.getOrganisation(), "activityVO.getOrganisation()");
        final String organisationName = Validate.notNull(activityVO.getOrganisation().getOrganisationName(),
                "organisationName");
        final String shortDesc = Validate.notEmpty(activityVO.getShortDesc(), "shortDesc");
        final String fullDesc = Validate.notEmpty(activityVO.getFullDesc(), "fullDesc");

        final String addressCategory = Validate.notEmpty(activityVO.getAddressCategory(), "addressCategory");
        final Address location = Validate.notNull(activityVO.getLocation(), "location");

        // Are the startTime and endTime properly situated?
        final LocalDateTime startTime = Validate.notNull(activityVO.getStartTime(), "activityVO.getStartTime()");
        final LocalDateTime endTime = Validate.notNull(activityVO.getEndTime(), "activityVO.getEndTime()");
        validateStartAndEndTimes(activityVO);

        // Are the late and last admission dates correctly submitted?
        final LocalDate lateAdmissionDate = activityVO.getLateAdmissionDate();
        final LocalDate lastAdmissionDate = Validate.notNull(activityVO.getLastAdmissionDate(), "lastAdmissionDate");
        validateAdmissionDates(lateAdmissionDate, lastAdmissionDate);

        // Get the required relations from the Database.
        final Organisation organisation = CommonPersistenceTasks.getOrganisation(entityManager, organisationName);
        final Category category = CommonPersistenceTasks.getAddressCategory(entityManager, addressCategory);

        // Is a Group responsible for the Activity?
        final Optional<Group> responsibleGroup =
                validateActivityHasResponsibleAndRetrieveGroup(entityManager, activityVO);

        // Create a new Activity using the supplied data.
        final Activity toReturn = new Activity(shortDesc,
                fullDesc,
                startTime,
                endTime,
                activityVO.getCost(),
                activityVO.getLateAdmissionCost(),
                lateAdmissionDate,
                lastAdmissionDate,
                activityVO.isCancelled(),
                activityVO.getDressCode(),
                category,
                location,
                activityVO.getAddressShortDescription(),
                organisation,
                responsibleGroup.orElse(null),
                activityVO.isOpenToGeneralPublic());

        // Find the current timestamp, as interpreted within the TimeZone where the Activity takes place.
        final TimeZone activityTimeZone = organisation.getTimeZone();
        final LocalDateTime now = LocalDateTime.now(activityTimeZone.toZoneId());

        // Convert all AdmissionVOs to Admissions, and add to the admissions of the Activity to return.
        activityVO.getAdmissions().stream().map(c -> {

            // Convert the AdmissionVO to an Admission
            final Membership admitted = CommonPersistenceTasks.getSingleMembership(
                    entityManager,
                    c.getAlias(),
                    c.getOrganisation());

            // Compile the admission note, which should be on the following format:
            //
            // a) If an admission note is supplied, insert it.
            // b) If the admitted Membership is not the active Membership (i.e. someone is admitting someone
            //    else), note who admitted whom.
            //
            final String admittedBySomeoneElse = " Anmäld av " + activeMembership.getAlias()
                    + " (" + activeMembership.getOrganisation().getOrganisationName() + ")";
            final String admissionNote = c.getNote().orElse("") // Use the note, if provided.
                    + (!admitted.equals(activeMembership) ? admittedBySomeoneElse : "");

            // All Done.
            return new Admission(
                    toReturn,
                    admitted,
                    now,
                    now,
                    admissionNote,
                    c.isResponsible(),
                    activeMembership);

        }).forEach(toReturn.getAdmissions()::add);

        // Persist & Flush.
        entityManager.persist(toReturn);
        entityManager.flush();

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Activity updateActivity(final ActivityVO activityVO,
                                   final boolean onlyUpdateNonNullProperties,
                                   final Membership activeMembership) {

        // Check sanity
        Validate.notNull(activityVO, "activityVO");
        Validate.notNull(activeMembership, "activeMembership");
        Validate.isTrue(0 < activityVO.getJpaID(), "0 < activityVO.getJpaID()");

        final Activity toUpdate = entityManager.find(Activity.class, activityVO.getJpaID());
        if (toUpdate == null) {
            return null;
        }

        // Update all sensible things to update
        SimpleIntrospector.copyJavaBeanProperties(activityVO, toUpdate);

        // Flush the entity manager.
        entityManager.flush();

        // All Done.
        return toUpdate;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("all")
    @Override
    public Admissions updateAdmissions(final Membership activeMembership, final Admissions admissions) {

        // Check sanity
        Validate.notNull(activeMembership, "activeMembership");
        Validate.notNull(admissions, "admissions");

        // Create the return wrapper
        final Organisation activeOrganisation = activeMembership.getOrganisation();
        final Admissions toReturn = new Admissions();

        // Find the current timestamp, as interpreted within the TimeZone where the Activity takes place.
        final TimeZone activityTimeZone = activeOrganisation.getTimeZone();
        final LocalDateTime now = LocalDateTime.now(activityTimeZone.toZoneId());

        // Business rules:
        //
        // 1. Non-administrators can update their own Admissions only.
        // 2. Administrators can update Admissions for people within their own Organisation.
        //
        final boolean isAdmin = organisationServiceBean.isAdministratorFor(
                activeMembership,
                activeMembership.getOrganisation());

        final List<AdmissionVO> acceptedAdmissions = new ArrayList<>();
        if (isAdmin) {

            admissions.getDetails()
                    .stream()
                    .filter(adm -> adm.getOrganisation().equalsIgnoreCase(activeOrganisation.getOrganisationName()))
                    .forEach(acceptedAdmissions::add);

        } else {

            admissions.getDetails()
                    .stream()
                    .filter(adm -> activeMembership.getId() == adm.getMembershipID())
                    .forEach(acceptedAdmissions::add);
        }

        // Now, process the accepted admissions
        if (log.isDebugEnabled()) {
            log.debug("Accepted [" + acceptedAdmissions.size() + "] out of [" + admissions.getDetails().size()
                    + "] Admissions for processing.");
        }

        // Find the Activity objects as indicated by the accepted AdmissionVOs
        if (!acceptedAdmissions.isEmpty()) {

            final List<Long> activityJpaIDs = acceptedAdmissions.stream()
                    .map(AdmissionVO::getActivityID)
                    .filter(Objects::nonNull)
                    .sorted()
                    .distinct()
                    .collect(Collectors.toList());

            if (activityJpaIDs != null && !activityJpaIDs.isEmpty()) {

                final List<Activity> impliedActivities = entityManager
                        .createQuery("select a from Activity a where a.id in :jpaIDs", Activity.class)
                        .setParameter("jpaIDs", activityJpaIDs)
                        .getResultList();

                if (log.isDebugEnabled()) {

                    log.debug("Found [" + (impliedActivities == null
                            ? "0 <Null impliedActivities>"
                            : "" + impliedActivities.size())
                            + "] activities from the [" + acceptedAdmissions.size() + "] accepted AdmissionVOs.");
                }

                if (impliedActivities != null && !impliedActivities.isEmpty()) {

                    for (Activity current : impliedActivities) {

                        // Get the current Admissions of this Activity
                        final Set<Admission> existingAdmissions = current.getAdmissions();
                        final Map<Long, Admission> existingAdmissionsMap = existingAdmissions
                                .stream()
                                .collect(Collectors.toMap(c -> c.getAdmitted().getId(), c -> c));
                        final Set<Admission> currentResponsible = existingAdmissions.stream()
                                .filter(Admission::isResponsible)
                                .collect(Collectors.toSet());

                        // Find the received Admissions for this Activity.
                        final Map<Long, AdmissionVO> receivedTargetState = acceptedAdmissions
                                .stream()
                                .filter(adm -> adm.getActivityID() == current.getId())
                                .collect(Collectors.toMap(AdmissionVO::getMembershipID, c -> c));

                        // Create collections where the new/updated/removed admission structures reside.
                        final SortedMap<Long, AdmissionVO> toAdd = new TreeMap<>();
                        final SortedMap<Long, AdmissionVO> toUpdate = new TreeMap<>();
                        final SortedMap<Long, Admission> toRemove = new TreeMap<>();

                        receivedTargetState.entrySet().stream()
                                .filter(e -> !e.getValue().getAdmitted())
                                .map(e -> existingAdmissionsMap.get(e.getKey()))
                                .filter(e -> {

                                    // At least one responsible must remain after removing this Admission.
                                    boolean atLeastOneResponsibleRemains = true;
                                    if (e.isResponsible()) {

                                        atLeastOneResponsibleRemains = currentResponsible.stream()
                                                .filter(Admission::isResponsible)
                                                .filter(adm -> adm.getAdmissionId().membershipId != e.getAdmissionId().membershipId)
                                                .collect(Collectors.toSet())
                                                .size() > 0;
                                    }

                                    // All Done.
                                    return atLeastOneResponsibleRemains;
                                })
                                .forEach(e -> toRemove.put(e.getAdmissionId().membershipId, e));

                        receivedTargetState.entrySet().stream()
                                .filter(e -> e.getValue().getAdmitted())
                                .filter(e -> existingAdmissionsMap.containsKey(e.getKey()))
                                .forEach(e -> toUpdate.put(e.getKey(), e.getValue()));

                        receivedTargetState.entrySet().stream()
                                .filter(e -> e.getValue().getAdmitted())
                                .filter(e -> !existingAdmissionsMap.containsKey(e.getKey()))
                                .forEach(e -> toAdd.put(e.getKey(), e.getValue()));

                        // Add / Persist new Admissions as required.
                        //
                        toAdd.forEach((key, value) -> {

                            // First, find the Membership for this JPA ID.
                            final Membership membership = entityManager.find(Membership.class, key);

                            // If this Membership was admitted by someone else, add a note stating so.
                            String admissionNote = value.getNote().orElse(null);
                            if (membership.getId() != activeMembership.getId()) {

                                final String admittedBySomeoneElse = " Anmäld av " + activeMembership.getAlias()
                                        + " (" + activeMembership.getOrganisation().getOrganisationName() + ")";

                                admissionNote = (admissionNote == null
                                        ? admittedBySomeoneElse
                                        : admissionNote + admittedBySomeoneElse);
                            }

                            // Create an Admission from the supplied Membership
                            final Admission toPersist = new Admission(
                                    current,
                                    membership,
                                    now,
                                    now,
                                    admissionNote,
                                    value.isResponsible(),
                                    activeMembership);

                            // Persist and add the Admission to this Activity's admission set.
                            entityManager.persist(toPersist);
                            existingAdmissions.add(toPersist);

                            // ... and add the new Admission to the return wrapper.
                            toReturn.getDetails().add(new AdmissionVO(toPersist));
                        });

                        // Remove the deleted admissions
                        //
                        toRemove.forEach((key, value) -> {

                            existingAdmissions.remove(value);
                            entityManager.remove(value);
                        });

                        // Update the rest of the relevant and received Admissions
                        //
                        toUpdate.forEach((key, value) -> {

                            final Admission admission = existingAdmissionsMap.get(key);
                            if (admission != null) {

                                // Update the Admission data.
                                admission.setAdmissionNote(value.getNote().orElse(null));
                                admission.setAdmittedBy(activeMembership);
                                admission.setResponsible(value.isResponsible());

                                // ... and add the updated Admission to the return wrapper.
                                toReturn.getDetails().add(new AdmissionVO(admission));

                            } else {
                                log.warn("Could not find an existing admission with MembershipJpaID ["
                                        + key + "]. Weird.");
                            }
                        });
                    }
                }
            }
        }

        // All Done
        return toReturn;
    }

    /**
     * Checks if the supplied Admission can be removed from the given Activity.
     *
     * @param toRemove The admission to remove.
     * @param activity The activity owning the admission.
     * @return {@code true} if it is OK to remove the single admission from the activity.
     */
    public static boolean isAdmissionRemovable(final Admission toRemove, final Activity activity) {

        // Check sanity
        Validate.notNull(toRemove, "toRemove");
        Validate.notNull(activity, "activity");

        // If we have a Group as responsible, any Admission can be removed.
        if (activity.getResponsible() != null) {
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
    @Override
    public CategoriesAndAddresses getActivityLocationAddresses(final Long organisationID) {

        final CategoriesAndAddresses toReturn = new CategoriesAndAddresses();

        // #1) Find the CategoryIDs of all relevant categories
        final TypedQuery<Category> query = entityManager.createQuery("select a from Category a"
                + " where a.classification like :" + OrganisationPatterns.PARAM_CLASSIFICATION
                + " order by a.categoryID", Category.class)
                .setParameter(OrganisationPatterns.PARAM_CLASSIFICATION, CategorizedAddress.ACTIVITY_CLASSIFICATION);

        final Map<String, Category> id2CategoryMap = new TreeMap<>();
        query.getResultList().stream()
                .filter(Objects::nonNull)
                .filter(c -> c.getCategoryID() != null && !c.getCategoryID().isEmpty())
                .forEach(c -> id2CategoryMap.put(c.getCategoryID(), c));

        if (log.isDebugEnabled()) {
            log.debug("Found [" + id2CategoryMap.size() + "] categoryIDs: "
                    + id2CategoryMap.keySet().stream().reduce((l, r) -> l + ", " + r).orElse("<none>"));
        }
        final Category homeAddressCategory = id2CategoryMap.get(HOMEADRESS_CATEGORY);

        // #2) Find the home addresses of all active Memberships within the organisation.
        final List<Membership> activeMemberships = entityManager.createNamedQuery(
                Membership.NAMEDQ_GET_BY_ORGANISATION_ID_LOGINPERMITTED, Membership.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_ID, organisationID)
                .setParameter(OrganisationPatterns.PARAM_LOGIN_PERMITTED, true)
                .getResultList();

        if (activeMemberships != null && !activeMemberships.isEmpty()) {

            final Organisation activeOrganisation = activeMemberships.get(0).getOrganisation();

            activeMemberships.stream()
                    .filter(Objects::nonNull)
                    .map(mShip -> {

                        final User user = mShip.getUser();
                        final Address homeAddress = user.getHomeAddress();

                        //
                        // Synthesize a better short and long desc; the CategorizedAddress
                        // synthesized for HomeAddresses does not have a corresponding DB entity.
                        //
                        final String shortDesc = "Hemma hos " + mShip.getAlias();
                        final String longDesc = shortDesc
                                + " (" + user.getFirstName()
                                + " " + user.getLastName() + ")";

                        // All Done.
                        return new CategorizedAddress(
                                shortDesc,
                                longDesc,
                                homeAddressCategory,
                                activeOrganisation,
                                homeAddress);
                    })
                    .forEach(toReturn::addCategorizedAddress);
        }

        // #3) Find all non-HomeAddress CategorizedAddresses.
        final List<String> allCategoryIDsExceptHomeAddresses = id2CategoryMap.keySet().stream()
                .filter(cat -> !cat.equalsIgnoreCase(HOMEADRESS_CATEGORY))
                .sorted()
                .collect(Collectors.toList());

        final int numCategories = padAndGetSize(allCategoryIDsExceptHomeAddresses, "0");
        final List<CategorizedAddress> allOtherCAddresses = entityManager.createNamedQuery(
                CategorizedAddress.NAMEDQ_GET_BY_ORGANISATION_ID_AND_CATEGORY_IDS, CategorizedAddress.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_ID, organisationID)
                .setParameter(OrganisationPatterns.PARAM_CATEGORY_IDS, allCategoryIDsExceptHomeAddresses)
                .setParameter(OrganisationPatterns.PARAM_NUM_CATEGORYIDS, numCategories)
                .getResultList();
        if (allOtherCAddresses != null && !allOtherCAddresses.isEmpty()) {
            allOtherCAddresses.forEach(toReturn::addCategorizedAddress);
        }

        // All Done.
        return toReturn;
    }

    //
    // Private helpers
    //

    private static void validateAdmissionDates(final LocalDate lateAdmissionDate, final LocalDate lastAdmissionDate)
            throws IllegalArgumentException {

        // The Last admission Date cannot be null.
        Validate.notNull(lastAdmissionDate, "lastAdmissionDate");

        if (lateAdmissionDate != null && lastAdmissionDate.isBefore(lateAdmissionDate)) {
            final String lastAdmissionDateString = TimeFormat.YEAR_MONTH_DATE.print(lastAdmissionDate);
            final String lateAdmissionDateString = TimeFormat.YEAR_MONTH_DATE.print(lateAdmissionDate);
            throw new IllegalArgumentException("Sista anmälningsdatum (" + lastAdmissionDateString
                    + ") kan inte vara före gränsen för sen anmälan (" + lateAdmissionDateString + ")");
        }
    }

    private static void validateStartAndEndTimes(final ActivityVO activityVO) {

        final LocalDateTime startTime = Validate.notNull(activityVO.getStartTime(), "activityVO.getStartTime()");
        final LocalDateTime endTime = Validate.notNull(activityVO.getEndTime(), "activityVO.getEndTime()");
        if (startTime.isAfter(endTime)) {

            final String endTimeString = TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(endTime);
            final String startTimeString = TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(startTime);
            throw new IllegalArgumentException("Aktivitetens sluttid [" + endTimeString
                    + "] måste vara efter starttiden [" + startTimeString + "]");
        }
    }

    private static Optional<Group> validateActivityHasResponsibleAndRetrieveGroup(final EntityManager entityManager,
                                                                                  final ActivityVO activityVO) {

        final Optional<AdmissionVO> firstResponsibleAdmission = activityVO.getAdmissions()
                .stream()
                .filter(AdmissionVO::isResponsible)
                .findFirst();

        Group responsibleGroup = null;
        if (!firstResponsibleAdmission.isPresent()) {

            final String groupName = activityVO.getResponsibleGroupName();
            boolean noResponsibleFound = true;
            if (groupName != null && !groupName.isEmpty()) {

                final List<Group> responsibleGroups = JpaUtilities.findEntities(Group.class,
                        Group.NAMEDQ_GET_BY_NAME_ORGANISATION,
                        true,
                        entityManager,
                        aQuery -> {
                            aQuery.setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME,
                                    activityVO.getOrganisation().getOrganisationName());
                            aQuery.setParameter(OrganisationPatterns.PARAM_GROUP_NAME, groupName);
                        });

                if (responsibleGroups != null && responsibleGroups.size() > 0) {
                    responsibleGroup = responsibleGroups.get(0);
                    noResponsibleFound = false;
                }
            }

            if (noResponsibleFound) {
                throw new IllegalArgumentException("Aktiviteter måste ha minst 1 ansvarig (person eller grupp).");
            }
        }

        // All Done.
        return responsibleGroup == null ? Optional.empty() : Optional.of(responsibleGroup);
    }
}
