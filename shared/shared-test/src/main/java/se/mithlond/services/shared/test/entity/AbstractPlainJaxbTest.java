/*
 * #%L
 * Nazgul Project: mithlond-services-shared-entity-test
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
package se.mithlond.services.shared.test.entity;

import org.joda.time.DateTimeZone;
import org.junit.Rule;
import se.jguru.nazgul.test.xmlbinding.AbstractStandardizedTimezoneTest;

/**
 * Abstract superclass for JAXB-related tests using a standard TimeZone for the
 * duration of each test method execution.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractPlainJaxbTest extends AbstractStandardizedTimezoneTest {

    /**
     * Active rule which assigns a standard DateTimeZone for the remainder of the tests,
     * executed within this AbstractStandardizedTimezoneTest.
     */
    @Rule public PlainJaxbContextRule jaxb;

    /**
     * The standard ClassLoader used to load the active test class.
     */
    protected ClassLoader standardClassLoader = getClass().getClassLoader();

    /**
     * Compound constructor, accepting a non-null DateTimeZone to be set as default
     * during the test run. Also creates a PlainJaxbContextRule applied within the test.
     *
     * @param desiredTimeZone The DateTimeZone to be used when the test cases are executed.
     * @see AbstractStandardizedTimezoneTest#AbstractStandardizedTimezoneTest(DateTimeZone)
     */
    protected AbstractPlainJaxbTest(final DateTimeZone desiredTimeZone) {

        // Delegate
        super(desiredTimeZone);

        // Assign internal state
        this.jaxb = new PlainJaxbContextRule();
    }

    /**
     * Default constructor, which creates an AbstractPlainJaxbTest using the {@code DateTimeZone.UTC} timezone.
     *
     * @see AbstractStandardizedTimezoneTest#AbstractStandardizedTimezoneTest()
     */
    protected AbstractPlainJaxbTest() {

        // Delegate
        this(DateTimeZone.UTC);
    }

    /**
     * Convenience method used to marshal all supplied object to an XML String using the standard ClassLoader.
     *
     * @param toMarshal The objects to marshal.
     * @return The resulting XML string.
     */
    protected String marshal(final Object... toMarshal) {
        return jaxb.marshal(standardClassLoader, toMarshal);
    }

    /**
     * Convenience method used to unmarshal the supplied XML string into an object of type T using
     * the standard ClassLoader.
     *
     * @param expectedReturnType The expected return type.
     * @param toUnmarshal        The XML string to unmarshal.
     * @param <T>                The expected return type.
     * @return The resurrected T object.
     */
    protected <T> T unmarshal(final Class<T> expectedReturnType, final String toUnmarshal) {
        return jaxb.unmarshal(standardClassLoader, expectedReturnType, toUnmarshal);
    }
}
