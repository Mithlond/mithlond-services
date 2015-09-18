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

import org.jboss.resteasy.spi.HttpRequest;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.PersonalSettings;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

/**
 * Resource superclass for sharing common functionality. Requires a Resteasy runtime.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public abstract class AbstractResource {

    // Internal state
    @Context
    private HttpRequest httpRequest;

    @Context
    private SecurityContext securityContext;

    /**
     * Retrieves the Membership of the active User, in JPA-disconnected state.
     *
     * @return the Membership of the active User, in JPA-disconnected state.
     * This implies that changes to the supplied Membership <strong>will not</strong>
     * be persisted when the invocation ends. Instead, use EJB services with corresponding
     * transactions to change database state. If the SecurityContext was not injected, this
     * method returns {@code null}.
     */
    protected Membership getDisconnectedActiveMembership() {

        final NazgulMembershipPrincipal principal = getPrincipal();
        if (principal != null) {
            return principal.getMembership();
        }

        return null;
    }

    /**
     * Retrieves the PersonalSettings of the active User, in JPA-disconnected state.
     *
     * @return the PersonalSettings of the active User, in JPA-disconnected state.
     * This implies that changes to the supplied Membership <strong>will not</strong>
     * be persisted when the invocation ends. Instead, use EJB services with corresponding
     * transactions to change database state. If the SecurityContext was not injected, this
     * method returns {@code null}.
     */
    protected PersonalSettings getDisconnectedPersonalSettings() {

        final NazgulMembershipPrincipal principal = getPrincipal();
        if (principal != null) {
            return principal.getPersonalSettings();
        }
        return null;
    }

    /**
     * Checks if the active user belongs to the group with the supplied name.
     *
     * @param group The name of a Group to which the user may belong.
     * @return {@code true} if the active membership belongs to the supplied group.
     */
    protected boolean isUserInGroup(final String group) {
        return securityContext != null && securityContext.isUserInRole(group);
    }

    //
    // Private helpers
    //

    private NazgulMembershipPrincipal getPrincipal() {

        NazgulMembershipPrincipal toReturn = null;

        // Handle null cases in unit tests.
        if (securityContext != null) {
            toReturn = (NazgulMembershipPrincipal) securityContext.getUserPrincipal();
        }

        return toReturn;
    }
}
