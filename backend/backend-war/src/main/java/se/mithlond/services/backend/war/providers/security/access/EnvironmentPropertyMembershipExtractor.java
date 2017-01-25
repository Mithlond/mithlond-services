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

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;

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

    // Internal state
    private final ServletPropertyAccessor propertyAccessor;

    /**
     * Compound constructor, injecting all required state into this EnvironmentPropertyMembershipExtractor.
     *
     * @param propertyAccessor The property accessor, which extracts information from Servlet Contexts.
     */
    @Inject
    public EnvironmentPropertyMembershipExtractor(final ServletPropertyAccessor propertyAccessor) {
        this.propertyAccessor = propertyAccessor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OrganisationAndAlias getOrganisationNameAndAlias(
            final ContainerRequestContext ctx,
            final HttpServletRequest request) {

        // Find the organisation ID.
        final String organisationProperty = propertyAccessor.get(request, ORGANISATION_KEY);
        final String aliasProperty = propertyAccessor.get(request, ALIAS_KEY);

        if (log.isDebugEnabled()) {

            final String orgName = organisationProperty != null ? organisationProperty : "<null>";
            final String alias = aliasProperty != null ? aliasProperty : "<null>";

            log.debug("Found alias " + alias + " from " + orgName);
        }

        // Compile the return value
        if (organisationProperty != null && aliasProperty != null) {

            // All Done.
            return new OrganisationAndAlias(organisationProperty, aliasProperty);
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
}
