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
package se.mithlond.services.organisation.model.address;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.organisation.model.helpers.WellKnownAddressTypes;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class WellKnownAddressTypeTest extends AbstractEntityTest {

    // Shared state
    private SortedMap<String, WellKnownAddressType> map;

    @Before
    public void setupSharedState() {
        jaxb.mapXmlNamespacePrefix(Patterns.NAMESPACE, "organisation");

        map = new TreeMap<>();
        for (WellKnownAddressType current : WellKnownAddressType.values()) {
            map.put(current.name(), current);
        }
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/addressTypes.xml");
        final WellKnownAddressTypes wrapper = new WellKnownAddressTypes();
        wrapper.add(WellKnownAddressType.values());

        // Act
        final String result = marshalToXML(wrapper);

        // Assert
        // System.out.println("Got: " + result);
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/addressTypes.xml");
        jaxb.add(WellKnownAddressTypes.class);

        // Act
        final WellKnownAddressTypes resurrected = unmarshalFromXML(WellKnownAddressTypes.class, data);

        // Assert
        Assert.assertNotNull(resurrected);

        final SortedMap<String, WellKnownAddressType> resurrectedMap = new TreeMap<>();
        for (WellKnownAddressType current : resurrected.getAddressTypes()) {
            resurrectedMap.put(current.name(), current);
        }

        Assert.assertEquals(map.size(), resurrectedMap.size());
        for (Map.Entry<String, WellKnownAddressType> current : resurrectedMap.entrySet()) {

            final WellKnownAddressType currentValue = current.getValue();
            final WellKnownAddressType comparison = map.get(current.getKey());

            Assert.assertEquals(comparison, currentValue);
            Assert.assertEquals(comparison.isMailDeliveryAddress(), currentValue.isMailDeliveryAddress());

            final Category currentCategory = currentValue.getCategory();
            final Category expectedCategory = comparison.getCategory();
            Assert.assertEquals(expectedCategory, currentCategory);
        }
    }
}
