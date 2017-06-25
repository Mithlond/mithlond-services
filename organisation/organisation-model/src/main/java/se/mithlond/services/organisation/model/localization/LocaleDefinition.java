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

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Definition for a persisted Locale which uses {@link se.mithlond.services.shared.spi.jaxb.adapter.LocaleAdapter} to
 * convert to a JAXB representation and {@link se.mithlond.services.shared.spi.jpa.converter.LocaleConverter} to
 * convert to a JPA representation. This is required since the native {@link Locale} class has no JAXB- or
 * JPA-annotations. The LocaleDefinitions hence has the appearance of an Enum, persisted within the Database.
 * However, the value persisted and transmitted correspond to the {@link Locale#toLanguageTag()} and
 * {@link Locale#forLanguageTag(String)} values respectively.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = LocaleDefinition.NAMEDQ_GET_BY_LOCALE,
                query = "select l from LocaleDefinition l "
                        + " where l.locale like :" + OrganisationPatterns.PARAM_LANGUAGE)
})
@Entity
@Table(name = "locale_definitions")
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"locale"})
@XmlAccessorType(XmlAccessType.FIELD)
public class LocaleDefinition implements Serializable, Validatable, Comparable<LocaleDefinition> {

    /**
     * NamedQuery for getting Localizations having a given language.
     */
    public static final String NAMEDQ_GET_BY_LOCALE = "LocaleDefinition.getByLocale";

    /**
     * <p>Unmodifiable Set containing un-managed version of the LocaleDefinition constants found within this class.</p>
     */
    @XmlTransient
    public static final Set<LocaleDefinition> COMMON_LOCALES = Collections.unmodifiableSet(
            Stream.of(TimeFormat.SWEDISH_LOCALE, TimeFormat.DANISH_LOCALE, TimeFormat.NORWEGIAN_LOCALE, Locale.UK)
                    .map(LocaleDefinition::new)
                    .collect(Collectors.toSet()));


    @Id
    @Column(nullable = false, length = 32)
    @XmlAttribute(required = true)
    private Locale locale;

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
        this.locale = locale;
    }

    /**
     * Converts this LocaleDefinition to a {@link Locale}.
     *
     * @return a {@link Locale} corresponding to this LocaleDefinition.
     * @see Locale
     */
    public Locale getLocale() {
        return this.locale;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "LocaleDefinition: " + this.locale.toLanguageTag();
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
        return Objects.hash(locale);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final LocaleDefinition that) {

        // Fail fast
        if (that == null) {
            return -1;
        } else if (that == this) {
            return 0;
        }

        // Delegate to internal state
        return this.getLocale().toLanguageTag().compareTo(that.getLocale().toLanguageTag());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        // Check sanity
        InternalStateValidationException.create()
                .notNullOrEmpty(locale, "locale")
                .endExpressionAndValidate();
    }
}
