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
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DeploymentTest {

    @Before
    public void setupSharedState() {

        try {
            final Field deploymentName = Deployment.class.getDeclaredField("deploymentType");
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
        System.clearProperty(Deployment.DEPLOYMENT_TYPE_KEY);
    }

    @Test
    public void validateDeploymentName() {

        // Assemble
        final String deploymentName = "FooBar";
        System.setProperty(Deployment.DEPLOYMENT_TYPE_KEY, deploymentName);

        // Act & Assert
        Assert.assertEquals(deploymentName, Deployment.getDeploymentType());
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnIncorrectDeploymentName() {

        // Assemble
        final String incorrectDeploymentName = "Foo Bar";
        System.setProperty(Deployment.DEPLOYMENT_TYPE_KEY, incorrectDeploymentName);

        // Act & Assert
        Deployment.getDeploymentType();
    }

    @Test
    public void validateMatchingPatterns() {

        // Assemble
        final SortedMap<String, Boolean> actual = new TreeMap<>();
        final SortedMap<String, Boolean> expected = new TreeMap<>();
        expected.put("/foo/bar/baz", true);
        expected.put("/contains whitespace/so/illegal", false);
        expected.put("noInitialSlash/so/illegal", false);
        expected.put("\\windoze\\path\\separators", true);
        expected.put("/foo-bar_/is/ok", true);
        expected.put("/Users/lj/Development/Projects/Tolkien/mithlond-services/content/content-impl-ejb/target/test"
                + "-classes/testdata/storageroot", true);

        // Act
        for (Map.Entry<String, Boolean> current : expected.entrySet()) {
            final Boolean isMatch = current.getKey().matches(Deployment.EXPECTED_STORAGE_ROOTDIR_PATTERN);
            actual.put(current.getKey(), isMatch);
        }

        // Assert
        for(Map.Entry<String, Boolean> current : actual.entrySet()) {
            final String msg = " [" + current.getKey() + "]: " + current.getValue()
                    + "  --  expected [" + expected.get(current.getKey()) + "]";
            final String currentKey = current.getKey();
            Assert.assertEquals(msg, expected.get(currentKey), actual.get(currentKey));
        }
    }
}
