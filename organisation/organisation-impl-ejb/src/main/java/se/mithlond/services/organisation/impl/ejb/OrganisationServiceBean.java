package se.mithlond.services.organisation.impl.ejb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.organisation.api.OrganisationService;
import se.mithlond.services.organisation.api.parameters.GroupIdSearchParameters;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.ejb.Stateless;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * OrganisationService Stateless EJB implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Stateless
public class OrganisationServiceBean extends AbstractJpaService implements OrganisationService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(OrganisationServiceBean.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Organisation> getOrganisations() {

        return entityManager.createNamedQuery(Organisation.NAMEDQ_GET_ALL, Organisation.class)
                .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Organisation> getOrganisation(@NotNull final String organisationName) {

        final List<Organisation> resultList = entityManager.createNamedQuery(
                Organisation.NAMEDQ_GET_BY_NAME, Organisation.class)
                .setParameter(Patterns.PARAM_ORGANISATION_NAME, organisationName)
                .getResultList();

        if(resultList == null || resultList.isEmpty()) {

            // Warn some.
            log.warn("Found no organisations for the name [" + organisationName + "]");

            // All done.
            return Optional.empty();
        } else if (resultList.size() > 1) {

            // This should really never happen...
            log.warn("Found [" + resultList.size() + "] organisations with the name ["
                    + organisationName + "]. Returning the first one.");
        }

        // All done.
        return Optional.of(resultList.get(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Group> getGroups(@NotNull final GroupIdSearchParameters searchParameters) {

        final List<Group> toReturn = new ArrayList<>();

        final int groupIDsSize = AbstractJpaService.padAndGetSize(searchParameters.getGroupIDs(), 0L);
        final int organisationIDsSize = AbstractJpaService.padAndGetSize(searchParameters.getOrganisationIDs(), 0L);
        final int classifierIDsSize = AbstractJpaService.padAndGetSize(searchParameters.getClassifierIDs(), 0L);

        final List<Long> groupIDs = searchParameters.getGroupIDs();
        final List<Long> organisationIDs = searchParameters.getOrganisationIDs();
        final List<Long> classifierIDs = searchParameters.getClassifierIDs();

        entityManager.createNamedQuery(Group.NAMEDQ_GET_BY_SEARCHPARAMETERS, Group.class)
                .setParameter(Patterns.PARAM_NUM_GROUPIDS, groupIDsSize)
                .setParameter(Patterns.PARAM_GROUP_IDS, groupIDs)
                .setParameter(Patterns.PARAM_NUM_ORGANISATIONIDS, organisationIDsSize)
                .setParameter(Patterns.PARAM_ORGANISATION_IDS, organisationIDs)


        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CategorizedAddress> getCategorizedAddresses(@NotNull final GroupIdSearchParameters searchParameters) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Category> getCategoriesByClassification(@NotNull final String classification) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CategorizedAddress updateCategorizedAddress(final CategorizedAddress toUpdate) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CategorizedAddress createCategorizedActivityAddress(final String shortDesc, final String fullDesc, final Address address, final String category, final String organisation) {
        return null;
    }
}
