/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-jaxb
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
package se.mithlond.services.shared.spi.jaxb.adapter;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocaleAdapterTest {

    private String[] transportForms = {null, "se", "se", "se-SE", "se-SE", "se-SE-x-lvariant-FI"};
    private String[] expectedTransportForms = {null, "se", "se", "se-SE", "se-SE", "se-SE-x-lvariant-FI"};
    private Locale[] objectForms = {
            null,
            new Locale("se"),
            new Locale("se"),
            new Locale("se", "SE"),
            new Locale("se", "SE"),
            new Locale("se", "SE", "FI")
    };

    private LocaleAdapter unitUnderTest = new LocaleAdapter();

    @Test
    public void validateConvertingToTransportForm() throws Exception {

        // Assemble
        final String[] results = new String[transportForms.length];

        // Act
        for (int i = 0; i < objectForms.length; i++) {
            results[i] = unitUnderTest.marshal(objectForms[i]);
        }

        // Assert
        for (int i = 0; i < results.length; i++) {
            Assert.assertEquals(expectedTransportForms[i], results[i]);
        }
    }

    @Test
    public void validateConvertingFromTransportForm() throws Exception {

        // Assemble
        final Locale[] results = new Locale[transportForms.length];

        // Act
        for (int i = 0; i < transportForms.length; i++) {
            results[i] = unitUnderTest.unmarshal(transportForms[i]);
        }

        // Assert
        for (int i = 0; i < results.length; i++) {
            Assert.assertEquals(objectForms[i], results[i]);
        }
    }

    @Test
    public void validateConvertingUsingLanguageTag() {

        // Assemble
        final String theOddNonMatchingLanguageTag = "nn-NO";
        final SortedMap<String, Locale> languageTag2Locale = new TreeMap<>();
        Arrays.stream(Locale.getAvailableLocales())
                .filter(c -> {

                    final String languageTag = c.toLanguageTag();
                    return !languageTag2Locale.keySet().contains(languageTag)
                            && !theOddNonMatchingLanguageTag.equalsIgnoreCase(languageTag);
                })
                .forEach(c -> languageTag2Locale.put(c.toLanguageTag(), c));

        // Act
        // languageTag2Locale.forEach((key1, value1) -> System.out.println("[" + key1 + "]: " + value1));
        final SortedMap<String, Locale> parsed = new TreeMap<>();
        languageTag2Locale.keySet().forEach(k -> parsed.put(k, Locale.forLanguageTag(k)));

        // Assert
        Assert.assertEquals(languageTag2Locale.size(), parsed.size());
        languageTag2Locale.forEach((key, value) -> {

            final Locale reparsedValue = parsed.get(key);

            if (!value.equals(reparsedValue)) {
                System.out.println("[" + key + "]: " + value + " (" + reparsedValue + ")");
            }

            // Check sanity
            Assert.assertEquals(value, reparsedValue);
        });
    }
}
