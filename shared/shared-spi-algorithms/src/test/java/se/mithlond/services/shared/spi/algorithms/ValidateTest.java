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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public class ValidateTest {

    @Test
    public void validateErrorMessageOnSuppliedArgumentName() {

        // Assemble
        final String argumentName = "fooBar";
        final String expectedMsg = "Cannot handle empty 'fooBar' argument.";

        // Act & Assert
        try {
            Validate.notEmpty("", argumentName);
        } catch (IllegalArgumentException expected) {
            Assert.assertEquals(expectedMsg, expected.getMessage());
        } catch (Exception e) {
            Assert.fail("Expected IllegalArgumentException, but got " + e);
        }
    }

    @Test
    public void validateErrorMessageOnNullArgumentName() {

        // Act & Assert
        try {
            Validate.notEmpty("", null);
        } catch (IllegalArgumentException expected) {
            Assert.assertEquals("Cannot handle empty argument.", expected.getMessage());
        } catch (Exception e) {
            Assert.fail("Expected IllegalArgumentException, but got " + e);
        }
    }

    @Test
    public void validateErrorMessageOnNullArgument() {

        // Assemble
        final String argumentName = "fooBar";
        final String expectedMsg = "Cannot handle null 'fooBar' argument.";

        // Act & Assert
        try {
            Validate.notNull(null, argumentName);
        } catch (NullPointerException expected) {
            Assert.assertEquals(expectedMsg, expected.getMessage());
        } catch (Exception e) {
            Assert.fail("Expected IllegalArgumentException, but got " + e);
        }
    }

    @Test
    public void validateErrorMessageOnNullArgumentWithNullName() {

        // Act & Assert
        try {
            Validate.notNull(null, null);
        } catch (NullPointerException expected) {
            Assert.assertEquals("Cannot handle null argument.", expected.getMessage());
        } catch (Exception e) {
            Assert.fail("Expected IllegalArgumentException, but got " + e);
        }
    }

    @Test
    public void validateReturningSameInstance() {

        // Assemble
        final StringBuilder toValidate = new StringBuilder();

        // Act
        final StringBuilder result = Validate.notNull(toValidate, "toValidate");

        // Assert
        Assert.assertSame(toValidate, result);
    }
}
