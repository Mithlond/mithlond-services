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
package se.mithlond.services.organisation.model.transport.address;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.address.WellKnownAddressType;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CategoriesAndAddressesTest extends AbstractPlainJaxbTest {

    // Shared state
    private List<CategorizedAddress> addresses;
    private List<Category> categories;
    private Organisation organisation;
    private Address organisationAddress;

    @Before
    public void setupSharedState() {

        organisationAddress = new Address("careOfLine", "departmentName", "street", "number",
                "city", "zipCode", "country", "description");
        organisation = new Organisation("name",
                "suffix",
                "phone",
                "bankAccountInfo",
                "postAccountInfo",
                organisationAddress,
                "emailSuffix",
                TimeFormat.SWEDISH_TIMEZONE.normalized(),
                TimeFormat.SWEDISH_LOCALE);

        addresses = new ArrayList<>();
        categories = new ArrayList<>();

        for (int i = 0; i < 10; i++) {

            for (WellKnownAddressType current : WellKnownAddressType.values()) {

                final Address currentAddress = new Address(
                        "careOfLine_" + i,
                        "departmentName_" + i,
                        "street_" + i,
                        "number_" + i,
                        "city_" + i,
                        "zipCode_" + i,
                        "country_" + i,
                        "description_" + i);

                addresses.add(new CategorizedAddress(
                        "shortDesc_" + i,
                        "fullDesc_" + i,
                        current,
                        organisation,
                        currentAddress));
            }
        }

        addresses.stream()
                .map(CategorizedAddress::getCategory)
                .filter(c -> !categories.contains(c))
                .forEach(c -> categories.add(c));
    }

    @Test
    public void validateMarshallingToXML() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/address/categoriesAndAddresses.xml");
        final CategorizedAddress[] addr = new CategorizedAddress[addresses.size()];
        final CategoriesAndAddresses unitUnderTest = new CategoriesAndAddresses(addresses.toArray(addr));

        // Act
        final String result = marshalToXML(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateMarshallingToJSon() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/address/categoriesAndAddresses.json");
        final CategorizedAddress[] addr = new CategorizedAddress[addresses.size()];
        final CategoriesAndAddresses unitUnderTest = new CategoriesAndAddresses(addresses.toArray(addr));

        // Act
        final String result = marshalToJSon(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertEquals(expected.replaceAll("\\s+", ""), result.replaceAll("\\s+", ""));
    }

    @Test
    public void valiateUnmarshallingFromXML() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/transport/address/categoriesAndAddresses.xml");
        jaxb.add(CategoriesAndAddresses.class);

        // Act
        final CategoriesAndAddresses resurrected = unmarshalFromXML(CategoriesAndAddresses.class, data);

        // Assert
        Assert.assertNotNull(resurrected);

        Assert.assertEquals(1, resurrected.getOrganisations().size());
        Assert.assertEquals(organisation, resurrected.getOrganisations().get(0));

        Assert.assertEquals(categories.size(), resurrected.getCategories().size());

        final List<Category> originalList = categories.stream().collect(Collectors.toList());
        final List<Category> resurrectedList = resurrected.getCategories().stream().collect(Collectors.toList());
        Collections.sort(originalList);
        Collections.sort(resurrectedList);

        for(int i = 0; i < originalList.size(); i++) {
            Assert.assertEquals(originalList.get(i), resurrectedList.get(i));
        }
    }
}
