package se.mithlond.services.backend.war.providers.security.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Standard {@link ServletPropertyAccessor} implementation, sporting standard implementations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StandardServletPropertyAccessor implements Serializable, ServletPropertyAccessor {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(StandardServletPropertyAccessor.class);

    /**
     * SortedMap relating a description to the corresponding Accessor instance.
     */
    private static final SortedMap<String, ServletPropertyAccessor> ACCESSORS;

    static {

        ACCESSORS = new TreeMap<>();
        ACCESSORS.put("Environment", (request, name) -> System.getenv(name));
        ACCESSORS.put("System Property", (request, name) -> System.getProperty(name));
        ACCESSORS.put("Request Header", HttpServletRequest::getHeader);
        ACCESSORS.put("Request Parameter", ServletRequest::getParameter);
        ACCESSORS.put("Session Attribute", (request, name) -> {

            final HttpSession session = request.getSession(false);
            return session != null ? (String) session.getAttribute(name) : null;
        });
        ACCESSORS.put("ServletContext Attribute", (request, name) -> {

            final ServletContext servletContext = request.getServletContext();
            return servletContext != null ? (String) servletContext.getAttribute(name) : null;
        });
        ACCESSORS.put("ServletContext InitParameter", (request, name) -> {

            final ServletContext servletContext = request.getServletContext();
            return servletContext != null ? servletContext.getInitParameter(name) : null;
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String get(final HttpServletRequest request, final String propertyName) {

        final PropertyAndSource propertyAndSource = ORDERED_PROPERTY_SOURCES
                .stream()
                .filter(source -> {

                    // Find the current value.
                    final String value = ACCESSORS.get(source).get(request, propertyName);

                    // Ignore both null and empty values.
                    return value != null && !value.isEmpty();
                })
                .map(source -> new PropertyAndSource(ACCESSORS.get(source).get(request, propertyName), source))
                .findFirst()
                .orElse(null);

        if(propertyAndSource != null && log.isDebugEnabled()) {
            log.debug("Found value " + propertyAndSource.value + " in " + propertyAndSource.nameOfSource);
        }

        // All Done.
        return (propertyAndSource == null ? null : propertyAndSource.value);
    }

    //
    // Private helpers
    //

    private class PropertyAndSource {
        String value;
        String nameOfSource;

        PropertyAndSource(final String value, final String nameOfSource) {
            this.value = value;
            this.nameOfSource = nameOfSource;
        }

        boolean isPresent() {
            return value != null && nameOfSource != null;
        }
    }

    private PropertyAndSource getProperty(final String propertyName, final HttpServletRequest request) {

        return ORDERED_PROPERTY_SOURCES.stream()
                .filter(source -> {

                    // Find the current value.
                    final String value = ACCESSORS.get(source).get(request, propertyName);

                    // Ignore both null and empty values.
                    return value != null && !value.isEmpty();
                })
                .map(source -> new PropertyAndSource(ACCESSORS.get(source).get(request, propertyName), source))
                .findFirst()
                .orElse(null);
    }
}
