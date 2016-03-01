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

import org.junit.Before;
import org.junit.Test;
import se.mithlond.services.content.model.localization.helpers.LocalizedTextsHolder;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalizedTextsTest extends AbstractPlainJaxbTest {

    // Shared state
    private LocalizedTextsHolder holder;

    @Before
    public void setupSharedState() {

        holder = new LocalizedTextsHolder();
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final LocalizedTexts unitUnderTest = new LocalizedTexts(new Localization("sv"), "Hejsan");
        unitUnderTest.setText(new Localization("no"), "Morrn Da");
        unitUnderTest.setText(new Localization("en"), "Hello");
        unitUnderTest.setText(new Localization("en", "US", null), "Hi");
        holder.addAll(unitUnderTest);

        // Act
        final String result = marshalToXML(holder);
        System.out.println("Got: " + result);

        // Assert
    }
}
