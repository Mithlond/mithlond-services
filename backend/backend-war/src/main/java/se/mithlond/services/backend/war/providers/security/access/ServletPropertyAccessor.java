package se.mithlond.services.backend.war.providers.security.access;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * Specification for how to read configuration properties from the Web
 * parts of a JavaEE container.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@FunctionalInterface
public interface ServletPropertyAccessor {

    /**
     * The List containing the property sources, in the order they should be
     * investigated for property values.
     */
    List<String> ORDERED_PROPERTY_SOURCES = Arrays.asList(
            "Environment",
            "System Property",
            "Request Header",
            "Request Parameter",
            "Session Attribute",
            "ServletContext Attribute",
            "ServletContext InitParameter");

    /**
     * Finds a property with the supplied propertyName from one of the sources, as given within
     * the {@link #ORDERED_PROPERTY_SOURCES}.
     *
     * @param request      A non-null HttpServletRequest, as received from the Container as
     *                     the result of an inbound request.
     * @param propertyName The name of the property to read.
     * @return The value of the property as requested - or {@code null} if none was foundl
     */
    String get(final HttpServletRequest request, final String propertyName);
}
