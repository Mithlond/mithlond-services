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

import org.junit.Assert;
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
public class SeparatorMenuItemTest extends AbstractEntityTest {

    // Shared state
    private MenuItems menuItems;
    private SeparatorMenuItem unitUnderTest1;
    private SeparatorMenuItem unitUnderTest2;

    @Before
    public void setupSharedState() {

        menuItems = new MenuItems();
        jaxb.add(MenuItems.class);

        unitUnderTest1 = new SeparatorMenuItem();
        unitUnderTest1.setEnabled(false);

        unitUnderTest2 = new SeparatorMenuItem();
        unitUnderTest2.addCssClass("blah");
    }

    @Test
    public void validateMarshallingToXML() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/separatorMenuItems.xml");

        menuItems.getRootMenu().addChild(unitUnderTest1);
        menuItems.getRootMenu().addChild(unitUnderTest2);

        // Act
        final String result = marshalToXML(menuItems);
        // System.out.println("Got: " + result);

        // Assert
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateUnmarshallingFromXML() {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/separatorMenuItems.xml");

        // Act
        final MenuItems resurrected = unmarshalFromXML(MenuItems.class, data);

        // Assert
        Assert.assertNotNull(resurrected);

        final StandardMenu rootMenu = resurrected.getRootMenu();
        Assert.assertNotNull(rootMenu);
        Assert.assertEquals(2, rootMenu.getChildren().size());

        final SeparatorMenuItem first = (SeparatorMenuItem) rootMenu.getChildren().get(0);
        Assert.assertNull(first.getCssClasses());

        final SeparatorMenuItem second = (SeparatorMenuItem) rootMenu.getChildren().get(1);
        Assert.assertEquals(1, second.getCssClasses().size());
        Assert.assertEquals("blah", second.getCssClasses().get(0));
    }
}
