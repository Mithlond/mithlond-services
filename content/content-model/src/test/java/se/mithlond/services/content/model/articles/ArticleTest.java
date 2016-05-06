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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbUtils;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.content.model.ContentPatterns;
import se.mithlond.services.content.model.transport.articles.Articles;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventLocator;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ArticleTest extends AbstractPlainJaxbTest {

    // Shared state
    private Locale defaultLocale;
    private List<Article> articleList;
    private Marshaller marshaller;
    private Unmarshaller unmarshaller;
    private SortedMap<Integer, String> contentMap = new TreeMap<>();
    private Organisation organisation;
    private Address address;

    private static String getEventSeverity(final int eventSeverity) {
        switch (eventSeverity) {
            case ValidationEvent.ERROR:
                return "ERROR";

            case ValidationEvent.FATAL_ERROR:
                return "FATAL ERROR";

            case ValidationEvent.WARNING:
                return "WARNING";

            default:
                return "UNKNOWN [" + eventSeverity + "]";
        }
    }

    @Before
    public void setupSharedState() {

        // Use Moxy as the JAXB implementation
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        // Stash the locale
        defaultLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        articleList = new ArrayList<>();

        // Create the JAXBContext and related objects.
        try {
            final JAXBContext ctx = JAXBContext.newInstance(Articles.class, Article.class, Markup.class);
            final JaxbNamespacePrefixResolver prefixResolver = new JaxbNamespacePrefixResolver();
            prefixResolver.put(ContentPatterns.NAMESPACE, "content");
            marshaller = JaxbUtils.getHumanReadableStandardMarshaller(ctx, prefixResolver, false);
            unmarshaller = ctx.createUnmarshaller();


            marshaller.setEventHandler(event -> {

                final StringBuffer buffer = new StringBuffer(" [" + getEventSeverity(event.getSeverity()) + "] ");
                final ValidationEventLocator locator = event.getLocator();
                if (locator != null) {
                    buffer.append(locator.getLineNumber() + "|" + locator.getColumnNumber() + "]\n");
                } else {
                    buffer.append(" No location available.\n");
                }
                buffer.append("\t" + event.getMessage());

                System.err.println(buffer.toString());

                final Throwable ex = event.getLinkedException();
                if (ex != null) {
                    ex.printStackTrace(System.err);
                }

                return true;
            });
        } catch (JAXBException e) {
            throw new IllegalStateException("Could not create JAXBContext related objects", e);
        }

        final LocalDateTime theLocalDate = LocalDateTime.of(2014, Month.APRIL, 2, 3, 4);
        final ZonedDateTime baseDate = ZonedDateTime.of(theLocalDate, TimeFormat.SWEDISH_TIMEZONE);

        address = new Address("careOfLine", "departmentName", "street", "number",
                "city", "zipCode", "country", "description");
        organisation = new Organisation("name",
                "suffix",
                "phone",
                "bankAccountInfo",
                "postAccountInfo",
                address,
                "emailSuffix",
                TimeFormat.SWEDISH_TIMEZONE,
                TimeFormat.SWEDISH_LOCALE);

        for (int i = 0; i < 10; i++) {

            // Create some mock content
            final String content = "<div foo='bar'>\n  <strong>ArticleTitle_" + i
                    + "</strong>\n  <content>content_" + i + "åäöÅÄÖëü</content>\n</div>";
            contentMap.put(i, content);

            // Create the Article
            articleList.add(new Article(
                    "title_" + i,
                    "author_" + i,
                    baseDate.plusHours(i),
                    content,
                    organisation));
        }
    }

    @After
    public void teardownSharedState() {
        Locale.setDefault(defaultLocale);
        System.clearProperty("javax.xml.bind.context.factory");
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/articles.xml");
        final Articles toMarshal = new Articles();
        toMarshal.getArticleList().addAll(this.articleList);

        // Act
        final StringWriter out = new StringWriter();
        marshaller.marshal(toMarshal, out);
        // System.out.println("Got: " + out.toString());

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, out.toString()).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/articles.xml");
        // System.out.println("Got: " + data.toString());

        // Act
        final Object result = unmarshaller.unmarshal(new StringReader(data));

        // Assert
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof Articles);

        final List<Article> articles = ((Articles) result).getArticleList();
        Assert.assertEquals(10, articles.size());

        final Article firstArticle = articles.get(0);
        Assert.assertEquals("title_0", firstArticle.getTitle());
        Assert.assertEquals("author_0", firstArticle.getAuthor());
        Assert.assertNull(firstArticle.getLastModified());
        Assert.assertEquals(
                contentMap.get(0).replaceAll("\\p{Space}", ""),
                firstArticle.getMarkup().trim().replaceAll("\\p{Space}", "").replaceAll("\"", "\\'"));
    }
}
