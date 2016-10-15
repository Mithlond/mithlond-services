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
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.content.model.ContentPatterns;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Holds data and metadata for an Article.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = Article.NAMEDQ_GET_BY_ORGANISATION_AND_LAST_MODIFICATION_DATE,
                query = "select a from Article a "
                        + " where a.owner.organisationName like :" + OrganisationPatterns.PARAM_ORGANISATION_NAME
                        + " and a.lastModified > :" + ContentPatterns.PARAM_LAST_MODIFIED)
})
@Entity
@XmlType(namespace = ContentPatterns.NAMESPACE, propOrder = {"title", "owner"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Article extends AbstractTimestampedText {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(Article.class);

    /**
     * NamedQuery for getting Localizations having a given language.
     */
    public static final String NAMEDQ_GET_BY_ORGANISATION_AND_LAST_MODIFICATION_DATE
            = "Article.getByOrgAndLastModDate";

    /**
     * The Title of this Article.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true)
    private String title;

    /**
     * The Organisation which formally owns this Article. Must not be null, in order
     * to establishing a responsible publisher for the article.
     */
    @ManyToOne(optional = false)
    @XmlTransient
    private Organisation owner;

    /**
     * The Sections being aggregated into this Article.
     */
    @OneToMany(mappedBy = "article")
    @XmlElementWrapper
    @XmlElement(name = "section")
    private List<Section> sections;

    /**
     * An optional image resource URL where to find an image for this Article.
     */
    @Basic
    @Column(length = 1024)
    @XmlElement
    private String leadingImage;

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
     * @param owner   The Organisation owning (the content of) this Article.
     */
    public Article(
            final String title,
            final Membership author,
            final Organisation owner) {
        this(title, author, LocalDateTime.now(), content, owner);
    }

    /**
     * Compound constructor creating an Article wrapping the supplied data.
     *
     * @param title     The title of this Article.
     * @param author    The Article author.
     * @param createdAt The timestamp when this article was created.
     * @param markup    The Article markup content.
     * @param owner     The Organisation owning (the content of) this Article.
     */
    public Article(
            final String title,
            final Membership author,
            final LocalDateTime createdAt,
            final String markup,
            final Organisation owner) {

        // Delegate
        super(createdAt, author);

        // Assign internal state
        this.title = title;
        this.owner = owner;
    }

    public Article(final LocalDateTime created,
            final Membership createdBy,
            final String title,
            final Organisation owner,
            final String leadingImage) {

        // Delegate
        super(created, createdBy);

        // Assign internal state
        this.title = title;
        this.owner = owner;
        this.leadingImage = leadingImage;
    }

    /**
     * Reassigns the title and updates the lastModified timestamp.
     *
     * @param title      a non-empty new title for this Article.
     * @param membership The Membership updating the Title of this Article.
     */
    public void setTitle(final String title, final Membership membership) {

        // Check sanity
        final String newTitle = Validate.notEmpty(title, "title");

        // Assign internal state
        setUpdated(null, membership);
        this.title = newTitle;
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
     * @return The Organisation owning the content of this Article.
     */
    public Organisation getOwner() {
        return owner;
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
                .notNullOrEmpty(title, "title")
                .notNull(owner, "owner")
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
        if (log.isDebugEnabled()) {
            final String contentType = content == null ? "<null>" : content.getClass().getName();
            log.debug("Got content [" + contentType + "]: " + content);
        }

        // Assign the markup content.
        markup = content.getContent();
    }
}
