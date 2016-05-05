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
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;
import se.mithlond.services.shared.spi.jpa.JpaUtilities;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

        if (log.isDebugEnabled()) {
            log.debug("Received " + parameters + " and activeMembership: " + activeMembership);
        }

        // Pad the ID Lists.
        final int organisationIDsSize = AbstractJpaService.padAndGetSize(parameters.getOrganisationIDs(), 0L);

        // Acquire the padded lists.
        final List<Long> organisationIDs = parameters.getOrganisationIDs();

        final List<Activity> activities = JpaUtilities.findEntities(Activity.class,
                Activity.NAMEDQ_GET_BY_ORGANISATION_IDS_AND_DATERANGE,
                true,
                entityManager,
                aQuery -> {
                    aQuery.setParameter(OrganisationPatterns.PARAM_NUM_ORGANISATIONIDS, organisationIDsSize);
                    aQuery.setParameter(OrganisationPatterns.PARAM_ORGANISATION_IDS, organisationIDs);
                    aQuery.setParameter(OrganisationPatterns.PARAM_START_TIME, parameters.getStartPeriod());
                    aQuery.setParameter(OrganisationPatterns.PARAM_END_TIME, parameters.getEndPeriod());
                });

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
        final Set<AdmissionVO> admissions = Validate.notNull(activityVO.getAdmissions(), "admissions");

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

        // Do we have a Responsible for the Activity?
        // If the ActivityVO
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

        // Persist
        entityManager.persist(toReturn);
        entityManager.flush();

        final TimeZone activityTimeZone = organisation.getTimeZone();
        final ZoneId activityZoneID = activityTimeZone.toZoneId();

        // Convert all inbound AdmissionVOs to proper Admissions
        final List<Admission> allAdmissions = admissions.stream()
                .map(c -> {

                    // Convert the AdmissionVO to an Admission
                    final Membership currentMembership = CommonPersistenceTasks.getSingleMembership(
                            entityManager, c.getAlias(), c.getOrganisation());

                    // Compile the admission note
                    final String admittedBySomeoneElse = " Anmäld av " + activeMembership.getAlias()
                            + " (" + activeMembership.getOrganisation().getOrganisationName() + ")";
                    final String admissionNote = c.getNote().orElse("")
                            + (!currentMembership.equals(activeMembership) ? admittedBySomeoneElse : "");

                    // All Done.
                    return new Admission(
                            toReturn,
                            currentMembership,
                            LocalDateTime.now(activityZoneID),
                            LocalDateTime.now(activityZoneID),
                            admissionNote,
                            c.isResponsible(),
                            activeMembership);
                })
                .collect(Collectors.toList());

        allAdmissions.stream().forEach(a -> {
            entityManager.persist(a);
            allAdmissions.add(a);
        });

        // Sync all IDs.
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


        return null;
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

        // Re-pack into a map relating to the JPA ID of the Activity.
        final Map<Long, Set<AdmissionVO>> admissionVOs = new TreeMap<>();
        Arrays.asList(admissionDetails)
                .stream()
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

        // We do formally permit handling multiple Activities per call ... but it would most likely imply an error.
        if (admissionVOs.size() > 1) {
            final String msg = admissionVOs.keySet().stream()
                    .map(c -> "" + c)
                    .reduce((l, r) -> l + ", " + r)
                    .orElse("<inga>");
            throw new IllegalArgumentException("Endast en Aktivitet kan behandlas per anrop. (Fick " + admissionVOs
                    .size() + "): " + msg);
        }

        // Find the activity to modify
        final Long activityJpaID = admissionVOs.keySet().iterator().next();
        final Activity toModify = entityManager.find(Activity.class, activityJpaID);
        final Map<AliasAndOrganisationName, AdmissionVO> modifiedAdmissions = admissionVOs.get(activityJpaID)
                .stream()
                .collect(Collectors.toMap(
                        c -> new AliasAndOrganisationName(c.getAlias(), c.getOrganisation()),
                        c -> c));

        // Map all current Admissions to their AliasAndOrganisationName.
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

        if (changeType == ChangeType.DELETE) {

            if (toModify.getResponsible() == null) {

                // Do we still have at least one Admission which is responsible for the Activity after deleting?
                final Map<AliasAndOrganisationName, Admission> responsibleAdmissionsAfterDelete =
                        currentResponsibleAdmissions.entrySet().stream()
                                .filter(c -> !modifiedAdmissions.containsKey(c.getKey()))
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                validateResponsibleAdmissionsStillExist(currentResponsibleAdmissions, responsibleAdmissionsAfterDelete);
            }

            // We may remove the Admissions as Requested.
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
            toBeRemoved.stream().forEach(c -> entityManager.remove(c));
            entityManager.flush();

            // All is well.
            return true;
        }

        // The logic to actually validate all changes in advance is quite complex.
        // Hence, simply perform the operation and validate the state afterwards.
        // If illegal, simply rollback the transaction.
        modifiedAdmissions.entrySet().stream().forEach(c -> {

        });


        // We should either add Admissions or modify existing Admissions
        if (toModify.getResponsible() == null) {

            // Do we still have at least one Admission which is responsible for the Activity after deleting?
            final Map<AliasAndOrganisationName, AdmissionVO> responsibleInboundModifications =
                    modifiedAdmissions.entrySet().stream()
                            .filter(c -> c.getValue().isResponsible())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            if (responsibleInboundModifications == null || responsibleInboundModifications.isEmpty()) {

                final Map<AliasAndOrganisationName, AdmissionVO> notResponsibleInboundModifications =
                        modifiedAdmissions.entrySet().stream()
                                .filter(c -> !c.getValue().isResponsible())
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


            }

            final Map<AliasAndOrganisationName, Admission> responsibleAdmissionsAfterModify =
                    currentResponsibleAdmissions.entrySet().stream()
                            .filter(c -> !modifiedAdmissions.containsKey(c.getKey()))
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            validateResponsibleAdmissionsStillExist(currentResponsibleAdmissions, responsibleAdmissionsAfterModify);
        }

        // Nopes.
        return false;
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
    public Map<Category, List<CategorizedAddress>> getActivityLocationAddresses(final String organisationName) {
        return null;
    }


    //
    // Private helpers
    //

    private static void validateAdmissionDates(final LocalDate lateAdmissionDate, final LocalDate lastAdmissionDate)
            throws IllegalArgumentException {

        // The Last admission Date cannot be null.
        Validate.notNull(lastAdmissionDate, "lastAdmissionDate");

        if (lateAdmissionDate != null) {

            if (lastAdmissionDate.isBefore(lateAdmissionDate)) {
                final String lastAdmissionDateString = TimeFormat.YEAR_MONTH_DATE.print(lastAdmissionDate);
                final String lateAdmissionDateString = TimeFormat.YEAR_MONTH_DATE.print(lateAdmissionDate);
                throw new IllegalArgumentException("Sista anmälningsdatum (" + lastAdmissionDateString
                        + ") kan inte vara före gränsen för sen anmälan (" + lateAdmissionDateString + ")");
            }
        }
    }

    private static void validateStartAndEndTimes(final ActivityVO activityVO) {

        final LocalDateTime startTime = Validate.notNull(activityVO.getStartTime(), "activityVO.getStartTime()");
        final LocalDateTime endTime = Validate.notNull(activityVO.getEndTime(), "activityVO.getEndTime()");
        if (endTime.isAfter(startTime)) {

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

                final List<Group> responsibleGroups = JpaUtilities.findEntities(Group.class, Group.NAMEDQ_GET_BY_NAME_ORGANISATION,
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
}
