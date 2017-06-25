/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model.transport.localization;

import se.jguru.nazgul.core.algorithms.api.Validate;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.localization.Localizable;
import se.mithlond.services.organisation.model.localization.LocalizedText;

import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import java.util.Locale;
import java.util.Objects;

/**
 * Transport model for a localization and a text.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE, propOrder = {"locale", "text", "classifier"})
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalizedTextVO implements Comparable<LocalizedTextVO> {

    /**
     * The compact transport form for a Localization.
     */
    @XmlAttribute(required = true)
    private Locale locale;

    /**
     * The classifier of this LocalizedTextVO.
     */
    @XmlAttribute
    private String classifier;

    /**
     * The localized text.
     */
    @XmlValue
    private String text;

    /**
     * JAXB-friendly constructor.
     */
    public LocalizedTextVO() {
    }

    /**
     * Compound constructor, creating a LocalizedText wrapping the supplied values.
     *
     * @param locale     a non-null Localization.
     * @param classifier a non-empty classifier.
     * @param text       a non-empty text.
     */
    public LocalizedTextVO(final Locale locale, final String classifier, final String text) {

        // Check sanity
        Validate.notNull(locale, "locale");
        Validate.notEmpty(text, "text");
        Validate.notEmpty(classifier, "classifier");

        // Assign internal state
        this.text = text;
        this.classifier = classifier;
        this.locale = locale;
    }

    /**
     * Convenience constructor creating a LocalizedTextVO using {@link Localizable#DEFAULT_CLASSIFIER} for classifier.
     *
     * @param locale a non-null Locale.
     * @param text   a non-null text.
     */
    public LocalizedTextVO(final Locale locale, final String text) {
        this(locale, Localizable.DEFAULT_CLASSIFIER, text);
    }

    /**
     * Convenience constructor creating a LocalizedTextVO instance from the supplied (and non-null) LocalizedText.
     *
     * @param localizedText a non-null LocalizedText instance.
     */
    public LocalizedTextVO(final LocalizedText localizedText) {
        this(localizedText.getTextLocale().getLocale(), localizedText.getClassifier(), localizedText.getText());
    }

    /**
     * @return The wrapped Localization.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @return The localized text.
     */
    public String getText() {
        return text;
    }

    /**
     * @return The non-empty classifier of this LocalizedTextVO.
     */
    public String getClassifier() {
        return classifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final LocalizedTextVO that) {

        // Return fast
        if (that == this) {
            return 0;
        } else if (that == null) {
            return -1;
        }

        // Delegate to internals.
        int toReturn = locale.toLanguageTag().compareTo(that.locale.toLanguageTag());
        if (toReturn == 0) {
            toReturn = text.compareTo(that.text);
        }
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LocalizedTextVO that = (LocalizedTextVO) o;
        return Objects.equals(locale, that.locale)
                && Objects.equals(text, that.text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(locale, text);
    }

    /**
     * Standard JAXB class-wide listener method, automagically invoked
     * after it has created an instance of this Class.
     *
     * @param marshaller The active Marshaller.
     */
    @SuppressWarnings("PMD")
    private void beforeMarshal(final Marshaller marshaller) {

        // Don't transport the "Default" classifier.
        if (Localizable.DEFAULT_CLASSIFIER.equals(classifier)) {
            this.classifier = null;
        }
    }

    /**
     * This method is called after all the properties (except IDREF) are unmarshalled for this object,
     * but before this object is set to the parent object.
     */
    @SuppressWarnings("PMD")
    private void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {

        // Resurrect the "Default" if we received a 'null' classifier
        if (this.classifier == null) {
            this.classifier = Localizable.DEFAULT_CLASSIFIER;
        }
    }
}
