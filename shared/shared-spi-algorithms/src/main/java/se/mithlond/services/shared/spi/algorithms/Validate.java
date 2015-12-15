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

/**
 * Simple argument validator, inspired by the commons-lang.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>
 */
public final class Validate {

    /**
     * Hide constructor for utility classes.
     */
    private Validate() {
        // Do nothing.
    }

    /**
     * Validates that the supplied object is not null, and throws a NullPointerException otherwise.
     *
     * @param object       The object to validate for {@code null}-ness.
     * @param argumentName The argument name of the object to validate. If supplied (i.e. non-{@code null}),
     *                     this value is used in composing a better exception message.
     * @return The supplied object - if it is was not null.
     * @throws NullPointerException if the supplied object was null.
     */
    public static <T> T notNull(final T object, final String argumentName) {

        // Check sanity
        if (object == null) {
            throw new NullPointerException(getMessage("null", argumentName));
        }

        // All done.
        return object;
    }

    /**
     * Validates that the supplied object is not null, and throws an IllegalArgumentException otherwise.
     *
     * @param aString      The string to validate for emptyness.
     * @param argumentName The argument name of the object to validate.
     *                     If supplied (i.e. non-{@code null}), this value is used in composing
     *                     a better exception message.
     * @return The non-empty
     */
    public static String notEmpty(final String aString, final String argumentName) {

        // Check sanity
        notNull(aString, argumentName);

        if (aString.length() == 0) {
            throw new IllegalArgumentException(getMessage("empty", argumentName));
        }
    }

    /**
     * Validates that the supplied condition is true, and throws an IllegalArgumentException otherwise.
     *
     * @param condition The condition to validate for truth.
     * @param message   The exception message used within the IllegalArgumentException if the condition is false.
     */
    public static void isTrue(final boolean condition, final String message) {

        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }

    //
    // Private helpers
    //

    private static String getMessage(final String exceptionDefinition, final String argumentName) {
        return "Cannot handle "
                + exceptionDefinition
                + (argumentName == null ? "" : " '" + argumentName + "'")
                + " argument.";
    }
}
