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

import se.mithlond.services.content.api.NavigationService;
import se.mithlond.services.content.api.navigation.transport.MenuStructure;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

/**
 * Standard stateless NavigationService POJO implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Stateless
public class MenuNavigation extends AbstractJpaService implements NavigationService {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * {@inheritDoc}
     */
    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MenuStructure getMenuStructure(final Organisation menuOwner, final List<Membership> callersMemberships) {

        // Check sanity
        Validate.notNull(menuOwner, "menuOwner");
        Validate.notNull(callersMemberships, "callersMemberships");

        // Read the raw menu data from the DB.
        // entityManager.createNamedQuery()

        // Populate the MenuStructure, caring for authorization.
        final MenuStructure toReturn = new MenuStructure(menuOwner.getOrganisationName());

        // All done.
        return toReturn;
    }

    //
    // Private helpers
    //
}
