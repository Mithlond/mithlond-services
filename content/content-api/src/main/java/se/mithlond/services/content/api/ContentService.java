package se.mithlond.services.content.api;

import se.mithlond.services.content.api.transport.Articles;
import se.mithlond.services.shared.authorization.api.SemanticAuthorizationPathProducer;

import java.util.List;

/**
 * Service specification for retrieving content to clients. For the purpose of Mithlond services,
 * "content" represents Articles of various kinds. Such Articles should be rendered on the client in
 * question, either in the form of Markup or within data structures relevant to the client type.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface ContentService {

    /**
     * Retrieves Articles from the named owner, and identified by the content selection path.
     *
     * @param contentOwner         The organisation owning the site for which a MenuStructure should be retrieved.
     * @param contentSelectionPath A resource path defining which Articles should be retrieved. Articles are
     *                             typically organized in a tree-like resource structure.
     * @param callersAuthPaths     The SemanticAuthorizationPathProducer of the caller, used to determine which
     *                             Articles should be retrieved. Typically, each authPath is a Membership.
     * @return An Articles transport holder containing the Articles matching the supplied owner,
     * selectionPath and authentication paths.
     */
    Articles getArticles(final String contentOwner, final String contentSelectionPath,
            final List<SemanticAuthorizationPathProducer> callersAuthPaths);
}
