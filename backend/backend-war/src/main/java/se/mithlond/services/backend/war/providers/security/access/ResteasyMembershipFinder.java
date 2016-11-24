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

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.backend.war.providers.security.OrganisationAndAlias;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * RestEasy-tailored implementation of the MembershipAndMethodFinder interface.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ResteasyMembershipFinder implements Serializable, MembershipFinder {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(ResteasyMembershipFinder.class);

    // Internal state
    private static final String METHOD_INVOKER_KEY = "org.jboss.resteasy.core.ResourceMethodInvoker";

    /**
     * {@inheritDoc}
     */
    @Override
    public OrganisationAndAlias getOrganisationNameAndAlias(
            final ContainerRequestContext ctx,
            final HttpServletRequest httpRequest) {

        if (log.isDebugEnabled()) {
            final String typeName = ctx == null ? "<none>" : ctx.getClass().getName();
            log.debug("Getting OrganisationAndAlias for ContainerRequestContext of type " + typeName);
        }

        // Production mode.
        final KeycloakSecurityContext securityContext = (KeycloakSecurityContext)
                httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
        final AccessToken accessToken = securityContext.getToken();

        // Printout the JSon Web Token state.
        if (log.isDebugEnabled()) {
            final SortedMap<String, Object> properties = new TreeMap<>();

            final StringBuilder builder = new StringBuilder();
            builder.append(" ============================\n");
            builder.append(" = KeyCloak AccessToken Info:\n");

            try {
                final PropertyDescriptor[] descriptors = Introspector
                        .getBeanInfo(AccessToken.class, Object.class)
                        .getPropertyDescriptors();

                final Class[] noArguments = new Class[0];
                for (PropertyDescriptor current : descriptors) {
                    Method getter = current.getReadMethod();
                    if (getter != null) {
                        properties.put(current.getName(), getter.invoke(accessToken, noArguments));
                    }
                }
            } catch (Exception e) {
                log.error("Could not acquire AccessToken JavaBean properties", e);
            }

            for (Map.Entry<String, Object> current : properties.entrySet()) {
                builder.append(" = [" + current.getKey() + "]: " + current.getValue() + "\n");
            }
            builder.append(" ============================\n");

            // All done.
            log.debug(builder.toString());
        }

        // Preferred Username == Alias
        // Realm == Organisation Name.
        return new OrganisationAndAlias(securityContext.getRealm(), accessToken.getPreferredUsername());
    }
}
