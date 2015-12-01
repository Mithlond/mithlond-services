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

import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Markup (HTML, XML etc.) can be stored easily as a String or CLOB within a
 * relational database. However, embedding markup in XML is more tricky, and
 * this is a helper class to assist in wrapping markup in a CDATA block to
 * protect it during transport.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"content"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Markup {

	// Internal state
	@XmlAnyElement(MarkupDomAdapter.class)
	private String content;

	/**
	 * JAXB-friendly constructor.
	 */
	public Markup() {
	}

	/**
	 * Compound constructor creating a Markup instance wrapping the supplied data.
	 *
	 * @param markupContent A non-null markup-formatted string.
	 */
	public Markup(final String markupContent) {

		// Check sanity
		Validate.notNull(markupContent, "markupContent");

		// Assign internal state
		// this.content = new JAXBElement<>(new QName(Patterns.NAMESPACE, "content"), String.class, markupContent);
		this.content = markupContent;
	}

	/**
	 * @return The non-null markup-formatted string supplied to this Markup at construction time.
	 */
	public String getContent() {
		return content;
	}
}
