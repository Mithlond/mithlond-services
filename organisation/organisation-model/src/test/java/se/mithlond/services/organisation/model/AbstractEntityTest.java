/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.junit.Assert;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.lang.reflect.Method;
import java.util.List;
import java.util.SortedMap;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractEntityTest extends AbstractPlainJaxbTest {

    /**
     * Pattern intended to discover XPaths for trivial (i.e. non-important) diffs within
     * XML structures sent and received.
     */
    public static final Pattern TRIVIAL_XPATH_PATTERN = Pattern.compile(
            "/entityTransporter\\[\\d+\\]/entityClasses\\[\\d+\\](/entityClass\\[\\d+\\](/.*)?)?");

    /**
     * Validates that the expected and actual XML-formatted strings are
     * identical, ignoring any metaClass differences.
     *
     * @param expected The expected XML.
     * @param actual   The actual, received XML.
     */
    protected void validateIdenticalContent(final String expected, final String actual) {

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

    /**
     * Assigns the JPA ID of the supplied NazgulEntity subclass by using
     * the {@link NazgulEntity#setId(long)} method.
     *
     * @param toAssign The {@link NazgulEntity} subclass object which should have its JPA ID reassigned.
     * @param jpaID    The new JPA ID.
     * @param <T>      The explicit NazgulEntity subclass.
     */
    public static <T extends NazgulEntity> void setJpaIDFor(final T toAssign, final long jpaID) {

        // protected void setId(final long id)

        try {
            final Method setIdMethod = NazgulEntity.class.getDeclaredMethod("setId", Long.TYPE);
            setIdMethod.setAccessible(true);
            setIdMethod.invoke(toAssign, jpaID);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not assign JpaID " + jpaID + " to instance of type "
                    + toAssign.getClass().getSimpleName(), e);
        }
    }
}
