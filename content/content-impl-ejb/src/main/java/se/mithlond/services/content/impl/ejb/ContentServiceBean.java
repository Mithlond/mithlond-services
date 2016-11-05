package se.mithlond.services.content.impl.ejb;

import se.mithlond.services.content.api.ContentService;
import se.mithlond.services.content.api.UnknownOrganisationException;
import se.mithlond.services.content.model.articles.Article;
import se.mithlond.services.content.model.transport.articles.Articles;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.shared.authorization.api.SemanticAuthorizationPathProducer;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import java.util.List;

/**
 * Default ContentService POJO implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ContentServiceBean extends AbstractJpaService implements ContentService {

    /**
     * {@inheritDoc}
     */
    @Override
    public Articles getArticles(final String contentOwner,
                                final String contentSelectionPath,
                                final List<SemanticAuthorizationPathProducer> callersAuthPaths)
            throws UnknownOrganisationException {

        // First, find the organisation
        final Organisation organisation = CommonPersistenceTasks.getOrganisation(entityManager, contentOwner);

        Article.NAMEDQ_GET_BY_ORGANISATION_AND_LAST_MODIFICATION_DATE

        return null;
    }
}
