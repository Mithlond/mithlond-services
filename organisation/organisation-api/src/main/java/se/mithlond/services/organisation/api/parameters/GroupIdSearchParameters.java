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
 * Wrapper type holding parameters for searching Groups, Organisations and Classifiers based on their IDs.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"groupIDs", "organisationIDs", "classifierIDs"})
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupIdSearchParameters
        extends AbstractSearchParameters<GroupIdSearchParameters.GroupIdSearchParametersBuilder> {

    // Internal state
    @XmlElementWrapper
    @XmlElement(name = "groupID")
    private List<Long> groupIDs;

    @XmlElementWrapper
    @XmlElement(name = "organisationID")
    private List<Long> organisationIDs;

    @XmlElementWrapper
    @XmlElement(name = "classifierID")
    private List<Long> classifierIDs;

    /**
     * Serializable-friendly constructor.
     */
    public GroupIdSearchParameters() {

        // Create internal state
        this.groupIDs = new ArrayList<>();
        this.organisationIDs = new ArrayList<>();
        this.classifierIDs = new ArrayList<>();
    }

    /**
     * Compound constructor creating a new GroupIdSearchParameters instance wrapping the supplied data.
     *
     * @param groupIDs        the JPA IDs of the groups for which results should be retrieved.
     * @param organisationIDs the JPA IDs of the organisations for which results should be retrieved.
     * @param classifierIDs   the JPA IDs of the classifiers for which results should be retrieved.
     */
    private GroupIdSearchParameters(final List<Long> groupIDs,
            final List<Long> organisationIDs,
            final List<Long> classifierIDs) {

        // Delegate
        this();

        // Assign internal state
        this.groupIDs.addAll(groupIDs);
        this.organisationIDs.addAll(organisationIDs);
        this.classifierIDs.addAll(classifierIDs);
    }

    /**
     * @return The non-null list of Group JPA IDs which should be found.
     */
    public List<Long> getGroupIDs() {
        return groupIDs;
    }

    /**
     * @return The non-null List of Organisation JPA IDs which should be found.
     */
    public List<Long> getOrganisationIDs() {
        return organisationIDs;
    }

    /**
     * @return The non-null List of Classifier JPA IDs which should be found.
     */
    public List<Long> getClassifierIDs() {
        return classifierIDs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void populateInternalStateMap(final SortedMap<String, String> toPopulate) {
        toPopulate.put("groupIDs", groupIDs.toString());
        toPopulate.put("organisationIDs", organisationIDs.toString());
        toPopulate.put("classifierIDs", classifierIDs.toString());
    }

    /**
     * Retrieves a GroupIdSearchParametersBuilder, to start populating a GroupIdSearchParameters instance.
     *
     * @return a new GroupIdSearchParametersBuilder used to populate with parameters for searches.
     */
    public static GroupIdSearchParametersBuilder builder() {
        return new GroupIdSearchParametersBuilder();
    }

    /**
     * Builder class for creating GroupIdSearchParameters instances.
     */
    public static class GroupIdSearchParametersBuilder
            extends AbstractParameterBuilder<GroupIdSearchParametersBuilder> {

        // Internal state
        private List<Long> groupIDs = new ArrayList<>();
        private List<Long> organisationIDs = new ArrayList<>();
        private List<Long> classifierIDs = new ArrayList<>();

        /**
         * Adds the provided group IDs parameter to be used by the GroupIdSearchParametersBuilder instance.
         *
         * @param groupIDs the groupIDs parameter to be used by the GroupIdSearchParameters instance.
         *                 Cannot be null or empty.
         * @return This GroupIdSearchParametersBuilder instance.
         */
        public GroupIdSearchParametersBuilder withGroupIDs(@NotNull final Long... groupIDs) {

            // Add the program to the programs List.
            return addValuesIfApplicable(this.groupIDs, "groupIDs", groupIDs);
        }

        /**
         * Adds the provided organisation IDs parameter to be used by the GroupIdSearchParametersBuilder instance.
         *
         * @param organisationIDs the organisationIDs parameter to be used by the GroupIdSearchParameters instance.
         *                        Cannot be null or empty.
         * @return This GroupIdSearchParametersBuilder instance.
         */
        public GroupIdSearchParametersBuilder withOrganisationIDs(@NotNull final Long... organisationIDs) {

            // Add the program to the programs List.
            return addValuesIfApplicable(this.organisationIDs, "organisationIDs", organisationIDs);
        }

        /**
         * Adds the provided classifier IDs parameter to be used by the GroupIdSearchParametersBuilder instance.
         *
         * @param classifierIDs the classifierIDs parameter to be used by the GroupIdSearchParameters instance.
         *                      Cannot be null or empty.
         * @return This GroupIdSearchParametersBuilder instance.
         */
        public GroupIdSearchParametersBuilder withClassifierIDs(@NotNull final Long... classifierIDs) {

            // Add the program to the programs List.
            return addValuesIfApplicable(this.classifierIDs, "classifierIDs", classifierIDs);
        }

        /**
         * {@inheritDoc}
         */
        public GroupIdSearchParameters build() {

            final GroupIdSearchParameters toReturn = new GroupIdSearchParameters(
                    groupIDs,
                    organisationIDs,
                    classifierIDs);

            toReturn.preferDetailedResponse = this.preferDetailedResponse;

            // All Done.
            return toReturn;
        }
    }
}
