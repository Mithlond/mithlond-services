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

import se.jguru.nazgul.core.persistence.model.NazgulEntity;

/**
 * Standard specification for services which should expose a standard (and generic)
 * interface for performing CUD (Create/Update/Delete) JPA operations.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface JpaCudService {

    /**
     * Creates / persists the provided entity within the active PersistenceContext.
     *
     * @param toCreate The entity to persist.
     * @throws PersistenceOperationFailedException if the entity could not be created.
     */
    void create(Object toCreate) throws PersistenceOperationFailedException;

    /**
     * Merges the supplied instance into the active PersistenceContext if it is detached,
     * implying that the supplied instance will be updated within the underlying database
     * when the JPA transaction ends.
     * <p/>
     * <strong>Note!</strong> As detailed by the JPA specification, the instance submitted
     * update should not be used after the update has been performed. Instead, use the returned
     * instance instead.
     *
     * @param toUpdate The entity to update into the active PersistenceContext, if required.
     * @param <T>      The type of instance to Merge.
     * @return The merged (i.e. re-attached) instance, which should be used instead
     * of the supplied argument.
     * @throws PersistenceOperationFailedException if the update operation failed.
     */
    <T extends NazgulEntity> T update(T toUpdate) throws PersistenceOperationFailedException;

    /**
     * Deletes the supplied object from the active PersistenceContext, implying deletion
     * from the underlying database upon transaction completion. While implementing subclasses
     * define transaction management semantics, the deleted entity argument can not be assumed
     * to be available within the underlying storage after this method has completed.
     *
     * @param toDelete The entity to delete.
     * @throws PersistenceOperationFailedException if the entity could not be deleted.
     */
    void delete(Object toDelete) throws PersistenceOperationFailedException;

    /**
     * Retrieves an Entity given its primary key.
     *
     * @param entityType The type of entity to be retrieved.
     * @param primaryKey The primary key of the supplied entity-
     * @param <T>        The type of Entity to retrieve.
     * @return The Entity with the supplied primaryKey.
     * @throws PersistenceOperationFailedException if the Entity could not be retrieved.
     */
    <T extends NazgulEntity> T findByPrimaryKey(final Class<T> entityType, final long primaryKey)
            throws PersistenceOperationFailedException;
}
