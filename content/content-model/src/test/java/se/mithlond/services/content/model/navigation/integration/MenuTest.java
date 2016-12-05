/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
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
package se.mithlond.services.content.model.navigation.integration;

import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.content.model.navigation.AbstractEntityTest;
import se.mithlond.services.content.model.navigation.integration.helpers.MenuItems;
import se.mithlond.services.organisation.model.localization.LocaleDefinition;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MenuTest extends AbstractEntityTest {

    // Shared state
    private MenuItems menuItems;

    @Before
    public void setupSharedState() {
        menuItems = new MenuItems();
    }

    @Test
    public void validateMarshallingToXML() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/menu.xml");
        final StandardMenu firstMenu = StandardMenu.getBuilder()
                .withDomId("firstMenu")
                .withLocalizedText("firstMenuTexts", LocaleDefinition.SWEDISH_LOCALE, "Medlemsmeny")
                .build();

        firstMenu.addChild(StandardMenuItem.getBuilder()
                .withAuthorizationPatterns("/mithlond/members")
                .withIconIdentifier("cog")
                .withHref("plainItemPage")
                .withLocalizedText("plainItemPageTexts", LocaleDefinition.SWEDISH_LOCALE, "Sök medlemmar")
                .build());
        firstMenu.addChild(new SeparatorMenuItem());
        firstMenu.addChild(new StandardMenuItem(null,
                null,
                null,
                null,
                "/mithlond/members",
                true,
                "lightbulb",
                "plainItemPage2",
                LocaleDefinition.SWEDISH_LOCALE,
                "Redigera medlemmar"));

        menuItems.getRootMenu().addChild(firstMenu);
        menuItems.getRootMenu().addChild(StandardMenuItem.getBuilder()
                .withAuthorizationPatterns("/mithlond/members,/forodrim/members")
                .withIconIdentifier("calendar")
                .withHref("plainItemPage3")
                .withLocalizedText("calendarPageTexts", LocaleDefinition.SWEDISH_LOCALE, "Kalenderuppgifter")
                .build());

        // Act
        final String result = marshalToXML(menuItems);
        // System.out.println("Got: " + result);

        // Assert
        validateIdenticalContent(expected, result);
    }
}
