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
package se.mithlond.services.content.model.localization;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.content.model.navigation.Localizable;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.Transient;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * A text translatable to several languages.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"texts", "defaultLocalization"})
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalizedTexts extends NazgulEntity implements Localizable {

    @ElementCollection
    @CollectionTable(name = "text_localizations", joinColumns = @JoinColumn(name = "localization_suite"))
    @Column(name = "localized_text")
    @MapKeyJoinColumn(name = "localization_id", referencedColumnName = "id")
    @XmlTransient
    private Map<Localization, String> data;

    /**
     * A sequence of LocalizedTextTuples holding the transported localized texts resources.
     */
    @Transient
    @XmlElement(name = "localizedText")
    private List<LocalizedTextTuple> texts;

    /**
     * The default Localization for this LocalizedTexts instance.
     */
    @ManyToOne
    @XmlElement(required = true)
    private Localization defaultLocalization;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public LocalizedTexts() {

        this.texts = new ArrayList<>();
        this.data = new TreeMap<>();
    }

    /**
     * Compound constructor creating a LocalizedTexts instance with the supplied Localization and Text.
     * The supplied Localization is also set to the default Localization of this LocalizedTexts.
     *
     * @param localization The non-null Localization.
     * @param text         The non-empty text.
     */
    public LocalizedTexts(final Localization localization, final String text) {

        // First, delegate
        this();

        // Assign the default localization.
        this.defaultLocalization = localization;

        // ... and assign the Localization/Text pair.
        this.setText(localization, text);
    }

    /**
     * {@inheritDoc}
     */
    public String getText(final Localization localization) {

        final Localization effectiveLocalization = localization == null ? defaultLocalization : localization;

        final Map.Entry<Localization, String> firstEntry = data.entrySet().stream()
                .filter(entry -> entry.getKey().equals(effectiveLocalization))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No text localization for ["
                        + effectiveLocalization.toString() + "]"));

        // All done.
        return firstEntry.getValue();
    }

    /**
     * Adds the supplied text for the given Localization.
     *
     * @param localization A non-null Localization.
     * @param text         A non-empty text.
     * @return The former text for the supplied Localization, or {@code null} if no previous text was supplied for
     * the supplied Localization.
     */
    public String setText(final Localization localization, final String text) {

        // Check sanity
        final Localization nonNullLocalization = Validate.notNull(localization, "localization");
        final String nonEmptyText = Validate.notEmpty(text, "text");

        // All done.
        return data.put(nonNullLocalization, nonEmptyText);
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
        if (!super.equals(o)) {
            return false;
        }
        final LocalizedTexts that = (LocalizedTexts) o;
        return Objects.equals(data, that.data)
                && Objects.equals(defaultLocalization, that.defaultLocalization);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), data, defaultLocalization);
    }

    /**
     * @return A List containing all Localizations found within this LocalizedTexts.
     */
    public List<Localization> getContainedLocalizations() {
        return data.keySet().stream().collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(defaultLocalization, "defaultLocalization")
                .notNullOrEmpty(data, "data")
                .endExpressionAndValidate();
    }

    /**
     * Factory method creating a LocalizedTexts instance for the supplied Language and the given Text.
     *
     * @param language The language, corresponding to the {@link java.util.Locale} language code.
     * @param text     The text to wrap in the returned LocalizedTexts instance for the supplied language.
     * @return A newly created LocalizedTexts instance wrapping the supplied text for the given language (code).
     */
    public static LocalizedTexts build(final String language, final String text) {

        // Check sanity
        Validate.notEmpty(language, "language");
        Validate.notEmpty(text, "text");

        // Create the Localization and its LocalizedTexts
        return new LocalizedTexts(new Localization(language), text);
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

        // Populate the transport tuple sequence
        final List<LocalizedTextTuple> textSequence = data.entrySet()
                .stream()
                .map(current -> new LocalizedTextTuple(current.getKey(), current.getValue()))
                .collect(Collectors.toList());
        this.texts.clear();
        this.texts.addAll(textSequence);
    }

    /**
     * This method is called after all the properties (except IDREF) are unmarshalled for this object,
     * but before this object is set to the parent object.
     */
    @SuppressWarnings("PMD")
    private void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {

        // Re-populate the data Map with the data from the transport tuple sequence
        data.clear();
        texts.stream().forEach(current -> data.put(current.getLocalization(), current.getText()));
    }
}
