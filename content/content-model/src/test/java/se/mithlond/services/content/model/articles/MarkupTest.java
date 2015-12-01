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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbNamespacePrefixResolver;
import se.jguru.nazgul.core.xmlbinding.spi.jaxb.helper.JaxbUtils;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.content.model.Patterns;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MarkupTest {

	// Shared state
	private static final String MARKUP = "<foo>bar<baz>gnat</baz></foo>";
	private Markup markup;
	private Marshaller marshaller;
	private Unmarshaller unmarshaller;

	@Before
	public void setupSharedState() {

		// Use Moxy as the JAXB implementation
		// System.setProperty("javax.xml.bind.context.factory", "org.eclipse.persistence.jaxb.JAXBContextFactory");
		// System.out.println("Using JAXB implementation: " + System.getProperty("javax.xml.bind.context.factory"));

		this.markup = new Markup(MARKUP);

		// Create the JAXBContext and related objects.
		try {
			final JAXBContext ctx = JAXBContext.newInstance(Markup.class);
			final JaxbNamespacePrefixResolver prefixResolver = new JaxbNamespacePrefixResolver();
			prefixResolver.put(Patterns.NAMESPACE, "content");
			marshaller = JaxbUtils.getHumanReadableStandardMarshaller(ctx, prefixResolver, false);
			unmarshaller = ctx.createUnmarshaller();

			Assert.assertTrue(ctx.getClass().getName().toLowerCase().contains("eclipse"));
		} catch (JAXBException e) {
			throw new IllegalStateException("Could not create JAXBContext related objects", e);
		}
	}

	@Test
	public void validateMarshallingUsingMoxy() throws Exception {

		// Assemble
		final String expected = XmlTestUtils.readFully("testdata/moxy_markup.xml");
		final StringWriter out = new StringWriter();

		// Act
		marshaller.marshal(markup, out);
		// System.out.println("Got: " + out.toString());

		// Assert
		Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, out.toString()).identical());
	}

	@Test
	public void validateUnmarshallingUsingMoxy() throws Exception {

		// Assemble
		final String data = XmlTestUtils.readFully("testdata/moxy_markup.xml");

		// Act
		final Object result = unmarshaller.unmarshal(new StringReader(data));

		// Assert
		Assert.assertNotNull(result);
		Assert.assertTrue(result instanceof Markup);

		final Markup resurrected = (Markup) result;
		Assert.assertEquals(MARKUP.replaceAll("\\p{Space}", ""), resurrected.getContent().replaceAll("\\p{Space}", ""));
	}

	@Test
	public void validateMarshallingToMoxyJson() throws Exception {

		// Assemble
		final String expected = XmlTestUtils.readFully("testdata/moxy_markup.json");
		final Markup toMarshal = new Markup("<div><h1>Title</h1><div style='foo'>content</div></div>");

		final String JSON_CONTENT_TYPE = "application/json";
		marshaller.setProperty("eclipselink.media-type", JSON_CONTENT_TYPE);
		final StringWriter out = new StringWriter();

		// Act
		marshaller.marshal(toMarshal, out);
		// System.out.println("Got: " + out.toString());

		// Assert
		Assert.assertEquals(expected.replaceAll("\\p{Space}", ""), out.toString().replaceAll("\\p{Space}", ""));
	}

	@Ignore("EclipseLink's Unmarshaller seems to confuse Elements and attributes in some cases. " +
			"This is not important to enable marshalling Markup content.")
	@Test
	public void validateUnmarshallingFromMoxyJson() throws Exception {

		// Assemble
		final String data = XmlTestUtils.readFully("testdata/moxy_markup.json");
		final Markup expected = new Markup("<div><h1>Title</h1><div style='foo'>content</div></div>");

		final String JSON_CONTENT_TYPE = "application/json";
		unmarshaller.setProperty("eclipselink.media-type", JSON_CONTENT_TYPE);

		// Act
		JAXBElement<Markup> result = unmarshaller.unmarshal(new StreamSource(new StringReader(data)), Markup.class);
		System.out.println("Got: " + result.getValue().getContent());

		// Assert
		final String expectedNoWhitespace = expected
				.getContent()
				.replaceAll("\\p{Space}", "");
		final String retrievedWithDoubleQuotes = result.getValue()
				.getContent()
				.replaceAll("\\p{Space}", "")
				.replace('"', '\'');
		Assert.assertEquals(expectedNoWhitespace, retrievedWithDoubleQuotes);
	}
}
