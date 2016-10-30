package se.mithlond.services.shared.spi.algorithms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Common utility class reading and writing properties from the java Sysmt
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public final class Surroundings {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(Surroundings.class);

    /**
     * <p>Retrieves a property (value) from the surroundings of the running threads.
     * Properties are read from the following contexts (and in order):</p>
     * <ol>
     * <li>The environment of the running process (i.e. from the {@link System#getenv(String)})</li>
     * <li>The java System Properties (i.e. from the {@link System#getProperty(String)})</li>
     * </ol>
     *
     * @param propertyName The non-empty property name whose property should be retrieved.
     * @return The name of the property to read.
     */
    public static String getProperty(final String propertyName) {

        // Check sanity
        Validate.notEmpty(propertyName, "propertyName");

        // First check - the environment.
        String toReturn = System.getenv(propertyName);
        boolean isReadFromEnvironment = true;

        if (toReturn == null) {

            // Secondly, check the Java System Properties.
            toReturn = System.getProperty(propertyName);
            isReadFromEnvironment = false;
        }

        if (log.isTraceEnabled() && toReturn != null) {
            log.trace("[" + propertyName + "]: " + toReturn + ", Read from "
                    + (isReadFromEnvironment ? "environment" : "system properties")
                    + ".");
        }

        // All done.
        return toReturn;
    }
}
