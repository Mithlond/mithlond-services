/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
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
package se.mithlond.services.shared.spi.algorithms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlTransient;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Common utility class reading and writing properties from the java System,
 * as well as locally available configuration files.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public final class Surroundings {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(Surroundings.class);

    /*
     * Hide constructor for utility classes.
     */
    private Surroundings() {
        // Do nothing
    }

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
    public static String getProperty(@NotNull final String propertyName) {

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

    /**
     * Retrieves a File wrapping a local configuration file, placed in a standardized file structure under the
     * {@link Deployment#getStorageRootDirectory()}, namely <code>organisationName + File.separator
     * + Deployment.getDeploymentType() + serviceNameOrPath.replace("/", File.separator)
     * + File.separator + fileName</code>.
     * This is the standard structure for all non-packaged source configuration files used by the services. Examples
     * of such non-packaged source configuration files are credentials, license keys and similar types of content.
     *
     * @param organisationName  The name of the organisation for which a locally stored configuration file should be
     *                          retrieved.
     * @param serviceNameOrPath The name of the service for which configuration should be retrieved, typically
     *                          something like "google_calendar". This can be an arbitrary path as illustrated above.
     * @param fileName          The name of the configuration file.
     * @return A configuration File as specified by the supplied parameters.
     */
    public static File getLocalConfigFile(@NotNull final String organisationName,
                                          @NotNull final String serviceNameOrPath,
                                          @NotNull final String fileName) {

        // Check sanity
        Validate.notEmpty(organisationName, "organisationName");
        Validate.notEmpty(serviceNameOrPath, "serviceNameOrPath");
        Validate.notEmpty(fileName, "fileName");

        // Synthesize the path to the local configuration file.
        final File toReturn = new File(Deployment.getStorageRootDirectory(),
                organisationName
                        + File.separator
                        + Deployment.getDeploymentType()
                        + File.separator
                        + serviceNameOrPath.replace("/", File.separator)
                        + File.separator
                        + fileName);

        // Check sanity
        final boolean fileExists = toReturn.exists() && toReturn.isFile();
        if (!fileExists) {
            throw new IllegalArgumentException("Local configuration file ["
                    + toReturn.getAbsolutePath()
                    + "] does not exist.");
        }

        // All Done.
        return toReturn;
    }

    /**
     * Retrieves the data from a local configuration file, placed in a standardized file structure under the
     * {@link Deployment#getStorageRootDirectory()}, namely <code>organisationName + File.separator
     * + Deployment.getDeploymentType() + serviceNameOrPath.replace("/", File.separator)
     * + File.separator + fileName</code>.
     * This is the standard structure for all non-packaged source configuration files used by the services. Examples
     * of such non-packaged source configuration files are credentials, license keys and similar types of content.
     *
     * @param organisationName  The name of the organisation for which a locally stored configuration file should be
     *                          retrieved.
     * @param serviceNameOrPath The name of the service for which configuration should be retrieved, typically
     *                          something like "google_calendar". This can be an arbitrary path as illustrated above.
     * @param fileName          The name of the configuration file.
     * @return An InputStream connected to the configuration file as specified by the supplied parameters.
     */
    public static InputStream getLocalConfigurationFile(@NotNull final String organisationName,
                                                        @NotNull final String serviceNameOrPath,
                                                        @NotNull final String fileName) {

        // Delegate
        final File configurationFile = getLocalConfigFile(organisationName, serviceNameOrPath, fileName);

        // Open a stream to the File.
        // Don't use a Reader, as the File may contain binary data.
        try {
            return new FileInputStream(configurationFile);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("Local configuration file ["
                    + configurationFile.getAbsolutePath()
                    + "] could not be opened.", e);
        }
    }

    /**
     * Retrieves the (text) data from a local configuration file, placed in a standardized file structure under the
     * {@link Deployment#getStorageRootDirectory()}, namely <code>organisationName + File.separator
     * + Deployment.getDeploymentType() + serviceNameOrPath.replace("/", File.separator)
     * + File.separator + fileName</code>.
     * This is the standard structure for all non-packaged source configuration files used by the services. Examples
     * of such non-packaged source configuration files are credentials, license keys and similar types of content.
     *
     * @param organisationName  The name of the organisation for which a locally stored configuration file should be
     *                          retrieved.
     * @param serviceNameOrPath The name of the service for which configuration should be retrieved, typically
     *                          something like "google_calendar". This can be an arbitrary path as illustrated above.
     * @param fileName          The name of the configuration file.
     * @return A BufferedReader connected to the configuration file as specified by the supplied parameters.
     */
    public static BufferedReader getLocalConfigurationTextFile(@NotNull final String organisationName,
                                                               @NotNull final String serviceNameOrPath,
                                                               @NotNull final String fileName) {

        // Delegate
        final InputStream stream = getLocalConfigurationFile(organisationName, serviceNameOrPath, fileName);

        // Wrap within a Reader
        return new BufferedReader(new InputStreamReader(stream));
    }

    /**
     * Retrieves the (text) data from a local configuration file, placed in a standardized file structure under the
     * {@link Deployment#getStorageRootDirectory()}, namely <code>organisationName + File.separator
     * + Deployment.getDeploymentType() + serviceNameOrPath.replace("/", File.separator)
     * + File.separator + fileName</code>.
     * This is the standard structure for all non-packaged source configuration files used by the services. Examples
     * of such non-packaged source configuration files are credentials, license keys and similar types of content.
     *
     * @param organisationName  The name of the organisation for which a locally stored configuration file should be
     *                          retrieved.
     * @param serviceNameOrPath The name of the service for which configuration should be retrieved, typically
     *                          something like "google_calendar". This can be an arbitrary path as illustrated above.
     * @param fileName          The name of the configuration file.
     * @return The content of the configuration file, typed as a String.
     */
    public static String getLocalConfigurationTextFileAsString(@NotNull final String organisationName,
                                                               @NotNull final String serviceNameOrPath,
                                                               @NotNull final String fileName) {
        // First, delegate
        final BufferedReader in = getLocalConfigurationTextFile(organisationName,
                serviceNameOrPath,
                fileName);

        // Read the whole file
        final StringBuilder builder = new StringBuilder();
        String aLine;
        try {
            while ((aLine = in.readLine()) != null) {
                builder.append(aLine).append(System.getProperty("line.separator"));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not convert local configuration file data to a String.", e);
        }

        // All Done.
        return builder.toString();
    }
}
