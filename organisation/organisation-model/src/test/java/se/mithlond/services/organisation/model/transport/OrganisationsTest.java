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
package se.mithlond.services.organisation.model.transport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class OrganisationsTest extends AbstractPlainJaxbTest {

    private Organisations unitUnderTest;
    private List<Organisation> organisations;
    private List<OrganisationVO> organisationVOs;

    @Before
    public void setupSharedState() {
        unitUnderTest = new Organisations();
        organisations = new ArrayList<>();
        organisationVOs = new ArrayList<>();

        for (int i = 0; i < 4; i++) {
            final Address address = new Address(
                    "careOfLine_" + i,
                    "departmentName_" + i,
                    "street_" + i,
                    "number_" + i,
                    "city_" + i,
                    "zipCode_" + i,
                    "country_" + i,
                    "description_" + i);

            organisations.add(new Organisation(
                    "name_" + i,
                    "suffix_" + i,
                    "phone_" + i,
                    "bankAccountInfo_" + i,
                    "postAccountInfo_" + i,
                    address,
                    "emailSuffix_" + i,
                    TimeFormat.SWEDISH_TIMEZONE.normalized(),
                    TimeFormat.SWEDISH_LOCALE));

            organisationVOs.add(new OrganisationVO((long) (25 + i), "name_" + i, "suffix_" + i));
        }
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        unitUnderTest.getOrganisations().addAll(organisations);
        unitUnderTest.getOrganisationVOs().addAll(organisationVOs);
        final String expected = XmlTestUtils.readFully("testdata/transport/compoundOrganisations.xml");

        // Act
        final String result = marshalToXML(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateMarshallingOnlyVOsToXML() throws Exception {

        // Assemble
        unitUnderTest.getOrganisationVOs().addAll(organisationVOs);
        final String expected = XmlTestUtils.readFully("testdata/transport/organisationVOs.xml");

        // Act
        final String result = marshalToXML(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateMarshallingOnlyVOsToJSON() throws Exception {

        // Assemble
        unitUnderTest.getOrganisationVOs().addAll(organisationVOs);
        final String expected = XmlTestUtils.readFully("testdata/transport/organisationVOs.json");

        // Act
        final String result = marshalToJSon(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void validateUnmarshallingOnlyVOsFromXML() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/transport/unorderedOrganisationVOs.xml");
        jaxb.add(Organisations.class);

        // Act
        final Organisations organisations = unmarshalFromXML(Organisations.class, data);

        // Assert
        Assert.assertNotNull(organisations);
        Assert.assertEquals(this.organisationVOs.size(), organisations.getOrganisationVOs().size());

        final List<OrganisationVO> frozen = organisations.getOrganisationVOs().stream().collect(Collectors.toList());
        for (int i = 0; i < this.organisationVOs.size(); i++) {
            Assert.assertEquals(frozen.get(i), this.organisationVOs.get(i));
        }
    }

    @Test
    public void validateUnmarshallingOnlyVOsFromJSON() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/transport/organisationVOs.json");
        jaxb.add(Organisations.class);

        // Act
        final Organisations organisations = unmarshalFromJSON(Organisations.class, data);

        // Assert
        Assert.assertNotNull(organisations);
        Assert.assertEquals(this.organisationVOs.size(), organisations.getOrganisationVOs().size());

        final List<OrganisationVO> frozen = organisations.getOrganisationVOs().stream().collect(Collectors.toList());
        for (int i = 0; i < this.organisationVOs.size(); i++) {
            Assert.assertEquals(frozen.get(i), this.organisationVOs.get(i));
        }
    }
}
