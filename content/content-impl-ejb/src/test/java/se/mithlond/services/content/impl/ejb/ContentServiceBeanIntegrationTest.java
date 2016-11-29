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

import org.junit.Assert;
import org.junit.Test;
import se.mithlond.services.content.model.articles.Article;
import se.mithlond.services.content.model.articles.Section;
import se.mithlond.services.content.model.transport.articles.Articles;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.membership.Membership;

import java.time.LocalDate;
import java.time.Month;
import java.time.Period;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ContentServiceBeanIntegrationTest extends AbstractContentIntegrationTest {

    // Shared state
    private ContentServiceBean unitUnderTest;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCustomSetup() {

        // First, handle the standard setup.
        super.doCustomSetup();

        // Create the test unit
        unitUnderTest = new ContentServiceBean();

        // Inject the EntityManager connected to the in-memory DB.
        injectEntityManager(unitUnderTest);
    }

    @Test
    public void validateFindingArticlesByContentPaths() throws Exception {

        // Assemble
        final Membership mifflondBilbo = entityManager.createNamedQuery(
                Membership.NAMEDQ_GET_BY_ALIAS_ORGANISATION, Membership.class)
                .setParameter(OrganisationPatterns.PARAM_ALIAS, "Bilbo Baggins")
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, "Mifflond")
                .getSingleResult();
        Assert.assertNotNull(mifflondBilbo);

        // System.out.println("Got: " + extractFlatXmlDataSet(iDatabaseConnection.createDataSet()));

        // Act
        final Articles foundArticles = unitUnderTest.getArticles(MIFFLOND_JPA_ID,
                mifflondBilbo,
                LocalDate.of(2016, Month.OCTOBER, 1),
                Period.ofMonths(4),
                20L,
                0L);

        // Assert
        Assert.assertEquals("Mifflond", foundArticles.getRealm());
        Assert.assertEquals(2, foundArticles.getArticleList().size());

        final Article hotNewsArticle = foundArticles.getArticleList().stream()
                .filter(article -> article.getContentPath().contains("news/hot"))
                .findFirst()
                .orElse(null);

        Assert.assertNotNull(hotNewsArticle);

        final List<Section> sections = hotNewsArticle.getSections();
        Assert.assertEquals(5, sections.size());

        for(int i = 1; i <= sections.size(); i++) {
            Assert.assertTrue(sections.get(i-1).getHeading().equals("This is section " + i));
        }
    }
}
