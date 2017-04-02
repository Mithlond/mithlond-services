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
package se.mithlond.services.shared.spi.algorithms.messages;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JmsCompliantMapTest {

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnAttemptingToInsertNonCompliantType() {

        // Assemble
        final JmsCompliantMap unitUnderTest = new JmsCompliantMap();

        // Act & Assert
        unitUnderTest.put("foo", new StringBuffer());
    }

    @Test
    public void validateHappyPath() {

        // Assemble
        final JmsCompliantMap unitUnderTest = new JmsCompliantMap();

        // Act
        unitUnderTest.put("foo", "bar");

        // Assert
        Assert.assertEquals(1, unitUnderTest.size());
        Assert.assertEquals("bar", unitUnderTest.get("foo"));
    }
}
