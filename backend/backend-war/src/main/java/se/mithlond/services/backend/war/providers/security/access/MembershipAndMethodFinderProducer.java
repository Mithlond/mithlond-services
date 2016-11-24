/*
 * #%L
 * Nazgul Project: mithlond-services-backend-war
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
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
package se.mithlond.services.backend.war.providers.security.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.shared.spi.algorithms.Deployment;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.ws.rs.Produces;
import java.lang.reflect.Constructor;
import java.util.stream.Stream;

/**
 * CDI producer class returning {@link MembershipFinder}s.
 * This class must be handled by the Container, in order for its @Produces-annotated
 * CDI factory methods to be invoked properly.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@ApplicationScoped
public class MembershipAndMethodFinderProducer {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(MembershipAndMethodFinderProducer.class);

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
     * MembershipFinder class property.
     */
    public static final String FINDER_CLASS_KEY = "nazgul_finder";

    @PostConstruct
    private void afterConstruction() {
        log.info("Producer instance [" + getClass().getSimpleName() + "] created.");
    }

    /**
     * Factory method producing a MembershipAndMethodFinder.
     *
     * @return a MembershipAndMethodFinder tailored for the current deployment environment.
     */
    @Produces
    @Default
    public MembershipFinder getAccessor() {

        KnownEnvironments currentEnvironment = KnownEnvironments.PRODUCTION;

        String deploymentType = Deployment.getDeploymentType();
        if (deploymentType != null && !deploymentType.isEmpty()) {

            // Find the current Deployment environment.
            currentEnvironment = Stream.of(KnownEnvironments.values())
                    .filter(env -> env.name().equalsIgnoreCase(deploymentType.trim()))
                    .findFirst()
                    .orElse(KnownEnvironments.PRODUCTION);

        }

        MembershipFinder toReturn = null;
        switch (currentEnvironment) {
            case DEVELOPMENT:

                // Custom creation logic.
                String membershipFinderClass = System.getenv(FINDER_CLASS_KEY);
                if (membershipFinderClass == null) {
                    membershipFinderClass = System.getProperty(FINDER_CLASS_KEY);
                }

                if (membershipFinderClass != null && !membershipFinderClass.isEmpty()) {

                    try {
                        final Class<?> finderClass = Thread.currentThread()
                                .getContextClassLoader()
                                .loadClass(membershipFinderClass);

                        Constructor<?> constructor = finderClass.getConstructor();
                        Object membershipFinder = constructor.newInstance();

                        if (membershipFinder instanceof MembershipFinder) {
                            toReturn = (MembershipFinder) membershipFinder;
                        }

                    } catch (Exception e) {
                        log.error("Could not create a MembershipFinder from class [" + membershipFinderClass + "]", e);
                    }
                }

                // Fallback to default creation?
                if (toReturn == null) {
                    toReturn = new ResteasyMembershipFinder();
                }
                break;

            default:
                toReturn = new ResteasyMembershipFinder();
                break;
        }

        if (log.isDebugEnabled()) {
            log.debug("DeploymentType " + deploymentType
                    + " ==> MembershipFinder of type "
                    + toReturn.getClass().getSimpleName());
        }

        // All Done.
        return toReturn;
    }
}
