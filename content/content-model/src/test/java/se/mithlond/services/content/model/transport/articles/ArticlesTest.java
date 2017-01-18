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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.content.model.ContentPatterns;
import se.mithlond.services.content.model.articles.Article;
import se.mithlond.services.content.model.articles.Section;
import se.mithlond.services.content.model.articles.media.BitmapImage;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ArticlesTest extends AbstractPlainJaxbTest {

    // Shared state
    private Articles unitUnderTest;
    private Organisation organisation;
    private User user;
    private Membership membership;
    private Address address;

    private Article anArticle;
    private Section section1, section2;
    private File jarFileFile;
    private ClassLoader originalClassLoader;

    @Before
    public void setupSharedState() throws Exception {

        // First, identify the path to the JAR containing images.
        final URL imageJarURL = getClass().getClassLoader().getResource("testdata/articles/images.jar");
        Assert.assertNotNull(imageJarURL);

        jarFileFile = new File(imageJarURL.getPath());
        Assert.assertTrue(jarFileFile.exists() && jarFileFile.isFile());

        // Setup the classloader to include the image JAR.
        originalClassLoader = Thread.currentThread().getContextClassLoader();
        final URL[] imageJarFileURLs = new URL[]{jarFileFile.toURI().toURL()};
        final URLClassLoader imageJarClassLoader = new URLClassLoader(imageJarFileURLs, originalClassLoader);
        Thread.currentThread().setContextClassLoader(imageJarClassLoader);

        address = new Address("careOfLine", "departmentName", "street", "number",
                "city", "zipCode", "country", "description");
        organisation = new Organisation("FooBar", "suffix", "phone", "bankAccountInfo",
                "postAccountInfo", address, "emailSuffix", TimeFormat.SWEDISH_TIMEZONE,
                TimeFormat.SWEDISH_LOCALE,
                WellKnownCurrency.SEK);

        user = new User("FirstName",
                "LastName",
                LocalDate.of(1968, Month.SEPTEMBER, 17),
                (short) 1234,
                address, null,
                new TreeMap<>(),
                "someToken");

        membership = new Membership("ERF Häxxmästaren", "Den onde", "haxx", true, user, organisation);

        // Create 2 Sections.
        final BitmapImage aJpgImage = BitmapImage.createFromResourcePath("images/example_jpg.jpg");
        this.section1 = new Section("section1_heading", true, "section1_text", aJpgImage);
        this.section2 = new Section("section2_heading", true, "section2_text");

        // Define some timestamps
        final LocalDateTime createdAt = LocalDateTime.of(2016, Month.FEBRUARY, 14, 12, 4);
        final LocalDateTime updatedAt = LocalDateTime.of(2016, Month.FEBRUARY, 15, 16, 17);

        // Create an Article
        this.anArticle = new Article(createdAt, membership, "Article Title", "/some/path", organisation);
        anArticle.addSection(section1, membership);
        anArticle.addSection(section2, membership);

        // Make the internal state fully defined.
        anArticle.setUpdated(updatedAt, membership);

        unitUnderTest = new Articles("Mithlond", "/news/mithlond", Collections.singletonList(anArticle));

        jaxb.add(Articles.class, Article.class, Section.class, BitmapImage.class);
        jaxb.mapXmlNamespacePrefix(ContentPatterns.TRANSPORT_NAMESPACE, "content_transport");
    }


    @Test
    public void validateMarshallingToXML() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/articles.xml");

        // Act
        final String result = marshalToXML(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateMarshallingToJSon() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/articles.json");

        // Act
        final String result = marshalToJSon(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void validateUnmarshallingFromXML() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/transport/articles.xml");

        // Act
        final Articles resurrected = unmarshalFromXML(Articles.class, data);

        // Assert
        Assert.assertNotNull(resurrected);
        Assert.assertEquals("Mithlond", resurrected.getRealm());

        final List<Article> articleList = resurrected.getArticleList();
        Assert.assertEquals(1, articleList.size());
        Assert.assertEquals("ERF Häxxmästaren", articleList.get(0).getCreatedBy().getAlias());
    }

    @Test
    public void validateUnmarshallingFromJSON() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/transport/articles.json");

        // Act
        final Articles resurrected = unmarshalFromJSON(Articles.class, data);

        // Assert
        Assert.assertNotNull(resurrected);
        Assert.assertEquals("Mithlond", resurrected.getRealm());

        final List<Article> articleList = resurrected.getArticleList();
        Assert.assertEquals(1, articleList.size());
        Assert.assertEquals("ERF Häxxmästaren", articleList.get(0).getCreatedBy().getAlias());
    }
}
