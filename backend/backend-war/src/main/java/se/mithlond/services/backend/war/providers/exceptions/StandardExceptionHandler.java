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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.backend.war.providers.headers.DynamicOriginCORSFilter;
import se.mithlond.services.backend.war.resources.RestfulParameters;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic RuntimeException handler, which populates a JSON response and includes the
 * error messages in outbound HTTP headers (i.e.
 * <code>{@value StandardExceptionHandler#INTERNAL_ERROR_TYPE_HEADER}</code> for the internal exception itself, and
 * <code>{@value StandardExceptionHandler#ERROR_CAUSE_HEADER}</code> for the cause).
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Provider
public class StandardExceptionHandler implements ExceptionMapper<Exception> {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(StandardExceptionHandler.class);

    /**
     * Header where the internal error is bound.
     */
    public static final String INTERNAL_ERROR_TYPE_HEADER = RestfulParameters.OUTBOUND_HEADER_PREFIX
            + "RuntimeException";

    /**
     * Header where the cause of the internal error is bound.
     */
    public static final String ERROR_CAUSE_HEADER = RestfulParameters.OUTBOUND_HEADER_PREFIX + "Cause";

    /**
     * Header where the cause chain of the internal error is bound.
     */
    public static final String CAUSE_CHAIN_HEADER = RestfulParameters.OUTBOUND_HEADER_PREFIX + "CauseChain";

    @Context
    private HttpServletRequest req;

    /**
     * {@inheritDoc}
     */
    @Override
    public Response toResponse(final Exception exception) {

        final String exceptionStackTrace = extractExceptionStacktrace(exception);

        final Response.ResponseBuilder responseBuilder = Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Failed to invoke service.\n" + exceptionStackTrace)
                .header(ERROR_CAUSE_HEADER, exception.getCause())
                .header(INTERNAL_ERROR_TYPE_HEADER, exception)
                .header(CAUSE_CHAIN_HEADER, exceptionStackTrace);

        if (req != null) {

            // Add the CORS Allow-Origin header to the error response as well.
            // This permits the Browser to display error message data.
            final String origin = req.getHeader(DynamicOriginCORSFilter.ORIGIN_KEY);
            responseBuilder.header(DynamicOriginCORSFilter.ACCESS_CONTROL_ORIGIN_HTTP_HEADER, origin);

            log.debug("Added CORS header [" + DynamicOriginCORSFilter.ACCESS_CONTROL_ORIGIN_HTTP_HEADER
                    + "] with the value [" + origin + "] to the response.");

        } else {

            log.warn("HttpServletRequest not injected. Not adding [" + DynamicOriginCORSFilter
                    .ACCESS_CONTROL_ORIGIN_HTTP_HEADER + "] header.");
        }

        final StringWriter errorWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(errorWriter));
        errorWriter.flush();

        // All Done.
        return responseBuilder
                .type(MediaType.TEXT_PLAIN)
                .entity(errorWriter.toString())
                .build();
    }

    //
    // Private helpers
    //

    private String extractExceptionStacktrace(final Exception ex) {

        // Create a sort-of human-readable string containing the exception stack trace.
        final StringBuilder builder = new StringBuilder();
        final List<String> segmentList = new ArrayList<>();

        for (Throwable current = ex; current != null; current = current.getCause()) {

            final String message = current instanceof ConstraintViolationException
                    ? extractContraintViolationExceptionStacktrace((ConstraintViolationException) current)
                    : current.getMessage();

            segmentList.add(" [" + current.getClass().getSimpleName() + "]: " + message);
        }

        for (int i = 0; i < segmentList.size(); i++) {
            final String current = " (" + i + "/" + segmentList.size() + "): " + segmentList.get(i) + "\n";
            builder.append(current);
        }

        // All done.
        return builder.toString();
    }

    /**
     * Extracts a string containing a {@link ConstraintViolationException}'s error representation.
     *
     * @param ex The {@link ConstraintViolationException} from which to extract the error representation.
     * @return The error string.
     */
    public static String extractContraintViolationExceptionStacktrace(final ConstraintViolationException ex) {

        final StringBuilder toReturn = new StringBuilder();
        final int numViolations = ex.getConstraintViolations().size();
        int index = 0;

        for (ConstraintViolation<?> current : ex.getConstraintViolations()) {

            final StringBuilder pathAndValue = new StringBuilder("[Path: ");
            for(Path.Node theNode : current.getPropertyPath()) {
                pathAndValue.append(theNode.getName()).append(".");
            }
            pathAndValue.replace(pathAndValue.length() - 1, pathAndValue.length(), "]: ");
            current.getInvalidValue();

            current.getPropertyPath();
            final String msg = "  Constraint [" + index++ + "/" + numViolations + " in ("
                    + current.getRootBeanClass().getName() + ")]: " + current.getMessage() + " - "
                    + pathAndValue.toString() + "\n";
            toReturn.append(msg);
        }

        // All Done
        return toReturn.toString();
    }
}
