package se.mithlond.services.organisation.api;

import se.mithlond.services.organisation.api.parameters.FoodAndAllergySearchParameters;
import se.mithlond.services.organisation.model.food.Allergy;
import se.mithlond.services.organisation.model.membership.Membership;
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
     * Retrieves a SortedMap relating Memberships to their corresponding allergies,
     * for all Memberships admitted to a particular Activity.
     *
     * @param activityJpaID The jpaID of an Activity.
     * @return A SortedMap relating Memberships admitted to the supplied Activity (jpaID) to their Allergies.
     * @see se.mithlond.services.organisation.model.activity.Activity
     */
    SortedMap<Membership, SortedSet<Allergy>> getAllergiesForActivity(final long activityJpaID);
}
