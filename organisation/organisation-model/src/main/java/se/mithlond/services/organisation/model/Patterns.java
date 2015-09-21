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
public final class Patterns {

    /**
     * The XML namespace used by content model transport objects.
     */
    public static final String NAMESPACE = "http://xmlns.mithlond.se/xml/ns/organisation";

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
}
