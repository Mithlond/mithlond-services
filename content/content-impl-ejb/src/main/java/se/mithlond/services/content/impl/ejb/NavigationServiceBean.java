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
import se.mithlond.services.content.model.navigation.AuthorizedNavItem;
import se.mithlond.services.content.model.navigation.integration.MenuStructure;
import se.mithlond.services.content.model.navigation.integration.SeparatorMenuItem;
import se.mithlond.services.content.model.navigation.integration.StandardMenu;
import se.mithlond.services.content.model.navigation.integration.StandardMenuItem;
import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.shared.authorization.api.Authorizer;
import se.mithlond.services.shared.authorization.api.SemanticAuthorizationPathProducer;
import se.mithlond.services.shared.authorization.api.SimpleAuthorizer;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.ejb.Stateless;
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
public class NavigationServiceBean extends AbstractJpaService implements NavigationService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(NavigationServiceBean.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public MenuStructure getMenuStructure(
            final String realm,
            final List<SemanticAuthorizationPathProducer> callersAuthPaths) {

        // Check sanity
        if (realm == null || realm.isEmpty()) {
            throw new UnknownOrganisationException(realm);
        }

        final List<SemanticAuthorizationPathProducer> effectivePathProducers = callersAuthPaths == null
                ? new ArrayList<>()
                : callersAuthPaths;

        // Read the raw MenuStructure superstructure from the database.
        final MenuStructure rawMenuStructure;
        try {

            rawMenuStructure = entityManager.createNamedQuery(
                    MenuStructure.NAMEDQ_GET_BY_ORGANISATION_NAME, MenuStructure.class)
                    .setParameter(Patterns.PARAM_ORGANISATION_NAME, realm)
                    .getSingleResult();
        } catch (Exception e) {
            throw new UnknownOrganisationException(realm, e);
        }

        // Compile all the AuthenticationPaths of the supplied Memberships.
        final SortedSet<SemanticAuthorizationPath> paths = new TreeSet<>();
        for (SemanticAuthorizationPathProducer current : effectivePathProducers) {
            paths.addAll(current.getPaths());
        }

        // Populate the return menu structure
        final StandardMenu authorizationFilteredMenu = new StandardMenu(role,
                domID,
                tabIndex,
                cssClasses,
                authPatterns,
                true,
                iconIdentifier,
                localizedTexts,
                href,
                null);
        populate(authorizationFilteredMenu, rawMenuStructure.getRootMenu(), paths);

        // All done.
        return new MenuStructure(realm, authorizationFilteredMenu);
    }

    //
    // Private helpers
    //

    private void populate(final StandardMenu toPopulate,
                          final StandardMenu rawSourceItems,
                          final SortedSet<SemanticAuthorizationPath> authorizationPaths) {

        // Perform a simple authorization.
        final Authorizer authorizer = SimpleAuthorizer.getInstance();

        for (AuthorizedNavItem current : rawSourceItems) {

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
}
