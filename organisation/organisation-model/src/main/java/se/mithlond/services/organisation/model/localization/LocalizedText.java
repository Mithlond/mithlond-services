/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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
package se.mithlond.services.organisation.model.localization;

import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * <p>Entity storage for localized text snippets, relating the text to three things:</p>
 * <ol>
 * <li>A {@link LocaleDefinition}</li>
 * <li>A {@link LocalizedTexts} suite</li>
 * <li>A classifier, being something like "Default", "Short Description", "Full Description" or similar.</li>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Table(name = "localized_text")
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"textLocale", "classifier", "text", "version"})
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalizedText implements Serializable, Validatable, Comparable<LocalizedText> {

    @EmbeddedId
    @XmlTransient
    private LocalizedTextId compoundJpaID;

    /**
     * The JPA Version of this Allergy.
     */
    @Version
    @XmlAttribute
    private long version;

    /**
     * The non-null LocaleDefinition for this LocalizedText.
     */
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @MapsId("localeId")
    @XmlElement(required = true)
    private LocaleDefinition textLocale;

    /**
     * The non-null suite of LocalizedText objects wherein this LocalizedText instance is part.
     * (I.e. the parent of this detail object).
     */
    @ManyToOne(optional = false)
    @JoinColumn(nullable = false)
    @MapsId("localizedTextsSuiteId")
    @XmlTransient
    private LocalizedTexts suite;

    /**
     * The non-null classifier for this LocalizedText, typically something like "Short Description",
     * "Full Description", "MenuItem Word", "Informative Text" etc. The classifier separates different
     * instances of LocalizedText for the same {@link LocalizedTexts} suite used for different purposes.
     * It is recommended - although not technically required - that each {@link LocalizedTexts} suite contains
     * a set of LocalizedText objects with the classifier {@link Localizable#DEFAULT_CLASSIFIER}.
     *
     * @see Localizable#DEFAULT_CLASSIFIER
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @MapsId("classifier")
    @XmlElement(required = true)
    private String classifier;

    /**
     * <p>The text of this LocalizedText object, which should be given in the Locale given by the {@link #textLocale}
     * field.</p>
     * <p><strong>Note!</strong> Since different DB-native collations cannot be enforced on a single column
     * containing several texts in different localizations, sorting in locale-correct order is the responsibility of
     * the application. This is a classical DB problem for localized texts which is beyond the scope of this service
     * model to solve.</p>
     */
    @Basic(optional = false)
    @Column(nullable = false, length = 2048)
    @XmlElement(required = true)
    private String text;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public LocalizedText() {
    }

    /**
     * Compound constructor creating a LocalizedText wrapping the supplied data.
     *
     * @param textLocale The non-null Locale for this LocaleText, which is the sole
     *                   identifier of the language/locale of the supplied text.
     * @param suite      The parent/suite of LocalizedText wherein this LocalizedText is a detail/part.
     * @param classifier The non-empty classifier of this LocalizedText instance.
     * @param text       The non-null text, assumed to be describing the concept of the parent suite and in the
     *                   supplied LocaleDefinition.
     */
    public LocalizedText(
            final LocaleDefinition textLocale,
            final LocalizedTexts suite,
            final String classifier,
            final String text) {

        this.textLocale = textLocale;
        this.suite = suite;
        this.classifier = classifier;
        this.text = text;
    }

    /**
     * Retrieves the non-null LocaleDefinition for this LocalizedText.
     *
     * @return The non-null LocaleDefinition for this LocalizedText.
     */
    public LocaleDefinition getTextLocale() {
        return textLocale;
    }

    /**
     * (Re-)assigns the LocaleDefinition of this LocalizedText. This is not considered a public API method, but rather
     * something done following a JAXB-unmarshalling when it should be possible to replace the unmanaged
     * LocaleDefinition with one managed by the EntityManager.
     *
     * @param localeDefinition A non-null LocaleDefinition.
     */
    protected void setTextLocale(final LocaleDefinition localeDefinition) {
        this.textLocale = Validate.notNull(localeDefinition, "localeDefinition");
    }

    /**
     * Retrieves the parent/suite of LocalizedText wherein this LocalizedText is a detail/part.
     *
     * @return The parent/suite of LocalizedText wherein this LocalizedText is a detail/part.
     */
    public LocalizedTexts getSuite() {
        return suite;
    }

    /**
     * (Re-)assigns the parent/suite of LocalizedText wherein this LocalizedText is a detail/part.
     *
     * @param suite the non-null parent/suite of LocalizedText wherein this LocalizedText is a detail/part.
     */
    public void setSuite(final LocalizedTexts suite) {
        this.suite = Validate.notNull(suite, "suite");
    }

    /**
     * Retrieves the non-empty classifier of this LocalizedText instance.
     *
     * @return the non-empty classifier of this LocalizedText instance.
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * Retrieves the non-null text wrapped within this LocalizedText instance.
     *
     * @return the non-null text wrapped within this LocalizedText instance.
     */
    public String getText() {
        return text;
    }

    /**
     * Assigns the text of this LocalizedText instance.
     *
     * @param text A non-empty String text, assumed to be tailored to the LocaleDefinition and classifier
     *             of this LocalizedText.
     */
    public void setText(final String text) {
        this.text = Validate.notEmpty(text, "text");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final LocalizedText o) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(textLocale, "textLocale")
                .notNull(suite, "suite")
                .notNullOrEmpty(classifier, "classifier")
                .notNull(text, "text")
                .endExpressionAndValidate();
    }
}
