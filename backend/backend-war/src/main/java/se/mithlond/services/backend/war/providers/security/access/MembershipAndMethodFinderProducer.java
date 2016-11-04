package se.mithlond.services.backend.war.providers.security.access;

import se.mithlond.services.shared.spi.algorithms.Deployment;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.Produces;
import java.util.stream.Stream;

/**
 * CDI producer class returning {@link MembershipAndMethodFinder}s.
 * This class must be handled by the Container, in order for its @Produces-annotated
 * CDI factory methods to be invoked properly.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@ApplicationScoped
public class MembershipAndMethodFinderProducer {

    /**
     * The environments known to this {@link MembershipAndMethodFinderProducer}.
     */
    enum KnownEnvironments {

        PRODUCTION,

        STAGING,

        DEVELOPMENT,

        UNIT_TEST
    }

    /**
     * Factory method producing a MembershipAndMethodFinder.
     *
     * @return a MembershipAndMethodFinder tailored for the current deployment environment.
     */
    @Produces
    public MembershipAndMethodFinder getAccessor() {

        KnownEnvironments currentEnvironment = KnownEnvironments.PRODUCTION;

        String deploymentType = Deployment.getDeploymentType();
        if (deploymentType != null && !deploymentType.isEmpty()) {

            // Find the current Deployment environment.
            currentEnvironment = Stream.of(KnownEnvironments.values())
                    .filter(env -> env.name().equalsIgnoreCase(deploymentType.trim()))
                    .findFirst()
                    .orElse(KnownEnvironments.PRODUCTION);

        }

        MembershipAndMethodFinder toReturn = null;
        switch (currentEnvironment) {
            case PRODUCTION:
                toReturn = new ResteasyMembershipAndMethodFinder();
                break;

            default:
                toReturn = null;
                break;
        }

        // All Done.
        return toReturn;
    }
}
