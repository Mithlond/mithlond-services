/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-jpa
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;
import java.util.Collection;

/**
 * Abstract stateless EJB implementation of a JPA CUD service implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractJpaService implements JpaCudService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(AbstractJpaService.class);

    /**
     * The persistence unit identifier common to all service implementations.
     */
    public static final String SERVICE_PERSISTENCE_UNIT = "services_PU";

    // Internal state
    /**
     * The per-call standard injected EntityManager.
     */
    @PersistenceContext(name = AbstractJpaService.SERVICE_PERSISTENCE_UNIT)
    protected EntityManager entityManager;

    /**
     * JPA does not handle null or empty collection parameters gracefully.
     * Hence the need for this operation, which retrieves the initial (before padding)
     * size of the supplied aCollection.
     *
     * @param aCollection A collection which may need padding.
     * @param padObject   An object added to aCollection only if {@code aCollection.isEmpty()}.
     * @param <T>         The type of element retrieved (and also to pad).
     * @return The size of the supplied aCollection <strong>before</strong> any padding took place.
     */
    public static <T> int padAndGetSize(final Collection<T> aCollection, final T padObject) {

        // Check sanity
        Validate.notNull(aCollection, "aCollection");

        if (aCollection.isEmpty()) {

            // Pad the collection
            aCollection.add(padObject);
            return 0;
        }

        // No need to pad the Collection. Simply return its size.
        return aCollection.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void create(final Object toCreate) throws PersistenceOperationFailedException {

        try {
            // Persist the T instance
            entityManager.persist(toCreate);
        } catch (Exception e) {
            logAndThrowPersistenceOperationFailedException("persist", toCreate, e);
        }
    }

    /**
     * Ensures that the supplied toUpdate entity is merged back to the EntityManager, if it is in a
     * detached state.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public <T extends NazgulEntity> T update(final T toUpdate) throws PersistenceOperationFailedException {

        try {

            final Class<T> nazgulEntitySubclass = (Class<T>) toUpdate.getClass();
            T managedEntity = findByPrimaryKey(nazgulEntitySubclass, toUpdate.getId());

            // Versions OK?
            final long managedVersion = managedEntity.getVersion();
            final long toUpdateVersion = toUpdate.getVersion();
            if (toUpdateVersion != managedVersion) {
                final String message = "JPA version mismatch for type [" + nazgulEntitySubclass.getSimpleName()
                        + "]. Received version [" + toUpdate.getVersion() + "], managed version [" + managedEntity
                        .getVersion() + "]";
                throw new IllegalStateException(message);
            }

            // Merge the properties of the supplied toUpdate instance.
            if (!entityManager.contains(toUpdate)) {

                if (log.isDebugEnabled()) {
                    log.debug("Non-managed entity of type [" + nazgulEntitySubclass.getName() + "] requires merge "
                            + "into the current EntityManager. Performing it.");
                }
                managedEntity = entityManager.merge(toUpdate);
            } else {

                if (log.isDebugEnabled()) {
                    log.debug("Managed entity of type [" + nazgulEntitySubclass.getName() + "] requires no JPA merge.");
                }
            }

            // All done.
            return managedEntity;

        } catch (Exception e) {
            logAndThrowPersistenceOperationFailedException("update", toUpdate, e);
        }

        // This should never happen.
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public <T extends NazgulEntity> T findByPrimaryKey(final Class<T> entityType, final long primaryKey)
            throws PersistenceOperationFailedException {

        // Check sanity
        Validate.notNull(entityType, "entityType");

        try {
            return entityManager.find(entityType, primaryKey);
        } catch (Exception e) {
            logAndThrowPersistenceOperationFailedException("findByPrimaryKey", entityType, e);
        }

        // This should never happen.
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void delete(final Object toDelete) throws PersistenceOperationFailedException {

        try {
            // Remove the T instance
            entityManager.remove(toDelete);
        } catch (Exception e) {
            logAndThrowPersistenceOperationFailedException("delete", toDelete, e);
        }
    }

    /**
     * Surrounds the originalParameter with '%' characters, implying that the original parameter will be able to be
     * used within a SQL LIKE statement. Null originalParameters yield a "%" response.
     *
     * @param originalParameter The parameter to surround.
     * @return The JPQL-LIKE-ready parameter.
     */
    protected String makeLikeParameter(final String originalParameter) {

        // Null parameters yield '%'
        if (originalParameter == null || "%".equalsIgnoreCase(originalParameter)) {
            return "%";
        }

        // Surround the parameter value with %'s if required.
        return "%" + originalParameter.trim() + "%";
    }

    /**
     * Retrieves a message detailing the Constraints which were violated.
     *
     * @param ex The ConstraintViolationException wrapping the constraint problems.
     * @return A formatted string containing the error message(s).
     */
    protected String getConstraintViolationErrorMessage(final ConstraintViolationException ex) {

        final StringBuilder builder = new StringBuilder();

        ex.getConstraintViolations()
                .stream()
                .map(cv -> {

                    // Synthesize a message
                    final String className = cv.getRootBeanClass().getSimpleName();
                    final String property = cv.getPropertyPath().toString();
                    final String message = cv.getMessage();
                    //Object invalidValue = violation.getInvalidValue();

                    return String.format("%s.%s %s", className, property, message);

                })
                .forEach(error -> builder.append(error).append("\n"));

        // All Done.
        return builder.toString();
    }

    //
    // Private helpers
    //

    private void logAndThrowPersistenceOperationFailedException(final String operation,
                                                                final Object toCreateOrClass,
                                                                final Exception e) {

        // Log somewhat
        String toCreateClass = "<null>";
        if (toCreateOrClass != null) {
            toCreateClass = toCreateOrClass instanceof Class
                    ? ((Class) toCreateOrClass).getName()
                    : toCreateOrClass.getClass().getName();
        }
        String msg = "Could not " + operation + " object of type [" + toCreateClass + "]";

        if (e instanceof OptimisticLockException) {
            final OptimisticLockException ole = (OptimisticLockException) e;
            msg += " Entity (" + ole.getEntity() + "), Cause: " + ole.getCause();
        }
        log.error(msg, e);

        // Rethrow
        throw new PersistenceOperationFailedException(msg, e);
    }
}
