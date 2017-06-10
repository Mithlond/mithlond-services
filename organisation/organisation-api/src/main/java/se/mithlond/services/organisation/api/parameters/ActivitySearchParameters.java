/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-api
 * %%
 * Copyright (C) 2015 Mithlond
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
package se.mithlond.services.organisation.api.parameters;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * Wrapper type holding parameters for searching Activity-related data.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"startPeriod", "endPeriod",
        "organisationIDs", "activityIDs", "membershipIDs", "freeTextSearch"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ActivitySearchParameters
        extends AbstractSearchParameters<ActivitySearchParameters.ActivitySearchParametersBuilder> {

    // Internal state
    @XmlElement(required = true)
    private LocalDateTime startPeriod;

    @XmlElement(required = true)
    private LocalDateTime endPeriod;

    @XmlElementWrapper
    @XmlElement(name = "organisationID")
    private List<Long> organisationIDs;

    @XmlElementWrapper
    @XmlElement(name = "activityID")
    private List<Long> activityIDs;

    @XmlElementWrapper
    @XmlElement(name = "membershipID")
    private List<Long> membershipIDs;

    @XmlElement
    private String freeTextSearch;

    /**
     * JAXB-friendly constructor.
     */
    public ActivitySearchParameters() {
    }

    /**
     * Compound constructor creating an AbstractSearchParameters instance wrapping the supplied data.
     *
     * @param startPeriod     the start of the time period for which results should be retrieved.
     * @param endPeriod       the end of the time period for which results should be retrieved.
     * @param organisationIDs the JPA IDs of the organisations for which results should be retrieved.
     * @param activityIDs     the JPA IDs of the activities for which results should be retrieved.
     * @param membershipIDs   the membership JPA IDs for which results should be retrieved.
     * @param freeTextSearch  an optional text snippet which should serve as the criterion of a free text search.
     */
    private ActivitySearchParameters(final LocalDateTime startPeriod,
            final LocalDateTime endPeriod,
            final List<Long> organisationIDs,
            final List<Long> activityIDs,
            final List<Long> membershipIDs,
            final String freeTextSearch) {

        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.organisationIDs = organisationIDs;
        this.activityIDs = activityIDs;
        this.membershipIDs = membershipIDs;
        this.freeTextSearch = freeTextSearch;
    }

    /**
     * @return the start of the time period for which results should be retrieved.
     */
    public LocalDateTime getStartPeriod() {
        return startPeriod;
    }

    /**
     * @return the end of the time period for which results should be retrieved.
     */
    public LocalDateTime getEndPeriod() {
        return endPeriod;
    }

    /**
     * @return the JPA IDs of the organisations for which results should be retrieved.
     */
    public List<Long> getOrganisationIDs() {
        return organisationIDs;
    }

    /**
     * @return the JPA IDs of the activities for which results should be retrieved.
     */
    public List<Long> getActivityIDs() {
        return activityIDs;
    }

    /**
     * @return the membership JPA IDs for which results should be retrieved.
     */
    public List<Long> getMembershipIDs() {
        return membershipIDs;
    }

    /**
     * @return an optional text snippet which should serve as the criterion of a free text search.
     */
    public String getFreeTextSearch() {
        return freeTextSearch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void populateInternalStateMap(final SortedMap<String, String> toPopulate) {
        toPopulate.put("activityIDs", activityIDs.toString());
        toPopulate.put("startPeriod", TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(startPeriod));
        toPopulate.put("endPeriod", TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(endPeriod));
        toPopulate.put("organisationIDs", organisationIDs.toString());
        toPopulate.put("membershipIDs", membershipIDs.toString());
        toPopulate.put("freeTextSearch", freeTextSearch);
    }

    /**
     * Retrieves a ActivitySearchParametersBuilder, to start populating a ActivitySearchParameters instance.
     *
     * @return a new ActivitySearchParametersBuilder used to populate with parameters for searches.
     */
    public static ActivitySearchParametersBuilder builder() {
        return new ActivitySearchParametersBuilder();
    }

    /**
     * Builder class for creating GroupIdSearchParameters instances.
     */
    public static class ActivitySearchParametersBuilder
            extends AbstractParameterBuilder<ActivitySearchParametersBuilder> {

        /**
         * The default period size, subtracted from the endPeriod to acquire the startPeriod
         * unless the endPeriod and startPeriod already exists and have correct internal relation
         * (i.e. endPeriod is after startPeriod).
         */
        public static final TemporalAmount DEFAULT_PERIOD_SIZE = Period.ofMonths(3);

        // Internal state
        private LocalDateTime startPeriod;
        private LocalDateTime endPeriod;
        private List<Long> organisationIDs = new ArrayList<>();
        private List<Long> activityIDs = new ArrayList<>();
        private List<Long> membershipIDs = new ArrayList<>();
        private String freeTextSearch = "%";

        /**
         * Adds the provided organisation IDs parameter to be used by the ActivitySearchParametersBuilder instance.
         *
         * @param organisationIDs the organisationIDs parameter to be used by the ActivitySearchParameters instance.
         *                        Cannot be null or empty.
         * @return This ActivitySearchParametersBuilder instance.
         */
        public ActivitySearchParametersBuilder withOrganisationIDs(@NotNull final Long... organisationIDs) {
            return addValuesIfApplicable(this.organisationIDs, "organisationIDs", organisationIDs);
        }

        /**
         * Adds the provided activity IDs parameter to be used by the ActivitySearchParametersBuilder instance.
         *
         * @param activityIDs the activityIDs parameter to be used by the ActivitySearchParameters instance.
         *                    Cannot be null or empty.
         * @return This ActivitySearchParametersBuilder instance.
         */
        public ActivitySearchParametersBuilder withActivityIDs(@NotNull final Long... activityIDs) {
            return addValuesIfApplicable(this.activityIDs, "activityIDs", activityIDs);
        }

        /**
         * Adds the provided membership IDs parameter to be used by the ActivitySearchParametersBuilder instance.
         *
         * @param membershipIDs the membershipIDs parameter to be used by the ActivitySearchParameters instance.
         *                      Cannot be null or empty.
         * @return This ActivitySearchParametersBuilder instance.
         */
        public ActivitySearchParametersBuilder withMembershipIDs(@NotNull final Long... membershipIDs) {
            return addValuesIfApplicable(this.membershipIDs, "membershipIDs", membershipIDs);
        }

        /**
         * Assigns the supplied endPeriod to this ActivitySearchParametersBuilder.
         * Unless the startPeriod value is set properly, it will be adjusted to be a default Period before the
         * supplied endPeriod.
         *
         * @param endPeriod The end of the period in which to search for Activities. Cannot be null or empty.
         * @return This ActivitySearchParametersBuilder instance.
         * @see #DEFAULT_PERIOD_SIZE
         */
        public ActivitySearchParametersBuilder withEndPeriod(@NotNull final LocalDateTime endPeriod) {

            if (endPeriod != null) {
                this.endPeriod = endPeriod;
                syncPeriod();
            }

            // All Done.
            return this;
        }

        /**
         * Assigns the supplied startPeriod to this ActivitySearchParametersBuilder.
         * Unless the endPeriod value is set properly, it will be adjusted to be a default Period after the
         * supplied startPeriod.
         *
         * @param startPeriod The start of the period in which to search for Activities. Cannot be null or empty.
         * @return This ActivitySearchParametersBuilder instance.
         * @see #DEFAULT_PERIOD_SIZE
         */
        public ActivitySearchParametersBuilder withStartPeriod(@NotNull final LocalDateTime startPeriod) {

            // Handle nulls.
            if (startPeriod != null) {
                this.startPeriod = startPeriod;
                syncPeriod();
            }

            // All Done.
            return this;
        }

        /**
         * Assigns the supplied startPeriod to this ActivitySearchParametersBuilder.
         * Unless the endPeriod value is set properly, it will be adjusted to be a default Period after the
         * supplied startPeriod.
         *
         * @param startLocalDateTimeString The start of the period in which to search for Activities, expected on the
         *                                 form {@link TimeFormat#COMPACT_LOCALDATETIME}.
         * @return This ActivitySearchParametersBuilder instance.
         * @see TimeFormat#COMPACT_LOCALDATETIME
         */
        public ActivitySearchParametersBuilder withStartPeriod(@NotNull final String startLocalDateTimeString) {

            // Handle nulls.
            if (startLocalDateTimeString != null && !startLocalDateTimeString.isEmpty()) {

                this.startPeriod = ((ZonedDateTime)TimeFormat.COMPACT_LOCALDATETIME.parse(startLocalDateTimeString))
                        .toLocalDateTime();
                syncPeriod();
            }

            // All Done.
            return this;
        }

        /**
         * Assigns the supplied freeText as a search criterion for the Activities to retrieve.
         *
         * @param freeText a non-empty text string to search for.
         * @return This ActivitySearchParametersBuilder instance.
         */
        public ActivitySearchParametersBuilder withFreeText(@NotNull final String freeText) {

            // Handle nulls.
            if (freeText != null && !freeText.isEmpty()) {
                this.freeTextSearch = freeText;
            }

            // All done.
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ActivitySearchParameters build() {

            // Ensure that we have a validity period
            if (startPeriod == null) {
                startPeriod = LocalDateTime.now();
                syncPeriod();
            }

            final ActivitySearchParameters toReturn = new ActivitySearchParameters(startPeriod,
                    endPeriod,
                    organisationIDs,
                    activityIDs,
                    membershipIDs,
                    freeTextSearch);

            // Add the preferred response mode.
            toReturn.preferDetailedResponse = this.preferDetailedResponse;

            // All Done.
            return toReturn;
        }

        //
        // private helpers
        //

        private void syncPeriod() {

            if (endPeriod != null) {
                if (startPeriod == null || startPeriod.isAfter(endPeriod)) {
                    this.startPeriod = endPeriod.minus(DEFAULT_PERIOD_SIZE);
                }
            } else {
                if (startPeriod != null) {
                    this.endPeriod = startPeriod.plus(DEFAULT_PERIOD_SIZE);
                }
            }
        }
    }
}
