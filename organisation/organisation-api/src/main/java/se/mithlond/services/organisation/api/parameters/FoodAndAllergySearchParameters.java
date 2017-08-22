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

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * Wrapper type holding parameters for searching Food and Allergy-related data.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"organisationIDs", "activityIDs", "membershipIDs", "groupIDs"})
@XmlAccessorType(XmlAccessType.FIELD)
public class FoodAndAllergySearchParameters
        extends AbstractSearchParameters<FoodAndAllergySearchParameters.FoodAndAllergySearchParametersBuilder> {

    // Internal state
    /**
     * An optional List of JPA IDs for Groups.
     */
    @XmlElementWrapper
    @XmlElement(name = "groupID")
    private List<Long> groupIDs;

    /**
     * An optional List of JPA IDs for Organisations.
     */
    @XmlElementWrapper
    @XmlElement(name = "organisationID")
    private List<Long> organisationIDs;

    /**
     * An optional List of JPA IDs for activities.
     */
    @XmlElementWrapper
    @XmlElement(name = "activityID")
    private List<Long> activityIDs;

    /**
     * An optional List of JPA IDs for Memberships.
     */
    @XmlElementWrapper
    @XmlElement(name = "membershipID")
    private List<Long> membershipIDs;

    /**
     * If true, only Memberships with the loginPermitted flag will be retrieved.
     */
    @XmlAttribute
    private boolean onlyLoginPermitted;

    /**
     * JAXB-friendly constructor.
     */
    public FoodAndAllergySearchParameters() {

        // Create internal state
        this.groupIDs = new ArrayList<>();
        this.organisationIDs = new ArrayList<>();
        this.activityIDs = new ArrayList<>();
        this.membershipIDs = new ArrayList<>();
        onlyLoginPermitted = true;
    }

    /**
     * Compound constructor creating
     *
     * @param groupIDs           An optional List of JPA IDs for Groups.
     * @param organisationIDs    An optional List of JPA IDs for Organisations.
     * @param activityIDs        An optional List of JPA IDs for Activities.
     * @param membershipIDs      An optional List of JPA IDs for Memberships.
     * @param onlyLoginPermitted If {@code true}, only retrieve Memberships who are permitted login.
     */
    private FoodAndAllergySearchParameters(final List<Long> groupIDs,
                                           final List<Long> organisationIDs,
                                           final List<Long> activityIDs,
                                           final List<Long> membershipIDs,
                                           final boolean onlyLoginPermitted) {

        // Delegate
        this();

        // Assign internal state
        this.groupIDs.addAll(groupIDs);
        this.organisationIDs.addAll(organisationIDs);
        this.activityIDs.addAll(activityIDs);
        this.membershipIDs.addAll(membershipIDs);
        this.onlyLoginPermitted = onlyLoginPermitted;
    }

    /**
     * @return A List of JPA IDs for Groups.
     */
    @NotNull
    public List<Long> getGroupIDs() {
        return groupIDs;
    }

    /**
     * @return A List of JPA IDs for Organisations.
     */
    @NotNull
    public List<Long> getOrganisationIDs() {
        return organisationIDs;
    }

    /**
     * @return A List of JPA IDs for Activities.
     */
    @NotNull
    public List<Long> getActivityIDs() {
        return activityIDs;
    }

    /**
     * @return A List of JPA IDs for Memberships.
     */
    @NotNull
    public List<Long> getMembershipIDs() {
        return membershipIDs;
    }

    /**
     * @return If {@code true}, only retrieve Memberships who are permitted login.
     */
    public boolean isOnlyLoginPermitted() {
        return onlyLoginPermitted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void populateInternalStateMap(final SortedMap<String, String> toPopulate) {
        toPopulate.put("groupIDs", groupIDs.toString());
        toPopulate.put("organisationIDs", organisationIDs.toString());
        toPopulate.put("activityIDs", activityIDs.toString());
        toPopulate.put("membershipIDs", membershipIDs.toString());
    }

    /**
     * Retrieves a FoodAndAllergySearchParametersBuilder, to start populating a FoodAndAllergySearchParameters instance.
     *
     * @return a new FoodAndAllergySearchParametersBuilder used to populate with parameters for searches.
     */
    public static FoodAndAllergySearchParameters.FoodAndAllergySearchParametersBuilder builder() {
        return new FoodAndAllergySearchParameters.FoodAndAllergySearchParametersBuilder();
    }

    /**
     * Builder class for creating FoodAndAllergySearchParameters instances.
     */
    public static class FoodAndAllergySearchParametersBuilder
            extends AbstractParameterBuilder<FoodAndAllergySearchParameters.FoodAndAllergySearchParametersBuilder> {

        // Internal state
        private List<Long> groupIDs = new ArrayList<>();
        private List<Long> organisationIDs = new ArrayList<>();
        private List<Long> activityIDs = new ArrayList<>();
        private List<Long> membershipIDs = new ArrayList<>();
        private boolean onlyLoginPermitted = true;

        /**
         * Adds the provided group IDs parameter to be used by the FoodAndAllergySearchParametersBuilder instance.
         *
         * @param membershipIDs the membershipIDs parameter to be used by the FoodAndAllergySearchParameters instance.
         *                      Cannot be null or empty.
         * @return This FoodAndAllergySearchParametersBuilder instance.
         */
        public FoodAndAllergySearchParametersBuilder withMembershipIDs(@NotNull final Long... membershipIDs) {

            // Add the program to the programs List.
            return addValuesIfApplicable(this.membershipIDs, "membershipIDs", membershipIDs);
        }

        /**
         * Adds the provided activity IDs parameter to be used by the FoodAndAllergySearchParametersBuilder instance.
         *
         * @param activityIDs the activityIDs parameter to be used by the FoodAndAllergySearchParameters instance.
         *                    Cannot be null or empty.
         * @return This FoodAndAllergySearchParametersBuilder instance.
         */
        public FoodAndAllergySearchParametersBuilder withActivityIDs(@NotNull final Long... activityIDs) {

            // Add the program to the programs List.
            return addValuesIfApplicable(this.activityIDs, "activityIDs", activityIDs);
        }

        /**
         * Adds the provided group IDs parameter to be used by the FoodAndAllergySearchParametersBuilder instance.
         *
         * @param groupIDs the groupIDs parameter to be used by the FoodAndAllergySearchParameters instance.
         *                 Cannot be null or empty.
         * @return This FoodAndAllergySearchParametersBuilder instance.
         */
        public FoodAndAllergySearchParametersBuilder withGroupIDs(@NotNull final Long... groupIDs) {

            // Add the program to the programs List.
            return addValuesIfApplicable(this.groupIDs, "groupIDs", groupIDs);
        }

        /**
         * Adds the provided organisation IDs parameter to be used by the FoodAndAllergySearchParametersBuilder instance.
         *
         * @param organisationIDs the organisationIDs parameter to be used by the FoodAndAllergySearchParameters instance.
         *                        Cannot be null or empty.
         * @return This FoodAndAllergySearchParametersBuilder instance.
         */
        public FoodAndAllergySearchParametersBuilder withOrganisationIDs(@NotNull final Long... organisationIDs) {

            // Add the program to the programs List.
            return addValuesIfApplicable(this.organisationIDs, "organisationIDs", organisationIDs);
        }

        /**
         * Adds the provided loginOnly parameter to be used by the FoodAndAllergySearchParametersBuilder instance.
         *
         * @param loginOnly the login parameter to be used by the FoodAndAllergySearchParameters instance.
         * @return This FoodAndAllergySearchParametersBuilder instance.
         */
        public FoodAndAllergySearchParametersBuilder withLoginOnly(@NotNull final boolean loginOnly) {

            // Add the program to the programs List.
            this.onlyLoginPermitted = loginOnly;
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FoodAndAllergySearchParameters build() {
            return new FoodAndAllergySearchParameters(
                    this.groupIDs,
                    this.organisationIDs,
                    this.activityIDs,
                    this.membershipIDs,
                    this.onlyLoginPermitted);
        }
    }
}
