/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
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
package se.mithlond.services.shared.spi.algorithms;

/**
 * <p>Helper utility to find and configure global settings which are related to separate deployments
 * (i.e. environment specific properties).</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class Deployment {

    /**
     * Prefix for every deployment property name used.
     */
    private static final String PREFIX = "mithlond.";

    /**
     * The system property key for finding the current deployment name.
     */
    public static final String DEPLOYMENT_NAME_KEY = PREFIX + "deployment";

    /**
     * The expected format of the Deployment Name property.
     */
    public static final String EXPECTED_DEPLOYMENT_NAME_PATTERN = "\\p{javaLetter}\\p{javaLetterOrDigit}+";

    // Internal state
    private static String deploymentName;

    /*
     * Hide utility-class constructors.
     */
    private Deployment() {
        // Do nothing
    }

    /**
     * Retrieves the deployment name, by reading the System property
     * <strong>{@value #DEPLOYMENT_NAME_KEY}</strong>, unless this is already done.
     *
     * @return the deployment name, as found within the System property <strong>{@value #DEPLOYMENT_NAME_KEY}</strong>.
     * @throws IllegalStateException if the System property <strong>{@value #DEPLOYMENT_NAME_KEY}</strong>
     *                               did not match the regular expression pattern
     *                               <strong>{@value #EXPECTED_DEPLOYMENT_NAME_PATTERN}</strong>.
     */
    public static String getDeploymentName() throws IllegalStateException {

        if (deploymentName == null) {

            // Find the deployment name from the corresponding System property.
            deploymentName = System.getProperty(DEPLOYMENT_NAME_KEY);

            // Check sanity
            if(!deploymentName.matches(EXPECTED_DEPLOYMENT_NAME_PATTERN)) {
                    throw new IllegalStateException("DeploymentName property [" + DEPLOYMENT_NAME_KEY
                            + "] not properly defined. Please define system property [" + DEPLOYMENT_NAME_KEY
                            + "] to match the regular expression [" + EXPECTED_DEPLOYMENT_NAME_PATTERN + "]");
            }
        }

        // All done.
        return deploymentName;
    }
}
