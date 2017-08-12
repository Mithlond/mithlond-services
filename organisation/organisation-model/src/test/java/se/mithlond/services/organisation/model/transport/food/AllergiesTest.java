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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.organisation.model.food.Allergy;
import se.mithlond.services.organisation.model.food.AllergySeverity;
import se.mithlond.services.organisation.model.food.FoodPreference;
import se.mithlond.services.organisation.model.localization.LocaleDefinition;
import se.mithlond.services.organisation.model.localization.LocalizedTexts;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.transport.OrganisationVO;
import se.mithlond.services.organisation.model.transport.membership.MembershipVO;
import se.mithlond.services.organisation.model.transport.user.UserVO;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.JpaIdMutator;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AllergiesTest extends AbstractEntityTest {

    // Shared state
    private FoodsAndCategories foodsAndCategories;
    private List<UserVO> userVOs;
    private List<User> users;
    private List<Membership> memberships;
    private List<MembershipVO> membershipVOs;
    private List<AllergyVO> allergyVoList;
    private List<Allergy> allergyList;
    private SortedSet<FoodPreferenceVO> foodPrefsVOs;
    private Allergies shallowTransportWrapper;
    private Allergies detailedTransportWrapper;
    private AllergySeverity minorSeverity, majorSeverity;
    private LocalizedTexts minorSeverityShortDesc, minorSeverityFullDesc, majorSeverityShortDesc, majorSeverityFullDesc;

    private Organisation mifflond;

    @Before
    public void setupSharedState() {

        // #0) Create some Foods, Categories. And setup JAXB context.
        //
        this.foodsAndCategories = new FoodsAndCategories();
        jaxb.add(Foods.class, Allergies.class);
        jaxb.mapXmlNamespacePrefix(OrganisationPatterns.NAMESPACE, "organisation");


        // #1) Create the organisation
        //
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
        AbstractEntityTest.setJpaIDFor(mifflond, 1L);
        final OrganisationVO mifflondVO = new OrganisationVO(mifflond);

        userVOs = new ArrayList<>();
        users = new ArrayList<>();

        // Create some users
        final User haxx = new User(
                "Mr",
                "Häxxmästaren",
                LocalDate.of(1968, Month.SEPTEMBER, 17),
                (short) 1235,
                new Address(null,
                        null,
                        "Testgatan",
                        "45 E",
                        "Grååååbo",
                        "234 54",
                        "Sverige",
                        "Hemma hos Mr Häxx"),
                null,
                null,
                "dasToken");
        JpaIdMutator.setId(haxx, 42);

        final User erion = new User(
                "Das",
                "Erion",
                LocalDate.of(1922, Month.FEBRUARY, 5),
                (short) 2345,
                new Address(null,
                        null,
                        "Yttertestgatan",
                        "25",
                        "Göteborg",
                        "411 11",
                        "Sverige",
                        "Hemma hos Das Erion"),
                null,
                null,
                "dasErionToken");
        JpaIdMutator.setId(erion, 43);

        users.add(haxx);
        users.add(erion);

        users.forEach(u -> userVOs.add(new UserVO(u)));

        // Add some memberships to the users
        memberships = new ArrayList<>();
        membershipVOs = new ArrayList<>();

        final Membership haxxMembership = new Membership("häxx", "Das Filur", "haxxx", true, haxx, mifflond);
        final Membership erionMembership = new Membership("erion", "Le Erion", "erionnn", true, erion, mifflond);

        memberships.add(haxxMembership);
        memberships.add(erionMembership);
        membershipVOs.add(new MembershipVO(haxxMembership));
        membershipVOs.add(new MembershipVO(erionMembership));

        // Create AllergySeverity descriptions.
        // Use a Swedish locale only.
        final LocaleDefinition swLocale = new LocaleDefinition(TimeFormat.SWEDISH_LOCALE);

        this.minorSeverityShortDesc = new LocalizedTexts("minorAllergyShortDesc",
                swLocale, "Default", "Obehag eller Preferens");

        this.minorSeverityFullDesc = new LocalizedTexts("minorAllergyFullDesc",
                swLocale, "Default", "Födoämnet kan förekomma i mat och du kan själv unvika att äta det. "
                + "Exempel: Morötter på din tallrik.");

        this.majorSeverityShortDesc = new LocalizedTexts("majorAllergyShortDesc",
                swLocale, "Default", "Får inte inmundigas");

        this.majorSeverityFullDesc = new LocalizedTexts("majorAllergyFullDesc",
                swLocale, "Default", "Födoämnet får inte "
                + "förekomma i din mat. Du kan behöva medicinering för att hantera effekterna av substansen.");

        // Create the corresponding AllergySeverities.
        minorSeverity = new AllergySeverity(2, minorSeverityShortDesc, minorSeverityFullDesc);
        majorSeverity = new AllergySeverity(3, majorSeverityShortDesc, majorSeverityFullDesc);

        // Add some Allergies and AllergyVOs
        allergyVoList = new ArrayList<>();
        allergyList = new ArrayList<>();

        allergyList.add(new Allergy(foodsAndCategories.beetroot, haxx, minorSeverity, "Minor Note"));
        allergyList.add(new Allergy(foodsAndCategories.carrot, haxx, majorSeverity, null));
        allergyList.add(new Allergy(foodsAndCategories.cauliflower, erion, minorSeverity, null));

        allergyList.forEach(al -> allergyVoList.add(new AllergyVO(al, TimeFormat.SWEDISH_LOCALE)));

        // Add some FoodPreferences
        foodPrefsVOs = new TreeSet<>();

        Stream.of(foodsAndCategories.meatsCategory, foodsAndCategories.milkCategory)
                .map(c -> new FoodPreference(c, erion))
                .map(FoodPreferenceVO::new)
                .forEach(c -> foodPrefsVOs.add(c));

        shallowTransportWrapper = new Allergies(TimeFormat.SWEDISH_LOCALE, userVOs, allergyVoList, foodPrefsVOs);
        detailedTransportWrapper = new Allergies(TimeFormat.SWEDISH_LOCALE,
                allergyList.toArray(new Allergy[allergyList.size()]));
    }

    @Test
    public void validateMarshallingDetailedWrapperToXML() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/food/detailedAllergies.xml");

        // Act
        final String result = marshalToXML(detailedTransportWrapper);
        // System.out.println("Got: " + result);

        // Assert
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateMarshallingToXML() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/food/allergies.xml");

        // Act
        final String result = marshalToXML(shallowTransportWrapper);
        // System.out.println("Got: " + result);

        // Assert
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateMarshallingToDetailedJSON() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/food/detailedAllergies.json");

        // Act
        final String result = marshalToJSon(detailedTransportWrapper);
        System.out.println("Got: " + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void validateMarshallingToJSON() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/food/allergies.json");

        // Act
        final String result = marshalToJSon(shallowTransportWrapper);
        // System.out.println("Got: " + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void validateUnmarshallingFromXML() {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/transport/food/allergies.xml");

        // Act
        final Allergies resurrected = unmarshalFromXML(Allergies.class, data);
        // System.out.println("Got: " + resurrected);

        // Assert
        Assert.assertNotNull(resurrected);
        Assert.assertEquals(shallowTransportWrapper.getUsers().size(), resurrected.getUsers().size());
        Assert.assertEquals(shallowTransportWrapper.getAllergyList().size(), resurrected.getAllergyList().size());
    }

    @Test
    public void validateUnmarshallingFromJSON() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/transport/food/allergies.json");

        // Act
        final Allergies resurrected = unmarshalFromJSON(Allergies.class, data);
        // System.out.println("Got: " + resurrected);

        // Assert
        Assert.assertNotNull(resurrected);
        Assert.assertEquals(this.shallowTransportWrapper.getUsers().size(), resurrected.getUsers().size());
        Assert.assertEquals(this.shallowTransportWrapper.getAllergyList().size(), resurrected.getAllergyList().size());
    }
}
