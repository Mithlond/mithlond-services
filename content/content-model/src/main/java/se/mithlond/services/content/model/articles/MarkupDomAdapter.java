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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.DomHandler;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * A DomHandler to ensure that markup is sent verbatim.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public class MarkupDomAdapter implements DomHandler<String, StreamResult> {

	// Our Logger
	private static final Logger log = LoggerFactory.getLogger(MarkupDomAdapter.class);

	// Internal state
	private static DocumentBuilder DOCUMENT_BUILDER;
	private static Transformer DECLARATION_PEELING_TRANSFORMER;
	private StringWriter writer = new StringWriter();

	/**
	 * {@inheritDoc}
	 */
	@Override
	public StreamResult createUnmarshaller(final ValidationEventHandler errorHandler) {

		// Reset the writer buffer
		writer.getBuffer().setLength(0);

		// All done.
		return new StreamResult(writer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getElement(final StreamResult rt) {

		// For some reason, an XML PI is prepended to the Unmarshalled content.
		/*
		<?xml version="1.0" encoding="UTF-8"?> <-- This!
		<div>
			<strong>ArticleTitle_1</strong>
            <content>content_1åäöÅÄÖëü</content>
         </div>
		 */

		// Just extract the Markup text.
		return peelOffXmlDeclaration(rt.getWriter().toString());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Source marshal(final String markupToMarshal, final ValidationEventHandler errorHandler) {
		try {
			final String xml = markupToMarshal.trim();

			if (log.isDebugEnabled()) {
				log.debug("Marshalling: " + xml);
			}

			// Pack the XML into a StreamSource.
			return new StreamSource(new StringReader(xml));

		} catch (Exception e) {

			final String msg = "Could not marshalToXML markup of size ["
					+ (markupToMarshal == null ? "<null/0>" : markupToMarshal.length()) + "]";
			throw new RuntimeException(msg, e);
		}
	}

	//
	// Private helpers
	//

	private String peelOffXmlDeclaration(final String data) {

		// Handle internal state
		if (DOCUMENT_BUILDER == null) {
			try {

				final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
				documentBuilderFactory.setNamespaceAware(true);
				documentBuilderFactory.setIgnoringElementContentWhitespace(true);
				DOCUMENT_BUILDER = documentBuilderFactory.newDocumentBuilder();

			} catch (ParserConfigurationException e) {
				throw new IllegalStateException("Could not create default DocumentBuilder", e);
			}

			try {

				DECLARATION_PEELING_TRANSFORMER = TransformerFactory.newInstance().newTransformer();
				DECLARATION_PEELING_TRANSFORMER.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

			} catch (TransformerConfigurationException e) {
				throw new IllegalStateException("Could not create Transformer", e);
			}
		}

		// Parse the inbound data as an InputSource
		final InputSource is = new InputSource(new StringReader(data));
		// is.setEncoding("UTF-8");
		// log.info("InputSource encoding: " + is.getEncoding());

		try {

			// Use some DOM weird magic to find the XML content of the element.
			// ... "because this is simple" ...
			final Document document = DOCUMENT_BUILDER.parse(is);

			// This is the JAXB RI way of doing things ...
			// final Element docElement = (Element) document.getDocumentElement().getFirstChild();

			// This is the EclipseLink MOXy way of doing things ...
			final Element docElement = document.getDocumentElement();

			final StringWriter buf = new StringWriter();
			DECLARATION_PEELING_TRANSFORMER.transform(new DOMSource(docElement), new StreamResult(buf));

			// All done.
			return buf.toString();

		} catch (Exception e) {
			throw new IllegalStateException("Could not acquire markup XML", e);
		}
	}
}
