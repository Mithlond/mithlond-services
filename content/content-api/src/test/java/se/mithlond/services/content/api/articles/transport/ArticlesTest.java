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
package se.mithlond.services.content.api.articles.transport;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbUtils;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.content.api.AbstractEntityTest;
import se.mithlond.services.content.api.transport.Articles;
import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.content.model.articles.Article;
import se.mithlond.services.content.model.articles.Markup;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ArticlesTest {

    // Shared state
    private String realm;
    private Articles articles;
    private String content1, content2;

    private Marshaller marshaller;
    private Unmarshaller unmarshaller;

    @Before
    public void setupSharedState() {

        // Use Moxy as the JAXB implementation
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        try {
            final JAXBContext ctx = JAXBContext.newInstance(Articles.class, Article.class, Markup.class);
            final JaxbNamespacePrefixResolver prefixResolver = new JaxbNamespacePrefixResolver();
            prefixResolver.put(Patterns.NAMESPACE, "content");
            marshaller = JaxbUtils.getHumanReadableStandardMarshaller(ctx, prefixResolver, false);
            unmarshaller = ctx.createUnmarshaller();
        } catch (JAXBException e) {
            throw new IllegalStateException("Could not setup JAXB marshaller/unmarshaller", e);
        }

        realm = "FooBar";
        List<Article> articleList = new ArrayList<>();
        articles = new Articles(realm, "/news/latest", articleList);

        // Create some arbitrary content
        content1 = "<div><header>foo</header><body>bar!</body></div>";
        content2 = "<div><header>foo2</header><body>bar2!</body></div>";

        // Populate the Articles.
        articleList.add(new Article("News_1",
                                    "ERF Häxxxxmästaren",
                                    ZonedDateTime.of(2015, 12, 12, 5, 2, 3, 0, TimeFormat.SWEDISH_TIMEZONE),
                                    content1));
        articleList.add(new Article("News_2",
                                    "ERF Häxxmästaren",
                                    ZonedDateTime.of(2015, 11, 11, 15, 22, 33, 0, TimeFormat.SWEDISH_TIMEZONE),
                                    content2));
    }

    @After
    public void teardownSharedState() {
        System.clearProperty("javax.xml.bind.context.factory");
    }


    @Test
    public void validateMarshallingToXML() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/articles.xml");
        final StringWriter out = new StringWriter();

        // Act
        marshaller.marshal(articles, out);
        final String result = out.toString();
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateMarshallingToJSon() throws Exception {

        // Assemble
        marshaller.setProperty(MarshallerProperties.MEDIA_TYPE, "application/json");
        marshaller.setProperty(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
        marshaller.setProperty(MarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");

        final String expected = XmlTestUtils.readFully("testdata/articles.json");
        final StringWriter out = new StringWriter();

        // Act
        marshaller.marshal(articles, out);
        final String result = out.toString();
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertEquals(expected.replaceAll("\\s+", ""), result.replaceAll("\\s+", ""));
    }


    @Test
    public void validateUnmarshallingFromXML() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/articles.xml");
        final Source source = new StreamSource(new StringReader(data));

        // Act
        final Articles resurrected = unmarshaller.unmarshal(source, Articles.class).getValue();

        // Assert
        Assert.assertNotNull(resurrected);
        Assert.assertEquals(realm, resurrected.getRealm());

        final List<Article> articleList = resurrected.getArticleList();
        Assert.assertEquals(2, articleList.size());
        Assert.assertEquals(content1, articleList.get(0).getMarkup().replaceAll("\\s+", ""));
        Assert.assertEquals("ERF Häxxxxmästaren", articleList.get(0).getAuthor());
    }

    @Test
    public void validateUnmarshallingFromJSON() throws Exception {

        // Assemble
        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
        unmarshaller.setProperty(UnmarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
        unmarshaller.setProperty(UnmarshallerProperties.JSON_NAMESPACE_PREFIX_MAPPER, true);
        unmarshaller.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");

        final String data = XmlTestUtils.readFully("testdata/articles.json");
        final Source source = new StreamSource(new StringReader(data));

        // Act
        final Articles resurrected = unmarshaller.unmarshal(source, Articles.class).getValue();

        // Assert
        Assert.assertNotNull(resurrected);
        Assert.assertEquals(realm, resurrected.getRealm());

        final List<Article> articleList = resurrected.getArticleList();
        Assert.assertEquals(2, articleList.size());
        Assert.assertEquals(content1, articleList.get(0).getMarkup().replaceAll("\\s+", ""));
        Assert.assertEquals(content2, articleList.get(1).getMarkup().replaceAll("\\s+", ""));
        Assert.assertEquals("ERF Häxxxxmästaren", articleList.get(0).getAuthor());
    }
}
