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

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * ContainerResponseFilter which adds required HTTP headers for {@code Access-Control-*}
 * in order to permit browsers to connect with RESTful requests to a host other than the
 * one which was used to get the Presentation itself. This permits splitting presentation
 * and backend hosts.
 */
@Provider
public class HttpCORSHeadersFilter implements ContainerResponseFilter {

    /**
     * The prefix for all Access-Control HTTP headers.
     */
    public static final String PREFIX = "Access-Control-Allow-";

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(
            final ContainerRequestContext requestContext,
            final ContainerResponseContext responseContext) throws IOException {

        // Get the response headers
        final MultivaluedMap<String, Object> responseHeaders = responseContext.getHeaders();

        // Add headers to permit browsers to connect to a host other than the
        // one which was used to get the Presentation itself.
        responseHeaders.add(PREFIX + "Origin", "*");
        responseHeaders.add(PREFIX + "Credentials", "true");
        responseHeaders.add(PREFIX + "Headers", "origin, content-type, accept, authorization");
        responseHeaders.add(PREFIX + "Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
    }
}