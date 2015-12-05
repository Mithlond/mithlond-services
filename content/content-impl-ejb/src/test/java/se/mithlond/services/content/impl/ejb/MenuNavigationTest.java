/*
 * #%L
 * Nazgul Project: mithlond-services-content-impl-ejb
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
package se.mithlond.services.content.impl.ejb;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbUtils;
import se.mithlond.services.content.api.UnknownOrganisationException;
import se.mithlond.services.content.api.transport.MenuStructure;
import se.mithlond.services.content.model.navigation.AuthorizedNavItem;
import se.mithlond.services.content.model.navigation.integration.SeparatorMenuItem;
import se.mithlond.services.content.model.navigation.integration.StandardMenu;
import se.mithlond.services.content.model.navigation.integration.StandardMenuItem;
import se.mithlond.services.shared.authorization.api.SemanticAuthorizationPathProducer;
import se.mithlond.services.shared.authorization.api.builder.AuthorizationPathBuilder;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;
import se.mithlond.services.shared.spi.algorithms.Deployment;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MenuNavigationTest {

    // Shared state
    private File menuRootDirectory;

    @Before
    public void setupSharedState() {

        // Find the relative path to the menuroot directory
        final URL resource = getClass().getClassLoader().getResource("testdata/storageroot");
        Assert.assertNotNull(resource);

        menuRootDirectory = new File(resource.getPath());
        Assert.assertTrue(menuRootDirectory.exists() && menuRootDirectory.isDirectory());
    }

    @After
    public void clearSystemProperties() throws Exception {

        // Clear the properties
        System.clearProperty(Deployment.DEPLOYMENT_NAME_KEY);
        System.clearProperty(Deployment.FILE_STORAGE_ROOTDIR_KEY);

        // Also clear the internal/static state of the Deployment singleton.
        final Field deploymentNameField = Deployment.class.getDeclaredField("deploymentName");
        final Field storageRootDirField = Deployment.class.getDeclaredField("storageRootDirectory");

        deploymentNameField.setAccessible(true);
        storageRootDirField.setAccessible(true);

        deploymentNameField.set(null, null);
        storageRootDirField.set(null, null);
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnMissingStorageRootProperty() {

        // Assemble
        final MenuNavigation unitUnderTest = new MenuNavigation();

        // Act & Assert
        unitUnderTest.setupEnvironmentStorageRootDir();
    }

    @Test(expected = IllegalStateException.class)
    public void validateExceptionOnMissingDeploymentProperty() {

        // Assemble
        System.setProperty(Deployment.FILE_STORAGE_ROOTDIR_KEY, menuRootDirectory.getAbsolutePath());
        final MenuNavigation unitUnderTest = new MenuNavigation();

        // Act & Assert
        unitUnderTest.setupEnvironmentStorageRootDir();
    }

    @Test(expected = UnknownOrganisationException.class)
    public void validateExceptionOnRetrievingMenuStructureForNonexistentOwner() {

        // Assemble
        System.setProperty(Deployment.FILE_STORAGE_ROOTDIR_KEY, menuRootDirectory.getAbsolutePath());
        System.setProperty(Deployment.DEPLOYMENT_NAME_KEY, "unittestdevelopment");

        final MenuNavigation unitUnderTest = new MenuNavigation();
        unitUnderTest.setupEnvironmentStorageRootDir();

        // Act & Assert
        unitUnderTest.getMenuStructure("NonExistent", new ArrayList<>());
    }

    @Test
    public void validateRetrievingUnauthorizedMenuStructure() {

        // Assemble
        System.setProperty(Deployment.FILE_STORAGE_ROOTDIR_KEY, menuRootDirectory.getAbsolutePath());
        System.setProperty(Deployment.DEPLOYMENT_NAME_KEY, "unittestdevelopment");

        final MenuNavigation unitUnderTest = new MenuNavigation();
        unitUnderTest.setupEnvironmentStorageRootDir();

        // Act
        final MenuStructure result = unitUnderTest.getMenuStructure("Foo", new ArrayList<>());
        // System.out.println("Got: " + marshal(result));

        // Assert
        Assert.assertNotNull(result);

        final List<AuthorizedNavItem> rootMenu = result.getRootMenu();
        Assert.assertEquals(2, rootMenu.size());

        Assert.assertEquals("firstMenuInDevelopment", rootMenu.get(0).getIdAttribute());

        final StandardMenuItem menuItem = (StandardMenuItem) rootMenu.get(1);
        Assert.assertFalse(menuItem.isEnabled());
        Assert.assertNull(menuItem.getHrefAttribute());
    }

    @Test
    public void validateRetrievingUnauthorizedMenuStructureFromStagingEnvironment() {

        // Assemble
        System.setProperty(Deployment.FILE_STORAGE_ROOTDIR_KEY, menuRootDirectory.getAbsolutePath());
        System.setProperty(Deployment.DEPLOYMENT_NAME_KEY, "unitteststaging");

        final MenuNavigation unitUnderTest = new MenuNavigation();
        unitUnderTest.setupEnvironmentStorageRootDir();

        // Act
        final MenuStructure result = unitUnderTest.getMenuStructure("Foo", new ArrayList<>());
        // System.out.println("Got: " + marshal(result));

        // Assert
        Assert.assertNotNull(result);

        final List<AuthorizedNavItem> rootMenu = result.getRootMenu();
        Assert.assertEquals(2, rootMenu.size());

        Assert.assertEquals("firstMenuInStaging", rootMenu.get(0).getIdAttribute());

        final StandardMenuItem menuItem = (StandardMenuItem) rootMenu.get(1);
        Assert.assertFalse(menuItem.isEnabled());
        Assert.assertNull(menuItem.getHrefAttribute());
    }

    @Test
    public void validateRetrievingAuthorizedMenuStructure() {

        // Assemble
        final SortedSet<SemanticAuthorizationPath> authorizationPaths
                = AuthorizationPathBuilder.parse("/forodrim/members/village_idiots");
        final List<SemanticAuthorizationPathProducer> authorizationPathProducerList = new ArrayList<>();
        authorizationPathProducerList.add(() -> authorizationPaths);

        System.setProperty(Deployment.FILE_STORAGE_ROOTDIR_KEY, menuRootDirectory.getAbsolutePath());
        System.setProperty(Deployment.DEPLOYMENT_NAME_KEY, "unittestdevelopment");

        final MenuNavigation unitUnderTest = new MenuNavigation();
        unitUnderTest.setupEnvironmentStorageRootDir();

        // Act
        final MenuStructure result = unitUnderTest.getMenuStructure("Foo", authorizationPathProducerList);
        // System.out.println("Got: " + marshal(result));

        // Assert
        Assert.assertNotNull(result);

        final List<AuthorizedNavItem> rootMenu = result.getRootMenu();
        Assert.assertEquals(2, rootMenu.size());

        /*
        <menuItem jpaId="0" version="0">
            <cssClasses>
                <cssClass>icon-fixed-width</cssClass>
            </cssClasses>
            <authPatterns>
                <pattern>/forodrim/members</pattern>   <---- Should match, and hence be authorized
                <pattern>/mithlond/members</pattern>
            </authPatterns>
            <iconIdentifier>calendar</iconIdentifier>
            <href>plainItemPage3</href>
        </menuItem>
         */
        final StandardMenuItem menuItem = (StandardMenuItem) rootMenu.get(1);
        Assert.assertTrue(menuItem.isEnabled());
        Assert.assertEquals("plainItemPage3", menuItem.getHrefAttribute());

        /*
        <subMenu domId="firstMenu" jpaId="0" version="0">
            <children>
                <menuItem jpaId="0" version="0">
                    <cssClasses>
                        <cssClass>icon-fixed-width</cssClass>
                    </cssClasses>
                    <authPatterns>
                        <pattern>/mithlond/members</pattern>  <---- Should not match, and hence no children acquired.
                    </authPatterns>
                    <iconIdentifier>cog</iconIdentifier>
                    <href>plainItemPage</href>
                </menuItem>
         */
        final StandardMenu firstMenu = (StandardMenu) rootMenu.get(0);
        Assert.assertTrue(firstMenu.isEnabled());

        final List<AuthorizedNavItem> children = firstMenu.getChildren();
        Assert.assertEquals(3, children.size());

        /*
            First menuItem should be:

            <menuItem enabled="false" jpaId="0" version="0">
                    <cssClasses>
                        <cssClass>icon-fixed-width</cssClass>
                        <cssClass>disabled</cssClass>
                    </cssClasses>
                    <iconIdentifier>cog</iconIdentifier>
                </menuItem>
         */
        final StandardMenuItem firstChild = (StandardMenuItem) children.get(0);
        Assert.assertFalse(firstChild.isEnabled());
        Assert.assertNull(firstChild.getHrefAttribute());
        Assert.assertEquals("cog", firstChild.getIconIdentifier());
        Assert.assertTrue(firstChild.getCssClasses().contains("disabled"));
        Assert.assertTrue(firstChild.getCssClasses().contains("icon-fixed-width"));

        Assert.assertTrue(children.get(1) instanceof SeparatorMenuItem);

        /*
        This should be the 3rd child:
        <menuItem jpaId="0" version="0">
                    <cssClasses>
                        <cssClass>icon-fixed-width</cssClass>
                    </cssClasses>
                    <iconIdentifier>lightbulb</iconIdentifier>
                    <href>plainItemPage2</href>
                </menuItem>
         */
        final StandardMenuItem thirdChild = (StandardMenuItem) children.get(2);
        Assert.assertTrue(thirdChild.isEnabled());
        Assert.assertEquals("plainItemPage2", thirdChild.getHrefAttribute());
        Assert.assertEquals("lightbulb", thirdChild.getIconIdentifier());
        Assert.assertTrue(thirdChild.getCssClasses().contains("icon-fixed-width"));
    }

    //
    // Private helpers
    //

    @SuppressWarnings("all")
    private String marshal(final MenuStructure aMenuStructure) {

        final StringWriter result;
        try {
            final JAXBContext context = JAXBContext.newInstance(MenuStructure.class);
            final JaxbNamespacePrefixResolver prefixMapper = new JaxbNamespacePrefixResolver();
            final Marshaller marshaller = JaxbUtils.getHumanReadableStandardMarshaller(context, prefixMapper, true);
            result = new StringWriter();

            marshaller.marshal(aMenuStructure, result);
        } catch (JAXBException e) {
            throw new IllegalStateException("Could not marshal a MenuStructure to XML", e);
        }

        // All done.
        return result.toString();
    }
}
