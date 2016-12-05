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
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
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
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A text translatable (or translated) to several languages.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Table(name = "localized_text_suites",
        uniqueConstraints = {@UniqueConstraint(
                name = "unq_suite_identifier",
                columnNames = {"suiteIdentifier"})})
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"suiteIdentifier", "defaultLocale", "textVOs"})
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalizedTexts extends NazgulEntity implements Localizable {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(LocalizedTexts.class);

    /**
     * The List of LocalizedText objects pertaining to this LocalizedTexts suite.
     */
    @OneToMany(mappedBy = "suite", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @XmlTransient
    private List<LocalizedText> texts;

    /**
     * A sequence of LocalizedTextTuples holding the transported localized texts resources.
     */
    @Transient
    @XmlElementWrapper
    @XmlElement(name = "text")
    private List<LocalizedTextVO> textVOs;

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
        this.textVOs = new ArrayList<>();
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
            final String classifier,
            final String text) {

        // First, delegate
        this();

        // Assign internal state
        this.defaultLocale = defaultLocale;
        this.suiteIdentifier = suiteIdentifier;
        this.setText(defaultLocale, classifier, text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText(final LocaleDefinition localeDefinition, final String classifier) {

        // Get the relevant LocalizedText child.
        final LocalizedText localizedText = getOrNull(localeDefinition, classifier);

        // All Done.
        return localizedText == null ? null : localizedText.getText();
    }

    /**
     * Adds the supplied text for the given Localization, creating the child LocalizedText for the supplied
     * LocaleDefinition and classifier if not already present.
     *
     * @param localeDefinition A non-null LocaleDefinition.
     * @param classifier       A non-empty classifier.
     * @param text             A non-empty text.
     * @return The former text for the supplied Localization, or {@code null} if no previous
     * text was supplied for the supplied Localization.
     */
    public String setText(final LocaleDefinition localeDefinition, final String classifier, final String text) {

        // Check sanity
        final String okClassifier = Validate.notEmpty(classifier, "classifier");
        final LocaleDefinition okLocale = Validate.notNull(localeDefinition, "localeDefinition");
        final String okText = Validate.notEmpty(text, "text");

        // Get the existing LocalizedText for the supplied localeDefinition and classifier.
        LocalizedText localizedText = getOrNull(okLocale, okClassifier);
        String toReturn = null;

        if (localizedText == null) {

            // Create a new LocalizedText child and add it to this LocalizedTexts parent.
            localizedText = new LocalizedText(okLocale, this, okClassifier, okText);
            this.texts.add(localizedText);

        } else {

            // Update the text
            toReturn = localizedText.getText();
            localizedText.setText(okText);
        }

        // All Done.
        return toReturn;
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
     * Retrieves all LocalizedText children from this LocalizedTexts parent.
     *
     * @return all LocalizedText children from this LocalizedTexts parent.
     */
    public List<LocalizedText> getTexts() {
        return texts;
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
        return Objects.equals(getSuiteIdentifier(), that.getSuiteIdentifier());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getSuiteIdentifier());
    }

    /**
     * @return A List containing all Localizations found within this LocalizedTexts.
     */
    public SortedSet<LocaleDefinition> getContainedLocalizations() {

        final SortedSet<LocaleDefinition> toReturn = new TreeSet<>();

        texts.stream()
                .filter(Objects::nonNull)
                .map(LocalizedText::getTextLocale)
                .distinct()
                .forEach(toReturn::add);

        // All Done.
        return toReturn;
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

        // Re-assign the LocaleDefinitions within each LocalizedText
        texts.forEach(current -> {

            final LocaleDefinition unmanagedLocale = current.getTextLocale();
            final Optional<LocaleDefinition> managedLocale = managedLocales
                    .stream()
                    .filter(aLocale -> aLocale.equals(unmanagedLocale))
                    .findFirst();

            if (!managedLocale.isPresent()) {
                throw new IllegalArgumentException("Existing Locale [" + unmanagedLocale
                        + "] was not found within the supplied managedLocales: " + managedLocales);
            }

            // Re-assign the managed Locale.
            current.setTextLocale(managedLocale.get());
        });

        if (log.isDebugEnabled()) {

            final PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
            log.debug("DefaultLocalization. IsLoaded: " + persistenceUtil.isLoaded(defaultLocale));

            final String dataMsg = texts.stream().filter(Objects::nonNull)
                    .map(LocalizedText::getTextLocale)
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
                .notNull(texts, "texts")
                .notNullOrEmpty(suiteIdentifier, "suiteIdentifier")
                .endExpressionAndValidate();
    }

    //
    // Private helpers
    //

    private LocalizedText getOrNull(final LocaleDefinition localeDefinition, final String classifier) {

        // Ensure we always have sane arguments.
        final LocaleDefinition effectiveLocale = localeDefinition == null ? defaultLocale : localeDefinition;
        final String effectiveClassifier = classifier != null && !classifier.isEmpty()
                ? classifier
                : Localizable.DEFAULT_CLASSIFIER;

        // Find an existing LocalizedText
        return texts.stream()
                .filter(Objects::nonNull)
                .filter(current -> current.getClassifier().equals(effectiveClassifier)
                        && current.getTextLocale().equals(effectiveLocale))
                .findFirst()
                .orElse(null);
    }

    /**
     * Standard JAXB class-wide listener method, automagically invoked
     * after it has created an instance of this Class.
     *
     * @param marshaller The active Marshaller.
     */
    @SuppressWarnings("PMD")
    private void beforeMarshal(final Marshaller marshaller) {

        // Convert the contained LocalizedTexts into LocalizedTextVOs.
        final List<LocalizedTextVO> voSequence = new ArrayList<>();
        texts.stream().map(LocalizedTextVO::new).forEach(voSequence::add);

        // Populate the transport tuple sequence
        this.textVOs.clear();
        this.textVOs.addAll(voSequence);
    }

    /**
     * This method is called after all the properties (except IDREF) are unmarshalled for this object,
     * but before this object is set to the parent object.
     */
    @SuppressWarnings("PMD")
    private void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {

        // Re-populate the data Map with the data from the transport tuple sequence
        texts.clear();
        textVOs.stream()
                .map(current -> new LocalizedText(current.getLocalization(),
                        this,
                        current.getClassifier(),
                        current.getText()))
                .forEach(texts::add);
    }
}
