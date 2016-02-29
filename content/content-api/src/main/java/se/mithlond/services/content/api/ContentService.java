/*
 * #%L
 * Nazgul Project: mithlond-services-content-api
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
     * @throws UnknownOrganisationException if the menuOwner was not the name of an existing organisation.
     */
    Articles getArticles(final String contentOwner,
                         final String contentSelectionPath,
                         final List<SemanticAuthorizationPathProducer> callersAuthPaths)
            throws UnknownOrganisationException;
}
