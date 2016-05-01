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
package se.mithlond.services.shared.spi.algorithms.exception;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.Set;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ExceptionMessageManagerTest {

    // Shared state
    private Validator validator;

    @Before
    public void setupSharedState() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    public void validateExceptionStacktrace() {

        // Assemble
        final ValidatingType foo = new ValidatingType("FirstName", null, 26);

        // Act
        final Set<ConstraintViolation<ValidatingType>> result = validator.validate(foo);
        final ConstraintViolationException ex = new ConstraintViolationException("Well...", result);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.size());

        final ConstraintViolation<ValidatingType> cv = result.iterator().next();
        final Path propertyPath = cv.getPropertyPath();
        Assert.assertEquals("lastName", propertyPath.toString());

        final String s = ExceptionMessageManager.extractConstraintViolationExceptionStacktrace(ex);
        Assert.assertTrue(s.contains("Constraint [0/1]: may not be null"));
    }

    @Test
    public void validateGeneratedStacktrace() {

        // Assemble
        final IOException rootCause = new IOException("This is an inner cause.");
        final IllegalArgumentException outerException = new IllegalArgumentException("This is a wrapper.", rootCause);

        // Act
        final String result = ExceptionMessageManager.getReadableStacktrace(outerException);

        // Assert
        Assert.assertTrue(result.contains("(1/2):  [IllegalArgumentException]: This is a wrapper."));
        Assert.assertTrue(result.contains("(2/2):  [IOException]: This is an inner cause."));
    }
}
