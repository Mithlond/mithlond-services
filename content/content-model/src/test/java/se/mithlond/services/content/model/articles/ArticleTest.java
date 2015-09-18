/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
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
package se.mithlond.services.content.model.articles;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.mithlond.services.content.model.articles.helpers.Articles;
import se.mithlond.services.content.model.navigation.AbstractEntityTest;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ArticleTest extends AbstractEntityTest {

    // Shared state
    private Locale defaultLocale;
    private List<Article> articleList;

    @Before
    public void setupSharedState() {
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        articleList = new ArrayList<>();

        final LocalDateTime theLocalDate = LocalDateTime.of(2014, Month.APRIL, 2, 3, 4);
        final ZonedDateTime baseDate = ZonedDateTime.of(theLocalDate, TimeFormat.SWEDISH_TIMEZONE);

        for (int i = 0; i < 10; i++) {
            articleList.add(new Article(
                    "title_" + i,
                    "author_" + i,
                    baseDate.plusHours(i),
                    "<div>content_" + i + "</div>"
                    // "content_" + i
            ));
        }

        jaxb.add(Articles.class);
    }

    @After
    public void teardownSharedState() {
        Locale.setDefault(defaultLocale);
    }

    @Ignore("Still working on the DomAdapter")
    @Test
    public void validateMarshalling() {

        // Assemble
        final Articles toMarshal = new Articles();
        toMarshal.getArticles().addAll(this.articleList);

        // Act
        final String result = marshal(toMarshal);
        System.out.println("Got: " + result);

        // Assert
    }
}
