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
import se.mithlond.services.content.model.navigation.AbstractAuthorizedNavItem;
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
    private StandardMenu rootMenu;
    private StandardMenu firstMenu;
    private MenuStructure menuStructure;

    @Before
    public void setupSharedState() {

        realm = "FooBar";
        rootMenu = StandardMenu.getBuilder()
                .withDomId("rootMenu")
                .withLocalizedText("sv", "Roooot")
                .build();

        menuStructure = new MenuStructure(realm, rootMenu);
        jaxb.add(MenuStructure.class);

        firstMenu = StandardMenu.getBuilder()
                .withDomId("firstMenu")
                .withLocalizedText("sv", "Första Menyn")
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
                .withLocalizedText("sv", "Medlemssida")
                .build());

        firstMenu.addChild(new SeparatorMenuItem());

        firstMenu.addChild(StandardMenuItem.getBuilder()
                .withDomId("membersIdeaPage")
                .withHref("/mithlond/members/ideas")
                .withLocalizedText("sv", "Idésida")
                .withIconIdentifier("lightbulb")
                .build());



        rootMenu.addChild(StandardMenuItem.getBuilder()
                .withAuthorizationPatterns("/mithlond/members,/forodrim/members")
                .withLocalizedText("sv", "Aktivitetskalender")
                .withEnabledStatus(true)
                .withHref("/calendar")
                .withDomId("plainItemPage3")
                .build());
    }

    @Test
    public void validateMarshallingToXML() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/menuStructure.xml");

        // Act
        final String result = marshalToXML(menuStructure);
        // System.out.println("Got: " + result);

        // Assert
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateMarshallingToJSON() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/menuStructure.json");

        // Act
        final String result = marshalToJSon(menuStructure);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertEquals(expected.replaceAll("\\s+", ""), result.replaceAll("\\s+", ""));
    }


    @Test
    public void validateUnmarshalling() {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/menuStructure.xml");

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
