/*-
 * #%L
 * Nazgul Project: mithlond-services-organisation-impl-ejb
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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
package se.mithlond.services.organisation.impl.ejb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.mithlond.services.organisation.api.FoodAndAllergyService;
import se.mithlond.services.organisation.api.OrganisationService;
import se.mithlond.services.organisation.api.parameters.FoodAndAllergySearchParameters;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.activity.Admission;
import se.mithlond.services.organisation.model.food.Allergy;
import se.mithlond.services.organisation.model.food.AllergySeverity;
import se.mithlond.services.organisation.model.food.Food;
import se.mithlond.services.organisation.model.food.FoodPreference;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.transport.convenience.food.SlimFoodPreferencesVO;
import se.mithlond.services.organisation.model.transport.food.Allergies;
import se.mithlond.services.organisation.model.transport.food.AllergyVO;
import se.mithlond.services.organisation.model.transport.food.FoodPreferenceVO;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Stateless EJB implementation of the FoodAndAllergyService specification.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Stateless
public class FoodAndAllergyServiceBean extends AbstractJpaService implements FoodAndAllergyService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(FoodAndAllergyServiceBean.class);

    private static final String NO_PREFERENCES_VALUE = "None";
    private static final Comparator<NazgulEntity> ID_COMPARATOR = (l, r) -> (int) (l.getId() - r.getId());

    @EJB
    private OrganisationService organisationServiceBean;

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Allergy> getKnownAllergies() {

        // Find the Allergies within the database.
        final List<Allergy> resultList = entityManager.createNamedQuery(
                Allergy.NAMEDQ_GET_ALL, Allergy.class)
                .getResultList();

        // Re-pack into a SortedSet.
        final SortedSet<Allergy> toReturn = new TreeSet<>();
        toReturn.addAll(resultList);

        if (log.isDebugEnabled()) {
            log.debug("Found [" + toReturn.size() + "] known allergies.");
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<Membership, SortedSet<Allergy>> getAllergiesFor(
            final FoodAndAllergySearchParameters searchParameters) {

        // Check sanity
        Validate.notNull(searchParameters, "searchParameters");

        // Create the return value
        final SortedMap<Membership, SortedSet<Allergy>> toReturn = new TreeMap<>();

        // TODO: IMPLEMENT THIS!
        if (log.isWarnEnabled()) {
            log.warn("'getAllergiesFor' is not yet implemented");
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<Membership, SortedSet<FoodPreference>> getPreferencesFor(
            @NotNull final FoodAndAllergySearchParameters searchParameters) {

        // Check sanity
        Validate.notNull(searchParameters, "searchParameters");

        // Create the return value
        final SortedMap<Membership, SortedSet<FoodPreference>> toReturn = new TreeMap<>();

        // TODO: IMPLEMENT THIS!
        if (log.isWarnEnabled()) {
            log.warn("'getPreferencesFor' is not yet implemented");
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<FoodPreference> getPreferencesFor(final Membership membership) {

        // Check sanity
        Validate.notNull(membership, "membership");

        // Find the Food preferences
        final List<FoodPreference> prefs = entityManager.createNamedQuery(
                FoodPreference.NAMEDQ_GET_BY_USERID, FoodPreference.class)
                .setParameter(OrganisationPatterns.PARAM_USER_ID, membership.getUser().getId())
                .getResultList();

        // Re-pack into a SortedSet.
        final SortedSet<FoodPreference> toReturn = new TreeSet<>();
        toReturn.addAll(prefs);

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Allergy> getAllergiesFor(final Membership membership) {

        // Check sanity
        Validate.notNull(membership, "membership");

        // Find the allergies
        final List<Allergy> allergies = entityManager.createNamedQuery(Allergy.NAMEDQ_GET_BY_USERID, Allergy.class)
                .setParameter(OrganisationPatterns.PARAM_USER_ID, membership.getUser().getId())
                .getResultList();

        // Re-pack into a SortedSet.
        final SortedSet<Allergy> toReturn = new TreeSet<>();
        toReturn.addAll(allergies);

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedMap<Membership, SortedSet<Allergy>> getAllergiesForActivity(final long activityJpaID) {

        // Create the return Map
        final SortedMap<Membership, SortedSet<Allergy>> toReturn = new TreeMap<>();

        final Activity activity = entityManager.find(Activity.class, activityJpaID);
        if (activity != null) {

            final Set<Membership> admittedMemberships = activity.getAdmissions()
                    .stream()
                    .map(Admission::getAdmitted)
                    .collect(Collectors.toSet());

            admittedMemberships.forEach(m -> {

                final List<Allergy> currentAllergies = entityManager.createNamedQuery(
                        Allergy.NAMEDQ_GET_BY_USERID, Allergy.class)
                        .setParameter(OrganisationPatterns.PARAM_USER_ID, m.getUser().getId())
                        .getResultList();


                if (currentAllergies == null || currentAllergies.isEmpty()) {
                    toReturn.put(m, new TreeSet<>());
                } else {

                    final SortedSet<Allergy> allergies = new TreeSet<>();
                    allergies.addAll(currentAllergies);

                    toReturn.put(m, allergies);
                }
            });
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Food> getAllFoods() {

        // Create the return wrapper
        final SortedSet<Food> toReturn = new TreeSet<>();

        // Find all Foods.
        final TypedQuery<Food> query = entityManager.createQuery("select f From Food f "
                + "order by f.category.categoryID, f.subCategory.categoryID", Food.class);
        toReturn.addAll(query.getResultList());

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<Category> getAllFoodPreferences() {

        // Create the return wrapper
        final SortedSet<Category> toReturn = new TreeSet<>();

        // Find all Foods.
        final TypedQuery<Category> query = entityManager.createNamedQuery(
                FoodPreference.NAMEDQ_GET_ALL,
                Category.class);
        toReturn.addAll(query.getResultList());

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SlimFoodPreferencesVO updateFoodPreferences(final Membership activeMembership,
                                                       final SlimFoodPreferencesVO receivedData) {

        // Check sanity
        Validate.notNull(activeMembership, "activeMembership");

        // Create the return wrapper
        final SlimFoodPreferencesVO toReturn = new SlimFoodPreferencesVO();

        // Fail fast
        if (receivedData == null || receivedData.getFoodPreferences() == null) {
            return toReturn;
        }
        final SortedSet<FoodPreferenceVO> receivedFoodPrefs = receivedData.getFoodPreferences();

        // Business rules:
        //
        // 1. Non-administrators can update their own food preferences only.
        // 2. Administrators can update food preferences for people within their own Organisation.
        //
        final Organisation activeOrganisation = activeMembership.getOrganisation();
        final boolean isAdmin = organisationServiceBean.isAdministratorFor(activeMembership, activeOrganisation);
        final long ownUserID = activeMembership.getUser().getId();

        if (!isAdmin) {

            // Only affect self's Food preferences
            final Set<FoodPreferenceVO> targetState = receivedFoodPrefs
                    .stream()
                    .filter(fp -> fp.getUserID() == ownUserID)
                    .collect(Collectors.toSet());

            if (targetState != null) {

                // Find all FoodPreference Categories.
                final List<Category> foodPreferenceCategories = entityManager.createNamedQuery(
                        FoodPreference.NAMEDQ_GET_ALL, Category.class)
                        .getResultList();

                // Persist received state into database
                this.persistFoodPreferencesFor(activeMembership.getUser(), targetState, foodPreferenceCategories);

                // Populate the return value
                if (!removeAllPreferences(targetState)) {
                    toReturn.getFoodPreferences().addAll(targetState);
                }

            } else {
                log.warn("Found null targetState, after filtering. Ignoring inbound FoodPreferences "
                        + "for non-admin Membership (" + activeMembership.getAlias() + " in "
                        + activeOrganisation.getOrganisationName() + ")");
            }

        } else {

            // Find all FoodPreference Categories.
            final List<Category> foodPreferenceCategories = entityManager.createNamedQuery(
                    FoodPreference.NAMEDQ_GET_ALL, Category.class)
                    .getResultList();

            // Affect Food preferences for users within own Organisation
            final SortedMap<User, Set<FoodPreferenceVO>> user2targetStateMap = new TreeMap<>(ID_COMPARATOR);
            final SortedMap<User, Membership> tmpCache = new TreeMap<>(ID_COMPARATOR);

            for (FoodPreferenceVO current : receivedFoodPrefs) {

                final User currentUser = user2targetStateMap.keySet().stream()
                        .filter(u -> u.getId() == current.getUserID())
                        .findFirst()
                        .orElseGet(() -> entityManager.find(User.class, current.getUserID()));

                if (currentUser != null) {

                    // Find the single Membership of the currentUser within the active organisation.
                    try {

                        final Membership validMembership = tmpCache.getOrDefault(currentUser,
                                entityManager.createNamedQuery(
                                        Membership.NAMEDQ_GET_BY_ORGANISATION_ID_LOGINPERMITTED_AND_USERID,
                                        Membership.class)
                                        .setParameter(OrganisationPatterns.PARAM_ORGANISATION_ID, activeOrganisation.getId())
                                        .setParameter(OrganisationPatterns.PARAM_USER_ID, current.getUserID())
                                        .setParameter(OrganisationPatterns.PARAM_LOGIN_PERMITTED, true)
                                        .getSingleResult());

                        if (log.isInfoEnabled()) {
                            log.info("Found valid Membership [" + validMembership.getAlias() + "] for "
                                    + "[" + currentUser.getId() + " (" + currentUser.getFirstName() + " "
                                    + currentUser.getLastName() + ")]");
                        }

                        if (validMembership != null) {

                            // Inscribe into the tmp cache.
                            if (!tmpCache.containsKey(currentUser)) {
                                tmpCache.put(currentUser, validMembership);
                            }

                            // Add the current food preference to the target state
                            Set<FoodPreferenceVO> targetState = user2targetStateMap.get(currentUser);
                            if (targetState == null) {

                                targetState = new TreeSet<>();
                                user2targetStateMap.put(currentUser, targetState);
                            }
                            targetState.add(current);

                        } else {

                            log.warn("Found no Membership within organisation ["
                                    + activeOrganisation.getOrganisationName() + "] for user [" + currentUser.getId()
                                    + " (" + currentUser.getFirstName() + " " + currentUser.getLastName()
                                    + ")]. Ignoring FoodPreference [" + current.getPreference()
                                    + "] for that User.");
                        }

                    } catch (Exception e) {

                        // Could not find the Membership for user with the currentID.
                        log.warn("Found no Membership within organisation ["
                                + activeOrganisation.getOrganisationName() + "] for user [" + currentUser.getId()
                                + " (" + currentUser.getFirstName() + " " + currentUser.getLastName()
                                + ")]. Not updating FoodPreferences for that User.", e);
                    }
                } else {

                    if (log.isWarnEnabled()) {
                        log.warn("Found no user for userID [" + current.getUserID() + "]. Ignoring preference for ["
                                + current.getPreference() + "]");
                    }
                }
            }

            // Update the state for each user within the database.
            user2targetStateMap.forEach(
                    (user, target) -> this.persistFoodPreferencesFor(user, target, foodPreferenceCategories));

            // Populate the return value
            user2targetStateMap.forEach((user, targetState) -> {

                if (!removeAllPreferences(targetState)) {
                    toReturn.getFoodPreferences().addAll(targetState);
                }
            });
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    public Allergies updateAllergies(final Membership activeMembership, final Allergies receivedData) {

        // Check sanity
        Validate.notNull(activeMembership, "activeMembership");

        // Create the return wrapper
        final Organisation activeOrganisation = activeMembership.getOrganisation();
        final Allergies toReturn = new Allergies(activeOrganisation.getLocale());

        // Fail fast
        if (receivedData == null || receivedData.getAllergyList() == null) {

            log.warn("Received no/null allergy list. Aborting.");
            return toReturn;
        }

        // Business rules:
        //
        // 1. Non-administrators can update their own Allergies only.
        // 2. Administrators can update Allergies for people within their own Organisation.
        //
        final boolean isAdmin = organisationServiceBean.isAdministratorFor(activeMembership, activeOrganisation);
        final long ownUserID = activeMembership.getUser().getId();
        final Locale organisationLocale = activeOrganisation.getLocale();

        final List<AllergySeverity> severities = entityManager.createNamedQuery(
                AllergySeverity.NAMEDQ_GET_ALL, AllergySeverity.class)
                .getResultList();

        if (log.isDebugEnabled()) {
            log.debug("Found all AllergySeverities: " + severities.stream()
                    .map(s -> s.getShortDescription().getText())
                    .reduce((l, r) -> l + ", " + r)
                    .orElse("none (Null)"));
        }

        if (!isAdmin) {

            // Only affect own Allergies
            final Set<AllergyVO> targetState = receivedData
                    .getAllergyList()
                    .stream()
                    .filter(aVO -> aVO.getUserID() == ownUserID)
                    .collect(Collectors.toSet());

            if (targetState != null) {

                // Find Allergies to add.
                this.persistAllergiesFor(activeMembership.getUser(), targetState, severities, organisationLocale);

            } else {

                log.warn("Found null targetState, after filtering. Ignoring inbound Allergies "
                        + "for non-admin Membership (" + activeMembership.getAlias() + " in "
                        + activeOrganisation.getOrganisationName() + ")");
            }

        } else {

            // Affect Food preferences for users within own Organisation
            final SortedMap<User, Set<AllergyVO>> user2AllergyMap = new TreeMap<>(ID_COMPARATOR);
            final SortedMap<User, Membership> user2MembershipCache = new TreeMap<>(ID_COMPARATOR);

            for (AllergyVO current : receivedData.getAllergyList()) {

                if (log.isDebugEnabled()) {
                    log.debug("Matching " + current.toString());
                }

                final User currentUser = user2AllergyMap.keySet().stream()
                        .filter(aUser -> aUser.getId() == current.getUserID())
                        .findFirst()
                        .orElseGet(() -> entityManager.find(User.class, current.getUserID()));

                if (currentUser == null) {
                    log.warn("Could not find User for JPA ID [" + current.getUserID() + "]");
                } else if (log.isDebugEnabled()) {
                    log.debug("User [" + currentUser.getId() + " (" + currentUser.getFirstName() + " "
                            + currentUser.getLastName() + ") is an admin for [" + activeOrganisation
                            .getOrganisationName() + "]");
                }

                if (currentUser != null) {

                    // Find the single Membership of the currentUser within the active organisation.
                    try {

                        final Membership validMembership = user2MembershipCache.getOrDefault(currentUser,
                                entityManager.createNamedQuery(
                                        Membership.NAMEDQ_GET_BY_ORGANISATION_ID_LOGINPERMITTED_AND_USERID,
                                        Membership.class)
                                        .setParameter(OrganisationPatterns.PARAM_ORGANISATION_ID, activeOrganisation.getId())
                                        .setParameter(OrganisationPatterns.PARAM_USER_ID, current.getUserID())
                                        .setParameter(OrganisationPatterns.PARAM_LOGIN_PERMITTED, true)
                                        .getSingleResult());

                        if (log.isInfoEnabled()) {
                            log.info("Found Membership with alias [" + validMembership.getAlias() + "] for "
                                    + "[" + currentUser.getId() + " (" + currentUser.getFirstName() + " "
                                    + currentUser.getLastName() + ")]");
                        }

                        if (validMembership != null) {

                            // Inscribe into the tmp cache.
                            if (!user2MembershipCache.containsKey(currentUser)) {
                                user2MembershipCache.put(currentUser, validMembership);
                            }

                            // Add the current food preference to the target state
                            Set<AllergyVO> targetState = user2AllergyMap.get(currentUser);
                            if (targetState == null) {

                                targetState = new TreeSet<>();
                                user2AllergyMap.put(currentUser, targetState);
                            }
                            targetState.add(current);

                        } else {

                            log.warn("Found no Membership within organisation ["
                                    + activeOrganisation.getOrganisationName() + "] for user [" + currentUser.getId()
                                    + " (" + currentUser.getFirstName() + " " + currentUser.getLastName()
                                    + ")]. Ignoring Allergy [" + current.getFoodName() + " :: " + current.getSeverity()
                                    + "] for that User.");
                        }

                    } catch (Exception e) {

                        // Could not find the Membership for user with the currentID.
                        log.warn("Found no Membership within organisation ["
                                + activeOrganisation.getOrganisationName() + "] for user [" + currentUser.getId()
                                + " (" + currentUser.getFirstName() + " " + currentUser.getLastName()
                                + ")]. Not updating Allergies for that User.", e);
                    }
                } else {

                    if (log.isWarnEnabled()) {
                        log.warn("Found no user for userID [" + current.getUserID() + "]. Ignoring allergies for ["
                                + current.getFoodName() + " :: " + current.getSeverity() + "]");
                    }
                }
            }

            // Update the state for each user within the database.
            user2AllergyMap.forEach(
                    (user, target) -> this.persistAllergiesFor(user, target, severities, organisationLocale));

            // Populate the return value
            user2AllergyMap.forEach((user, targetState) -> {
                toReturn.getAllergyList().addAll(targetState);
            });
        }

        // All Done.
        return toReturn;
    }

    //
    // Private helpers
    //

    private void persistAllergiesFor(final User user,
                                     final Set<AllergyVO> targetState,
                                     final List<AllergySeverity> severities,
                                     final Locale locale) {

        final String userLogMsg = "[" + user.getId() + " (" + user.getFirstName()
                + " " + user.getLastName() + ")]";

        final List<Allergy> existingAllergies = entityManager.createNamedQuery(
                Allergy.NAMEDQ_GET_BY_USERID, Allergy.class)
                .setParameter(OrganisationPatterns.PARAM_USER_ID, user.getId())
                .getResultList();

        final Map<String, AllergySeverity> localizedSeverityMap = severities.stream()
                .collect(Collectors.toMap(as -> as.getShortDescription().getText(locale, "Default"), as -> as));
        final Map<String, Allergy> existingAllergyMap = existingAllergies.stream()
                .collect(Collectors.toMap(a -> "" + a.getUser().getId() + "_" + a.getFood().getId(), a -> a));
        final Map<String, AllergyVO> targetVoMap = targetState.stream()
                .collect(Collectors.toMap(a -> "" + a.getUserID() + "_" + a.getFoodJpaID(), a -> a));

        // Collect all existing Allergies which should be removed from the database,
        // and the received AllergyVOs which should be converted to Allergy objects and persisted.
        final Set<Allergy> toRemove = new TreeSet<>();
        final Set<Allergy> toAdd = new TreeSet<>();
        final Set<Allergy> toUpdate = new TreeSet<>();

        final Map<Long, Food> id2FoodMap = getAllFoods()
                .stream()
                .collect(Collectors.toMap(NazgulEntity::getId, f -> f));

        targetVoMap.entrySet().stream()
                .filter(e -> !existingAllergyMap.containsKey(e.getKey()))
                .map(e -> {

                    final AllergyVO currentVO = e.getValue();

                    // All Done.
                    return new Allergy(id2FoodMap.get(currentVO.getFoodJpaID()),
                            user,
                            localizedSeverityMap.get(currentVO.getSeverity()),
                            currentVO.getNote());

                }).forEach(toAdd::add);

        existingAllergyMap.entrySet().stream()
                .filter(a -> !targetVoMap.containsKey(a.getKey()))
                .map(Map.Entry::getValue)
                .forEach(toRemove::add);

        existingAllergyMap.entrySet().stream()
                .filter(a -> targetVoMap.containsKey(a.getKey()))
                .map(Map.Entry::getValue)
                .forEach(toUpdate::add);

        if (log.isDebugEnabled()) {

            log.debug(userLogMsg + " about to REMOVE [" + toRemove.size() + "] Allergies: " + toRemove
                    .stream()
                    .map(a -> "[" + a.getFood().getLocalizedFoodName().getText() + " :: "
                            + a.getSeverity().getShortDescription().getText() + "]")
                    .sorted()
                    .reduce((l, r) -> l + "\n " + r).orElse("<none>"));

            log.debug(userLogMsg + " about to ADD [" + toAdd.size() + "] Allergies: " + toAdd
                    .stream()
                    .map(a -> "[" + a.getFood().getLocalizedFoodName().getText() + " :: "
                            + a.getSeverity().getShortDescription().getText() + "]")
                    .sorted()
                    .reduce((l, r) -> l + "\n " + r).orElse("<none>"));

            log.debug(userLogMsg + " about to UPDATE [" + toUpdate.size() + "] Allergies: " + toUpdate
                    .stream()
                    .map(a -> "[" + a.getFood().getLocalizedFoodName().getText() + " :: "
                            + a.getSeverity().getShortDescription().getText() + "]")
                    .sorted()
                    .reduce((l, r) -> l + "\n " + r).orElse("<none>"));
        }

        // Update the database state
        toAdd.forEach(a -> entityManager.persist(a));
        toRemove.forEach(a -> entityManager.remove(a));
        toUpdate.forEach(a -> {

            final String key = "" + a.getUser().getId() + "_" + a.getFood().getId();
            final AllergyVO desiredState = targetVoMap.get(key);

            // Update all applicable properties
            a.setSeverity(localizedSeverityMap.get(desiredState.getSeverity()));
            a.setNote(desiredState.getNote());
        });
    }

    private void persistFoodPreferencesFor(final User user,
                                           final Set<FoodPreferenceVO> targetState,
                                           final List<Category> foodPreferenceCategories) {

        final String userLogMsg = "[" + user.getId() + " (" + user.getFirstName()
                + " " + user.getLastName() + ")]";

        final List<FoodPreference> existingFoodPrefs = entityManager.createNamedQuery(
                FoodPreference.NAMEDQ_GET_BY_USERID, FoodPreference.class)
                .setParameter(OrganisationPatterns.PARAM_USER_ID, user.getId())
                .getResultList();
        final Map<String, FoodPreference> catId2FoodPrefMap = existingFoodPrefs
                .stream()
                .collect(Collectors.toMap(fp -> fp.getCategory().getCategoryID(), fp -> fp));

        // Check sanity:
        // Should we remove all Food Preferences?
        if (removeAllPreferences(targetState)) {
            log.info("As instructed, removing all Food Preferences for " + userLogMsg);
        }

        // Collect all existing FoodPreferences which should be removed from the database,
        // and the received FoodPreferenceVOs which should be converted to FoodPreferences and
        // persisted.
        final Set<FoodPreference> toRemove = new TreeSet<>();
        final Set<FoodPreferenceVO> toAdd = new TreeSet<>();

        targetState.stream().filter(fp -> !catId2FoodPrefMap.containsKey(fp.getPreference())).forEach(toAdd::add);
        existingFoodPrefs.stream()
                .filter(pref -> isAbsentFromTargetState(pref, targetState))
                .forEach(toRemove::add);

        if (log.isDebugEnabled()) {

            log.debug("About to update FoodPreferences in Database for " + userLogMsg

                    + ".\nTo Remove: " + toRemove.stream()
                    .map(pref -> pref.getCategory().getCategoryID())
                    .sorted()
                    .reduce((l, r) -> l + ", " + r)
                    .orElse("<none>")

                    + "\nTo Add: " + toAdd
                    + "\nTargetState: " + targetState

                    + "\nExisting: " + existingFoodPrefs.stream()
                    .map(fp -> fp.getCategory().getCategoryID())
                    .sorted()
                    .reduce((l, r) -> l + ", " + r)
                    .orElse("<none>"));
        }

        // Update the database state.
        toRemove.forEach(pref -> entityManager.remove(pref));
        toAdd.stream()
                .map(fp -> {

                    final Category theCategory = foodPreferenceCategories
                            .stream()
                            .filter(c -> c.getCategoryID().equalsIgnoreCase(fp.getPreference()))
                            .findFirst()
                            .orElse(null);

                    return theCategory != null ? new FoodPreference(theCategory, user) : null;
                })
                .filter(Objects::nonNull)
                .forEach(pref -> entityManager.persist(pref));
    }

    private boolean removeAllPreferences(final Set<FoodPreferenceVO> targetState) {
        return targetState != null
                && (targetState.size() == 1
                && targetState.iterator().next().getPreference().equalsIgnoreCase(NO_PREFERENCES_VALUE));
    }

    private boolean isAbsentFromTargetState(final FoodPreference aPref, final Set<FoodPreferenceVO> targetState) {

        final String toMatch = aPref.getCategory().getCategoryID();
        final Optional<String> foundMatchingCategoryIDs = targetState.stream()
                .map(FoodPreferenceVO::getPreference)
                .filter(catID -> {

                    final boolean isMatch = catID.equalsIgnoreCase(toMatch);
                    if (log.isDebugEnabled()) {
                        log.debug("Comparing foodPreference Category [" + toMatch + "] with received VO ["
                                + catID + "]: " + isMatch);
                    }

                    return isMatch;
                })
                .findFirst();

        final boolean toReturn = !foundMatchingCategoryIDs.isPresent();

        if (log.isDebugEnabled()) {
            log.debug("... isAbsentFromTargetState: " + toReturn);
        }

        return toReturn;
    }
}
