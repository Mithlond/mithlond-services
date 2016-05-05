/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Utility class holding a suite of reusable patterns, constants and algorithms.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public final class OrganisationPatterns {

    /**
     * The XML namespace used by organisation model entity objects.
     */
    public static final String NAMESPACE = "http://xmlns.mithlond.se/xml/ns/organisation";

    /**
     * The XML namespace used by organisation model transport objects.
     */
    public static final String TRANSPORT_NAMESPACE = "http://xmlns.mithlond.se/xml/ns/organisation/transport";

    /**
     * NamedQuery parameter name for membershipId.
     */
    public static final String PARAM_MEMBERSHIP_ID = "membershipId";

    /**
     * NamedQuery parameter name for organisationName.
     */
    public static final String PARAM_ORGANISATION_NAME = "organisationName";

    /**
     * NamedQuery parameter name for groupName.
     */
    public static final String PARAM_GROUP_NAME = "groupName";

    /**
     * NamedQuery parameter name for alias.
     */
    public static final String PARAM_ALIAS = "alias";

    /**
     * NamedQuery parameter name for firstName.
     */
    public static final String PARAM_FIRSTNAME = "firstName";

    /**
     * NamedQuery parameter name for lastName.
     */
    public static final String PARAM_LASTNAME = "lastName";

    /**
     * NamedQuery parameter name for loginPermitted.
     */
    public static final String PARAM_LOGIN_PERMITTED = "loginPermitted";

    /**
     * NamedQuery parameter name for classification.
     */
    public static final String PARAM_CLASSIFICATION = "classification";

    /**
     * NamedQuery parameter name for categoryID.
     */
    public static final String PARAM_CATEGORY_ID = "categoryID";

    /**
     * NamedQuery parameter name for environment identifier.
     */
    public static final String PARAM_ENVIRONMENT_ID = "environmentId";

    /**
     * NamedQuery parameter name for event calendar.
     */
    public static final String PARAM_EVENT_CALENDAR = "eventCalendar";

    /**
     * NamedQuery parameter name for the start of a time interval.
     */
    public static final String PARAM_START_TIME = "startTime";

    /**
     * NamedQuery parameter name for the end of a time interval.
     */
    public static final String PARAM_END_TIME = "endTime";

    /**
     * NamedQuery parameter name for the number of groupIDs found.
     */
    public static final String PARAM_NUM_GROUPIDS = "numGroupIDs";

    /**
     * NamedQuery parameter name for the groupIDs found.
     */
    public static final String PARAM_GROUP_IDS = "groupIDs";

    /**
     * NamedQuery parameter name for the number of organisationIDs found.
     */
    public static final String PARAM_NUM_ORGANISATIONIDS = "numOrganisationIDs";

    /**
     * NamedQuery parameter name for the organisationIDs found.
     */
    public static final String PARAM_ORGANISATION_IDS = "organisationIDs";

    /**
     * NamedQuery parameter name for the organisationID found.
     */
    public static final String PARAM_ORGANISATION_ID = "organisationID";

    /**
     * NamedQuery parameter name for the number of categoryIDs found.
     */
    public static final String PARAM_NUM_CATEGORYIDS = "numCategoryIDs";

    /**
     * NamedQuery parameter name for the classificationIDs found.
     */
    public static final String PARAM_CATEGORY_IDS = "categoryIDs";

    /**
     * NamedQuery parameter name for the number of classificationIDs found.
     */
    public static final String PARAM_NUM_CLASSIFICATIONIDS = "numClassificationIDs";

    /**
     * NamedQuery parameter name for the classificationIDs found.
     */
    public static final String PARAM_CLASSIFICATION_IDS = "classificationIDs";

    /**
     * NamedQuery parameter name for the number of classifications found.
     */
    public static final String PARAM_NUM_CLASSIFICATIONS = "numClassifications";

    /**
     * NamedQuery parameter name for the classifications found.
     */
    public static final String PARAM_CLASSIFICATIONS = "classifications";

    /**
     * NamedQuery parameter name for a pattern matching the careOfLine in an Address.
     */
    public static final String PARAM_ADDRESSCAREOFLINE = "careOfLine";

    /**
     * NamedQuery parameter name for a pattern matching the city in an Address.
     */
    public static final String PARAM_CITY = "city";

    /**
     * NamedQuery parameter name for language.
     */
    public static final String PARAM_LANGUAGE = "language";

    /**
     * NamedQuery parameter name for a pattern matching the country in an Address or Localization.
     */
    public static final String PARAM_COUNTRY = "country";

    /**
     * NamedQuery parameter name for variant.
     */
    public static final String PARAM_VARIANT = "variant";

    /**
     * NamedQuery parameter name for a pattern matching the department in an Address.
     */
    public static final String PARAM_DEPARTMENT = "dept";

    /**
     * NamedQuery parameter name for a description.
     */
    public static final String PARAM_DESCRIPTION = "description";

    /**
     * NamedQuery parameter name for a short description (in a Listable).
     */
    public static final String PARAM_SHORT_DESC = "shortDesc";

    /**
     * NamedQuery parameter name for a full description (in a Listable)
     */
    public static final String PARAM_FULL_DESC = "fullDesc";

    /**
     * NamedQuery parameter name for a number (typically within an Address)
     */
    public static final String PARAM_NUMBER = "number";

    /**
     * NamedQuery parameter name for a street (typically within an Address)
     */
    public static final String PARAM_STREET = "street";

    /**
     * NamedQuery parameter name for a zip code (typically within an Address)
     */
    public static final String PARAM_ZIPCODE = "zipcode";

    /**
     * NamedQuery parameter name for primary keys ("IDs").
     */
    public static final String PARAM_IDS = "ids";
}
