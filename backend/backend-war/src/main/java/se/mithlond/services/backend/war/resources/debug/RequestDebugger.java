/*
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
package se.mithlond.services.backend.war.resources.debug;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.backend.war.resources.AbstractResource;
import se.mithlond.services.organisation.model.transport.OrganisationVO;
import se.mithlond.services.organisation.model.transport.Organisations;

import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Debug resource which simply assists in logging inbound request structures.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Path("/debug")
public class RequestDebugger extends AbstractResource {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(RequestDebugger.class);

    @GET
    public Response handler(
            @Context final HttpHeaders headers,
            @Context final Request request,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo) {

        return logInboundData(headers, request, securityContext, uriInfo);
    }

    @OPTIONS
    public Response preFlightResponder(
            @Context final HttpHeaders headers,
            @Context final Request request,
            @Context final SecurityContext securityContext,
            @Context final UriInfo uriInfo) {

        return logInboundData(headers, request, securityContext, uriInfo);
    }

    //
    // Private helpers
    //

    private Response logInboundData(
            final HttpHeaders headers,
            final Request request,
            final SecurityContext securityContext,
            final UriInfo uriInfo) {

        // Scramble the log data
        final StringBuilder builder = new StringBuilder("Using method [" + request.getMethod() + "]\n");

        // #1) Handle HttpHeaders
        this.log("HttpHeaders", "acceptableLanguages", headers.getAcceptableLanguages(), builder);
        this.log("HttpHeaders", "acceptableMediaTypes", headers.getAcceptableMediaTypes(), builder);
        /*this.log("HttpHeaders", "requestHeaders",
                headers.getRequestHeaders().entrySet().stream().sorted().map(c -> "  [" + c.getKey() + "]: "
                        + c.getValue().stream()
                        .reduce((l, r) -> l + ", " + r)
                        .orElse("<null>") + "\n").collect(Collectors.toList()),
                builder);*/
        this.log("HttpHeaders", "cookies",
                headers.getCookies()
                        .entrySet()
                        .stream()
                        .map(c -> "[" + c.getKey() + "]: " + c.getValue().toString())
                        .collect(Collectors.toList()),
                builder);

        // #2) Handle Request
        this.log("Request", "getMethod", Collections.singletonList(request.getMethod()), builder);

        // #3) Handle SecurityContext
        this.log("SecurityContext",
                "isSecure",
                Collections.singletonList(securityContext.isSecure()),
                builder);
        builder.append("   [authenticationScheme]: " + securityContext.getAuthenticationScheme() + "\n");
        final Principal userPrincipal = securityContext.getUserPrincipal();

        if (userPrincipal != null) {

            builder.append("   [userPrincipal Name]: " + userPrincipal.getName() + "\n");
            builder.append("   [userPrincipal toString()]: " + userPrincipal.toString() + "\n");
            builder.append("   [userPrincipal class]: " + userPrincipal.getClass().getName() + "\n");

            if (userPrincipal instanceof KeycloakPrincipal) {

                final KeycloakPrincipal kcPrincipal = (KeycloakPrincipal) userPrincipal;
                final KeycloakSecurityContext kcSecContext = kcPrincipal.getKeycloakSecurityContext();

                builder.append("   [ksSecContext realm]: " + kcSecContext.getRealm() + "\n");
                builder.append("   [ksSecContext idTokenString]: " + kcSecContext.getIdTokenString() + "\n");
                builder.append("   [ksSecContext tokenString]: " + kcSecContext.getTokenString() + "\n");

                final IDToken idToken = kcSecContext.getIdToken();
                if (idToken != null) {
                    builder.append("   [IDToken birthdate]: " + idToken.getBirthdate() + "\n");
                    builder.append("   [IDToken name]: " + idToken.getName() + "\n");
                    builder.append("   [IDToken email]: " + idToken.getEmail() + "\n");
                }

                final AccessToken accessToken = kcSecContext.getToken();

                if (accessToken != null) {
                    final AccessToken.Access realmAccess = accessToken.getRealmAccess();
                    builder.append("   [RealmAccess roles]: " + realmAccess.getRoles() + "\n");
                }
            }
        } else {

            builder.append("   [userPrincipal]: " + userPrincipal + "\n");
        }

        // Extract the result, log it, and send back the response.
        final String result = builder.toString();
        log.info(result + "\n\n");

        final String jsonData = "{ foo : \"bar\"}";

        // All Done.
        return Response.status(Response.Status.OK)
                .type(MediaType.APPLICATION_JSON)
                .entity(new Organisations(
                        new OrganisationVO(42L, "Fjodjim", "Tokholms Toksällskap"))
                )
                .build();
    }

    private void log(final String category,
            final String property,
            final Collection<?> stuff,
            final StringBuilder builder) {

        builder.append(" [" + category + " :: " + property + "] contains ["
                + (stuff == null ? "<null>" : stuff.size() + "] elements...\n"));

        if (stuff != null && !stuff.isEmpty()) {
            final AtomicInteger index = new AtomicInteger();
            stuff.forEach(item -> builder.append("   [" + index.getAndIncrement() + "]: " + item + "\n"));
        }
    }
}
