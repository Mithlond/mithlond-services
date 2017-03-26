package se.mithlond.services.integration.war;

import javax.annotation.PostConstruct;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@ApplicationPath("/app")
public class NazgulIntegrationApplication extends Application {

    // Singleton instances
    private static Main camelMain;

    /**
     * Default constructor.
     */
    public NazgulIntegrationApplication() {
    }

    /**
     *
     */
    @PostConstruct
    public void initializeCamel() {

        // Initialize the CamelContext.
    }
}
