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
import se.mithlond.services.content.model.localization.Localization;
import se.mithlond.services.content.model.localization.LocalizedTexts;
import se.mithlond.services.content.model.navigation.AbstractAuthorizedNavItem;
import se.mithlond.services.content.model.navigation.AbstractLinkedNavItem;
import se.mithlond.services.content.model.navigation.integration.MenuStructure;
import se.mithlond.services.content.model.navigation.integration.SeparatorMenuItem;
import se.mithlond.services.content.model.navigation.integration.StandardMenu;
import se.mithlond.services.content.model.navigation.integration.StandardMenuItem;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.shared.authorization.api.AuthorizationPattern;
import se.mithlond.services.shared.authorization.api.Authorizer;
import se.mithlond.services.shared.authorization.api.SemanticAuthorizationPathProducer;
import se.mithlond.services.shared.authorization.api.SimpleAuthorizer;
import se.mithlond.services.shared.authorization.api.UnauthorizedException;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Standard stateless NavigationService POJO implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Stateless
public class NavigationServiceBean extends AbstractJpaService implements NavigationService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(NavigationServiceBean.class);

    // Internal state
    private Authorizer simpleAuthorizer = SimpleAuthorizer.getInstance();

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
        final StandardMenu rawRootMenu = rawMenuStructure.getRootMenu();
        final String requiredAuthPatterns = rawRootMenu.getRequiredAuthorizationPatterns().stream()
                .map(AuthorizationPattern::toString)
                .reduce((left, right) -> left + "," + right)
                .orElse(null);

        // Does the supplied SemanticAuthorizationPaths imply that the caller
        // is authorized to view the current AuthorizedNavItem?
        final boolean isAuthorized = SimpleAuthorizer.getInstance()
                .isAuthorized(rawRootMenu.getRequiredAuthorizationPatterns(), paths);

        final StandardMenu authorizationFilteredMenu = new StandardMenu(rawRootMenu.getRoleAttribute(),
                rawRootMenu.getIdAttribute(),
                rawRootMenu.getTabIndexAttribute(),
                synthesizeCssClasses(rawRootMenu.getCssClasses(), isAuthorized),
                requiredAuthPatterns,
                true,
                rawRootMenu.getIconIdentifier(),
                rawRootMenu.getLocalizedTexts(),
                rawRootMenu.getHrefAttribute(),
                null);
        populateRecursively(authorizationFilteredMenu, rawRootMenu, paths);

        // All done.
        return new MenuStructure(realm, authorizationFilteredMenu);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MenuStructure createOrUpdate(
            final String realm,
            final MenuStructure menuStructure,
            final List<SemanticAuthorizationPathProducer> callersAuthPaths) throws UnauthorizedException {

        // Check sanity
        Validate.notEmpty(realm, "Cannot handle null or empty 'realm' argument.");
        Validate.notNull(menuStructure, "Cannot handle null or empty 'menuStructure' argument.");
        Validate.isTrue(realm.equals(menuStructure.getOrganisationName()), "Realm value [" + realm
                + "] must be equal to the OrganisationName within the supplied menuStructure ["
                + menuStructure.getOrganisationName() + "]");
        final List<SemanticAuthorizationPathProducer> effectiveAuthorizationPaths = callersAuthPaths == null
                ? new ArrayList<>()
                : callersAuthPaths;

        // Collect the caller's effective SemanticAuthorizationPaths.
        final SortedSet<SemanticAuthorizationPath> possessedAuthPaths = new TreeSet<>();
        effectiveAuthorizationPaths.stream().forEach(current -> possessedAuthPaths.addAll(current.getPaths()));

        // Is the caller authorized to create or update the MenuStructure for the supplied realm?
        final SortedSet<AuthorizationPattern> requiredPatterns = REALM_AUTHORIZATION_PATTERN_FUNCTION.apply(realm);
        simpleAuthorizer.validateAuthorization(
                requiredPatterns,
                possessedAuthPaths,
                "MenuStructure for realm [" + realm + "] could not be created/updated. "
                        + "Caller lacks required AuthorizationPattern (" + requiredPatterns + ").");

        // Create or Retrieve all Localizations within the LocalizedTexts within the MenuStructure.
        final Map<String, Localization> stringForm2LocalizationMap = getLocalizations(menuStructure)
                .stream()
                .collect(Collectors.toMap(Localization::toString, current -> current));

        // Should we update the MenuStructure?
        final List<MenuStructure> menuStructures = entityManager.createNamedQuery(MenuStructure
                .NAMEDQ_GET_BY_ORGANISATION_NAME, MenuStructure.class)
                .setParameter(Patterns.PARAM_ORGANISATION_NAME, realm)
                .getResultList();

        // Find the Organization for the supplied realm (i.e. OrganisationName).
        final List<Organisation> organisationList = entityManager.createNamedQuery(
                Organisation.NAMEDQ_GET_BY_NAME, Organisation.class)
                .setParameter(Patterns.PARAM_ORGANISATION_NAME, menuStructure.getOrganisationName())
                .getResultList();
        if (organisationList.size() != 1) {
            throw new IllegalArgumentException("Organisation name [" + menuStructure.getOrganisationName()
                    + "] did not correspond to a single known Organisation. Found ["
                    + organisationList.size() + "] Organizations which implies an invalid request argument.");
        }

        // Assign the managed Organisation to our MenuStructure.
        final Organisation managedOrganisation = organisationList.get(0);
        menuStructure.setOwningOrganisation(managedOrganisation);

        // Debug somewhat before starting persisting everything.
        if (log.isDebugEnabled()) {

            final String operationType = menuStructures.size() == 0 ? "Create" : "Update";
            log.debug("Got Organisation [" + managedOrganisation.getOrganisationName() + "], and ["
                    + menuStructures.size() + "] existing/persisted MenuStructures --> " + operationType
                    + " operation.");
        }

        MenuStructure toReturn;
        if (menuStructures == null || menuStructures.isEmpty()) {

            // Persist the MenuStructure (nonexistent within the local database for the given Realm).
            // This should be done
            entityManager.persist(menuStructure);
            entityManager.flush();

            // Now persist all children recursively.
            final StandardMenu rootMenu = menuStructure.getRootMenu();
            persistOrUpdate(rootMenu, stringForm2LocalizationMap, entityManager);

            // Assign the toReturn value.
            toReturn = menuStructure;

        } else {

            // Update (i.e. overwrite) the MenuStructure present within the local
            // database for the given Realm with the supplied MenuStructure.
            persistOrUpdate(menuStructure.getRootMenu(), stringForm2LocalizationMap, entityManager);

            // Assign the toReturn value.
            toReturn = menuStructure;
        }

        // All Done.
        return toReturn;
    }


    //
    // Private helpers
    //

    private <T> void findAll(final List<T> resultHolder,
            final StandardMenu currentMenu,
            final Function<AbstractAuthorizedNavItem, T> visitorFunction) {

        // Check sanity
        Validate.notNull(resultHolder, "Cannot handle null 'resultHolder' argument.");
        Validate.notNull(currentMenu, "Cannot handle null 'currentMenu' argument.");
        Validate.notNull(visitorFunction, "Cannot handle null 'visitorFunction' argument.");

        // Extract data from the currentMenu
        final T immediateResult = visitorFunction.apply(currentMenu);
        if (immediateResult != null) {
            resultHolder.add(immediateResult);
        }

        // Loop through all children of the supplied ResultHolder
        for (AbstractAuthorizedNavItem currentChild : currentMenu.getChildren()) {

            // Recurse depth first.
            final T currentChildResult = visitorFunction.apply(currentChild);
            if (currentChild instanceof StandardMenu) {
                findAll(resultHolder, (StandardMenu) currentChild, visitorFunction);
            }

            // Add the result if it was non-null.
            if (currentChildResult != null) {
                resultHolder.add(currentChildResult);
            }
        }
    }

    private SortedSet<Localization> getLocalizations(final MenuStructure menuStructure) {

        final StandardMenu rootMenu = menuStructure.getRootMenu();
        final List<List<Localization>> intermediary = new ArrayList<>();

        findAll(intermediary, rootMenu, anItem -> {

            if (anItem instanceof AbstractLinkedNavItem) {

                // In this case, we should be able to find a Localization.
                final List<Localization> toReturn = new ArrayList<>();

                // Populate and return
                final AbstractLinkedNavItem alni = (AbstractLinkedNavItem) anItem;
                toReturn.addAll(alni.getLocalizedTexts().getContainedLocalizations());
                return toReturn;
            }

            // Nah.
            return null;
        });

        // Collect all found localizations.
        final SortedSet<Localization> allLocalizations = new TreeSet<>();
        intermediary.stream().forEach(allLocalizations::addAll);

        final List<Localization> alreadyPersisted = allLocalizations
                .stream()
                .filter(current -> current.getId() != 0L)
                .collect(Collectors.toList());
        final List<Localization> needsPersisting = allLocalizations
                .stream()
                .filter(current -> current.getId() == 0L)
                .filter(current -> !alreadyPersisted.contains(current))
                .collect(Collectors.toList());

        if (log.isDebugEnabled()) {
            log.debug("Found Localizations.... "
                    + "\n  Already persisted: " + alreadyPersisted
                    + "\n  Needs persisting : " + needsPersisting);
        }

        final List<Localization> managedLocalizations = alreadyPersisted.size() > 0
                ? entityManager.createNamedQuery(Localization.NAMEDQ_GET_BY_PRIMARY_KEYS, Localization.class)
                .setParameter(se.mithlond.services.content.model.Patterns.PARAM_IDS, alreadyPersisted)
                .getResultList()
                : new ArrayList<>();

        if (needsPersisting.size() > 0) {

            needsPersisting.stream()
                    .filter(current -> current.getLocale().getCountry() != null
                            && current.getLocale().getLanguage() != null)
                    .map(current -> {

                        // See if the required Localization is already persisted.
                        final String country = current.getLocale().getCountry();
                        final String language = current.getLocale().getLanguage();

                        // Find the Localization in question.
                        final List<Localization> resultList = entityManager.createNamedQuery(
                                Localization.NAMEDQ_GET_BY_LANGUAGE_AND_COUNTRY, Localization.class)
                                .setParameter(se.mithlond.services.content.model.Patterns.PARAM_COUNTRY, country)
                                .setParameter(se.mithlond.services.content.model.Patterns.PARAM_LANGUAGE, language)
                                .getResultList();

                        // All Done.
                        return resultList.isEmpty() ? current : resultList.get(0);
                    })
                    .forEach(current -> {

                        final Localization currentLocalization = (Localization) current;

                        if (currentLocalization.getId() == 0L) {

                            // Persist the Localization
                            entityManager.persist(currentLocalization);
                            entityManager.flush();
                        }

                        // Add the (now) managed Localization to the holder List.
                        managedLocalizations.add(currentLocalization);
                    });
        }

        // Sort and return
        final SortedSet<Localization> toReturn = new TreeSet<>();
        toReturn.addAll(managedLocalizations);
        return toReturn;
    }

    private void persistOrUpdate(
            final StandardMenu menuStructure,
            final Map<String, Localization> stringForm2LocalizationMap,
            final EntityManager entityManager) {

        final List<AbstractAuthorizedNavItem> children = menuStructure.getChildren();
        final int numChildren = children.size();

        // Persist or update all children found within the current menuStructure.
        for (int index = 0; index < numChildren; index++) {

            // Persist or update the current child?
            final AbstractAuthorizedNavItem currentChild = children.get(index);
            final boolean isUpdateOperation = currentChild.getId() != 0L;

            // Move the current Child to another Parent?
            if (!currentChild.getParent().equals(menuStructure)) {

                if (log.isDebugEnabled()) {
                    log.debug("Moving StandardMenu child [" + currentChild.getClass().getSimpleName()
                            + "] to another Parent (" + menuStructure.toString() + ")");
                }

                currentChild.setParent(menuStructure);
            }

            // Ensure that any existing LocalizedTexts of the currentChild holds only managed Localizations.
            if (currentChild instanceof AbstractLinkedNavItem) {

                final AbstractLinkedNavItem currentCastChild = (AbstractLinkedNavItem) currentChild;
                final LocalizedTexts localizedTexts = currentCastChild.getLocalizedTexts();
                localizedTexts.assignManagedLocalizations(stringForm2LocalizationMap.values());
            }

            // Update the index of the current child.
            currentChild.setIndex(index);

            // Persist or update the current child?
            if (!isUpdateOperation) {
                entityManager.persist(currentChild);
                entityManager.flush();
            }

            // Descend?
            if (currentChild instanceof StandardMenu) {

                final StandardMenu currentStandardMenu = (StandardMenu) currentChild;
                persistOrUpdate(currentStandardMenu, stringForm2LocalizationMap, entityManager);
            }
        }
    }

    private void populateRecursively(
            final StandardMenu toPopulate,
            final StandardMenu template,
            final SortedSet<SemanticAuthorizationPath> userAuthorization) {

        // Should we copy the current template StandardMenu to the toPopulate response?
        if (simpleAuthorizer.isAuthorized(template.getRequiredAuthorizationPatterns(), userAuthorization)) {

            // First, add the authorized StandardMenu itself.
            toPopulate.addChild(processAuthorizationAndCopy(template, true));

            // Now loop through all the children of the template StandardMenu, and add them as well.
            for (AbstractAuthorizedNavItem currentTemplateChild : template.getChildren()) {

                // Is this child authorized by the given userAuthorization?
                final boolean isAuthorized = simpleAuthorizer.isAuthorized(
                        currentTemplateChild.getRequiredAuthorizationPatterns(),
                        userAuthorization);

                // We seem to love polymorphic implementation...
                if (currentTemplateChild instanceof StandardMenu) {

                    // Cast the currentTemplateChild into a StandardMenu.
                    final StandardMenu templateSubMenu = (StandardMenu) currentTemplateChild;
                    final StandardMenu processedSubMenu = processAuthorizationAndCopy(templateSubMenu, isAuthorized);

                    // Perform a Depth-first recursion.
                    populateRecursively(processedSubMenu, templateSubMenu, userAuthorization);

                } else if (currentTemplateChild instanceof StandardMenuItem) {

                    toPopulate.addChild(processAuthorizationAndCopy((StandardMenuItem) currentTemplateChild, isAuthorized));
                } else if (currentTemplateChild instanceof SeparatorMenuItem) {
                    toPopulate.addChild(currentTemplateChild);
                } else {
                    throw new IllegalArgumentException("Cannot handle AuthorizedNavItem of type ["
                            + currentTemplateChild.getClass().getSimpleName() + "]");
                }
            }

        } else {

            // The user was not authorized to see the template StandardMenu.
            if (log.isDebugEnabled()) {
                log.debug("Insufficient authorization for [" + template.getRequiredAuthorizationPatterns()
                        + "] from userAuthorization [" + userAuthorization
                        + "]. Not adding StandardMenu template [JpaID: " + template.getId() + ", Text: "
                        + template.getLocalizedTexts().getText() + "]");
            }
        }
    }

    private StandardMenu processAuthorizationAndCopy(
            final StandardMenu rawMenu,
            final boolean isAuthorized) {

        // #1: Create the resulting StandardMenu
        return new StandardMenu(
                rawMenu.getRoleAttribute(),
                rawMenu.getIdAttribute(),
                rawMenu.getTabIndexAttribute(),
                synthesizeCssClasses(rawMenu.getCssClasses(), isAuthorized),
                null,
                isAuthorized && rawMenu.isEnabled(),
                rawMenu.getIconIdentifier(),
                rawMenu.getLocalizedTexts(),
                isAuthorized ? rawMenu.getHrefAttribute() : null,
                null);
    }

    private StandardMenuItem processAuthorizationAndCopy(
            final StandardMenuItem templateMenuItem,
            final boolean isAuthorized) {

        return new StandardMenuItem(
                templateMenuItem.getRoleAttribute(),
                templateMenuItem.getIdAttribute(),
                templateMenuItem.getTabIndexAttribute(),
                synthesizeCssClasses(templateMenuItem.getCssClasses(), isAuthorized),
                null,
                isAuthorized && templateMenuItem.isEnabled(),
                templateMenuItem.getIconIdentifier(),
                isAuthorized ? templateMenuItem.getHrefAttribute() : null,
                templateMenuItem.getLocalizedTexts(),
                null);
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
