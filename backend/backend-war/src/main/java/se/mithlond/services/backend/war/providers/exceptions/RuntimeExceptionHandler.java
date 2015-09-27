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
package se.mithlond.services.backend.war.providers.exceptions;

import se.mithlond.services.backend.war.resources.Parameters;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Generic RuntimeException handler, which populates a JSON response and includes the
 * error messages in outbound HTTP headers (i.e.
 * <code>{@value RuntimeExceptionHandler#INTERNAL_ERROR_TYPE_HEADER}</code> for the internal exception itself, and
 * <code>{@value RuntimeExceptionHandler#ERROR_CAUSE_HEADER}</code> for the cause).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Provider
public class RuntimeExceptionHandler implements ExceptionMapper<RuntimeException> {

    /**
     * Header where the internal error is bound.
     */
    public static final String INTERNAL_ERROR_TYPE_HEADER = Parameters.OUTBOUND_HEADER_PREFIX + "RuntimeException";

    /**
     * Header where the cause of the internal error is bound.
     */
    public static final String ERROR_CAUSE_HEADER = Parameters.OUTBOUND_HEADER_PREFIX + "Cause";

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(final RuntimeException exception) {

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Failed to invoke service.")
                .header(ERROR_CAUSE_HEADER, exception.getCause())
                .header(INTERNAL_ERROR_TYPE_HEADER, exception)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
