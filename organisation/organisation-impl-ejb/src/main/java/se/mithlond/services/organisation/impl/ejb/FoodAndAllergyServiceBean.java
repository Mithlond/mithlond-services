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
import se.mithlond.services.organisation.api.FoodAndAllergyService;
import se.mithlond.services.organisation.api.OrganisationService;
import se.mithlond.services.organisation.api.parameters.FoodAndAllergySearchParameters;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.activity.Admission;
import se.mithlond.services.organisation.model.food.Allergy;
import se.mithlond.services.organisation.model.food.Food;
import se.mithlond.services.organisation.model.food.FoodPreference;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.transport.convenience.food.SlimFoodPreferencesVO;
import se.mithlond.services.organisation.model.transport.food.FoodPreferenceVO;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
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

        // Create the return wrapper
        final SlimFoodPreferencesVO toReturn = new SlimFoodPreferencesVO();

        // Fail fast
        if (receivedData != null || receivedData.getFoodPreferences().isEmpty()) {
            return toReturn;
        }

        // Business rules:
        //
        // 1. Non-administrators can update their own food preferences only.
        // 2. Administrators can update food preferences for people within their own Organisation.
        //
        final Organisation activeOrganisation = activeMembership.getOrganisation();
        final SortedSet<FoodPreferenceVO> foodPreferences = new TreeSet<>();
        final boolean isAdmin = organisationServiceBean.isAdministratorFor(activeMembership, activeOrganisation);
        final long ownUserID = activeMembership.getId();


        if (!isAdmin) {

            // Only affect self's Food preferences
            final Set<FoodPreferenceVO> targetState = receivedData.getFoodPreferences()
                    .stream()
                    .filter(fp -> fp.getUserID() == ownUserID)
                    .collect(Collectors.toSet());

            // Persist received state into database
            this.persistFoodPreferencesFor(activeMembership.getUser(), targetState);

            // Populate the return value
            toReturn.getFoodPreferences().addAll(targetState);

        } else {

            // Affect Food preferences for users within own Organisation
            final SortedMap<User, Set<FoodPreferenceVO>> user2targetStateMap = new TreeMap<>(
                    (l, r) -> (int) (l.getId() - r.getId()));
            final SortedMap<User, Membership> tmpCache = new TreeMap<>((l, r) -> (int) (l.getId() - r.getId()));

            for (FoodPreferenceVO current : foodPreferences) {

                final User currentUser = user2targetStateMap.keySet().stream()
                        .filter(u -> u.getId() == current.getUserID())
                        .findFirst()
                        .orElseGet(() -> entityManager.find(User.class, current.getUserID()));

                if (currentUser != null) {

                    // Find the single Membership of the currentUser within the active organisation.
                    final Membership validMembership = tmpCache.getOrDefault(currentUser,
                            entityManager.createNamedQuery(
                                    Membership.NAMEDQ_GET_BY_ORGANISATION_ID_LOGINPERMITTED_AND_USERID,
                                    Membership.class)
                                    .setParameter(OrganisationPatterns.PARAM_ORGANISATION_ID, activeOrganisation.getId())
                                    .setParameter(OrganisationPatterns.PARAM_USER_ID, current.getUserID())
                                    .getSingleResult());

                    if (validMembership != null) {

                        // Inscribe into the tmp cache.
                        if (!tmpCache.containsKey(currentUser)) {
                            tmpCache.put(currentUser, validMembership);
                        }

                        // Add the current food preference to the target state
                        final Set<FoodPreferenceVO> targetState = user2targetStateMap
                                .computeIfAbsent(currentUser, k -> new TreeSet<>());
                        targetState.add(current);
                    }
                }
            }

            // Update the state for each user within the database.
            user2targetStateMap.forEach(this::persistFoodPreferencesFor);

            // Populate the return value
            user2targetStateMap.forEach((user, targetState) -> toReturn.getFoodPreferences().addAll(targetState));
        }

        // All Done.
        return toReturn;
    }

    //
    // Private helpers
    //

    private void persistFoodPreferencesFor(final User user, final Set<FoodPreferenceVO> targetState) {

        final Set<FoodPreference> toRemove = new TreeSet<>();
        final Set<FoodPreferenceVO> toAdd = new TreeSet<>();

        final List<FoodPreference> existingFoodPrefs = entityManager.createNamedQuery(
                FoodPreference.NAMEDQ_GET_BY_USERID, FoodPreference.class)
                .setParameter(OrganisationPatterns.PARAM_USER_ID, user.getId())
                .getResultList();
        final Map<String, FoodPreference> catId2FoodPrefMap = existingFoodPrefs
                .stream()
                .collect(Collectors.toMap(fp -> fp.getCategory().getCategoryID(), fp -> fp));

        // Note all new FoodPreferenceVOs
        targetState.stream().filter(fp -> !catId2FoodPrefMap.containsKey(fp.getPreference())).forEach(toAdd::add);

        // Find all non-present (i.e. removed) FoodPreferences
        existingFoodPrefs.stream()
                .filter(pref -> isAbsentFromTargetState(pref, targetState))
                .forEach(toRemove::add);

        // Remove what should be removed from the database
        toRemove.forEach(pref -> entityManager.remove(pref));
        toAdd.stream()
                .map(fp -> new FoodPreference(theCategory, user))
                .forEach(pref -> entityManager.persist(pref));
    }

    private boolean isAbsentFromTargetState(final FoodPreference aPref, final Set<FoodPreferenceVO> targetState) {

        final Optional<FoodPreferenceVO> foundMatch = targetState.stream()
                .filter(fp -> aPref.getCategory().getCategoryID().equalsIgnoreCase(fp.getPreference()))
                .findFirst();

        return !foundMatch.isPresent();
    }
}
