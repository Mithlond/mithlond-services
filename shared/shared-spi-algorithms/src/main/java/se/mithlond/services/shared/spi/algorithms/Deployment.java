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

import java.io.File;

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
     * The key for finding the local file storage root directory.
     */
    public static final String FILE_STORAGE_ROOTDIR_KEY = PREFIX + "storage.rootdir";

    /**
     * The expected format of the Deployment Name property.
     */
    public static final String EXPECTED_DEPLOYMENT_NAME_PATTERN = "\\p{javaLetter}\\p{javaLetterOrDigit}+";

    /**
     * The expected format of the Storage Rootdir property.
     */
    public static final String EXPECTED_STORAGE_ROOTDIR_PATTERN = "([/\\\\][\\p{javaLetterOrDigit}-_]+)+";

    // Internal state
    private static String deploymentName;
    private static File storageRootDirectory;

    /*
     * Hide utility-class constructors.
     */
    private Deployment() {
        // Do nothing
    }

    /**
     * Retrieves the deployment name, by reading the System or environment property
     * <strong>{@value #DEPLOYMENT_NAME_KEY}</strong>, unless this is already done.
     *
     * @return the deployment name, as found within the System or environment property
     * <strong>{@value #DEPLOYMENT_NAME_KEY}</strong>.
     * @throws IllegalStateException if the System or environment property
     *                               <strong>{@value #DEPLOYMENT_NAME_KEY}</strong>
     *                               did not match the regular expression pattern
     *                               <strong>{@value #EXPECTED_DEPLOYMENT_NAME_PATTERN}</strong>.
     */
    public static String getDeploymentName() throws IllegalStateException {

        if (deploymentName == null) {

            // Find the deployment name.
            deploymentName = getProperty(DEPLOYMENT_NAME_KEY);

            // Check sanity
            if (deploymentName == null || !deploymentName.matches(EXPECTED_DEPLOYMENT_NAME_PATTERN)) {
                throw new IllegalStateException(
                        getErrorMessage(DEPLOYMENT_NAME_KEY, EXPECTED_DEPLOYMENT_NAME_PATTERN));
            }
        }

        // All done.
        return deploymentName;
    }

    /**
     * Retrieves the storage root directory, by reading the System or environment property
     * <strong>{@value #FILE_STORAGE_ROOTDIR_KEY}</strong>, unless this is already done.
     *
     * @return the storage root directory File, as created from the path found within the System or environment
     * property <strong>{@value #FILE_STORAGE_ROOTDIR_KEY}</strong>.
     * @throws IllegalStateException if the System or environment property
     *                               <strong>{@value #FILE_STORAGE_ROOTDIR_KEY}</strong>
     *                               did not match the regular expression pattern
     *                               <strong>{@value #EXPECTED_STORAGE_ROOTDIR_PATTERN}</strong>, or if the path
     *                               supplied was to an existing non-directory structure.
     */
    public static File getStorageRootDirectory() throws IllegalStateException {

        if (storageRootDirectory == null) {

            // Find the file storage root dir.
            String path = getProperty(FILE_STORAGE_ROOTDIR_KEY);

            // Check sanity
            if (path == null || !path.matches(EXPECTED_STORAGE_ROOTDIR_PATTERN)) {
                throw new IllegalStateException(
                        getErrorMessage(FILE_STORAGE_ROOTDIR_KEY, EXPECTED_STORAGE_ROOTDIR_PATTERN));
            }

            File candidate = new File(path);
            if (candidate.exists()) {
                if (candidate.isDirectory()) {
                    storageRootDirectory = candidate;
                } else {
                    throw new IllegalStateException("Path [" + path + "] does not point to a directory. "
                            + "Please correct the configuration and restart the application.");
                }
            } else {
                candidate.mkdirs();
                storageRootDirectory = candidate;
            }
        }

        // All done.
        return storageRootDirectory;
    }

    //
    // Private helpers
    //

    private static String getErrorMessage(final String propertyName, final String expectedPattern) {
        return "Property [" + propertyName + "] not properly defined. "
                + "Please define system or environment property [" + propertyName + "] to match the regular "
                + "expression \"" + expectedPattern + "\" and restart the application.";
    }

    private static String getProperty(final String propertyName) {

        String toReturn = System.getProperty(propertyName);
        if (toReturn == null) {
            toReturn = System.getenv(propertyName);
        }

        // All done.
        return toReturn;
    }
}
