/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model.localization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.transport.localization.LocalizedTextVO;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUtil;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * A text translatable (or translated) to several languages.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Table(name = "localized_texts",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "suiteIdentifierIsUnique", columnNames = {"suiteIdentifier"})})
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"suiteIdentifier", "defaultLocale", "texts"})
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalizedTexts extends NazgulEntity implements Localizable {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(LocalizedTexts.class);

    @ElementCollection
    @CollectionTable(name = "text_localizations", joinColumns = @JoinColumn(name = "localization_id"))
    @Column(name = "localized_text")
    @MapKeyJoinColumn(name = "localization_id", referencedColumnName = "id")
    @XmlTransient
    private Map<LocaleDefinition, String> data;

    /**
     * A sequence of LocalizedTextTuples holding the transported localized texts resources.
     */
    @Transient
    @XmlElement(name = "localizedText")
    private List<LocalizedTextVO> texts;

    /**
     * The default Localization for this LocalizedTexts instance.
     */
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.REFRESH, CascadeType.PERSIST})
    @JoinColumn(name = "default_localization_id")
    @XmlElement(required = true)
    private LocaleDefinition defaultLocale;

    /**
     * A human-readable identifier for the suite of LocalizedTexts.
     * Should typically be in english.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true)
    private String suiteIdentifier;

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
     * @param suiteIdentifier The human-readable suiteIdentifier of this LocalizedTexts instance.
     * @param defaultLocale   The non-null Localization.
     * @param text            The non-empty text.
     */
    public LocalizedTexts(
            final String suiteIdentifier,
            final LocaleDefinition defaultLocale,
            final String text) {

        // First, delegate
        this();

        // Assign internal state
        this.defaultLocale = defaultLocale;
        this.suiteIdentifier = suiteIdentifier;
        this.setText(defaultLocale, text);
    }

    /**
     * {@inheritDoc}
     */
    public String getText(final LocaleDefinition localeDefinition) {

        final LocaleDefinition effectiveLocale = localeDefinition == null ? defaultLocale : localeDefinition;

        final Map.Entry<LocaleDefinition, String> firstEntry = data.entrySet().stream()
                .filter(entry -> entry.getKey().equals(effectiveLocale))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No text localization for ["
                        + effectiveLocale.toString() + "]"));

        // All done.
        return firstEntry.getValue();
    }

    /**
     * Adds the supplied text for the given Localization.
     *
     * @param localeDefinition A non-null LocaleDefinition.
     * @param text             A non-empty text.
     * @return The former text for the supplied Localization, or {@code null} if no previous
     * text was supplied for the supplied Localization.
     */
    public String setText(final LocaleDefinition localeDefinition, final String text) {

        // Check sanity
        final LocaleDefinition nonNullLocale = Validate.notNull(localeDefinition, "localization");
        final String nonEmptyText = Validate.notEmpty(text, "text");

        // All done.
        return data.put(nonNullLocale, nonEmptyText);
    }

    /**
     * @return The non-empty identifier for this LocalizedTexts.
     */
    public String getSuiteIdentifier() {
        return suiteIdentifier;
    }

    /**
     * Updates the non-empty suiteIdentifier for this {@link LocalizedTexts} bundle.
     *
     * @param suiteIdentifier The identifier for this Suite of Localized Texts.
     */
    public void setSuiteIdentifier(final String suiteIdentifier) {

        // Check sanity
        Validate.notEmpty(suiteIdentifier, "Cannot handle null or empty 'suiteIdentifier' argument.");

        // Assign internal state
        this.suiteIdentifier = suiteIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {

        // Fail fast.
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        // Delegate to internal state
        final LocalizedTexts that = (LocalizedTexts) o;
        return Objects.equals(data, that.data)
                && Objects.equals(defaultLocale, that.defaultLocale)
                && Objects.equals(suiteIdentifier, that.suiteIdentifier);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(data, defaultLocale, suiteIdentifier);
    }

    /**
     * @return A List containing all Localizations found within this LocalizedTexts.
     */
    public List<LocaleDefinition> getContainedLocalizations() {
        return data.keySet().stream().collect(Collectors.toList());
    }

    /**
     * Merge-style method to call (on the server side only) whenever this LocalizedTexts instances
     * needs to be merged into an existing EntityManager transaction. This implies that all Localizations within the
     * internal data map should be replaced with their managed siblings as read from the local database.
     *
     * @param managedLocales The non-null set of managed Localizations found within the local database.
     */
    public void assignManagedLocalizations(final Collection<LocaleDefinition> managedLocales) {

        // Check sanity
        Validate.notNull(managedLocales, "Cannot handle null 'managedLocalizations' argument.");

        // Update the defaultLocalization
        final Optional<LocaleDefinition> managedDefaultLocalization = managedLocales
                .stream()
                .filter(current -> current.equals(defaultLocale))
                .findFirst();
        if (!managedDefaultLocalization.isPresent()) {
            throw new IllegalArgumentException("Required managedLocalization [" + defaultLocale + "] not found.");
        }
        this.defaultLocale = managedDefaultLocalization.get();

        // Re-create the data map to hold the managed Localizations from the provided managedLocalizations Set.
        this.data = data.entrySet().stream().collect(Collectors.toMap(current -> {

            // Find the corresponding managed Localization within the 'managedLocalizations' Set.
            final LocaleDefinition key = current.getKey();
            final Optional<LocaleDefinition> managedLocalization = managedLocales
                    .stream()
                    .filter(aLoc -> aLoc.equals(key))
                    .findFirst();

            if (!managedLocalization.isPresent()) {
                throw new IllegalArgumentException("Existing Localization [" + key
                        + "] was not found within the supplied managedLocalizations: " + managedLocales);
            }

            // All done.
            return managedLocalization.get();

        }, Map.Entry::getValue));

        if (log.isDebugEnabled()) {

            final PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
            log.debug("DefaultLocalization. IsLoaded: " + persistenceUtil.isLoaded(defaultLocale));

            final String dataMsg = data.keySet().stream()
                    .map(c -> "Localization [" + c.toString() + "] IsLoaded: " + persistenceUtil.isLoaded(c))
                    .reduce((l, r) -> l + "," + r)
                    .orElse("<none>");
            log.debug("Data Localizations: " + dataMsg);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(defaultLocale, "defaultLocalization")
                .notNullOrEmpty(data, "data")
                .notNullOrEmpty(suiteIdentifier, "suiteIdentifier")
                .endExpressionAndValidate();
    }

    /**
     * Factory method creating a LocalizedTexts instance for the supplied Language and the given Text.
     *
     * @param defaultLanguage The defaultLanguage, corresponding to the {@link java.util.Locale} language code.
     * @param text            The text to wrap in the returned LocalizedTexts instance for the supplied defaultLanguage.
     * @return A newly created LocalizedTexts instance wrapping the supplied text for the given defaultLanguage (code).
     */
    public static LocalizedTexts build(final String suiteIdentifier, final String defaultLanguage, final String text) {

        // Check sanity
        Validate.notEmpty(defaultLanguage, "defaultLanguage");
        Validate.notEmpty(text, "text");
        Validate.notEmpty(suiteIdentifier, "suiteIdentifier");

        // Create the Localization and its LocalizedTexts
        return new LocalizedTexts(suiteIdentifier, new LocaleDefinition(defaultLanguage), text);
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
        final List<LocalizedTextVO> textSequence = data.entrySet()
                .stream()
                .map(current -> new LocalizedTextVO(current.getKey(), current.getValue()))
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
        texts.forEach(current -> data.put(current.getLocalization(), current.getText()));
    }
}
