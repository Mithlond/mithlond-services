/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model.transport.convenience.food;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.transport.food.FoodPreferenceVO;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SlimFoodPreferencesVOTest extends AbstractEntityTest {

    // Shared state
    private SlimFoodPreferencesVO unitUnderTest;

    @Before
    public void setupSharedState() {

        // #0) Setup JAXB context.
        jaxb.add(SlimFoodPreferencesVO.class);

        unitUnderTest = new SlimFoodPreferencesVO(
                new FoodPreferenceVO("Vegetariska Räkor", null, 42),
                new FoodPreferenceVO("Barn", "Äter inte Barn", 42)
        );
    }

    @Test
    public void validateMarshallingToJSon() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully(
                "testdata/transport/convenience/food/slimFoodPreferenceVO.json");

        // Act
        final String result = marshalToJSon(unitUnderTest);
        // System.out.println("Got JSON:\n" + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void validateMarshallingToXML() {

        // Assemble
        final String expected = XmlTestUtils.readFully(
                "testdata/transport/convenience/food/slimFoodPreferenceVO.xml");

        // Act
        final String result = marshalToXML(unitUnderTest);
        // System.out.println("Got XML:\n" + result);

        // Assert
        validateIdenticalXml(expected, result);
    }
}
