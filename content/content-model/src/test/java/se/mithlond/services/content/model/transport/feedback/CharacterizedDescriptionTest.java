/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
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
package se.mithlond.services.content.model.transport.feedback;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CharacterizedDescriptionTest extends AbstractPlainJaxbTest {

    // Shared state
    private CharacterizedDescription unitUnderTest;

    @Before
    public void setupSharedState() {

        // Configure the JAXB context
        jaxb.add(CharacterizedDescription.class);

        unitUnderTest = new CharacterizedDescription("bugreport", "This is a bug report");
    }

    @Test
    public void validateMarshallingToJSon() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/characterizedDescription.json");

        // Act
        final String result = marshalToJSon(unitUnderTest);
        // System.out.println("Got:\n" + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void validateUnmarshallingFromJSON() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/transport/characterizedDescription.json");

        // Act
        final CharacterizedDescription resurrected = unmarshalFromJSON(CharacterizedDescription.class, data);

        // Assert
        Assert.assertNotNull(resurrected);
        Assert.assertEquals(resurrected, unitUnderTest);
    }
}
