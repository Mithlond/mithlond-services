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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.content.api.NavigationService;
import se.mithlond.services.content.api.UnknownOrganisationException;
import se.mithlond.services.content.api.transport.MenuStructure;
import se.mithlond.services.content.model.navigation.AuthorizedNavItem;
import se.mithlond.services.content.model.navigation.integration.SeparatorMenuItem;
import se.mithlond.services.content.model.navigation.integration.StandardMenu;
import se.mithlond.services.content.model.navigation.integration.StandardMenuItem;
import se.mithlond.services.shared.authorization.api.Authorizer;
import se.mithlond.services.shared.authorization.api.SemanticAuthorizationPathProducer;
import se.mithlond.services.shared.authorization.api.SimpleAuthorizer;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;
import se.mithlond.services.shared.spi.algorithms.Deployment;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Standard stateless NavigationService POJO implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Stateless
public class MenuNavigation implements NavigationService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(MenuNavigation.class);

    /**
     * The file name of the raw Menu structure.
     */
    public static final String MENU_STRUCTURE_PATH = "navigation/menu_structure.xml";

    // Internal state
    private File environmentStorageRootDir;

    /**
     * Initialization method to configure the environmentStorageRootDir.
     */
    @PostConstruct
    public void setupEnvironmentStorageRootDir() {

        final File navRoot = new File(Deployment.getStorageRootDirectory(), Deployment.getDeploymentName());
        if (navRoot.exists() && navRoot.isDirectory()) {

            // All is well
            environmentStorageRootDir = navRoot;

            if (log.isInfoEnabled()) {
                log.info("MenuNavigation configured to use environmentStorageRootDir ["
                        + environmentStorageRootDir.getAbsolutePath() + "]");
            }
        } else {
            throw new IllegalStateException("NavigationStorageRootDir [" + navRoot.getAbsolutePath() + "] must be an " +
                    "existing directory. Please create it and restart the application.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MenuStructure getMenuStructure(final String menuOwner,
                                          final List<SemanticAuthorizationPathProducer> callersAuthPaths) {

        // Check sanity
        if(menuOwner == null || menuOwner.isEmpty()) {
            throw new UnknownOrganisationException(menuOwner);
        }

        final List<SemanticAuthorizationPathProducer> effectivePathProducers = callersAuthPaths == null
                ? new ArrayList<>()
                : callersAuthPaths;

        // Read the raw MenuStructure superstructure from disk.
        final MenuStructure rawMenuStructure;
        try {
            rawMenuStructure = readRawMenuStructure(menuOwner);
        } catch (IllegalStateException e) {
            throw new UnknownOrganisationException(menuOwner, e);
        }

        // Compile all the AuthenticationPaths of the supplied Memberships.
        final SortedSet<SemanticAuthorizationPath> paths = new TreeSet<>();
        for (SemanticAuthorizationPathProducer current : effectivePathProducers) {
            paths.addAll(current.getPaths());
        }

        // Populate the return menu structure
        final MenuStructure toReturn = new MenuStructure(menuOwner);
        populate(toReturn.getRootMenu(), rawMenuStructure.getRootMenu(), paths);

        // All done.
        return toReturn;
    }

    //
    // Private helpers
    //

    private void populate(final List<AuthorizedNavItem> toPopulate,
                          final List<AuthorizedNavItem> sourceItems,
                          final SortedSet<SemanticAuthorizationPath> authorizationPaths) {

        final Authorizer authorizer = SimpleAuthorizer.getInstance();

        for (AuthorizedNavItem current : sourceItems) {

            // Does the supplied SemanticAuthorizationPaths imply that the caller
            // is authorized to view the current AuthorizedNavItem?
            final boolean isAuthorized = authorizer.isAuthorized(current.getRequiredAuthorizationPatterns(),
                    authorizationPaths);

            // Uhm ... polymorphic impementation, anyone?
            if (current instanceof StandardMenu) {
                toPopulate.add(complete((StandardMenu) current, isAuthorized, authorizationPaths));
            } else if (current instanceof StandardMenuItem) {
                toPopulate.add(complete((StandardMenuItem) current, isAuthorized));
            } else if (current instanceof SeparatorMenuItem) {
                toPopulate.add(current);
            } else {
                throw new IllegalArgumentException("Cannot handle AuthorizedNavItem of type ["
                        + current.getClass().getSimpleName() + "]");
            }
        }
    }

    private StandardMenu complete(final StandardMenu rawMenu,
                                  final boolean isAuthorized,
                                  final SortedSet<SemanticAuthorizationPath> authorizationPaths) {

        // #1: Create the resulting StandardMenu
        final StandardMenu toReturn = new StandardMenu(
                rawMenu.getRoleAttribute(),
                rawMenu.getIdAttribute(),
                rawMenu.getTabIndexAttribute(),
                synthesizeCssClasses(rawMenu.getCssClasses(), isAuthorized),
                null,
                isAuthorized && rawMenu.isEnabled(),
                rawMenu.getIconIdentifier(),
                isAuthorized ? rawMenu.getHrefAttribute() : null);

        // #2: Complete all children to the supplied StandardMenu - if we are authorized.
        //     Otherwise, simply ignore processing the children.
        if (isAuthorized) {
            final List<AuthorizedNavItem> completedChildren = new ArrayList<>();
            populate(completedChildren, rawMenu.getChildren(), authorizationPaths);

            // ... and add the processed children.
            toReturn.getChildren().addAll(completedChildren);
        }

        // All done.
        return toReturn;
    }

    private StandardMenuItem complete(final StandardMenuItem rawMenuItem, final boolean isAuthorized) {

        return new StandardMenuItem(
                rawMenuItem.getRoleAttribute(),
                rawMenuItem.getIdAttribute(),
                rawMenuItem.getTabIndexAttribute(),
                synthesizeCssClasses(rawMenuItem.getCssClasses(), isAuthorized),
                null,
                isAuthorized && rawMenuItem.isEnabled(),
                rawMenuItem.getIconIdentifier(),
                isAuthorized ? rawMenuItem.getHrefAttribute() : null);
    }

    private String synthesizeCssClasses(final List<String> currentCss, final boolean isAuthorized) {

        StringBuilder builder = new StringBuilder();
        if (currentCss != null) {

            for (String current : currentCss) {

                // Check sanity
                if (current.equalsIgnoreCase("enabled") && !isAuthorized) {
                    continue;
                }

                if (builder.length() > 0) {
                    builder.append(SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING);
                }
                builder.append(current);
            }
        }

        if (!isAuthorized) {
            if (builder.length() > 0) {
                builder.append(SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING);
            }
            builder.append("disabled");
        }

        // All done.
        return builder.toString();
    }

    private MenuStructure readRawMenuStructure(final String organisationName) {

        // Check sanity
        Validate.notEmpty(organisationName, "Cannot handle null or empty 'organisation' name.");

        // Find the raw menustructure file.
        final File rawMenuStructureFile = new File(environmentStorageRootDir,
                organisationName + "/" + MENU_STRUCTURE_PATH);
        final boolean menuStructureFound = rawMenuStructureFile.exists() && rawMenuStructureFile.isFile();

        if (!menuStructureFound) {
            throw new IllegalStateException("Could not find required menu structure file ["
                    + rawMenuStructureFile.getAbsolutePath() + "]. Verify configuration and restart the application.");
        }

        JAXBContext jaxbContext = null;
        try {
            jaxbContext = JAXBContext.newInstance(MenuStructure.class);
        } catch (JAXBException e) {
            throw new IllegalArgumentException("Could not create JAXB context.", e);
        }

        try {
            final Source source = new StreamSource(new FileReader(rawMenuStructureFile));
            final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(source, MenuStructure.class).getValue();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not Unmarshal MenuStructure", e);
        }
    }
}
