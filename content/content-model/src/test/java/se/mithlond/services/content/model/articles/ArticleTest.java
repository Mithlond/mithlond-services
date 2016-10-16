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
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.transport.EntityTransporter;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.content.model.articles.media.BitmapImage;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ArticleTest extends AbstractPlainJaxbTest {

    // Shared state
    private Organisation organisation;
    private User user;
    private Membership membership;
    private Address address;

    private Article unitUnderTest;
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
                TimeFormat.SWEDISH_LOCALE);

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
        this.unitUnderTest = new Article(createdAt, membership, "Article Title", organisation);
        unitUnderTest.addSection(section1, membership);
        unitUnderTest.addSection(section2, membership);

        // Make the internal state fully defined.
        unitUnderTest.setUpdated(updatedAt, membership);

        jaxb.add(Article.class, Section.class, BitmapImage.class, EntityTransporter.class);
    }

    @After
    public void restoreClassLoader() {

        try {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        } catch (Exception e) {
            // Do nothing.
        }
    }

    @Test
    public void validateMarshallingToXML() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/articles/wrappedArticle.xml");

        final EntityTransporter<Object> wrapper = new EntityTransporter<>();
        wrapper.addItem(organisation);
        wrapper.addItem(user);
        wrapper.addItem(membership);
        wrapper.addItem(unitUnderTest);

        // Act
        final String result = marshalToXML(wrapper);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateMarshallingToJSon() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/articles/wrappedArticle.json");

        final EntityTransporter<Object> wrapper = new EntityTransporter<>();
        wrapper.addItem(organisation);
        wrapper.addItem(user);
        wrapper.addItem(membership);
        wrapper.addItem(unitUnderTest);

        // Act
        final String result = marshalToJSon(wrapper);
        // System.out.println("Got: " + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }
}
