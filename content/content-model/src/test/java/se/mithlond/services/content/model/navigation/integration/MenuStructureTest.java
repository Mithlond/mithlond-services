/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
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
package se.mithlond.services.content.model.navigation.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.localization.LocalizedTexts;
import se.mithlond.services.content.model.navigation.AbstractAuthorizedNavItem;
import se.mithlond.services.content.model.navigation.AbstractEntityTest;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MenuStructureTest extends AbstractEntityTest {

    // Shared state
    private StandardMenu rootMenu;
    private StandardMenu firstMenu;
    private MenuStructure menuStructure;

    private Address address;
    private Organisation organisation;

    @Before
    public void setupSharedState() {

        jaxb.add(MenuStructure.class);
        jaxb.add(LocalizedTexts.class);

        address = new Address("careOfLine", "departmentName", "street", "number",
                "city", "zipCode", "country", "description");
        organisation = new Organisation("name", "suffix", "phone", "bankAccountInfo",
                "postAccountInfo", address, "emailSuffix",
                TimeFormat.SWEDISH_TIMEZONE,
                TimeFormat.SWEDISH_LOCALE);

        rootMenu = StandardMenu.getBuilder()
                .withDomId("rootMenu")
                .withLocalizedText("rootMenuTexts", "sv", "Roooot")
                .build();

        menuStructure = new MenuStructure(rootMenu, organisation);

        firstMenu = StandardMenu.getBuilder()
                .withDomId("firstMenu")
                .withLocalizedText("firstMenuTexts", "sv", "Första Menyn")
                .withHref("/firstMenu")
                .withIconIdentifier("cog")
                .withTabIndex(1)
                .build();
        rootMenu.addChild(firstMenu);

        firstMenu.addChild(StandardMenuItem.getBuilder()
                .withAuthorizationPatterns("/mithlond/members")
                .withDomId("membersPage")
                .withHref("/members/list")
                .withIconIdentifier("man")
                .withLocalizedText("membersPageTexts", "sv", "Medlemssida")
                .build());

        firstMenu.addChild(new SeparatorMenuItem());

        firstMenu.addChild(StandardMenuItem.getBuilder()
                .withDomId("membersIdeaPage")
                .withHref("/mithlond/members/ideas")
                .withLocalizedText("membersIdeaPageTexts", "sv", "Idésida")
                .withIconIdentifier("lightbulb")
                .build());


        rootMenu.addChild(StandardMenuItem.getBuilder()
                .withAuthorizationPatterns("/mithlond/members,/forodrim/members")
                .withLocalizedText("calendarPageTexts", "sv", "Aktivitetskalender")
                .withEnabledStatus(true)
                .withHref("/calendar")
                .withDomId("plainItemPage3")
                .build());
    }

    @Test
    public void validateMarshallingToXML() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/menuItems.xml");

        // Act
        final String result = marshalToXML(menuStructure);
        // System.out.println("Got: " + result);

        // Assert
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateMarshallingToJSON() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/menuStructure.json");

        // Act
        final String result = marshalToJSon(menuStructure);
        System.out.println("Got: " + result);

        // Assert
        // Assert.assertEquals(expected.replaceAll("\\s+", ""), result.replaceAll("\\s+", ""));
        JSONAssert.assertEquals(expected, result, true);
    }


    @Test
    public void validateUnmarshalling() {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/menuItems.xml");

        // Act
        final MenuStructure resurrected = unmarshalFromXML(MenuStructure.class, data);

        // Assert
        Assert.assertNotNull(resurrected);
        final List<AbstractAuthorizedNavItem> rootMenu = resurrected.getRootMenu().getChildren();
        Assert.assertEquals(2, rootMenu.size());

        final StandardMenu first = (StandardMenu) rootMenu.get(0);
        Assert.assertEquals("firstMenu", first.getIdAttribute());

        final List<AbstractAuthorizedNavItem> firstChildren = first.getChildren();
        Assert.assertEquals(firstMenu.getChildren().size(), firstChildren.size());
        Assert.assertEquals("/mithlond/members/(\\p{javaLetterOrDigit}|_)*",
                firstChildren.get(0).getRequiredAuthorizationPatterns().first().toString());

        final StandardMenuItem second = (StandardMenuItem) rootMenu.get(1);
        Assert.assertEquals("/calendar", second.getHrefAttribute());
    }
}
