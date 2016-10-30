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
package se.mithlond.services.organisation.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.organisation.model.transport.Organisations;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class OrganisationTest extends AbstractEntityTest {

    // Shared state
    private Organisation unitUnderTest;
    private Address address;

    @Before
    public void setupSharedState() {

        address = new Address("careOfLine", "departmentName", "street", "number",
                "city", "zipCode", "country", "description");
        unitUnderTest = new Organisation("name", "suffix", "phone", "bankAccountInfo",
                "postAccountInfo", address, "emailSuffix",
                TimeFormat.SWEDISH_TIMEZONE.normalized(),
                TimeFormat.SWEDISH_LOCALE,
                WellKnownCurrency.SEK);
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/anOrganisation.xml");

        // Act
        final String result = marshalToXML(new Organisations(unitUnderTest));

        // Assert
        // System.out.println("Got: " + result);
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/anOrganisation.xml");
        jaxb.add(Organisations.class);

        // Act
        final Organisations result = unmarshalFromXML(Organisations.class, data);

        // Assert
        Assert.assertEquals(1, result.getOrganisations().size());

        final Organisation resurrected = result.getOrganisations().iterator().next();
        Assert.assertNotSame(unitUnderTest, resurrected);
        Assert.assertEquals(unitUnderTest, resurrected);
    }
}
