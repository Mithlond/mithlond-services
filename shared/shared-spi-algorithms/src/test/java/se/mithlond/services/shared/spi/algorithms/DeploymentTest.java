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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DeploymentTest {

    @Before
    public void setupSharedState() {

        try {
            final Field deploymentName = Deployment.class.getDeclaredField("deploymentName");
            deploymentName.setAccessible(true);
            if(deploymentName.get(null) != null) {
                deploymentName.set(null, null);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not null out deploymentName", e);
        }
    }

    @After
    public void teardownSharedPropertyState() {
        System.clearProperty(Deployment.DEPLOYMENT_NAME_KEY);
    }

    @Test
    public void validateDeploymentName() {

        // Assemble
        final String deploymentName = "FooBar";
        System.setProperty(Deployment.DEPLOYMENT_NAME_KEY, deploymentName);

        // Act & Assert
        Assert.assertEquals(deploymentName, Deployment.getDeploymentName());
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnIncorrectDeploymentName() {

        // Assemble
        final String incorrectDeploymentName = "Foo Bar";
        System.setProperty(Deployment.DEPLOYMENT_NAME_KEY, incorrectDeploymentName);

        // Act & Assert
        Deployment.getDeploymentName();
    }
}
