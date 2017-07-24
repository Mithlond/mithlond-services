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
import se.mithlond.services.organisation.api.parameters.FoodAndAllergySearchParameters;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.activity.Admission;
import se.mithlond.services.organisation.model.food.Allergy;
import se.mithlond.services.organisation.model.food.Food;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import java.util.List;
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
}
