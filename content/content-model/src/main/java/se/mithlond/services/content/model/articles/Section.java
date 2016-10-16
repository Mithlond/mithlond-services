/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
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
import se.mithlond.services.content.model.ContentPatterns;
import se.mithlond.services.content.model.articles.media.BitmapImage;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Section within an Article or document.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = ContentPatterns.NAMESPACE,
        propOrder = {"heading", "showHeading", "text", "images"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Section extends NazgulEntity {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(Section.class);

    /**
     * Separator string indicating that a BitmapImage specification should follow.
     */
    public static final String IMAGE_START = "#IMG###";

    /**
     * Separator string indicating that a BitmapImage specification should follow.
     */
    public static final String IMAGE_END = "###IMG#";

    /**
     * Indicates if the heading should be shown.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement
    private String heading;

    /**
     * Indicates if the heading should be shown.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlAttribute
    private boolean showHeading;

    /**
     * The text of this section, where image placeholders
     */
    @Basic(optional = false)
    @Column(nullable = false, length = 4096)
    @XmlElement
    private String text;

    /**
     * The List of BitmapImages used/referred within this Section.
     */
    @ManyToMany
    @XmlElementWrapper
    @XmlElement(name = "image")
    private List<BitmapImage> images;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public Section() {
        this.images = new ArrayList<>();
    }

    /**
     * Compound constructor creating a Section wrapping the supplied data.
     *
     * @param heading     The heading of this Section.
     * @param showHeading A boolean indicating if the heading should be shown.
     * @param text        The text of this Section.
     * @param images      The images within this Section.
     */
    public Section(
            final String heading,
            final boolean showHeading,
            final String text,
            final BitmapImage... images) {

        // Delegate.
        this();

        // Assign internal state
        this.heading = heading;
        this.showHeading = showHeading;
        this.text = text;

        if (images != null && images.length > 0) {
            Arrays.stream(images).filter(c -> c != null).forEach(c -> this.images.add(c));
        }
    }

    /**
     * Retrieves the heading of this Section.
     *
     * @return the heading of this Section.
     */
    public String getHeading() {
        return heading;
    }

    /**
     * Updates the heading of this Section.
     *
     * @param heading the new, non-empty, heading of this Section.
     */
    public void setHeading(final String heading) {
        this.heading = Validate.notEmpty(heading, "heading");
    }

    /**
     * @return True if the heading should be shown.
     */
    public boolean isShowHeading() {
        return showHeading;
    }

    /**
     * Flips the showHeading flag.
     *
     * @param showHeading true to indicate that the heading should be shown.
     */
    public void setShowHeading(final boolean showHeading) {
        this.showHeading = showHeading;
    }

    /**
     * Retrieves the text of this Section.
     *
     * @return the text of this Section.
     */
    public String getText() {
        return text;
    }

    /**
     * Re-assigns the text of this Section.
     *
     * @param text the non-empty, new text of this Section.
     */
    public void setText(final String text) {
        this.text = Validate.notEmpty(text, "text");
    }

    /**
     * @return The BitmapImages within this Section.
     */
    public List<BitmapImage> getImages() {
        return images;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(heading, "heading")
                .notNullOrEmpty(text, "text")
                .notNull(images, "images")
                .endExpressionAndValidate();
    }
}
