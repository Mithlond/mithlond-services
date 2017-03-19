/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SurroundingsTest extends AbstractDeploymentTest {

    // Shared state
    private File localStorageRootDir;
    private static final String ORGANISATION_NAME = "Mifflond";
    private static final String SERVICE = "FoogleCalendarService";
    private static final String CONFIGURATION_FILE_NAME = "someConfiguration.txt";

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSetup() {

        localStorageRootDir = new File(testDataDirectory, "localTestDir");
        Validate.isTrue(localStorageRootDir.exists(), "localStorageRootDir.exists()");
        Validate.isTrue(localStorageRootDir.isDirectory(), "localStorageRootDir.isDirectory()");

        // Assign the storage rootdir key
        System.setProperty(Deployment.DEPLOYMENT_STORAGE_ROOTDIR_KEY, localStorageRootDir.getAbsolutePath());
    }

    @Test
    public void validateReadingDevelopmentConfigurationAsText() {

        // Assemble
        final String deploymentType = "Development";

        // Act
        final String developmentConfig = getConfigurationValueFor(deploymentType);

        // Assert
        Assert.assertEquals("Development configuration text.", developmentConfig.trim());
    }

    @Test
    public void validateReadingStagingConfigurationAsText() {

        // Assemble
        final String deploymentType = "Staging";

        // Act
        final String stagingConfig = getConfigurationValueFor(deploymentType);

        // Assert
        Assert.assertEquals("Staging configuration text.", stagingConfig.trim());
    }

    @Test
    public void validateReadingProductionConfigurationAsText() {

        // Assemble
        final String deploymentType = "Production";

        // Act
        final String productionConfig = getConfigurationValueFor(deploymentType);

        // Assert
        Assert.assertEquals("Production configuration text.", productionConfig.trim());
    }

    //
    // Private helpers
    //

    private String getConfigurationValueFor(final String deploymentType) {

        System.setProperty(Deployment.DEPLOYMENT_TYPE_KEY, deploymentType);

        return Surroundings.getLocalConfigurationTextFileAsString(
                ORGANISATION_NAME,
                SERVICE,
                CONFIGURATION_FILE_NAME);
    }
}
