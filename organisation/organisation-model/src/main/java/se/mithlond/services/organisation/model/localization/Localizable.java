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

import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Specification for a text which can be localized into several languages.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@FunctionalInterface
public interface Localizable {

    /**
     * Standard locale unless another is provided.
     */
    @XmlTransient
    LocaleDefinition DEFAULT_LOCALE = new LocaleDefinition("sv", "SE", null);

    /**
     * Retrieves the default text of this AbstractAuthorizedNavItem.
     *
     * @return The default text of this AbstractAuthorizedNavItem.
     */
    default String getText() {
        return getText(DEFAULT_LOCALE);
    }

    /**
     * The text in the supplied locale.
     *
     * @param localeDefinition The localization for which a text should be retrieved.
     * @return The text in the supplied locale, or {@link #DEFAULT_LOCALE} if no text was found for the supplied
     * Locale or if the supplied locale argument was {@code null}.
     */
    String getText(LocaleDefinition localeDefinition);

    /**
     * Retrieves the text for a locale with the supplied language and optional country.
     *
     * @param language The language code (such as "sv") of a Locale for which the text of this Localizable
     *                 should be retrieved.
     * @param country  The country code (such as "SE") of a Locale for which the text of this
     *                 Localizable should be retrieved. The country code may be null.
     * @param variant The variant of a Localization for which the text of this Localizable should be retrieved.
     *                The variant can be {@code null}.
     * @return the text for a locale with the supplied language and optional country.
     */
    default String getText(String language, String country, String variant) {

        // Handle the arguments.
        final String nonEmptyLang = Validate.notEmpty(language, "language");

        // Delegate.
        return getText(new LocaleDefinition(nonEmptyLang, country, variant));
    }
}
