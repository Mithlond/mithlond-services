/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-jpa
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
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
package se.mithlond.services.shared.spi.jpa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.algorithms.exception.ExceptionMessageManager;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Utility class holding helper methods to simplify working with JPA-based databases.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class JpaUtilities {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(JpaUtilities.class);

    /*
     * Hide the constructor in utility classes.
     */
    private JpaUtilities() {
    }

    /**
     * Convenience method to find entities within the database.
     *
     * @param jpqlOrNamedQuery  Custom JPQL to find Entites as required, or the ID of a NamedQuery.
     * @param namedQuery        If {@code true}, the jpqlOrNamedQuery contains the ID of a NamedQuery, and
     *                          otherwise the custom JPQL to use in creating a TypedQuery.
     * @param resultType        The expected return type. If unknown or compound, use {@code Object.class}.
     * @param entityManager     A non-null EntityManager
     * @param optionalDecorator An optional QueryDecorator used to decorate the TypedQuery after it has been created.
     * @param <T>               The type of Entity to retrieve.
     * @return The result of the JPQL query being fired against the database.
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public static <T> List<T> findEntities(final Class<T> resultType,
            final String jpqlOrNamedQuery,
            final boolean namedQuery,
            final EntityManager entityManager,
            final QueryDecorator optionalDecorator) {

        // Check sanity
        final String nonNull = Validate.notEmpty(jpqlOrNamedQuery, "jpqlOrNamedQuery");
        final EntityManager em = Validate.notNull(entityManager, "entityManager");
        final Class<T> expectedType = Validate.notNull(resultType, "resultType");

        // #1) Create the TypedQuery
        final TypedQuery<T> query = namedQuery
                ? em.createNamedQuery(nonNull, expectedType)
                : em.createQuery(nonNull, expectedType);

        // #2) Decorate the TypedQuery, if asked to.
        if (optionalDecorator != null) {
            optionalDecorator.decorate(query);
        }

        // #3) Fire the Query; return the results
        List<T> toReturn = null;
        try {
            toReturn = query.getResultList();
            return toReturn;
        } catch (Exception e) {

            final String readableStacktrace = ExceptionMessageManager.getReadableStacktrace(e);
            log.error("Exception in fetching results from Database...\n" + readableStacktrace);
            return new ArrayList<>();
        }
    }

    /**
     * Convenience method to persist entities within the database, and then read them back
     * (with Version and JpaID assigned).
     *
     * @param jpqlOrNamedQuery  Custom JPQL to find Entites as required, or the ID of a NamedQuery.
     * @param namedQuery        If {@code true}, the jpqlOrNamedQuery contains the ID of a NamedQuery, and otherwise
     *                          the custom JPQL to use in creating a TypedQuery.
     * @param resultType        The expected return type. If unknown or compound, use {@code Object.class}.
     * @param entityManager     A non-null EntityManager
     * @param optionalDecorator An optional QueryDecorator used to decorate the TypedQuery after it has been created.
     * @param <T>               The type of Entity to retrieve.
     * @return The result of the JPQL query being fired against the database.
     */
    @SuppressWarnings("all")
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public static <T> List<T> persistAndRetrieveEntities(final Class<T> resultType,
            final List<T> toPersist,
            final String jpqlOrNamedQuery,
            final boolean namedQuery,
            final EntityManager entityManager,
            final QueryDecorator optionalDecorator) {

        // Check sanity
        final List<T> persistAll = Validate.notNull(toPersist, "toPersist");
        final EntityManager em = Validate.notNull(entityManager, "entityManager");

        // #1) Persist all supplied entities
        for (T current : persistAll) {
            em.persist(current);
        }

        // #2) Flush the EntityManager, to enable retrieving the newly persisted data.
        try {
            entityManager.flush();
        } catch (Exception e) {
            log.error("Could not flush EntityManager", e);
        }

        // #3) Delegate and return
        return findEntities(resultType, jpqlOrNamedQuery, namedQuery, em, optionalDecorator);
    }

    /**
     * Retrieves an Optional single instance out of a List of T instances, provided that
     * the {@code entities} List is non-null and contains at least one element.
     *
     * @param entities               The list of T entities.
     * @param entityName             The name of the Entity type, in singular.
     * @param entityToStringFunction A function reference to a method converting T type entities to a debug string.
     * @param <T>                    The type of instances provided within the supplied list of Entities.
     * @return An Optional T instance.
     */
    public static <T> Optional<T> getSingleInstance(final List<T> entities,
            final String entityName,
            final Function<T, String> entityToStringFunction) {

        if (entities == null) {
            return Optional.empty();
        }

        final int numEntities = entities.size();
        T toReturn = null;
        switch (numEntities) {
            case 0:
                log.warn("Received no " + entityName + "s.");
                break;

            case 1:
                toReturn = entities.get(0);
                break;

            default:

                toReturn = entities.get(0);

                // Find the effective convert-to-string function.
                final Function<T, String> toStringFunction = entityToStringFunction == null
                        ? T::toString
                        : entityToStringFunction;

                final String msg = entities.stream()
                        .map(toStringFunction)
                        .reduce((l, r) -> l + "," + r)
                        .orElse("<none>");
                log.warn("Received [" + numEntities + "] " + entityName + "s. Returning ["
                        + toStringFunction.apply(toReturn) + "] out of " + msg);
                break;
        }

        // All Done.
        return toReturn == null ? Optional.empty() : Optional.of(toReturn);
    }
}
