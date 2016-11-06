/*
 * #%L
 * Nazgul Project: mithlond-services-backend-war
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
package se.mithlond.services.backend.war.providers.security.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.backend.war.providers.security.OrganisationAndAlias;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Mock-alike MembershipAndMethodFinder which harvests information from the
 * Environment, System Properties, and lastly HTTP request arguments.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EnvironmentPropertyMembershipExtractor implements MembershipFinder {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(EnvironmentPropertyMembershipExtractor.class);

    /**
     * Organisation name property.
     */
    public static final String ORGANISATION_KEY = "nazgul_org";

    /**
     * Alias property.
     */
    public static final String ALIAS_KEY = "nazgul_alias";

    /**
     * The List containing the property sources
     */
    public static final List<String> ORDERED_PROPERTY_SOURCES = Arrays.asList(
            "Environment",
            "System Property",
            "Request Header",
            "Request Parameter",
            "Session Attribute",
            "ServletContext Attribute",
            "ServletContext InitParameter");

    private static SortedMap<String, Accessor> accessors = new TreeMap<>();

    static {
        accessors.put("Environment", (request, name) -> System.getenv(name));
        accessors.put("System Property", (request, name) -> System.getProperty(name));
        accessors.put("Request Header", HttpServletRequest::getHeader);
        accessors.put("Request Parameter", ServletRequest::getParameter);
        accessors.put("Session Attribute", (request, name) -> {

            final HttpSession session = request.getSession(false);
            return session != null ? (String) session.getAttribute(name) : null;
        });
        accessors.put("ServletContext Attribute", (request, name) -> {

            final ServletContext servletContext = request.getServletContext();
            return servletContext != null ? (String) servletContext.getAttribute(name) : null;
        });
        accessors.put("ServletContext InitParameter", (request, name) -> {

            final ServletContext servletContext = request.getServletContext();
            return servletContext != null ? servletContext.getInitParameter(name) : null;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrganisationAndAlias getOrganisationNameAndAlias(
            final ContainerRequestContext ctx,
            final HttpServletRequest request) {

        // Find the organisation ID.
        final PropertyAndSource organisationProperty = getProperty(ORGANISATION_KEY, request);
        final PropertyAndSource aliasProperty = getProperty(ALIAS_KEY, request);

        if (log.isDebugEnabled()) {

            final String organisationName = organisationProperty != null && organisationProperty.value != null
                    ? organisationProperty.value
                    : "<null>";
            final String organisationSource = organisationProperty != null && organisationProperty.nameOfSource != null
                    ? organisationProperty.nameOfSource
                    : "<null>";

            log.debug("Found organisation name " + organisationName + " from " + organisationSource);
        }

        if (log.isDebugEnabled()) {

            final String aliasName = aliasProperty != null && aliasProperty.value != null
                    ? aliasProperty.value
                    : "<null>";
            final String aliasSource = aliasProperty != null && aliasProperty.nameOfSource != null
                    ? aliasProperty.nameOfSource
                    : "<null>";

            log.debug("Found alias " + aliasName + " from " + aliasSource);
        }

        // Compile the return value
        if (organisationProperty != null && organisationProperty.isPresent()
                && aliasProperty != null && aliasProperty.isPresent()) {

            // All Done.
            return new OrganisationAndAlias(organisationProperty.value, aliasProperty.value);
        }

        // Nah.
        return null;
    }

    //
    // Private helpers
    //

    /**
     * Specification for how to retrieve a String property value given a supplied property name,
     * given an (optionally used) inbound HttpServletRequest.
     */
    @FunctionalInterface
    interface Accessor {
        String get(HttpServletRequest request, String propertyName);
    }

    private class PropertyAndSource {
        String value;
        String nameOfSource;

        PropertyAndSource(final String value, final String nameOfSource) {
            this.value = value;
            this.nameOfSource = nameOfSource;
        }

        boolean isPresent() {
            return value != null && nameOfSource != null;
        }
    }

    private PropertyAndSource getProperty(final String propertyName, final HttpServletRequest request) {

        return ORDERED_PROPERTY_SOURCES.stream()
                .filter(source -> {

                    // Find the current value.
                    final String value = accessors.get(source).get(request, propertyName);

                    // Ignore both null and empty values.
                    return value != null && !value.isEmpty();
                })
                .map(source -> new PropertyAndSource(accessors.get(source).get(request, propertyName), source))
                .findFirst()
                .orElse(null);
    }
}
