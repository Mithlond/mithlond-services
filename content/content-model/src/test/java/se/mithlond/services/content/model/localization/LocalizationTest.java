/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
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
package se.mithlond.services.content.model.localization;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.content.model.localization.helpers.Localizations;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalizationTest extends AbstractPlainJaxbTest {

    // Shared state
    private Localizations unitUnderTest;
    private List<Localization> localizations;

    @Before
    public void setupSharedState() {

        // While this gives an exhaustive list of all known Locales,
        // the result could vary between JVM installations - which makes
        // the test results unpredictable.
        //
        // Hence, use a smaller, but well-known Localization set.
        /*
        localizations = Arrays.asList(Locale.getAvailableLocales())
                .stream()
                .filter(locale -> locale.getLanguage() != null && !locale.getLanguage().isEmpty())
                .map(locale -> new Localization(locale.getLanguage(), locale.getCountry(), locale.getVariant()))
                .distinct()
                .filter(localization -> localization.toString().startsWith("sv"))
                .collect(Collectors.toList());
        */

        localizations = new ArrayList<>();
        localizations.add(new Localization("sv"));
        localizations.add(new Localization("sv", "SE", null));
        localizations.add(new Localization("en"));
        localizations.add(new Localization("en", "GB", null));
        Collections.sort(localizations);

        /*
        localizations.stream()
                .sorted()
                .map(Localization::toString)
                .forEach(System.out::println);
        */

        unitUnderTest = new Localizations(localizations);
        jaxb.add(Localizations.class);
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/localizations.xml");

        // Act
        final String result = marshalToXML(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/localizations.xml");

        // Act
        final Localizations unmarshalled = unmarshalFromXML(Localizations.class, data);

        // Assert
        Assert.assertNotNull(unmarshalled);

        final List<Localization> localizations = unmarshalled.getLocalizations();
        Assert.assertEquals(this.localizations.size(), localizations.size());

        for(int i = 0; i < localizations.size(); i++) {
            Assert.assertEquals("left: " + this.localizations.get(i) + ", right: " + localizations.get(i),
                    this.localizations.get(i), localizations.get(i));
            Assert.assertEquals(0, this.localizations.get(i).compareTo(localizations.get(i)));
            Assert.assertEquals(this.localizations.get(i).hashCode(), localizations.get(i).hashCode());
        }
    }
}
