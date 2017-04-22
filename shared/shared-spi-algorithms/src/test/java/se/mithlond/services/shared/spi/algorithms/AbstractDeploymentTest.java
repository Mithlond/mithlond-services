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

import org.junit.After;
import org.junit.Before;
import se.jguru.nazgul.core.algorithms.api.Validate;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractDeploymentTest {

    // Shared state
    protected File testDataDirectory;

    @Before
    public void setupSharedState() {

        final Class<Deployment> deploymentClass = Deployment.class;
        nullifyStaticField(deploymentClass, "deploymentType");
        nullifyStaticField(deploymentClass, "storageRootDirectory");

        final URL resource = getClass().getClassLoader().getResource("testdata");
        Validate.notNull(resource, "resource");

        testDataDirectory = new File(resource.getPath());
        Validate.isTrue(testDataDirectory.exists(), "testDataDirectory.exists()");
        Validate.isTrue(testDataDirectory.isDirectory(), "testDataDirectory.isDirectory()");

        // Delegate
        onSetup();
    }

    protected void onSetup() {
        // Do Nothing.
    }

    @After
    public void teardownSharedPropertyState() {
        System.clearProperty(Deployment.DEPLOYMENT_TYPE_KEY);
        System.clearProperty(Deployment.DEPLOYMENT_STORAGE_ROOTDIR_KEY);
    }

    /**
     * Assigns a null value to the supplied Class::declaredFieldName.
     *
     * @param aClass             The Class wherein the Field resides.
     * @param aDeclaredFieldName The name of the Declared Field.
     */
    @SuppressWarnings("all")
    public static void nullifyStaticField(final Class<?> aClass, final String aDeclaredFieldName) {

        try {
            final Field toNullify = aClass.getDeclaredField(aDeclaredFieldName);
            toNullify.setAccessible(true);
            if (toNullify.get(null) != null) {
                toNullify.set(null, null);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not null out field [" + aClass.getSimpleName()
                    + "::" + aDeclaredFieldName + "]", e);
        }
    }
}
