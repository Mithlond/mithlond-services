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

import se.mithlond.services.content.model.transport.articles.Articles;
import se.mithlond.services.content.model.transport.articles.ContentPaths;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPathProducer;

import java.time.LocalDate;
import java.time.Period;

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
     * @param owningOrganisationID The JPA ID of the organisation owning the Articles to be retrieved.
     * @param caller               The SemanticAuthorizationPathProducer of the caller, used to determine which
     *                             Articles should be retrieved.
     * @param endDate              The LocalDate marking the end of the interval for creation time
     *                             articles to search for.
     * @param period               The period of dates to use in order to define an interval within which to search
     *                             for Articles.
     * @param maxResults           The maximum results desired within the Articles wrapper.
     * @param startAtIndex         The index within the search to start with.
     * @return An Articles transport holder containing the Articles matching the supplied owner,
     * selectionPath and authentication paths.
     */
    Articles getArticles(final Long owningOrganisationID,
                         final SemanticAuthorizationPathProducer caller,
                         final LocalDate endDate,
                         final Period period,
                         final Long maxResults,
                         final Long startAtIndex);

    /**
     * Retrieves all articles matching the given ContentPaths for the supplied caller and with modification dates
     * within the supplied time interval.
     *
     * @param contentPaths The non-null ContentPaths for the articles to retrieve.
     * @param caller       The SemanticAuthorizationPathProducer of the caller, used to determine which
     *                     Articles should be retrieved.
     * @param maxResults   The maximum results desired within the Articles wrapper.
     * @param startAtIndex The index within the search to start with.
     * @return The Articles matching the supplied parameters.
     */
    Articles getArticles(final ContentPaths contentPaths,
                         final SemanticAuthorizationPathProducer caller,
                         final Long maxResults,
                         final Long startAtIndex);

    /**
     * Finds the ContentPaths for articles available within the supplied organisation and viewable/accessable
     * for the supplied caller.
     *
     * @param owningOrganisationID The JPA ID of the organisation owning the Articles on the ContentPaths
     *                             to be retrieved.
     * @param caller               The SemanticAuthorizationPathProducer of the caller, used to determine which
     *                             Articles should be retrieved.
     * @param endDate              The LocalDate marking the end of the interval for creation time
     *                             articles to search for.
     * @param period               The period of dates to use in order to define an interval within which
     *                             to search for Articles.
     * @param maxResults           The maximum results desired within the ContentPaths wrapper.
     * @param startAtIndex         The index within the search to start with.
     * @return A ContentPaths transport wrapper containing the requested ContentPaths.
     */
    ContentPaths getContentPaths(final Long owningOrganisationID,
                                 final SemanticAuthorizationPathProducer caller,
                                 final LocalDate endDate,
                                 final Period period,
                                 final Long maxResults,
                                 final Long startAtIndex);
}
