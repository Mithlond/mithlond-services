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

/**
 * Specification for finding an active Membership and invoked JaxRS method.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface MembershipFinder {

    /**
     * The OrganisationAndAlias instance retrieved by the
     * {@link #getOrganisationNameAndAlias(ContainerRequestContext, HttpServletRequest)} method whenever an
     * organisation and alias could not be established from the inbound request.
     */
    OrganisationAndAlias UNKNOWN_ORGANISATION_AND_ALIAS = new OrganisationAndAlias("Unknown", "Nobody");

    /**
     * Retrieves the OrganisationAndAlias from the data within the supplied ContainerRequestContext.
     *
     * @param ctx     The non-null ContainerRequestContext.
     * @param request The active and non-null HttpServletRequest.
     * @return The populated OrganisationAndAlias instance.
     */
    OrganisationAndAlias getOrganisationNameAndAlias(
            final ContainerRequestContext ctx,
            final HttpServletRequest request);
}
