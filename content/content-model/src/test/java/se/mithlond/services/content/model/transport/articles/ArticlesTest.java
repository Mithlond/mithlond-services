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
package se.mithlond.services.content.model.transport.articles;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.content.model.articles.Article;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ArticlesTest extends AbstractPlainJaxbTest {

    // Shared state
    private String realm;
    private Articles articles;
    private String content1, content2;
    private Organisation organisation;
    private Address address;


    @Before
    public void setupSharedState() {

        // Use Moxy as the JAXB implementation
        System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");

        address = new Address("careOfLine", "departmentName", "street", "number",
                "city", "zipCode", "country", "description");
        organisation = new Organisation("FooBar", "suffix", "phone", "bankAccountInfo",
                "postAccountInfo", address, "emailSuffix", TimeFormat.SWEDISH_TIMEZONE,
                TimeFormat.SWEDISH_LOCALE);
        realm = organisation.getOrganisationName();

        articles = new Articles(realm, "/news/latest", new ArrayList<>());

        // Create some arbitrary content
        content1 = "<div><header>foo</header><body>bar!</body></div>";
        content2 = "<div><header>foo2</header><body>bar2!</body></div>";

        // Populate the Articles.
        articles.getArticleList().add(new Article("News_1",
                "ERF Häxxxxmästaren",
                ZonedDateTime.of(2015, 12, 12, 5, 2, 3, 0, TimeFormat.SWEDISH_TIMEZONE),
                content1, organisation));
        articles.getArticleList().add(new Article("News_2",
                "ERF Häxxmästaren",
                ZonedDateTime.of(2015, 11, 11, 15, 22, 33, 0, TimeFormat.SWEDISH_TIMEZONE),
                content2, organisation));

        jaxb.add(Articles.class);
        jaxb.getUnMarshallerProperties().put(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");
        jaxb.getMarshallerProperties().put(MarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");
    }

    @After
    public void teardownSharedState() {
        System.clearProperty("javax.xml.bind.context.factory");
    }


    @Test
    public void validateMarshallingToXML() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/articles.xml");

        // Act
        final String result = marshalToXML(articles);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateMarshallingToJSon() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/articles.json");

        // Act
        final String result = marshalToJSon(articles);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertEquals(expected.replaceAll("\\s+", ""), result.replaceAll("\\s+", ""));
    }


    @Test
    public void validateUnmarshallingFromXML() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/transport/articles.xml");

        // Act
        final Articles resurrected = unmarshalFromXML(Articles.class, data);

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
        /*
        unmarshaller.setProperty(UnmarshallerProperties.MEDIA_TYPE, "application/json");
        unmarshaller.setProperty(UnmarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
        unmarshaller.setProperty(UnmarshallerProperties.JSON_NAMESPACE_PREFIX_MAPPER, true);
        unmarshaller.setProperty(UnmarshallerProperties.JSON_ATTRIBUTE_PREFIX, "@");
        */
        final String data = XmlTestUtils.readFully("testdata/transport/articles.json");

        // Act
        final Articles resurrected = unmarshalFromJSON(Articles.class, data);

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
