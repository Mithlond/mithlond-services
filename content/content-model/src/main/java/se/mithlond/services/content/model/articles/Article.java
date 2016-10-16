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
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Holds data and metadata for an Article, which consists of some initial metadata (author, created and modified
 * timestamps, as well as organisational owner), and then constructed by concatenating sections.
 * Each section contains a heading, an optional image and (somewhat structured) text.
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
@XmlType(namespace = ContentPatterns.NAMESPACE, propOrder = {"title", "sections"})
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
    @ManyToMany(fetch = FetchType.EAGER,
            cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    @JoinTable(name = "article_sections")
    @XmlElementWrapper
    @XmlElement(name = "section")
    private List<Section> sections;

    /**
     * JPA/JAXB-friendly constructor.
     */
    public Article() {
        this.sections = new ArrayList<>();
    }

    /**
     * Convenience constructor, creating an Article using the supplied data and
     * using the current timestamp ("now") for createdAt.
     *
     * @param title  The title of this Article.
     * @param author The Article author.
     * @param owner  The Organisation owning (the content of) this Article.
     */
    public Article(
            final String title,
            final Membership author,
            final Organisation owner) {

        // Delegate
        this(null, author, title, owner);
    }

    /**
     * Compound constructor creating an Article wrapping the supplied data.
     *
     * @param title     The title of this Article.
     * @param createdBy The Membership who (initially) created this Article.
     * @param owner     The Organisation owning the Article. This implies Editor and responsibility for the text
     *                  being published.
     * @param created   The timestamp when this Article was created. If {@code null}, the current timestamp is used.
     */
    public Article(final LocalDateTime created,
            final Membership createdBy,
            final String title,
            final Organisation owner) {

        // Delegate
        super((created == null ? LocalDateTime.now() : created), createdBy);

        // Assign internal state
        this.sections = new ArrayList<>();
        this.title = title;
        this.owner = owner;
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
     * @return The title of this Article. Never null or empty.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return The Organisation owning the content of this Article.
     */
    public Organisation getOwner() {
        return owner;
    }

    /**
     * Retrieves an unmodifiable version of the Sections List.
     *
     * @return an unmodifiable version of the Sections List.
     */
    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    /**
     * Adds the supplied Section to the List of known Sections within this Article.
     *
     * @param section    The Section to add.
     * @param membership The Membership adding the Section.
     */
    public void addSection(final Section section, final Membership membership) {

        // Check sanity
        final Section newSection = Validate.notNull(section, "section");

        // Assign internal state
        setUpdated(null, membership);
        this.sections.add(newSection);
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
}
