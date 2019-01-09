/*-
 * #%L
 * Nazgul Project: mithlond-services-organisation-domain-model
 * %%
 * Copyright (C) 2018 jGuru Europe AB
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
package se.mithlond.services.organisation.domain.model.localization

import java.io.Serializable
import java.util.Locale

/**
 * The default/standard classifier if none is provided by the caller.
 */
const val DEFAULT_CLASSIFIER = "Default"

/**
 * Specification for a DB-stored text which can be localized into several languages.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface Localizable : Serializable {

    /**
     * Retrieves the default text of this AbstractAuthorizedNavItem.
     *
     * @return The default text of this AbstractAuthorizedNavItem.
     */
    fun getText(): String? = getText(null, DEFAULT_CLASSIFIER)

    /**
     * Retrieves the default text of this AbstractAuthorizedNavItem.
     *
     * @param classifier The classifier of the text to retrieve.
     * @return The default text of this AbstractAuthorizedNavItem.
     */
    fun getText(classifier: String?): String? = getText(null, classifier)

    /**
     * The text in the supplied locale and for the given classifier.
     *
     * @param classifier The classifier of the text to retrieve.
     * @param locale     The locale for which a text should be retrieved.
     * @return The text in the supplied locale, or a standard Locale if no text was found for the supplied
     * Locale or if the supplied locale argument was `null`.
     */
    fun getText(locale: Locale?, classifier: String?): String?

    /**
     * Retrieves the text for a locale with the supplied language and optional country.
     *
     * @param languageTag The language tag value for a Locale (c.f. [Locale.toLanguageTag]).
     * @param classifier  The classifier of the text to retrieve.
     * @return the text for a locale with the supplied language and optional country.
     */
    fun getText(languageTag: String, classifier: String): String? =
            getText(Locale.forLanguageTag(languageTag), classifier)
}
