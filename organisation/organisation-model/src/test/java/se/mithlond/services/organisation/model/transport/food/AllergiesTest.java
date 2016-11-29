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
package se.mithlond.services.organisation.model.transport.food;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.organisation.model.localization.LocaleDefinition;
import se.mithlond.services.organisation.model.transport.user.UserVO;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AllergiesTest extends AbstractPlainJaxbTest {

    // Shared state
    private List<UserVO> users;
    private List<AllergyVO> allergyList;
    private Allergies allergies;

    private Organisation mifflond;

    @Before
    public void setupSharedState() {

        // Setup the JAXB context
        jaxb.add(Allergies.class);
        jaxb.add(LocaleDefinition.class);

        // #1) Create the organisation
        Address address = new Address(
                "careOfLine",
                "departmentName",
                "street",
                "number",
                "city",
                "zipCode",
                "country",
                "description");
        mifflond = new Organisation("mifflond",
                "suffix",
                "phone",
                "bankAccountInfo",
                "postAccountInfo",
                address,
                "emailSuffix",
                TimeFormat.SWEDISH_TIMEZONE.normalized(),
                TimeFormat.SWEDISH_LOCALE,
                WellKnownCurrency.SEK);

        users = new ArrayList<>();

        for (int i = 0; i < 5; i++) {

            final LocalDate birthDay = LocalDate.of(1971 + i, Month.FEBRUARY, 1 + i);
            users.add(new UserVO((long) i + 1, "firstName_" + i, "lastName_" + i, birthDay));
        }

        allergyList = new ArrayList<>();
        for (int i = 2; i < 7; i++) {
            allergyList.add(new AllergyVO(
                    (long) i + 4,
                    "allergy_description_" + i,
                    "severity_" + i,
                    "foodName_" + i,
                    (long) (i - 2),
                    (long) i));
        }

        allergies = new Allergies(users, allergyList);
    }

    @Test
    public void validateMarshallingToXML() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/food/allergies.xml");

        // Act
        final String result = marshalToXML(allergies);
        // System.out.println("Got: " + result);

        // Assert
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateMarshallingToJSON() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/food/allergies.json");

        // Act
        final String result = marshalToJSon(allergies);
        // System.out.println("Got: " + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }
}
