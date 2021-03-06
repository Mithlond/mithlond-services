/*-
 * #%L
 * Nazgul Project: mithlond-services-organisation-api
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
package se.mithlond.services.organisation.api;

import se.mithlond.services.organisation.api.parameters.FoodAndAllergySearchParameters;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.food.Allergy;
import se.mithlond.services.organisation.model.food.Food;
import se.mithlond.services.organisation.model.food.FoodPreference;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.transport.convenience.food.SlimFoodPreferencesVO;
import se.mithlond.services.organisation.model.transport.food.Allergies;
import se.mithlond.services.shared.spi.jpa.JpaCudService;

import javax.ejb.Local;
import javax.validation.constraints.NotNull;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * <p>Service specification for queries extracting information regarding foods and allergies.
 * All allergies relate to a {@link se.mithlond.services.organisation.model.user.User}, and not to
 * {@link se.mithlond.services.organisation.model.membership.Membership}s.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see MembershipService
 */
@Local
public interface FoodAndAllergyService extends JpaCudService {

    /**
     * Retrieves all known allergies.
     *
     * @return all known allergies.
     */
    SortedSet<Allergy> getKnownAllergies();

    /**
     * Retrieves a sortedMap relating Memberships to their Allergies.
     *
     * @param searchParameters the search parameters for the users and allergies to retrieve.
     * @return A SortedMap relating Memberships to their Allergies.
     */
    SortedMap<Membership, SortedSet<Allergy>> getAllergiesFor(
            @NotNull final FoodAndAllergySearchParameters searchParameters);

    /**
     * Retrieves the Set of Allergies for the supplied Membership.
     *
     * @param membership The Membership for which allergies should be retrieved.
     * @return A SortedSet containing the known Allergy objects for the supplied Membership.
     */
    SortedSet<Allergy> getAllergiesFor(@NotNull final Membership membership);

    /**
     * Retrieves all Memberships within the supplied organisation, mapped to their respective FoodPreferences.
     * Memberships who have no food preferences are not included in the returned SortedMap.
     *
     * @param searchParameters the search parameters for the users and allergies to retrieve.
     * @return A SortedMap relating Membership to a SortedSet of FoodPreferences.
     */
    SortedMap<Membership, SortedSet<FoodPreference>> getPreferencesFor(
            @NotNull final FoodAndAllergySearchParameters searchParameters);

    /**
     * Retrieves the Set of FoodPreferences for the supplied Membership.
     *
     * @param membership The Membership for which food preferences should be retrieved.
     * @return A SortedSet containing the known food preferences objects for the supplied Membership.
     */
    SortedSet<FoodPreference> getPreferencesFor(@NotNull final Membership membership);

    /**
     * Retrieves a SortedMap relating Memberships to their corresponding allergies,
     * for all Memberships admitted to a particular Activity.
     *
     * @param activityJpaID The jpaID of an Activity.
     * @return A SortedMap relating Memberships admitted to the supplied Activity (jpaID) to their Allergies.
     * @see se.mithlond.services.organisation.model.activity.Activity
     */
    SortedMap<Membership, SortedSet<Allergy>> getAllergiesForActivity(final long activityJpaID);

    /**
     * Retrieves all known Food objects.
     *
     * @return all known Food objects.
     */
    SortedSet<Food> getAllFoods();

    /**
     * Retrieves all known FoodPreference Categories.
     *
     * @return all known Food objects.
     */
    SortedSet<Category> getAllFoodPreferences();

    /**
     * Updates the Food Preferences with the supplied data.
     *
     * @param activeMembership The Membership performing this call. If the Membership is considered an Administrator,
     *                         the SlimFoodPreferencesVO are updated even for other Memberships. Otherwise, only the
     *                         activeMemberships own Food preferences are updated.
     * @param receivedData     The Food preferences to update.
     * @return The resulting Food preferences.
     */
    SlimFoodPreferencesVO updateFoodPreferences(final Membership activeMembership,
                                                final SlimFoodPreferencesVO receivedData);

    /**
     * Updates the Allergies with the supplied data.
     *
     * @param activeMembership The Membership performing this call. If the Membership is considered an Administrator,
     *                         the Allergies are updated even for other Memberships. Otherwise, only the
     *                         activeMemberships own Allergies are updated.
     * @param receivedData     The expected/desired state of Allergies.
     * @return The resulting Allergies.
     */
    Allergies updateAllergies(final Membership activeMembership,
                              final Allergies receivedData);
}
