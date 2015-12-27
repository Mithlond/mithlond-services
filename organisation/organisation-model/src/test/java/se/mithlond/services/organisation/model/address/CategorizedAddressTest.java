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
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.helpers.CategorizedAddresses;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CategorizedAddressTest extends AbstractEntityTest {

    // Shared state
    private CategorizedAddress[] addressArray;
    private List<CategorizedAddress> addresses;
    private Organisation organisation;
    private Address organisationAddress;

    @Before
    public void setupSharedState() {

        organisationAddress = new Address("careOfLine", "departmentName", "street", "number",
                "city", "zipCode", "country", "description");
        organisation = new Organisation("name", "suffix", "phone", "bankAccountInfo",
                "postAccountInfo", organisationAddress, "emailSuffix");

        addresses = new ArrayList<>();

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

        addressArray = addresses.toArray(new CategorizedAddress[addresses.size()]);
    }

    @Test
    public void validateMarshalling() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/categorizedAddresses.xml");
        final CategorizedAddresses toMarshal = new CategorizedAddresses(addressArray);

        // Act
        final String result = marshalToXML(toMarshal);

        // Assert
        // System.out.println("Got: " + result);
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateUnmarshalling() {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/categorizedAddresses.xml");
        jaxb.add(CategorizedAddresses.class);

        // Act
        final CategorizedAddresses result = unmarshalFromXML(CategorizedAddresses.class, data);

        // Assert
        Assert.assertNotNull(result);

        final List<CategorizedAddress> resurrected = result.getCategorizedAddresses();
        Assert.assertEquals(addresses.size(), resurrected.size());

        for (int i = 0; i < addresses.size(); i++) {

            final CategorizedAddress expected = addresses.get(i);
            final CategorizedAddress actual = resurrected.get(i);

            Assert.assertNotSame(expected, actual);
            Assert.assertEquals(expected, actual);
        }
    }
}
