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
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Definition for persisted Locales. The JPA representation of Locales spread
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = LocaleDefinition.NAMEDQ_GET_BY_LANGUAGE_ONLY,
                query = "select l from LocaleDefinition l "
                        + " where l.language = :" + OrganisationPatterns.PARAM_LANGUAGE + " and l.country = ''"),
        @NamedQuery(name = LocaleDefinition.NAMEDQ_GET_BY_LANGUAGE_AND_COUNTRY,
                query = "select l from LocaleDefinition l "
                        + " where l.language = :" + OrganisationPatterns.PARAM_LANGUAGE + " and l.country = :"
                        + OrganisationPatterns.PARAM_COUNTRY)
})
@Entity
@Table(name = "locale_definitions", uniqueConstraints = {
        @UniqueConstraint(name = "unq_locale_parts", columnNames = {"language", "country", "variant"})})
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"locale"})
@XmlAccessorType(XmlAccessType.FIELD)
public class LocaleDefinition extends NazgulEntity implements Comparable<LocaleDefinition> {

    /**
     * NamedQuery for getting Localizations by only language (and also returning the locale with only language
     * matching the supplied value).
     */
    public static final String NAMEDQ_GET_BY_LANGUAGE_ONLY = "LocaleDefinition.getByLanguageOnly";

    /**
     * NamedQuery for getting Localizations by language and country.
     */
    public static final String NAMEDQ_GET_BY_LANGUAGE_AND_COUNTRY = "LocaleDefinition.getByLanguageAndCountry";

    /**
     * Comparator for LocaleDefinitions which only considers the language part of the LocaleDefinition.
     */
    @XmlTransient
    public static final Comparator<LocaleDefinition> LANGUAGE_COMPARATOR = (l, r) -> {

        // Fail fast
        if (l == null) {
            return -1;
        } else if (r == null) {
            return 1;
        }

        // All Done.
        return l.getLocale().getLanguage().compareTo(r.getLocale().getLanguage());
    };

    /**
     * Comparator for LocaleDefinitions which compares the full languageTag of each locale.
     */
    @XmlTransient
    public static final Comparator<LocaleDefinition> LOCALE_COMPARATOR = (l, r) -> {

        // Fail fast
        if (l == null) {
            return -1;
        } else if (r == null) {
            return 1;
        }

        // All Done.
        return l.getLocale().toLanguageTag().compareTo(r.getLocale().toLanguageTag());
    };


    /**
     * <p>Unmodifiable Set containing un-managed version of the LocaleDefinition constants found within this class.</p>
     */
    @XmlTransient
    public static final Set<LocaleDefinition> COMMON_LOCALES = Collections.unmodifiableSet(
            Stream.of(TimeFormat.SWEDISH_LOCALE, TimeFormat.DANISH_LOCALE, TimeFormat.NORWEGIAN_LOCALE, Locale.UK)
                    .map(LocaleDefinition::new)
                    .collect(Collectors.toSet()));

    /**
     * The language tag of this LocaleDefinition.
     */
    @Transient
    @XmlAttribute(required = true)
    private String locale;

    @XmlTransient
    @Basic(optional = false)
    @Column(nullable = false, length = 6)
    private String language;

    @XmlTransient
    @Basic(optional = false)
    @Column(nullable = false, length = 6)
    private String country;

    @XmlTransient
    @Basic(optional = false)
    @Column(nullable = false, length = 16)
    private String variant;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public LocaleDefinition() {
    }

    /**
     * Convenience constructor to create a LocaleDefinition from its language tag
     * (c.f. {@link Locale#forLanguageTag(String)}).
     *
     * @param languageTag The language tag for this LocaleDefinition. Cannot be null or empty.
     * @see Locale#forLanguageTag(String)
     */
    public LocaleDefinition(@NotNull final String languageTag) {
        this(Locale.forLanguageTag(languageTag));
    }

    /**
     * Convenience constructor creating a LocaleDefinition wrapping the supplied Locale.
     *
     * @param locale a Locale to convert into a LocaleDefinition.
     */
    public LocaleDefinition(@NotNull final Locale locale) {

        // Delegate
        super();

        // Assign internal state
        this.locale = locale.toLanguageTag();

        this.language = locale.getLanguage();
        this.country = locale.getCountry();
        this.variant = locale.getVariant();
    }

    /**
     * Converts this LocaleDefinition to a {@link Locale}.
     *
     * @return a {@link Locale} corresponding to this LocaleDefinition.
     * @see Locale
     */
    public Locale getLocale() {
        return new Locale(language, country, variant);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "LocaleDefinition: " + getLocale().toLanguageTag();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {

        // Fail fast
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        // Check own state
        final LocaleDefinition that = (LocaleDefinition) o;
        return Objects.equals(this.getLocale().toLanguageTag(), that.getLocale().toLanguageTag());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getLocale().toLanguageTag());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final LocaleDefinition that) {
        return LOCALE_COMPARATOR.compare(this, that);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateEntityState() throws InternalStateValidationException {

        // Check sanity
        InternalStateValidationException.create()
                .notNull(language, "language")
                .notNull(country, "country")
                .notNull(variant, "variant")
                .endExpressionAndValidate();
    }

    /**
     * Standard JAXB class-wide listener method, automagically invoked
     * immediately before this object is Marshalled.
     *
     * @param marshaller The active Marshaller.
     */
    @SuppressWarnings("all")
    private void beforeMarshal(final Marshaller marshaller) {
        this.locale = getLocale().toLanguageTag();
    }

    /**
     * This method is called after all the properties (except IDREF) are unmarshalled for this object,
     * but before this object is set to the parent object.
     */
    @SuppressWarnings("PMD")
    private void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {

        // Resurrect the JPA state
        final Locale tmp = Locale.forLanguageTag(this.locale);

        this.language = tmp.getLanguage();
        this.country = tmp.getCountry();
        this.variant = tmp.getVariant();
    }
}
