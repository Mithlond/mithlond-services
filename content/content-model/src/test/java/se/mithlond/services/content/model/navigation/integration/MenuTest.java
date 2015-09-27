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
import se.mithlond.services.content.model.navigation.AuthorizedNavItem;
import se.mithlond.services.content.model.navigation.integration.helpers.MenuItems;

import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MenuTest extends AbstractEntityTest {

    // Shared state
    private MenuItems menuItems;

    @Before
    public void setupSharedState() {
        menuItems = new MenuItems();
        jaxb.add(MenuItems.class);
    }

    @Test
    public void validateMarshalling() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/menuItems.xml");
        final StandardMenu firstMenu = new StandardMenu(null, "firstMenu", null, null, null, true, null, null);
        final List<AuthorizedNavItem> children = firstMenu.getChildren();
        children.add(new StandardMenuItem(null, null, null, null,
                "/mithlond/members", true, "cog", "plainItemPage"));
        children.add(new SeparatorMenuItem());
        children.add(new StandardMenuItem(null, null, null, null,
                "/mithlond/members", true, "lightbulb", "plainItemPage2"));

        menuItems.getRootMenu().add(firstMenu);
        menuItems.getRootMenu().add(new StandardMenuItem(null, null, null, null,
                "/mithlond/members,/forodrim/members", true, "calendar", "plainItemPage3"));

        // Act
        final String result = marshal(menuItems);
        System.out.println("Got: " + result);

        // Assert
        validateIdenticalContent(expected, result);
    }
}
