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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.organisation.api.MembershipService;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.PersonalSettings;
import se.mithlond.services.shared.authorization.api.AuthorizationPattern;
import se.mithlond.services.shared.authorization.api.Authorizer;
import se.mithlond.services.shared.authorization.api.RequireAuthorization;
import se.mithlond.services.shared.authorization.api.Segmenter;
import se.mithlond.services.shared.authorization.api.SimpleAuthorizer;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.List;
import java.util.TreeSet;

/**
 * <p>Abstract ContainerRequestFilter implementation used to ensure that an active user is authenticated
 * and has required authorization to access a JAX-RS resource method. This abstract implementation should
 * not sport any JAX-RS implementation-specific classes at all. The ContainerRequestFilter must be a
 * post-matching filter, to actually be able to retrieve the {@link Method method} invoked.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Priority(Priorities.AUTHENTICATION)
public abstract class AbstractSecurityFilter implements ContainerRequestFilter {

    /**
     * Default authentication required to invoke any method, unless annotated with
     */
    public static final AuthorizationPattern DEFAULT_AUTH_PATTERN = new AuthorizationPattern(Segmenter.ANY, "member");

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractSecurityFilter.class);

    @EJB
    private MembershipService membershipService;

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ContainerRequestContext ctx) {

        // Find the authorization requirements of the currently invoked method.
        final Method targetMethod = getTargetMethod(ctx);
        String requiredAuthPatterns = null;

        // Debug some?
        printRequestHeaders(ctx.getHeaders());

        // Adhere to the standard WRT the @PermitAll annotation.
        if (!targetMethod.isAnnotationPresent(PermitAll.class)) {

            // Access denied, as per the spec?
            // If so, abort further processing and return a 'forbidden' (HTTP 403) status.
            if (targetMethod.isAnnotationPresent(DenyAll.class)) {
                ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity("Access to requested resource explicitly denied.")
                        .build());
                return;
            }

            // We need to validate the active User's authorization.
            final RequireAuthorization authAnnotation = targetMethod.getAnnotation(RequireAuthorization.class);
            final String authAnnotationPatterns = authAnnotation == null ? "" : authAnnotation.authorizationPatterns();
            if (authAnnotation != null && !authAnnotationPatterns.isEmpty()) {
                requiredAuthPatterns = authAnnotation.authorizationPatterns();
            } else {
                requiredAuthPatterns = DEFAULT_AUTH_PATTERN.toString();
            }

            if(log.isDebugEnabled()) {
                log.debug("Got targetMethod [" + targetMethod + "] and requiredAuthPatterns ["
                        + requiredAuthPatterns + "]");
            }

            // Find the Membership of the active caller.
            final OrganisationAndAlias holder = getOrganisationNameAndAlias(ctx);
            final Membership activeMembership = membershipService.getMembership(
                    holder.getOrganisationName(), holder.getAlias());

            if(log.isDebugEnabled()) {
                log.debug("OrganisationAndAlias " + holder + " and requiredAuthPatterns ["
                        + requiredAuthPatterns + "]");
            }

            if (activeMembership == null) {

                // The user is not logged in. Abort.
                ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Insufficient credentials supplied.")
                        .build());
                return;
            }

            // We have a Membership and information about the required authorization.
            // Find out if we are authorized to invoke the target Method.
            // If the user does not possess the required roles, simply abort.
            if (getAuthorizer().isAuthorized(requiredAuthPatterns, activeMembership.getPaths())) {

                // Dig out the PersonalSettings, and continue processing.
                final PersonalSettings personalSettings = membershipService.getPersonalSettingsFor(activeMembership);
                ctx.setSecurityContext(new NazgulMembershipSecurityContext(activeMembership, personalSettings));
            } else {
                // The user is unauthorized. Abort.
                ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Insufficient authorization for resource [" + ctx.getUriInfo().getRequestUri() + "]")
                        .build());
            }
        }

        // We can continue the invocation
        // This is the RestEasy/KeyCloak way to extract the access token.
        /*
        final KeycloakSecurityContext securityContext = (KeycloakSecurityContext)
                httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
        final AccessToken accessToken = securityContext.getToken();
        securityContext.getRealm()
        accessToken.getPreferredUsername();
        */
    }

    /**
     * Retrieves the OrganisationAndAlias from the data within the supplied ContainerRequestContext.
     *
     * @param ctx The non-null ContainerRequestContext.
     * @return The populated OrganisationAndAlias instance.
     */
    protected abstract OrganisationAndAlias getOrganisationNameAndAlias(final ContainerRequestContext ctx);

    /**
     * Retrieves the target Method being invoked within the current JAX-RS resource.
     *
     * @param ctx The active ContainerRequestContext.
     * @return the target Method being invoked within the current JAX-RS resource.
     */
    protected abstract Method getTargetMethod(final ContainerRequestContext ctx);

    /**
     * Retrieves the Authorizer used to authorize memberships for accessing resource methods.
     *
     * @return the Authorizer used to authorize memberships for accessing resource methods.
     */
    protected Authorizer getAuthorizer() {
        return SimpleAuthorizer.getInstance();
    }

    //
    // Private helpers
    //

    private void printRequestHeaders(final MultivaluedMap<String, String> headers) {

        // Printout the HTTP headers, if available.
        if (log.isDebugEnabled()) {

            final StringBuilder builder = new StringBuilder();
            builder.append("\n\n====== [Inbound Request Headers] ======\n");
            for (String currentKey : new TreeSet<>(headers.keySet())) {

                final List<String> values = headers.get(currentKey);
                if (values.size() <= 1) {
                    builder.append("  [" + currentKey + "]: " + values.get(0) + "\n");
                } else {
                    for (int i = 0; i < values.size(); i++) {
                        builder.append("  [" + currentKey + " (" + i + "/" + (values.size() - 1)
                                + ")]: " + values.get(i) + "\n");
                    }
                }
            }
            builder.append("====== [End Inbound Request Headers] ======\n\n");
            log.debug(builder.toString());
        }
    }

    /*
    private String getRequestParameter(final ContainerRequestContext ctx, final String parameter) {

        // 1) Search the HTTP Headers for the parameter
        String toReturn = ctx.getHeaderString(parameter);

        // 2) Search the HTTP query parameters for the parameter
        if (toReturn == null) {
            toReturn = ctx.getUriInfo().getQueryParameters().getFirst(parameter);
        }

        // All done.
        return toReturn;
    }
    */
}
