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
