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

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.persistence.Query;

/**
 * Abstract JPA CUD service implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractJpaService implements JpaCudService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(AbstractJpaService.class);

    /**
     * The persistence unit identifier common to all service implementations.
     */
    public static final String SERVICE_PERSISTENCE_UNIT = "mithlond_PU";

    /**
     * <p>Internal helper which retrieves the EntityManager from the implementing subclass.
     * The specs for the subclass using PersistenceUnit with a {@code JTA}-style transaction
     * management (i.e.{@code &lt;persistence-unit transaction-type="JTA"&gt;}) implies that
     * the container will do EntityManager (PersistenceContext/Cache) creating and tracking.</p>
     * <p>Typically, the EntityManager should be injected by the EJB container:</p>
     * <pre>
     *     <code>
     *
     *         // Inject an EntityManager
     *         &#064;PersistenceContext(name = ...someName...)
     *         private EntityManager entityManager;
     *
     *         // Expose the EntityManager to the superclass.
     *         &#064;Override
     *         protected EntityManager getEntityManager() {
     *              return entityManager;
     *         }
     *     </code>
     * </pre>
     * <h2>Rules for obtaining the EntityManager</h2>
     * <p>These rules are generic, for any Container-Managed EntityManager (in which case you cannot manage the
     * transactions on the EntityManager from the application):</p>
     * <ul>
     * <li>You cannot use the EntityManagerFactory to get an EntityManager</li>
     * <li>You can only get an EntityManager supplied by the container</li>
     * <li>An EntityManager can be injected via the @PersistenceContext annotation only (not @PersistenceUnit)</li>
     * <li>You are not allowed to use @PersistenceUnit to refer to a unit of type JTA</li>
     * <li>The EntityManager given by the container is a reference to the PersistenceContext/Cache
     * associated with a JTA Transaction.</li>
     * <li>If no JTA transaction is in progress, the EntityManager cannot be used because
     * there is no PersistenceContext/Cache.</li>
     * <li>Everyone with an EntityManager reference to the same unit in the same transaction will
     * automatically have a reference to the same PersistenceContext/Cache</li>
     * <li>The PersistenceContext/Cache is flushed and cleared at JTA commit time</li>
     * </ul>
     *
     * @return An EntityManager ready for use.
     */
    protected abstract EntityManager getEntityManager();

    /**
     * {@inheritDoc}
     */
    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void create(final Object toCreate) throws PersistenceOperationFailedException {

        try {
            // Persist the T instance
            getEntityManager().persist(toCreate);
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
            if(toUpdateVersion != managedVersion) {
                final String message = "JPA version mismatch for type [" + nazgulEntitySubclass.getSimpleName()
                        + "]. Received version [" + toUpdate.getVersion() + "], managed version [" + managedEntity
                        .getVersion() + "]";
                throw new IllegalStateException(message);
            }

            // Merge the properties of the supplied toUpdate instance.
            if (!getEntityManager().contains(toUpdate)) {

                if (log.isDebugEnabled()) {
                    log.debug("Non-managed entity of type [" + nazgulEntitySubclass.getName() + "] requires merge "
                            + "into the current EntityManager. Performing it.");
                }
                managedEntity = getEntityManager().merge(toUpdate);
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
        Validate.notNull(entityType, "Cannot handle null entityType argument.");

        try {
            return getEntityManager().find(entityType, primaryKey);
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
            getEntityManager().remove(toDelete);
        } catch (Exception e) {
            logAndThrowPersistenceOperationFailedException("delete", toDelete, e);
        }
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
