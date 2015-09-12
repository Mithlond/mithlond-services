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

        // Read the raw menu structure
        return null;
    }

    //
    // Private helpers
    //
}
