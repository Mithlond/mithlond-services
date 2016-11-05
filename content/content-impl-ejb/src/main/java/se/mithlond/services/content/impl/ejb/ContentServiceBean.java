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
import se.mithlond.services.content.model.transport.articles.Articles;
import se.mithlond.services.content.model.transport.articles.ContentPaths;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.shared.authorization.api.SemanticAuthorizationPathProducer;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

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
    public ContentPaths getContentPaths(final Long owningOrganisationID,
                                        final SemanticAuthorizationPathProducer caller,
                                        final LocalDate startingDate,
                                        final Period period) {

        final ContentPaths toReturn = new ContentPaths();

        if (owningOrganisationID != null && caller != null) {

            // Calculate the effective interval
            final LocalDate effStartDate = startingDate == null
                    ? LocalDate.now(TimeFormat.SWEDISH_TIMEZONE)
                    : startingDate;
            final Period effPeriod = period == null
                    ? Period.ofYears(1)
                    : period;

            // Find the organisation
            final Organisation org = entityManager.find(Organisation.class, owningOrganisationID);
            if(org != null) {

                final List<String> authPaths = caller.getPaths()
                        .stream()
                        .map(SemanticAuthorizationPath::getPath)
                        .collect(Collectors.toList());

                // Find the paths available

            }
        }

        // Do we have an owning organisation?

        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Articles getArticles(final Long owningOrganisationID,
                                final String contentPath,
                                final SemanticAuthorizationPathProducer caller,
                                final LocalDate startingDate,
                                final Period period) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Articles getArticles(final ContentPaths contentPaths,
                                final SemanticAuthorizationPathProducer caller,
                                final LocalDate startingDate,
                                final Period period) {
        return null;
    }
}
