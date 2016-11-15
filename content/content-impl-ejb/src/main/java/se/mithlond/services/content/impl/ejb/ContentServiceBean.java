/*
 * #%L
 * Nazgul Project: mithlond-services-content-impl-ejb
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
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

import se.mithlond.services.content.api.ContentService;
import se.mithlond.services.content.model.ContentPatterns;
import se.mithlond.services.content.model.articles.Article;
import se.mithlond.services.content.model.transport.articles.Articles;
import se.mithlond.services.content.model.transport.articles.ContentPaths;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.authorization.api.GlobAuthorizationPattern;
import se.mithlond.services.shared.authorization.api.SimpleAuthorizer;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPathProducer;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.jaxb.ErrorCode;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Default ContentService POJO implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ContentServiceBean extends AbstractJpaService implements ContentService {

    // Internal state
    private static final SimpleAuthorizer AUTHORIZER = SimpleAuthorizer.getInstance();

    /**
     * {@inheritDoc}
     */
    @Override
    public ContentPaths getContentPaths(
            final Long owningOrganisationID,
            final SemanticAuthorizationPathProducer caller,
            final LocalDate endDate,
            final Period period,
            final Long maxResults,
            final Long startAtIndex) {

        final ContentPaths toReturn = new ContentPaths();

        if (owningOrganisationID != null && caller != null) {

            try {

                // Find the organisation
                final Organisation org = CommonPersistenceTasks.getOrganisation(entityManager, owningOrganisationID);

                // Set the realm to be the organisation name
                toReturn.setRealm(org.getOrganisationName());

                // Authorize the call within this service.
                final SortedSet<GlobAuthorizationPattern> authPatterns = new TreeSet<>();
                authPatterns.add(new GlobAuthorizationPattern(org.getOrganisationName(), GlobAuthorizationPattern.ANY));

                if (AUTHORIZER.isAuthorized(authPatterns, caller.getPaths())) {

                    // Calculate the effective interval
                    final LocalDate intervalStart = endDate == null
                            ? LocalDate.now(TimeFormat.SWEDISH_TIMEZONE)
                            : endDate;
                    final LocalDate intervalEnd = CommonPersistenceTasks.getStartTimeFrom(intervalStart, period);

                    // Find the paths available
                    final List<String> pathsFound = entityManager.createNamedQuery(
                            Article.NAMEDQ_GET_BY_ORGANISATION_ID_AND_CONTENT_PATH, String.class)
                            .setParameter(ContentPatterns.PARAM_INTERVAL_START, intervalStart)
                            .setParameter(ContentPatterns.PARAM_INTERVAL_END, intervalEnd)
                            .setParameter(OrganisationPatterns.PARAM_ORGANISATION_ID, org.getId())
                            .setMaxResults(getEffective(maxResults).intValue())
                            .setFirstResult(getOffset(startAtIndex).intValue())
                            .getResultList();

                    if (pathsFound != null) {
                        toReturn.getContentPaths().addAll(pathsFound);
                    }
                } else {

                    // Notify the user.
                    toReturn.setError(ErrorCode.UNAUTHORIZED, "" + org.getOrganisationName());
                }

            } catch (Exception e) {
                toReturn.setError(ErrorCode.INTERNAL_SERVER_ERROR, "" + e);
            }
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Articles getArticles(final Long owningOrganisationID,
            final SemanticAuthorizationPathProducer caller,
            final LocalDate endDate,
            final Period period,
            final Long maxResults,
            final Long startAtIndex) {

        final Articles toReturn = new Articles();

        if (owningOrganisationID != null && caller != null) {

            try {

                // Find the organisation
                final Organisation org = CommonPersistenceTasks.getOrganisation(entityManager, owningOrganisationID);
                toReturn.setRealm(org.getOrganisationName());

                // Authorize the call within this service.
                final SortedSet<GlobAuthorizationPattern> authPatterns = new TreeSet<>();
                authPatterns.add(new GlobAuthorizationPattern(org.getOrganisationName(), GlobAuthorizationPattern.ANY));

                if (AUTHORIZER.isAuthorized(authPatterns, caller.getPaths())) {


                    final List<Article> articleResults = entityManager.createNamedQuery(
                            Article.NAMEDQ_GET_BY_CREATION_DATE_FOR_ORGANISATION, Article.class)
                            .setParameter(OrganisationPatterns.PARAM_ORGANISATION_ID, org.getId())
                            .setMaxResults(getEffective(maxResults).intValue())
                            .setFirstResult(getOffset(startAtIndex).intValue())
                            .getResultList();

                    if (articleResults != null && !articleResults.isEmpty()) {
                        toReturn.getArticleList().addAll(articleResults);
                    }

                } else {

                    // Notify the user.
                    toReturn.setError(ErrorCode.UNAUTHORIZED, "" + org.getOrganisationName());
                }

            } catch (Exception e) {
                toReturn.setError(ErrorCode.INTERNAL_SERVER_ERROR, "" + e);
            }
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Articles getArticles(final ContentPaths contentPaths,
            final SemanticAuthorizationPathProducer caller,
            final Long maxResults,
            final Long startAtIndex) {

        final Articles toReturn = new Articles();

        if (contentPaths != null && contentPaths.getContentPaths() != null) {

            try {

                // Extract the data from the ContentPaths
                final String organisationName = contentPaths.getRealm();
                final List<String> thePaths = contentPaths.getContentPaths();

                // Find the organisation
                final Organisation org = CommonPersistenceTasks.getOrganisation(entityManager, organisationName);
                toReturn.setRealm(org.getOrganisationName());

                // Authorize the call within this service.
                final SortedSet<GlobAuthorizationPattern> authPatterns = new TreeSet<>();
                authPatterns.add(new GlobAuthorizationPattern(org.getOrganisationName(), GlobAuthorizationPattern.ANY));

                if (AUTHORIZER.isAuthorized(authPatterns, caller.getPaths())) {

                    final List<Article> articleResults = entityManager.createNamedQuery(
                            Article.NAMEDQ_GET_BY_ORGANISATION_ID_AND_CONTENT_PATH, Article.class)
                            .setParameter(OrganisationPatterns.PARAM_ORGANISATION_ID, org.getId())
                            .setParameter(ContentPatterns.PARAM_CONTENT_PATHS, thePaths)
                            .setMaxResults(getEffective(maxResults).intValue())
                            .setFirstResult(getOffset(startAtIndex).intValue())
                            .getResultList();

                    if (articleResults != null && !articleResults.isEmpty()) {
                        toReturn.getArticleList().addAll(articleResults);
                    }

                } else {

                    // Notify the user.
                    toReturn.setError(ErrorCode.UNAUTHORIZED, "" + org.getOrganisationName());
                }

            } catch (Exception e) {
                toReturn.setError(ErrorCode.INTERNAL_SERVER_ERROR, "" + e);
            }
        }

        // All Done.
        return toReturn;
    }

    //
    // Private helpers
    //

    private static Long getOffset(final Long receivedValue) {
        return receivedValue == null ? 0L : Math.abs(receivedValue);
    }

    private static Long getEffective(
            final Long receivedValue,
            final long defaultMin,
            final long defaultMax) {

        return receivedValue == null
                ? defaultMax
                : Math.max(defaultMin, Math.min(defaultMax, receivedValue));
    }

    private static Long getEffective(final Long receivedValue) {
        return getEffective(receivedValue, 40, 5);
    }
}
