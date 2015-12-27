/*
 * #%L
 * Nazgul Project: mithlond-services-content-api
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
package se.mithlond.services.content.api.navigation.transport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.content.api.AbstractEntityTest;
import se.mithlond.services.content.api.transport.MenuStructure;
import se.mithlond.services.content.model.navigation.AuthorizedNavItem;
import se.mithlond.services.content.model.navigation.integration.SeparatorMenuItem;
import se.mithlond.services.content.model.navigation.integration.StandardMenu;
import se.mithlond.services.content.model.navigation.integration.StandardMenuItem;

import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MenuStructureTest extends AbstractEntityTest {

    // Shared state
    private String realm;
    private StandardMenu firstMenu;
    private MenuStructure menuStructure;

    @Before
    public void setupSharedState() {
        realm = "FooBar";
        menuStructure = new MenuStructure(realm);
        jaxb.add(MenuStructure.class);

        firstMenu = new StandardMenu(null, "firstMenu", null, null, null, true, null, null);
        final List<AuthorizedNavItem> children = firstMenu.getChildren();
        children.add(new StandardMenuItem(null, null, null, null,
                "/mithlond/members", true, "cog", "plainItemPage"));
        children.add(new SeparatorMenuItem());
        children.add(new StandardMenuItem(null, null, null, null,
                "/mithlond/members", true, "lightbulb", "plainItemPage2"));

        menuStructure.add(firstMenu, new StandardMenuItem(null, null, null, null,
                "/mithlond/members,/forodrim/members", true, "calendar", "plainItemPage3"));
    }

    @Test
    public void validateMarshalling() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/menuStructure.xml");

        // Act
        final String result = marshalToXML(menuStructure);
        // System.out.println("Got: " + result);

        // Assert
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateUnmarshalling() {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/menuStructure.xml");

        // Act
        final MenuStructure resurrected = unmarshalFromXML(MenuStructure.class, data);

        // Assert
        Assert.assertNotNull(resurrected);
        final List<AuthorizedNavItem> rootMenu = resurrected.getRootMenu();
        Assert.assertEquals(2, rootMenu.size());

        final StandardMenu first = (StandardMenu) rootMenu.get(0);
        Assert.assertEquals("firstMenu", first.getIdAttribute());

        final List<AuthorizedNavItem> firstChildren = first.getChildren();
        Assert.assertEquals(firstMenu.getChildren().size(), firstChildren.size());
        Assert.assertEquals("/mithlond/members/(\\p{javaLetterOrDigit}|_)*",
                firstChildren.get(0).getRequiredAuthorizationPatterns().first().toString());

        final StandardMenuItem second = (StandardMenuItem) rootMenu.get(1);
        Assert.assertEquals("calendar", second.getIconIdentifier());
    }
}
