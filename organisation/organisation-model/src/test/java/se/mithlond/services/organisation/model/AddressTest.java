/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.mithlond.services.organisation.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.helpers.Addresses;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AddressTest extends AbstractEntityTest {

    // Shared data
    private List<Address> addresses;

    @Before
    public void setupSharedState() {

        addresses = new ArrayList<>();

        for(int i = 0; i < 10; i++) {
            addresses.add(new Address(
                    "careOfLine_" + i,
                    "departmentName_" + i,
                    "street_" + i,
                    "number_" + i,
                    "city_" + i,
                    "zipCode_" + i,
                    "country_" + i,
                    "description_" + i));
        }
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/singleAddress.xml");

        // Act
        final String result = marshal(new Addresses(addresses.get(0)));

        // Assert
        // System.out.println("Got: " + result);
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final Addresses expected = new Addresses();
        expected.getAddresses().addAll(this.addresses);

        final String data = XmlTestUtils.readFully("testdata/severalAddresses.xml");

        // Act
        jaxb.add(Addresses.class);
        final Addresses result = unmarshal(Addresses.class, data);

        // Assert
        final List<Address> resurrected = result.getAddresses();
        Assert.assertEquals(10, resurrected.size());
        Assert.assertEquals(resurrected.get(2), addresses.get(2));
    }

    @Test
    public void validateComparisonAndEquality() throws Exception {

        // Assemble
        final Address address1 = new Address("col", "dept", "street", "number", "city", "zipCode", "Sverige", "desc");
        final Address address2 = new Address("col", "dept", "street", "number", "city", "zipCode", "Sverige", "desc");
        final Address address3 = new Address(null, "dept", "street", "number", "city", "zipCode", "Sverige", "desc");

        // Act & Assert
        Assert.assertEquals(address1, address2);
        Assert.assertNotSame(address1, address2);
        Assert.assertEquals(0, address1.compareTo(address2));

        Assert.assertNotEquals(address1, address3);
    }
}
