/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-api
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
package se.mithlond.services.organisation.api.parameters;

import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.shared.spi.algorithms.Validate;

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
 * Wrapper type holding parameters for searching CategorizedAddress-related data.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"organisationID", "classifiers", "shortDescPattern",
        "fullDescPattern", "addressCareOfLinePattern", "departmentNamePattern", "streetPattern", "numberPattern",
        "cityPattern", "zipCodePattern", "countryPattern", "descriptionPattern"})
@XmlAccessorType(XmlAccessType.FIELD)
public class CategorizedAddressSearchParameters extends
        AbstractSearchParameters<CategorizedAddressSearchParameters.CategorizedAddressSearchParametersBuilder> {

    // Internal state

    /**
     * The ID of the organisation for which CategorizedAddresses should be found.
     */
    @XmlAttribute
    private Long organisationID;

    /**
     * An optional List of classifiers for the CategorizedAddresses to find.
     */
    @XmlElementWrapper
    @XmlElement(name = "classifier")
    private List<String> classifiers;

    /**
     * Pattern to match with short description of retrieved CategorizedAddresses.
     */
    @XmlElement
    private String shortDescPattern;

    /**
     * Pattern to match with full description of retrieved CategorizedAddresses.
     */
    @XmlElement
    private String fullDescPattern;

    /**
     * Pattern to match with careOfLine of retrieved CategorizedAddresses.
     */
    @XmlElement
    private String addressCareOfLinePattern;

    /**
     * Pattern to match with departmentName of retrieved CategorizedAddresses.
     */
    @XmlElement
    private String departmentNamePattern;

    /**
     * Pattern to match with street of retrieved CategorizedAddresses.
     */
    @XmlElement
    private String streetPattern;

    /**
     * Pattern to match with number of retrieved CategorizedAddresses.
     */
    @XmlAttribute
    private String numberPattern;

    /**
     * Pattern to match with city of retrieved CategorizedAddresses.
     */
    @XmlElement
    private String cityPattern;

    /**
     * Pattern to match with zipCode of retrieved CategorizedAddresses.
     */
    @XmlAttribute
    private String zipCodePattern;

    /**
     * Pattern to match with country of retrieved CategorizedAddresses.
     */
    @XmlElement
    private String countryPattern;

    /**
     * Pattern to match with description of retrieved CategorizedAddresses.
     */
    @XmlElement
    private String descriptionPattern;

    /**
     * JAXB-friendly constructor.
     */
    public CategorizedAddressSearchParameters() {
    }

    /**
     * Compound constructor creating a CategorizedAddressSearchParameters wrapping the supplied data.
     *
     * @param organisationID           The ID of the organisation for which CategorizedAddresses should be found.
     * @param classifiers              A List of classifiers for the CategorizedAddresses to find.
     * @param shortDescPattern         Pattern to match with short description of retrieved CategorizedAddresses.
     * @param fullDescPattern          Pattern to match with full description of retrieved CategorizedAddresses.
     * @param addressCareOfLinePattern Pattern to match with careOfLine of retrieved CategorizedAddresses.
     * @param departmentNamePattern    Pattern to match with departmentName of retrieved CategorizedAddresses.
     * @param streetPattern            Pattern to match with street of retrieved CategorizedAddresses.
     * @param numberPattern            Pattern to match with number of retrieved CategorizedAddresses.
     * @param cityPattern              Pattern to match with city of retrieved CategorizedAddresses.
     * @param zipCodePattern           Pattern to match with zipCode of retrieved CategorizedAddresses.
     * @param countryPattern           Pattern to match with country of retrieved CategorizedAddresses.
     * @param descriptionPattern       Pattern to match with description of retrieved CategorizedAddresses.
     */
    private CategorizedAddressSearchParameters(final Long organisationID,
            final List<String> classifiers,
            final String shortDescPattern,
            final String fullDescPattern,
            final String addressCareOfLinePattern,
            final String departmentNamePattern,
            final String streetPattern,
            final String numberPattern,
            final String cityPattern,
            final String zipCodePattern,
            final String countryPattern,
            final String descriptionPattern) {

        // Assign internal state
        this.organisationID = organisationID;
        this.classifiers = classifiers;
        this.shortDescPattern = shortDescPattern;
        this.fullDescPattern = fullDescPattern;
        this.addressCareOfLinePattern = addressCareOfLinePattern;
        this.departmentNamePattern = departmentNamePattern;
        this.streetPattern = streetPattern;
        this.numberPattern = numberPattern;
        this.cityPattern = cityPattern;
        this.zipCodePattern = zipCodePattern;
        this.countryPattern = countryPattern;
        this.descriptionPattern = descriptionPattern;
    }

    /**
     * @return The organisation ID of the CategorizedAddresses to retrieve.
     */
    public Long getOrganisationID() {
        return organisationID;
    }

    /**
     * @return A List of classifier IDs for the CategorizedAddresses to retrieve.
     */
    public List<String> getClassifierIDs() {
        return classifiers;
    }

    /**
     * @return Pattern to match with short description of retrieved CategorizedAddresses.
     */
    public String getShortDescPattern() {
        return shortDescPattern;
    }

    /**
     * @return Pattern to match with full description of retrieved CategorizedAddresses.
     */
    public String getFullDescPattern() {
        return fullDescPattern;
    }

    /**
     * @return Pattern to match with careOfLine of retrieved CategorizedAddresses.
     */
    public String getAddressCareOfLinePattern() {
        return addressCareOfLinePattern;
    }

    /**
     * @return Pattern to match with department name of retrieved CategorizedAddresses.
     */
    public String getDepartmentNamePattern() {
        return departmentNamePattern;
    }

    /**
     * @return Pattern to match with street of retrieved CategorizedAddresses.
     */
    public String getStreetPattern() {
        return streetPattern;
    }

    /**
     * @return Pattern to match with number of retrieved CategorizedAddresses.
     */
    public String getNumberPattern() {
        return numberPattern;
    }

    /**
     * @return Pattern to match with city of retrieved CategorizedAddresses.
     */
    public String getCityPattern() {
        return cityPattern;
    }

    /**
     * @return Pattern to match with zipCode of retrieved CategorizedAddresses.
     */
    public String getZipCodePattern() {
        return zipCodePattern;
    }

    /**
     * @return Pattern to match with country of retrieved CategorizedAddresses.
     */
    public String getCountryPattern() {
        return countryPattern;
    }

    /**
     * @return Pattern to match with description of retrieved CategorizedAddresses.
     */
    public String getDescriptionPattern() {
        return descriptionPattern;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void populateInternalStateMap(final SortedMap<String, String> toPopulate) {

        toPopulate.put("organisationID", "" + organisationID);
        toPopulate.put("classifiers", classifiers.toString());
        toPopulate.put("shortDescPattern", shortDescPattern);
        toPopulate.put("fullDescPattern", fullDescPattern);
        toPopulate.put("descriptionPattern", descriptionPattern);
        toPopulate.put("addressCareOfLinePattern", addressCareOfLinePattern);
        toPopulate.put("departmentNamePattern", departmentNamePattern);
        toPopulate.put("streetPattern", streetPattern);
        toPopulate.put("numberPattern", numberPattern);
        toPopulate.put("cityPattern", cityPattern);
        toPopulate.put("zipCodePattern", zipCodePattern);
        toPopulate.put("countryPattern", countryPattern);
    }

    /**
     * Builder class for creating GroupIdSearchParameters instances.
     */
    public static class CategorizedAddressSearchParametersBuilder
            extends AbstractParameterBuilder<CategorizedAddressSearchParametersBuilder> {

        // Internal state
        private Long organisationID = null;
        private List<String> classifiers = new ArrayList<>();
        private String shortDescPattern = ANY_STRING;
        private String fullDescPattern = ANY_STRING;
        private String addressCareOfLinePattern = ANY_STRING;
        private String departmentNamePattern = ANY_STRING;
        private String streetPattern = ANY_STRING;
        private String numberPattern = ANY_STRING;
        private String cityPattern = ANY_STRING;
        private String zipCodePattern = ANY_STRING;
        private String countryPattern = ANY_STRING;
        private String descriptionPattern = ANY_STRING;

        /**
         * Adds the provided classifier IDs parameter to be used by this builder.
         *
         * @param classifiers the classifiers parameter to be used by this builder. Cannot be null or empty.
         * @return This Builder instance.
         */
        public CategorizedAddressSearchParametersBuilder withClassifiers(@NotNull final String... classifiers) {
            return addValuesIfApplicable(this.classifiers, "classifiers", classifiers);
        }

        /**
         * Adds the provided short desc pattern parameter to be used by this builder.
         *
         * @param shortDescPattern the pattern parameter to be used by this builder. Cannot be null or empty.
         * @return This Builder instance.
         */
        public CategorizedAddressSearchParametersBuilder withShortDescPattern(@NotNull final String shortDescPattern) {
            this.shortDescPattern = Validate.notNull(shortDescPattern, "shortDescPattern");
            return this;
        }

        /**
         * Adds the provided full desc pattern parameter to be used by this builder.
         *
         * @param fullDescPattern the pattern parameter to be used by this builder. Cannot be null or empty.
         * @return This Builder instance.
         */
        public CategorizedAddressSearchParametersBuilder withFullDescPattern(@NotNull final String fullDescPattern) {
            this.shortDescPattern = Validate.notNull(fullDescPattern, "fullDescPattern");
            return this;
        }

        /**
         * Adds the provided careOfLine pattern parameter to be used by this builder.
         *
         * @param addressCareOfLinePattern the pattern parameter to be used by this builder. Cannot be null or empty.
         * @return This Builder instance.
         */
        public CategorizedAddressSearchParametersBuilder withAddressCareOfLinePattern(
                @NotNull final String addressCareOfLinePattern) {
            this.shortDescPattern = Validate.notNull(addressCareOfLinePattern, "addressCareOfLinePattern");
            return this;
        }

        /**
         * Adds the provided full desc pattern parameter to be used by this builder.
         *
         * @param departmentNamePattern the pattern parameter to be used by this builder. Cannot be null or empty.
         * @return This Builder instance.
         */
        public CategorizedAddressSearchParametersBuilder withDepartmentNamePattern(
                @NotNull final String departmentNamePattern) {
            this.shortDescPattern = Validate.notNull(departmentNamePattern, "departmentNamePattern");
            return this;
        }

        /**
         * Adds the provided street pattern parameter to be used by this builder.
         *
         * @param streetPattern the pattern parameter to be used by this builder. Cannot be null or empty.
         * @return This Builder instance.
         */
        public CategorizedAddressSearchParametersBuilder withStreetPattern(@NotNull final String streetPattern) {
            this.shortDescPattern = Validate.notNull(streetPattern, "streetPattern");
            return this;
        }

        /**
         * Adds the provided number pattern parameter to be used by this builder.
         *
         * @param numberPattern the pattern parameter to be used by this builder. Cannot be null or empty.
         * @return This Builder instance.
         */
        public CategorizedAddressSearchParametersBuilder withNumberPattern(@NotNull final String numberPattern) {
            this.shortDescPattern = Validate.notNull(numberPattern, "numberPattern");
            return this;
        }

        /**
         * Adds the provided city pattern parameter to be used by this builder.
         *
         * @param cityPattern the pattern parameter to be used by this builder. Cannot be null or empty.
         * @return This Builder instance.
         */
        public CategorizedAddressSearchParametersBuilder withCityPattern(@NotNull final String cityPattern) {
            this.shortDescPattern = Validate.notNull(cityPattern, "cityPattern");
            return this;
        }

        /**
         * Adds the provided zipCode pattern parameter to be used by this builder.
         *
         * @param zipCodePattern the pattern parameter to be used by this builder. Cannot be null or empty.
         * @return This Builder instance.
         */
        public CategorizedAddressSearchParametersBuilder withZipCodePattern(@NotNull final String zipCodePattern) {
            this.shortDescPattern = Validate.notNull(zipCodePattern, "zipCodePattern");
            return this;
        }

        /**
         * Adds the provided country pattern parameter to be used by this builder.
         *
         * @param countryPattern the pattern parameter to be used by this builder. Cannot be null or empty.
         * @return This Builder instance.
         */
        public CategorizedAddressSearchParametersBuilder withCountryPattern(@NotNull final String countryPattern) {
            this.shortDescPattern = Validate.notNull(countryPattern, "countryPattern");
            return this;
        }

        /**
         * Adds the provided description pattern parameter to be used by this builder.
         *
         * @param descriptionPattern the pattern parameter to be used by this builder. Cannot be null or empty.
         * @return This Builder instance.
         */
        public CategorizedAddressSearchParametersBuilder withDescriptionPattern(
                @NotNull final String descriptionPattern) {
            this.shortDescPattern = Validate.notNull(descriptionPattern, "descriptionPattern");
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public CategorizedAddressSearchParameters build() {
            return new CategorizedAddressSearchParameters(organisationID,
                    classifiers,
                    shortDescPattern,
                    fullDescPattern,
                    addressCareOfLinePattern,
                    departmentNamePattern,
                    streetPattern,
                    numberPattern,
                    cityPattern,
                    zipCodePattern,
                    countryPattern,
                    descriptionPattern);
        }
    }
}
