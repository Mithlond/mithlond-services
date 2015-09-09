package se.mithlond.services.content.impl.ejb;

import se.mithlond.services.content.api.NavigationService;
import se.mithlond.services.content.api.navigation.transport.MenuStructure;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.membership.Membership;
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

    // Internal state
    @PersistenceContext
    private EntityManager entityManager;

    /**
     * {@inheritDoc}
     */
    @Override
    protected EntityManager getEntityManager() {
        return null;
    }

    @Override
    public MenuStructure getMenuStructure(final Organisation menuOwner, final List<Membership> callersMemberships) {
        return null;
    }
}
