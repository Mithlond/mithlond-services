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
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import java.io.Serializable;
import java.util.Locale;

/**
 * Specification for a text which can be localized into several languages.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@FunctionalInterface
public interface Localizable extends Serializable {

    /**
     * Default classifier value for Localizables without a particular
     * classifier such as "Short Description", "Full Description" etc.
     */
    String DEFAULT_CLASSIFIER = "Default";

    /**
     * Retrieves the default text of this AbstractAuthorizedNavItem.
     *
     * @return The default text of this AbstractAuthorizedNavItem.
     */
    default String getText() {
        return getText(TimeFormat.SWEDISH_LOCALE, DEFAULT_CLASSIFIER);
    }

    /**
     * The text in the supplied locale and for the given classifier.
     *
     * @param classifier The classifier of the text to retrieve.
     * @param locale     The locale for which a text should be retrieved.
     * @return The text in the supplied locale, or a standard Locale if no text was found for the supplied
     * Locale or if the supplied locale argument was {@code null}.
     */
    String getText(Locale locale, String classifier);

    /**
     * Retrieves the text for a locale with the supplied language and optional country.
     *
     * @param languageTag The language tag value for a Locale (c.f. {@link Locale#toLanguageTag()}).
     * @param classifier  The classifier of the text to retrieve.
     * @return the text for a locale with the supplied language and optional country.
     */
    default String getText(String languageTag, String classifier) {

        // Handle the arguments.
        final String nonEmptyLangTag = Validate.notEmpty(languageTag, "languageTag");

        // Delegate.
        return getText(Locale.forLanguageTag(nonEmptyLangTag), classifier);
    }
}
