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

import se.mithlond.services.backend.war.providers.security.OrganisationAndAlias;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

/**
 * Mock-alike MembershipAndMethodFinder which harvests information from the
 * Environment, System Properties, and lastly HTTP request arguments.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EnvironmentPropertyMembershipAndMethodExtractor implements MembershipAndMethodFinder {

    /**
     * Organisation id key
     */
    public static final String ORGANISATION_KEY = "org";

    /**
     * {@inheritDoc}
     */
    @Override
    public OrganisationAndAlias getOrganisationNameAndAlias(
            final ContainerRequestContext ctx,
            final HttpServletRequest request) {

        // Find the id from the Environment, System Properties and lastly request arguments.
        String asdsad = System.getenv("asdsad");

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getTargetMethod(final ContainerRequestContext ctx) {
        return null;
    }

    private String getPropertyIn(final Function<Object, String> getter) {
        getter.apply()
    }
}
