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

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.Locale;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * Definition for a persisted Locale; required since {@link Locale} is not JAXB- or JPA-annotated.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = Localization.NAMEDQ_GET_BY_LANGUAGE,
                query = "select l from Localization l "
                        + " where l.language like :" + OrganisationPatterns.PARAM_LANGUAGE),
        @NamedQuery(name = Localization.NAMEDQ_GET_BY_LANGUAGE_AND_COUNTRY,
                query = "select l from Localization l "
                        + " where l.language like :" + OrganisationPatterns.PARAM_LANGUAGE
                        + " and l.country like :" + OrganisationPatterns.PARAM_COUNTRY),
        @NamedQuery(name = Localization.NAMEDQ_GET_BY_PRIMARY_KEYS,
                query = "select l from Localization l "
                        + " where l.id in :" + OrganisationPatterns.PARAM_IDS)
})
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "enum_localizations",
                columnNames = {"language", "country", "variant"})
})
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = "compactStringForm")
@XmlAccessorType(XmlAccessType.FIELD)
public class Localization extends NazgulEntity implements Comparable<Localization> {

    /**
     * NamedQuery for getting Localizations having a given language.
     */
    public static final String NAMEDQ_GET_BY_LANGUAGE = "Localization.getByLanguage";

    /**
     * NamedQuery for getting Localizations having a given language.
     */
    public static final String NAMEDQ_GET_BY_LANGUAGE_AND_COUNTRY = "Localization.getByLanguageAndCountry";

    /**
     * NamedQuery for getting Localizations with primary keys in a provided Collection of values.
     */
    public static final String NAMEDQ_GET_BY_PRIMARY_KEYS = "Localization.getByPrimaryKeys";

    private static final int COLUMN_WIDTH = 10;
    private static final String SEPARATOR = "..";
    private static final String NONE = "none";

    /**
     * The XML identifier/compact string form of this Localization, synthesized as [language]:[country]:[variant].
     */
    @Transient
    @XmlAttribute(required = true)
    private String compactStringForm;

    /**
     * The language code for this Localization.
     */
    @Basic(optional = false)
    @Column(nullable = false, length = COLUMN_WIDTH)
    @XmlTransient
    private String language;

    /**
     * The optional country code for this Localization.
     */
    @Column(length = COLUMN_WIDTH)
    @XmlTransient
    private String country;

    /**
     * The optional variant for this Localization.
     */
    @Column(length = COLUMN_WIDTH)
    @XmlTransient
    private String variant;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public Localization() {
    }

    /**
     * Convenience constructor to create a language-only localization.
     *
     * @param language The language code for this Localization. Cannot be null or empty.
     */
    public Localization(final String language) {
        this(language, null, null);
    }

    /**
     * Compound constructor creating a new Localization wrapping the supplied data.
     *
     * @param language The language code for this Localization. Cannot be null or empty.
     * @param country  The optional Country code for this Localization.
     * @param variant  The optional Variant code for this Localization.
     * @see Locale
     */
    public Localization(final String language, final String country, final String variant) {
        this.language = language;
        this.country = country;
        this.variant = variant;
    }

    /**
     * Converts this Localization to a {@link Locale}.
     *
     * @return a {@link Locale} corresponding to this Localization.
     * @see Locale
     */
    public Locale getLocale() {

        if (variant == null && country == null) {

            // Language-only localization
            return new Locale(language);
        } else if (variant == null) {

            // Language-and-Country localization
            return new Locale(language, country);
        }

        // Full localization available.
        return new Locale(language, country, variant);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return compactStringForm == null ? createCompactStringForm() : compactStringForm;
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
        final Localization that = (Localization) o;
        return Objects.equals(language, that.language)
                && Objects.equals(country, that.country)
                && Objects.equals(variant, that.variant);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(language, country, variant);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Localization that) {

        if (that == null) {
            return -1;
        } else if (that == this) {
            return 0;
        }

        int toReturn = this.language.compareTo(that.language);
        if (toReturn == 0) {

            // Handle null values.
            final String thisCountry = this.country == null ? "" : this.country;
            final String thatCountry = that.country == null ? "" : that.country;
            toReturn = thisCountry.compareTo(thatCountry);
        }
        if (toReturn == 0) {

            // Handle null values.
            final String thisVariant = this.variant == null ? "" : this.variant;
            final String thatVariant = that.variant == null ? "" : that.variant;
            toReturn = thisVariant.compareTo(thatVariant);
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        // Check sanity
        InternalStateValidationException.create()
                .notNullOrEmpty(language, "language")
                .endExpressionAndValidate();

        // Synthesize internal state
        createCompactStringForm();
    }

    /**
     * Builder method creating a Localization from the supplied compactStringForm.
     *
     * @param compactStringForm A compact string form supplied on the form
     *                          <code>[language]:[country]:[variant]</code>.
     * @return A Localization created from the supplied compactForm.
     */
    public static Localization parse(final String compactStringForm) {

        // Parse the compactStringForm to acquire the internal state variables
        final StringTokenizer tok = new StringTokenizer(compactStringForm, SEPARATOR, false);
        final String incorrectMessage = "Expected 'compactStringForm' on the form [country]" + SEPARATOR + "[language]"
                + SEPARATOR + "[variant], where [language] and [variant] may be null, indicated by the value '"
                + NONE + "'. Got incorrect toParse argument '" + compactStringForm
                + "'. This implies an internal error.";
        Validate.isTrue(tok.countTokens() == 3, incorrectMessage);

        // Populate the internal state
        final String language = tok.nextToken();
        final String tmpCountry = tok.nextToken();
        final String tmpVariant = tok.nextToken();

        final String country = NONE.equalsIgnoreCase(tmpCountry) ? null : tmpCountry;
        final String variant = NONE.equalsIgnoreCase(tmpVariant) ? null : tmpVariant;

        // All done.
        return new Localization(language, country, variant);
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
        this.compactStringForm = createCompactStringForm();
    }

    private String createCompactStringForm() {

        // Synthesize the XmlID
        final String countryString = country == null || country.isEmpty() ? NONE : country;
        final String variantString = variant == null || variant.isEmpty() ? NONE : variant;

        // All done.
        return language + SEPARATOR + countryString + SEPARATOR + variantString;
    }

    /**
     * This method is called after all the properties (except IDREF) are unmarshalled for this object,
     * but before this object is set to the parent object.
     */
    @SuppressWarnings("PMD")
    private void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {

        final Localization parsed = parse(compactStringForm);
        this.language = parsed.language;
        this.country = parsed.country;
        this.variant = parsed.variant;
    }
}
