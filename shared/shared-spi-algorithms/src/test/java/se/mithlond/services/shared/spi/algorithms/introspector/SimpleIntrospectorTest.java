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
package se.mithlond.services.shared.spi.algorithms.introspector;

import org.junit.Assert;
import org.junit.Test;
import se.mithlond.services.shared.spi.algorithms.introspection.SimpleIntrospector;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SimpleIntrospectorTest {

    @Test
    public void validateCopyingSimpleJavaBeanProperties() {

        // Assemble
        final Long twentyFour = 24L;
        final SimplePropertyHolder from = new SimplePropertyHolder("foo", twentyFour, 42);
        final SimplePropertyHolder to = new SimplePropertyHolder();

        // Act
        SimpleIntrospector.copyJavaBeanProperties(from, to);

        // Assert
        Assert.assertEquals("foo", to.getStringProperty());
        Assert.assertEquals(twentyFour, to.getLongProperty());
        Assert.assertEquals((Integer) 42, to.getIntProperty());
    }
}
