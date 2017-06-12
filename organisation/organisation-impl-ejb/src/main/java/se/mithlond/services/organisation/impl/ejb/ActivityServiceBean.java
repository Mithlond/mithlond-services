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
import se.mithlond.services.organisation.model.transport.address.CategoriesAndAddresses;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.algorithms.introspection.SimpleIntrospector;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;
import se.mithlond.services.shared.spi.jpa.JpaUtilities;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
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
        } else {

            toReturn.getActivityVOs().addAll(activities
                    .stream()
                    .map(ActivityVO::new)
                    .collect(Collectors.toList()));
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

    private class AliasAndOrganisationName implements Comparable<AliasAndOrganisationName> {

        public String alias;
        public String organisationName;

        public AliasAndOrganisationName(final String alias, final String organisationName) {
            this.alias = alias;
            this.organisationName = organisationName;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final AliasAndOrganisationName that = (AliasAndOrganisationName) o;
            return Objects.equals(alias, that.alias)
                    && Objects.equals(organisationName, that.organisationName);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hash(alias, organisationName);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int compareTo(final AliasAndOrganisationName that) {

            if (that == null) {
                return -1;
            } else if (that == this) {
                return 0;
            }

            final String thisAlias = this.alias == null ? "" : this.alias;
            final String thatAlias = that.alias == null ? "" : that.alias;

            int toReturn = thisAlias.compareTo(thatAlias);
            if (toReturn == 0) {

                final String thisOrganisation = this.organisationName == null ? "" : this.organisationName;
                final String thatOrganisation = that.organisationName == null ? "" : that.organisationName;
                toReturn = thisOrganisation.compareTo(thatOrganisation);
            }

            return toReturn;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Alias: " + alias + " (Organisation: " + organisationName + ")";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean modifyAdmission(final ChangeType changeType,
                                   final Membership actingMembership,
                                   final AdmissionVO... admissionDetails) throws IllegalStateException {

        // Check sanity
        Validate.notNull(changeType, "changeType");
        Validate.notNull(actingMembership, "actingMembership");
        Validate.notNull(admissionDetails, "admissionDetails");

        // #1) Re-pack into a map relating to the JPA ID of the Activity.
        //
        final Map<Long, Set<AdmissionVO>> admissionVOs = new TreeMap<>();
        Arrays.stream(admissionDetails)
                .filter(c -> c.getActivityID() != null)
                .forEach(c -> {

                    final Long activityID = c.getActivityID();

                    Set<AdmissionVO> voSet = admissionVOs.get(activityID);
                    if (voSet == null) {
                        voSet = new TreeSet<>();
                        admissionVOs.put(activityID, voSet);
                    }

                    // More than 1 admission? Complain.
                    if (!voSet.add(c)) {
                        final String msg = "Alias: " + c.getAlias() + " i Organisation: " + c.getOrganisation();
                        throw new IllegalArgumentException("Endast 1 anmälan per Medlem är tillåten i en aktivitet. "
                                + "Fann minst 2 anmälningar för " + msg);
                    }
                });

        // #2) We could permit handling multiple Activities per call
        //     ... but it would most likely imply an input error, so treat it as an error for now.
        //
        if (admissionVOs.size() > 1) {
            final String msg = admissionVOs.keySet().stream()
                    .map(c -> "" + c)
                    .reduce((l, r) -> l + ", " + r)
                    .orElse("<inga>");
            throw new IllegalArgumentException("Endast en Aktivitet kan behandlas per anrop. (Fick " + admissionVOs
                    .size() + "): " + msg);
        }

        // #3) Find the activity to modify
        //
        final Long activityJpaID = admissionVOs.keySet().iterator().next();
        final Activity toModify = entityManager.find(Activity.class, activityJpaID);
        final Map<AliasAndOrganisationName, AdmissionVO> modifiedAdmissions = admissionVOs.get(activityJpaID)
                .stream()
                .collect(Collectors.toMap(
                        c -> new AliasAndOrganisationName(c.getAlias(), c.getOrganisation()),
                        c -> c));

        // #4) Extract the current Admissions information; Map an AliasAndOrganisationName to the Admissions.
        //
        final Map<AliasAndOrganisationName, Admission> currentAdmissionMap = toModify.getAdmissions()
                .stream()
                .collect(Collectors.toMap(
                        c -> new AliasAndOrganisationName(c.getAdmitted().getAlias(),
                                c.getAdmitted().getOrganisation().getOrganisationName()),
                        c -> c));
        final Map<AliasAndOrganisationName, Admission> currentResponsibleAdmissions = currentAdmissionMap.entrySet()
                .stream()
                .filter(c -> c.getValue().isResponsible())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // #5) Handle the changes per change type.
        //
        if (changeType == ChangeType.DELETE) {

            // #5.1) If a group is responsible for the Activity, we have responsible parties.
            //
            if (toModify.getResponsible() == null) {

                // Do we still have at least one Admission which is responsible for the Activity after deleting?
                final Map<AliasAndOrganisationName, Admission> responsibleAdmissionsAfterDelete =
                        currentResponsibleAdmissions.entrySet().stream()
                                .filter(c -> !modifiedAdmissions.containsKey(c.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                validateResponsibleAdmissionsStillExist(currentResponsibleAdmissions, responsibleAdmissionsAfterDelete);
            }

            // #5.2) Remove the Admissions as requested.
            //
            final List<Admission> toBeRemoved = toModify.getAdmissions().stream()
                    .filter(c -> {

                        final AliasAndOrganisationName current = new AliasAndOrganisationName(
                                c.getAdmitted().getAlias(),
                                c.getAdmitted().getOrganisation().getOrganisationName());

                        return modifiedAdmissions.containsKey(current);
                    })
                    .filter(c -> c != null)
                    .collect(Collectors.toList());

            toModify.getAdmissions().removeAll(toBeRemoved);
            toBeRemoved.forEach(c -> entityManager.remove(c));
            entityManager.flush();

            // All is well.
            return true;
        }

        //
        // #6) We should either add Admissions or modify existing Admissions
        //     Perform the operation and stash the results in an extra Admissions Collection,
        //     which can be evaluated before actually committing the changes.
        //
        //     This allows for validating that the Activity still has at least 1 responsible Admission.
        //
        if (toModify.getResponsible() == null) {

            // #6.1) Are any of the inbound modifications responsible?
            //
            final Map<AliasAndOrganisationName, Admission> responsibleAdmissionsAfterModify =
                    currentResponsibleAdmissions.entrySet().stream()
                            .filter(c -> {

                                // Does the current AliasAndOrganisationName have an inbound AdmissionVO?
                                // (I.e. should we change the associated Admission for the AliasAndOrganisationName?)
                                final AdmissionVO currentModifiedAdmissionVO = modifiedAdmissions.get(c.getKey());

                                // Collect the two positive cases.
                                final boolean inboundAdmissionVoIsResponsible = currentModifiedAdmissionVO != null
                                        && currentModifiedAdmissionVO.isResponsible();
                                final boolean responsibleExistingAdmissionWasNotChanged =
                                        currentModifiedAdmissionVO == null;

                                // All Done.
                                return responsibleExistingAdmissionWasNotChanged || inboundAdmissionVoIsResponsible;
                            })
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            validateResponsibleAdmissionsStillExist(currentResponsibleAdmissions, responsibleAdmissionsAfterModify);
        }

        // #7) Perform the requested changes.
        //
        final Map<AliasAndOrganisationName, AdmissionVO> newAdmissionVOs = modifiedAdmissions.entrySet().stream()
                .filter(c -> currentAdmissionMap.get(c.getKey()) == null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        newAdmissionVOs.entrySet().forEach(c -> {

            // Extract the admission VO
            final AdmissionVO admissionData = c.getValue();

            // Find the Membership to be admitted.
            final Membership admitted = CommonPersistenceTasks.getSingleMembership(entityManager,
                    admissionData.getAlias(),
                    admissionData.getOrganisation());

            // Dig out the current LocalDateTime
            final ZoneId standardZoneID = toModify.getOwningOrganisation().getTimeZone().toZoneId();
            final LocalDateTime now = LocalDateTime.now(standardZoneID);

            // Create the new admission
            final Admission newAdmission = new Admission(toModify,
                    admitted,
                    now,
                    now,
                    createAdmissionNote(actingMembership, admissionData, admitted),
                    admissionData.isResponsible(),
                    actingMembership);

            // Add the new Admission to the modified activity
            toModify.getAdmissions().add(newAdmission);
        });

        modifiedAdmissions.entrySet().stream()
                .filter(c -> currentAdmissionMap.get(c.getKey()) != null)
                .forEach(c -> {

                    // Modify the corresponding Admission
                    final Admission existingAdmission = currentAdmissionMap.get(c.getKey());
                    final AdmissionVO updatedAdmissionData = c.getValue();

                    // Update the existing admission's data
                    existingAdmission.setAdmissionNote(createAdmissionNote(
                            actingMembership,
                            updatedAdmissionData,
                            existingAdmission.getAdmitted()));
                    existingAdmission.setAdmittedBy(actingMembership);
                    existingAdmission.setResponsible(updatedAdmissionData.isResponsible());
                });

        // All is well.
        return true;
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

        // Find the CategoryIDs of all relevant categories
        final TypedQuery<Category> query = entityManager.createQuery("select a from Category a"
                + " where a.classification like :" + OrganisationPatterns.PARAM_CLASSIFICATION
                + " order by a.categoryID", Category.class)
                .setParameter(OrganisationPatterns.PARAM_CLASSIFICATION, CategorizedAddress.ACTIVITY_CLASSIFICATION);

        final List<String> categoryIDs = new ArrayList<>();
        query.getResultList().stream()
                .filter(Objects::nonNull)
                .map(Category::getCategoryID)
                .filter(catID -> catID != null && !catID.isEmpty())
                .forEach(categoryIDs::add);

        if (log.isDebugEnabled()) {
            log.debug("Found [" + categoryIDs.size() + "] categoryIDs: "
                    + categoryIDs.stream().reduce((l, r) -> l + ", " + r).orElse("<none>"));
        }

        // Pad the ID Lists.
        final int categoryIDsSize = AbstractJpaService.padAndGetSize(categoryIDs, "none");

        // Extract all activity location CategorizedAddresses known to the Organisation.
        final List<CategorizedAddress> categorizedAddresses = JpaUtilities.findEntities(CategorizedAddress.class,
                CategorizedAddress.NAMEDQ_GET_BY_ORGANISATION_ID_AND_CATEGORY_IDS,
                true,
                entityManager,
                aQuery -> {
                    /*
                    query = "select a from CategorizedAddress a "
                        + "where a.owningOrganisation.id = :" + OrganisationPatterns.PARAM_ORGANISATION_ID
                        + " and ( 0 = :" + OrganisationPatterns.PARAM_NUM_CATEGORYIDS
                        + " or a.category.categoryID in :" + OrganisationPatterns.PARAM_CATEGORY_IDS + " ) "
                        + " order by a.shortDesc"),
                     */
                    aQuery.setParameter(OrganisationPatterns.PARAM_ORGANISATION_ID, organisationID);
                    aQuery.setParameter(OrganisationPatterns.PARAM_NUM_CATEGORYIDS, categoryIDsSize);
                    aQuery.setParameter(OrganisationPatterns.PARAM_CATEGORY_IDS, categoryIDs);
                });

        CategoriesAndAddresses toReturn = new CategoriesAndAddresses();
        categorizedAddresses.forEach(toReturn::addCategorizedAddress);

        if (log.isDebugEnabled()) {
            log.debug("Found [" + categorizedAddresses.size() + "] categorizedAddresses.");
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

    private static void validateResponsibleAdmissionsStillExist(
            final Map<AliasAndOrganisationName, Admission> currentResponsibleAdmissions,
            final Map<AliasAndOrganisationName, Admission> responsibleAdmissionsAfterOperation) {

        if (responsibleAdmissionsAfterOperation.isEmpty()) {

            // Complain
            final String msg = currentResponsibleAdmissions.keySet().stream()
                    .map(AliasAndOrganisationName::toString)
                    .reduce((l, r) -> l + ", " + r)
                    .orElse("<inga>");

            throw new IllegalArgumentException("Åtminstone 1 Medlem eller Grupp måste vara ansvarig för en "
                    + "Aktivitet. Kan inte sudda samtliga ansvariga: " + msg);
        }
    }

    private static String createAdmissionNote(final Membership admittedBy,
                                              final AdmissionVO admissionVO,
                                              final Membership admitted) {
        // Compile the admission note
        final String admittedBySomeoneElse = " Anmäld av " + admittedBy.getAlias()
                + " (" + admittedBy.getOrganisation().getOrganisationName() + ")";
        return admissionVO.getNote().orElse("")
                + (!admitted.equals(admittedBy) ? admittedBySomeoneElse : "");
    }
}
