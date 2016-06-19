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
package se.mithlond.services.backend.war.providers.security;

import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.authorization.api.Authorizer;
import se.mithlond.services.shared.authorization.api.SimpleAuthorizer;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

/**
 * SecurityContext which uses a Membership for Principal.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NazgulMembershipSecurityContext implements SecurityContext {

    // Internal state
    private NazgulMembershipPrincipal principal;

    /**
     * Creates a NazgulMembershipSecurityContext wrapping the supplied Membership.
     *
     * @param membership The authenticated Membership.
     */
    public NazgulMembershipSecurityContext(final Membership membership) {
        this.principal = new NazgulMembershipPrincipal(membership);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Principal getUserPrincipal() {
        return getPrincipal();
    }

    /**
     * @return The wrapped NazgulMembershipPrincipal.
     */
    public NazgulMembershipPrincipal getPrincipal() {
        return principal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserInRole(final String role) {

        // Check sanity
        if (null == role || role.isEmpty()) {
            return false;
        }

        // Delegate processing.
        final Authorizer authorizer = SimpleAuthorizer.getInstance();
        return authorizer.isAuthorized(role, this.getPrincipal().getMembership().getPaths());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSecure() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAuthenticationScheme() {
        return BASIC_AUTH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final String alias = (getPrincipal() == null ? "<unknown>" : getPrincipal().getName());
        return "NazgulMembershipSecurityContext for Membership [" + alias + "]";
    }
}
