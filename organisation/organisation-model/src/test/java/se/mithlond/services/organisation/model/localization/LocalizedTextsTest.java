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
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.localization.helpers.LocalizedTextsHolder;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalizedTextsTest extends AbstractPlainJaxbTest {

    // Shared state
    private LocalizedTextsHolder holder;
    private LocalizedTexts unitUnderTest;

    @Before
    public void setupSharedState() {

        final String classifier = Localizable.DEFAULT_CLASSIFIER;
        unitUnderTest = new LocalizedTexts("Hello", new LocaleDefinition("sv"), classifier, "Hejsan");
        unitUnderTest.setText(TimeFormat.NORWEGIAN_LOCALE, classifier, "Morrn Da");
        unitUnderTest.setText(Locale.ENGLISH, classifier, "Hello");
        unitUnderTest.setText(Locale.US, classifier, "Hi");

        holder = new LocalizedTextsHolder();
        holder.addAll(unitUnderTest);

        jaxb.add(LocalizedTextsHolder.class);
    }

    @Test
    public void validateMarshallingToXML() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/localizedTexts.xml");

        // Act
        final String result = marshalToXML(holder);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateMarshallingWithUTF8CharsInClassifierToXML() throws Exception {

        // Assemble
        final String shortUtf8Classifier = "shåårtDesc";
        unitUnderTest.setText(TimeFormat.NORWEGIAN_LOCALE, shortUtf8Classifier, "Mårning Dø");
        unitUnderTest.setText(Locale.ENGLISH, shortUtf8Classifier, "Hëllööö?");
        unitUnderTest.setText(TimeFormat.SWEDISH_LOCALE, shortUtf8Classifier, "É du gô eller?");

        final String expected = XmlTestUtils.readFully("testdata/localizedUtf8ClassifierTexts.xml");

        // Act
        final String result = marshalToXML(holder);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateMarshallingWithUTF8CharsInClassifierToJSon() throws Exception {

        // Assemble
        final String shortUtf8Classifier = "shåårtDesc";
        unitUnderTest.setText(TimeFormat.NORWEGIAN_LOCALE, shortUtf8Classifier, "Mårning Dø");
        unitUnderTest.setText(Locale.ENGLISH, shortUtf8Classifier, "Hëllööö?");
        unitUnderTest.setText(TimeFormat.SWEDISH_LOCALE, shortUtf8Classifier, "É du gô eller?");

        final String expected = XmlTestUtils.readFully("testdata/localizedUtf8ClassifierTexts.json");

        // Act
        final String result = marshalToJSon(holder);
        // System.out.println("Got: " + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void validateMarshallingToJSON() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/localizedTexts.json");

        // Act
        final String result = marshalToJSon(holder);
        // System.out.println("Got: " + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/localizedTexts.xml");

        // Act
        final LocalizedTextsHolder unmarshalled = jaxb.unmarshal(standardClassLoader,
                false,
                LocalizedTextsHolder.class,
                data);

        // Assert
        Assert.assertNotNull(unmarshalled);

        final Set<LocaleDefinition> localeDefinitions = unmarshalled.getLocaleDefinitions();
        final List<LocalizedTexts> localizedTextsList = unmarshalled.getLocalizedTextsList();

        Assert.assertEquals(holder.getLocaleDefinitions().size(), localeDefinitions.size());
        holder.getLocaleDefinitions().forEach(current -> Assert.assertTrue(localeDefinitions.contains(current)));

        Assert.assertEquals(holder.getLocalizedTextsList().size(), localizedTextsList.size());
        holder.getLocalizedTextsList().forEach(current -> Assert.assertTrue(localizedTextsList.contains(current)));
    }

    @Test
    public void validateUnmarshallingUtf8ClassifierTextsFromXML() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/localizedUtf8ClassifierTexts.xml");

        // Act
        final LocalizedTextsHolder unmarshalled = unmarshalFromXML(LocalizedTextsHolder.class, data);

        // Assert
        validateUnmarshalledUtf8Texts(unmarshalled);
    }

    @Test
    public void validateUnmarshallingUtf8ClassifierTextsFromJSON() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/localizedUtf8ClassifierTexts.json");

        // Act
        final LocalizedTextsHolder unmarshalled = unmarshalFromJSON(LocalizedTextsHolder.class, data);

        // Assert
        validateUnmarshalledUtf8Texts(unmarshalled);
    }

    @Test
    public void generateInsertSQL() {

        // Assemble
        final Function<LocaleDefinition, String> INSERT_VALUES = ld -> "'" + ld.getLocale().getCountry()
                + "', '" + ld.getLocale().getLanguage()
                + "', '" + ld.getLocale().getVariant() + "'";

        final SortedSet<String> existingLocaleTags = new TreeSet<>();

        final AtomicInteger index = new AtomicInteger();
        LocaleDefinition.COMMON_LOCALES.stream()
                .map(c -> "insert into locale_definitions values (" + index.incrementAndGet() + ", "
                        + INSERT_VALUES.apply(c) + "); ")
                .forEach(System.out::println);

        LocaleDefinition.COMMON_LOCALES.stream()
                .map(c -> c.getLocale().toLanguageTag())
                .forEach
                (existingLocaleTags::add);
        
        Arrays.stream(Locale.getAvailableLocales())
                .filter(c -> !"".equals(c.getLanguage()))
                .sorted(Comparator.comparing(Locale::getLanguage))
                .map(LocaleDefinition::new)
                .filter(c -> !LocaleDefinition.COMMON_LOCALES.contains(c))
                .forEach(c -> {

                    final String languageTag = c.getLocale().toLanguageTag();
                    if(!existingLocaleTags.contains(languageTag)) {

                        existingLocaleTags.add(languageTag);
                        final String insertSQL = "insert into locale_definitions values ("
                                + index.incrementAndGet() + ", " + INSERT_VALUES.apply(c) + "); ";
                        System.out.println(insertSQL);
                    }
                });

        // Act

        // Assert
    }

    //
    // Private helpers
    //

    private void validateUnmarshalledUtf8Texts(final LocalizedTextsHolder unmarshalled) {

        final String shortUtf8Classifier = "shåårtDesc";
        final Stream<String> expectedTextStream = Stream.of("Mårning Dø", "Hëllööö?", "É du gô eller?");

        Assert.assertNotNull(unmarshalled);

        final Set<LocalizedText> oddClassifierTexts = unmarshalled.getLocalizedTextsList()
                .iterator()
                .next()
                .getTexts()
                .stream()
                .filter(lt -> shortUtf8Classifier.equals(lt.getClassifier()))
                .collect(Collectors.toSet());

        Assert.assertEquals(3, oddClassifierTexts.size());
        oddClassifierTexts.forEach(c -> Assert.assertEquals(shortUtf8Classifier, c.getClassifier()));

        expectedTextStream.forEach(c -> Assert.assertTrue(
                oddClassifierTexts.stream().anyMatch(lt -> lt.getText().equals(c))));
    }
}
