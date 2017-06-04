/*-
 * #%L
 * Nazgul Project: mithlond-services-backend-war
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.backend.war.providers.security.MembershipData;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import java.io.Serializable;

/**
 * Simplified Membership finder for Keycloak integration.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class KeycloakMembershipFinder implements MembershipFinder, Serializable {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(KeycloakMembershipFinder.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public MembershipData getMembershipData(final ContainerRequestContext ctx,
                                            final HttpServletRequest request) {

        if (request != null) {

            // Get the AuthorizationContext from Keycloak
            final KeycloakSecurityContext kcContext = (KeycloakSecurityContext) request
                    .getAttribute(KeycloakSecurityContext.class.getName());

            if (kcContext != null) {

                // Extract all data from the Keycloak IDToken.
                final AccessToken token = kcContext.getToken();
                if (token != null) {

                    String realm = null;
                    String firstName = null;
                    String lastName = null;
                    String subject = null;

                    try {
                        //
                        // Get required data:
                        // a) Organisation name :=  Realm
                        // b) UserIdentifier := Subject
                        // c) FirstName := name
                        // d) LastName := Family Name
                        //
                        realm = kcContext.getRealm();
                        firstName = token.getGivenName();
                        lastName = token.getFamilyName();
                        subject = token.getSubject();

                    } catch (Exception e) {
                        log.error("Could not retrieve MembershipData from Keycloak SecurityContext", e);
                    }

                    if (realm != null && firstName != null && lastName != null) {
                        final MembershipData toReturn = new MembershipData(realm, firstName, lastName, subject);

                        if(log.isDebugEnabled()) {
                            log.debug("Synthesized " + toReturn.toString());
                        }

                        // All Done.
                        return toReturn;
                    }

                } else {
                    log.warn("Got null AccessToken from KeycloakSecurityContext.");
                }
            }
        }

        log.warn("Could not retrieve MembershipData from KeycloakSecurityContext. Returning UNKNOWN.");

        // Nopes.
        return UNKNOWN_MEMBERSHIP_DATA;
    }
}
