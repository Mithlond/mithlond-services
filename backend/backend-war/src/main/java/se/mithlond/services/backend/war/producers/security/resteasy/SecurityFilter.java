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
package se.mithlond.services.backend.war.producers.security.resteasy;

import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.spi.HttpRequest;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import se.mithlond.services.backend.war.producers.security.AbstractSecurityFilter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;

/**
 * RestEasy-flavoured AbstractSecurityFilter implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Provider
public class SecurityFilter extends AbstractSecurityFilter {

    // Internal state
    private static final String METHOD_INVOKER_KEY = "org.jboss.resteasy.core.ResourceMethodInvoker";

    @Context
    private HttpRequest httpRequest;

    /**
     * {@inheritDoc}
     */
    @Override
    protected AbstractSecurityFilter.OrganisationAndAlias getOrganisationNameAndAlias(
            final ContainerRequestContext ctx) {

        final KeycloakSecurityContext securityContext = (KeycloakSecurityContext)
                httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
        final AccessToken accessToken = securityContext.getToken();

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
}
