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
package se.mithlond.services.backend.war.providers.headers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.backend.war.providers.security.StandardSecurityFilter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;

/**
 * <p>ContainerResponseFilter which adds the dynamic HTTP header as required for proper CORS
 * operations, catering for the "pre-flight" calls as required by browsers to fetch AJAX data.</p>
 * <p>This requires 3 things:</p>
 * <ol>
 * <li><strong>Static HTTP headers</strong>: Should be configured within the application server
 * or HTTP proxy (such as NGinx or Apache).</li>
 * <li><strong>Dynamic HTTP headers</strong>: Should be configured within this {@link DynamicOriginCORSFilter}.
 * Specifically, since Keycloak implies that the HTTP header "access-control-allow-credentials: true" is sent
 * from the browser to the JAXRS server, the server must respond with a HTTP header "Access-Control-Allow-Origin"
 * which cannot be the wildcard (i.e. '*'), but must instead be the DNS and port of the caller.</li>
 * <li><strong>OPTION calls</strong>: Should be terminated within this {@link DynamicOriginCORSFilter}, to
 * return proper HTTP headers. Otherwise, all resource methods must be duplicated since the JAXRS specification
 * does not permit several method annotations (i.e. @GET and @OPTIONS) on the same method.</li>
 * </ol>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Provider
@PreMatching
public class DynamicOriginCORSFilter implements ContainerRequestFilter, ContainerResponseFilter {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(StandardSecurityFilter.class);

    /**
     * The prefix for all Access-Control HTTP headers.
     */
    public static final String ACCESS_CONTROL_ORIGIN_HTTP_HEADER = "Access-Control-Allow-Origin";

    /**
     * The Http header property key for the CORS origin header.
     */
    public static final String ORIGIN_KEY = "Origin";

    /**
     * The value of the getMethod for HTTP OPTIONS calls.
     */
    public static final String OPTIONS_METHOD = "OPTIONS";

    /*
     * The property which can be used to indicate that Dynamic origin should be suppressed.
     * This is typically used if this JAXRS application is executed behind a proxy where SAME-ORIGIN principle
     * would apply between the client application and this JAXRS backend.
     */
    // public static final String DYNAMIC_ORIGIN_SUPPRESSED = "no_dynamic_origin";

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ContainerRequestContext requestContext) throws IOException {

        // TODO: If we get no "Origin" header, calculate it from the URI supplied.
        final String dynamicOrigin = requestContext.getHeaderString(ORIGIN_KEY);

        // If this is an OPTIONS call (i.e. pre-flight CORS call), we
        // should simply append the dynamic origin header and return.
        if (OPTIONS_METHOD.equalsIgnoreCase(requestContext.getMethod())) {

            requestContext.abortWith(Response
                    .ok()
                    .entity("CORS preflight OPTIONS call.")
                    .header(ACCESS_CONTROL_ORIGIN_HTTP_HEADER, dynamicOrigin)
                    .build());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(
            final ContainerRequestContext requestContext,
            final ContainerResponseContext responseContext)
            throws IOException {

        if (!OPTIONS_METHOD.equalsIgnoreCase(requestContext.getMethod())) {

            final String dynamicOrigin = requestContext.getHeaderString(ORIGIN_KEY);

            if (dynamicOrigin != null) {

                // Simply add the dynamic origin to the outbound headers.
                final MultivaluedMap<String, Object> responseHeaders = responseContext.getHeaders();

                final List<Object> accessControlOriginValues = responseHeaders.get(ACCESS_CONTROL_ORIGIN_HTTP_HEADER);
                if (accessControlOriginValues == null) {
                    responseHeaders.add(ACCESS_CONTROL_ORIGIN_HTTP_HEADER, dynamicOrigin);
                } else {
                    log.warn("ResponseHeader [" + ACCESS_CONTROL_ORIGIN_HTTP_HEADER + "] already held the value: "
                            + accessControlOriginValues.stream()
                            .map(c -> "" + c)
                            .reduce((l, r) -> l + ", r")
                            .orElse("<none>") + ". Not adding dynamicOrigin [" + dynamicOrigin + "]");
                }
            }
        }
    }
}