package se.mithlond.services.content.model.navigation.integration;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.content.model.localization.LocalizedTexts;
import se.mithlond.services.content.model.navigation.AbstractAuthorizedNavItem;
import se.mithlond.services.content.model.navigation.AbstractEntityTest;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;

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
                "postAccountInfo", address, "emailSuffix");

        rootMenu = StandardMenu.getBuilder()
                .withDomId("rootMenu")
                .withLocalizedText("sv", "Roooot")
                .build();

        menuStructure = new MenuStructure(rootMenu, organisation);

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
        final String expected = XmlTestUtils.readFully("testdata/menuItems.xml");

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
