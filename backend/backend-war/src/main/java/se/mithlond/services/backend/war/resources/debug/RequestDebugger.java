package se.mithlond.services.backend.war.resources.debug;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.backend.war.providers.headers.HttpCORSHeadersFilter;
import se.mithlond.services.backend.war.resources.AbstractResource;

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
        this.log("SecurityContext",
                "getUserPrincipal",
                Collections.singletonList(securityContext.getUserPrincipal()),
                builder);

        // Extract the result, log it, and send back the response.
        final String result = builder.toString();
        log.info(result + "\n\n");

        final String jsonData = "{ foo : \"bar\"}";

        // All Done.
        return Response.status(Response.Status.OK)
                .entity(jsonData)
                .header(HttpCORSHeadersFilter.PREFIX + "Origin", "*")
                .type(MediaType.APPLICATION_JSON)
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
