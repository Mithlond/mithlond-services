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
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Holds data and metadata for an Article.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"title", "author", "createdAt", "lastModified", "content"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Article extends NazgulEntity {

	// Our Logger
	private static final Logger log = LoggerFactory.getLogger(Article.class);

	// Internal state
	@Basic(optional = false)
	@Column(nullable = false)
	@XmlElement(required = true)
	private String title;

	@Basic(optional = false)
	@Column(nullable = false)
	@XmlElement(required = true)
	private String author;

	@Basic(optional = false)
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@XmlElement(required = true)
	private Calendar createdAt;

	@Basic(optional = true)
	@Column(nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	@XmlElement
	private Calendar lastModified;

	@Basic(optional = false)
	@Column(nullable = false)
	@Lob
	@XmlTransient
	private String markup;

	@Transient
	@XmlElement(required = true)
	private Markup content;

	/**
	 * JPA/JAXB-friendly constructor.
	 */
	public Article() {
	}

	/**
	 * Convenience constructor, creating an Article using the supplied data and
	 * using the current timestamp ("now") for createdAt.
	 *
	 * @param title   The title of this Article.
	 * @param author  The Article author.
	 * @param content The Article content.
	 */
	public Article(final String title, final String author, final String content) {
		this(title, author, ZonedDateTime.now(), content);
	}

	/**
	 * Compound constructor creating an Article wrapping the supplied data.
	 *
	 * @param title     The title of this Article.
	 * @param author    The Article author.
	 * @param createdAt The timestamp when this article was created.
	 * @param markup    The Article markup content.
	 */
	public Article(final String title,
				   final String author,
				   final ZonedDateTime createdAt,
				   final String markup) {
		this.title = title;
		this.author = author;
		this.createdAt = GregorianCalendar.from(createdAt);
		this.markup = markup;
	}

	/**
	 * Reassigns the title and updates the lastModified timestamp.
	 *
	 * @param title a non-empty new title for this Article.
	 */
	public void setTitle(final String title) {

		// Assign internal state
		this.title = Validate.notEmpty(title, "title");
		this.lastModified = GregorianCalendar.from(ZonedDateTime.now());
	}

	/**
	 * Reassigns the markup content and updates the lastModified timestamp.
	 *
	 * @param markup a non-empty new markup content for this Article.
	 */
	public void setMarkup(final String markup) {

		// Assign internal state
		this.markup = Validate.notEmpty(markup, "markup");
		this.lastModified = GregorianCalendar.from(ZonedDateTime.now());
	}

	/**
	 * @return The title of this Article. Never null or empty.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return The Article author. Never null or empty.
	 */
	public String getAuthor() {
		return author;
	}

	/**
	 * @return The ZonedDateTime when this Article was created. Never null.
	 */
	public ZonedDateTime getCreatedAt() {
		return ((GregorianCalendar) createdAt).toZonedDateTime();
	}

	/**
	 * @return The ZonedDateTime when this Article was last modified, or {@code null} if not modified after creation.
	 */
	public ZonedDateTime getLastModified() {
		return lastModified == null ? null : ((GregorianCalendar) lastModified).toZonedDateTime();
	}

	/**
	 * @return The full markup content of this article.
	 */
	public String getMarkup() {
		return markup;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateEntityState() throws InternalStateValidationException {

		InternalStateValidationException.create()
				.notNullOrEmpty(author, "author")
				.notNullOrEmpty(title, "title")
				.notNullOrEmpty(markup, "markup")
				.notNull(createdAt, "createdAt")
				.endExpressionAndValidate();
	}

	//
	// Private helpers
	//

	/**
	 * Standard JAXB class-wide listener method, automagically invoked
	 * after it has created an instance of this Class.
	 *
	 * @param marshaller The active Marshaller.
	 */
	@SuppressWarnings("PMD")
	private void beforeMarshal(final Marshaller marshaller) {
		this.content = new Markup(markup);
	}

	/**
	 * This method is called after all the properties (except IDREF) are unmarshalled for this object,
	 * but before this object is set to the parent object.
	 */
	@SuppressWarnings("PMD")
	private void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {

		// Log somewhat
		final String contentType = content == null ? "<null>" : content.getClass().getName();
		log.info("Got content [" + contentType + "]: " + content);
		markup = content.getContent();
	}
}
