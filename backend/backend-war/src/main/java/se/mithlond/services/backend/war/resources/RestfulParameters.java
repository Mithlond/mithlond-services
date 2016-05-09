/*
 * #%L
 * Nazgul Project: mithlond-services-backend-war
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
package se.mithlond.services.backend.war.resources;

/**
 * Utility class to hold constants and methods related to RESTful parameters.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class RestfulParameters {

    /**
     * The prefix of all outbound custom headers.
     */
    public static final String OUTBOUND_HEADER_PREFIX = "mithlond.services.";

    /**
     * Parameter name for a JPA ID.
     */
    public static final String JPA_ID = "id";

    /**
     * Parameter name for indicating if (full) details should be emitted in the response.
     */
    public static final String DETAILS = "detail";

    /**
     * Parameter name for the name of an organisation.
     */
    public static final String ORGANISATION_NAME = "org";

    /**
     * Parameter name for a JPA ID of an Organisation.
     */
    public static final String ORGANISATION_JPA_ID = "orgid";

    /**
     * Parameter name for the (marshalled) data of a MenuStructure.
     */
    public static final String MENUSTRUCTURE_DATA = "menustructure";

    /**
     * Parameter name for deciding if inbound data is in XML or JSON form.
     */
    public static final String ISXML = "isxml";

    /**
     * Parameter name for a date given in yyyyMMdd form.
     */
    public static final String FROM_DATE = "frd";

    /**
     * Parameter name for a date given in yyyyMMdd form.
     */
    public static final String TO_DATE = "frd";

    /*
     * Hide constructor for utility classes.
     */
    private RestfulParameters() {
        // Do nothing
    }
}
