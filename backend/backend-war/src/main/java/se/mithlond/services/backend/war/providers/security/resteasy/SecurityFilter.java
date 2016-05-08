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
package se.mithlond.services.backend.war.providers.security.resteasy;

import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.backend.war.providers.security.AbstractSecurityFilter;
import se.mithlond.services.backend.war.providers.security.OrganisationAndAlias;
import se.mithlond.services.shared.spi.algorithms.Deployment;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * RestEasy-flavoured AbstractSecurityFilter implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Provider
public class SecurityFilter extends AbstractSecurityFilter {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(SecurityFilter.class);

    // Internal state
    private static final String METHOD_INVOKER_KEY = "org.jboss.resteasy.core.ResourceMethodInvoker";

    @Context
    private HttpRequest httpRequest;

    /**
     * {@inheritDoc}
     */
    @Override
    protected OrganisationAndAlias getOrganisationNameAndAlias(final ContainerRequestContext ctx)
            throws IllegalStateException {

        if (log.isDebugEnabled()) {
            final String typeName = ctx == null ? "<none>" : ctx.getClass().getName();
            log.debug("Getting OrganisationAndAlias for ContainerRequestContext of type " + typeName);
            log.debug("Got HttpRequest " + httpRequest);
        }

        // Development mode?
        if (isDevelopmentEnvironment()) {
            return getDevelopmentOrganisationAndAlias();
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

    /**
     * {@inheritDoc}
     */
    @Override
    protected Method getTargetMethod(final ContainerRequestContext ctx) {

        // Dig out the invoked resource method.
        final ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) ctx.getProperty(METHOD_INVOKER_KEY);
        return methodInvoker.getMethod();
    }

    //
    // Private helpers
    //

    private boolean isDevelopmentEnvironment() throws IllegalStateException {

        final String deploymentName = Deployment.getDeploymentType();
        final boolean isDevEnvironment = deploymentName != null && deploymentName.equalsIgnoreCase("development");

        if (log.isDebugEnabled()) {
            log.debug("Got DeploymentName: " + deploymentName + " --> isDevEnvironment: " + isDevEnvironment);
        }

        // All Done.
        return isDevEnvironment;
    }

    private OrganisationAndAlias getDevelopmentOrganisationAndAlias() {
        return new OrganisationAndAlias(getProperty("dev.organisation"), getProperty("dev.alias"));
    }

    private static String getProperty(final String propertyName) {

        String toReturn = System.getProperty(propertyName);
        if (toReturn == null) {
            toReturn = System.getenv(propertyName);
        }

        // All done.
        return toReturn;
    }
}
