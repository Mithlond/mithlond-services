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

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Rule;
import se.jguru.nazgul.test.xmlbinding.AbstractStandardizedTimezoneTest;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;

import java.util.List;
import java.util.SortedMap;
import java.util.regex.Pattern;

/**
 * Abstract superclass for JAXB-related tests using a standard TimeZone for the
 * duration of each test method execution.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractPlainJaxbTest extends AbstractStandardizedTimezoneTest implements ExtendedJaxbOperations {

    /**
     * Pattern intended to discover XPaths for trivial (i.e. non-important) diffs within
     * XML structures sent and received.
     */
    public static final Pattern TRIVIAL_XPATH_PATTERN = Pattern.compile(
            "/entityTransporter\\[\\d+\\]/entityClasses\\[\\d+\\](/entityClass\\[\\d+\\](/.*)?)?");

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
     * {@inheritDoc}
     */
    @Override
    public String marshalToXML(final Object... toMarshal) {
        return jaxb.marshal(standardClassLoader, false, toMarshal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String marshalToJSon(final Object... toMarshal) {
        return jaxb.marshal(standardClassLoader, true, toMarshal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T unmarshalFromXML(final Class<T> expectedReturnType, final String toUnmarshal) {
        return jaxb.unmarshal(standardClassLoader, false, expectedReturnType, toUnmarshal);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T unmarshalFromJSON(final Class<T> expectedReturnType, final String toUnmarshal) {
        return jaxb.unmarshal(standardClassLoader, true, expectedReturnType, toUnmarshal);
    }

    /**
     * Validates that the expected and actual XML-formatted strings are
     * identical, ignoring any metaClass differences.
     *
     * @param expected The expected XML.
     * @param actual   The actual, received XML.
     */
    protected void validateIdenticalContent(final String expected, final String actual) {
        validateIdenticalXml(expected, actual);
    }

    /**
     * Validates that the expected and actual XML-formatted strings are
     * identical, ignoring any metaClass differences.
     *
     * @param expected The expected XML.
     * @param actual   The actual, received XML.
     */
    public static void validateIdenticalXml(final String expected, final String actual) {

        final Diff diff;
        try {
            diff = XmlTestUtils.compareXmlIgnoringWhitespace(expected, actual);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not compare XMLs.", e);
        }

        if (!diff.identical()) {

            // Validate that the difference is only within the entityClasses element.
            final SortedMap<String, List<Difference>> diffMap = XmlTestUtils.getXPathLocationToDifferenceMap(diff);
            for (String current : diffMap.keySet()) {
                if (!TRIVIAL_XPATH_PATTERN.matcher(current).matches()) {
                    Assert.fail("Diff [" + current + "] was non-trivial. (" + diffMap.get(current) + ")");
                    break;
                }
            }
        }
    }
}
