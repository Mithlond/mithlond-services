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
import se.mithlond.services.content.model.localization.helpers.LocalizedTextsHolder;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalizedTextsTest extends AbstractPlainJaxbTest {

    // Shared state
    private LocalizedTextsHolder holder;
    private LocalizedTexts unitUnderTest;

    @Before
    public void setupSharedState() {

        unitUnderTest = new LocalizedTexts(new Localization("sv"), "Hejsan");
        unitUnderTest.setText(new Localization("no"), "Morrn Da");
        unitUnderTest.setText(new Localization("en"), "Hello");
        unitUnderTest.setText(new Localization("en", "US", null), "Hi");

        holder = new LocalizedTextsHolder();
        holder.addAll(unitUnderTest);

        jaxb.add(LocalizedTextsHolder.class);
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/localizedTexts.xml");

        // Act
        final String result = marshalToXML(holder);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
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

        final Set<Localization> localizations = unmarshalled.getLocalizations();
        final List<LocalizedTexts> localizedTextsList = unmarshalled.getLocalizedTextsList();

        Assert.assertEquals(holder.getLocalizations().size(), localizations.size());
        holder.getLocalizations().stream()
                .forEach(current -> Assert.assertTrue(localizations.contains(current)));

        Assert.assertEquals(holder.getLocalizedTextsList().size(), localizedTextsList.size());
        holder.getLocalizedTextsList().stream()
                .forEach(current -> Assert.assertTrue(localizedTextsList.contains(current)));
    }
}
