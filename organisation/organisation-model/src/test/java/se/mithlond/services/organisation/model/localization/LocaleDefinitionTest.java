/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model.localization;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;
import se.mithlond.services.organisation.model.localization.helpers.Localizations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocaleDefinitionTest extends AbstractPlainJaxbTest {

    // Shared state
    private Localizations unitUnderTest;
    private List<LocaleDefinition> localeDefinitions;

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

        localeDefinitions = new ArrayList<>();
        localeDefinitions.add(new LocaleDefinition("sv"));
        localeDefinitions.add(new LocaleDefinition("sv", "SE", null));
        localeDefinitions.add(new LocaleDefinition("en"));
        localeDefinitions.add(new LocaleDefinition("en", "GB", null));
        Collections.sort(localeDefinitions);

        /*
        localizations.stream()
                .sorted()
                .map(Localization::toString)
                .forEach(System.out::println);
        */

        unitUnderTest = new Localizations(localeDefinitions);
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

        final List<LocaleDefinition> localeDefinitions = unmarshalled.getLocaleDefinitions();
        Assert.assertEquals(this.localeDefinitions.size(), localeDefinitions.size());

        for(int i = 0; i < localeDefinitions.size(); i++) {
            Assert.assertEquals("left: " + this.localeDefinitions.get(i) + ", right: " + localeDefinitions.get(i),
                    this.localeDefinitions.get(i), localeDefinitions.get(i));
            Assert.assertEquals(0, this.localeDefinitions.get(i).compareTo(localeDefinitions.get(i)));
            Assert.assertEquals(this.localeDefinitions.get(i).hashCode(), localeDefinitions.get(i).hashCode());
        }
    }
}
