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
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * Definition for persisted Locales.
 * This JPA representation of Locales spread its internal state into columns to facilitate searching.
 * 
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations", uniqueConstraints = [
    UniqueConstraint(name = "unq_language_country_variant", columnNames = ["language", "country", "variant"])
])
data class LocaleDefinition @JvmOverloads constructor(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_LocaleDefinition")
        @field:SequenceGenerator(schema = "organisations", name = "seq_LocaleDefinition",
                sequenceName = "seq_LocaleDefinition", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 6)
        var language: String = "",

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 6)
        var country: String = "",

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 16)
        var variant: String = ""

) : Serializable, Comparable<LocaleDefinition> {

    /**
     * Convenience constructor creating a LocaleDefinition wrapping the supplied Locale.
     *
     * @param locale a Locale to convert into a LocaleDefinition.
     */
    constructor(locale: Locale) : this(null, locale.language, locale.country, locale.variant)

    /**
     * Converts this LocaleDefinition to a [java.util.Locale].
     *
     * @return a [Locale] corresponding to this LocaleDefinition.
     * @see Locale
     */
    fun getLocale(): Locale = Locale(language, country, variant)

    override fun compareTo(other: LocaleDefinition): Int {

        var toReturn = this.language.compareTo(other.language)

        if(toReturn == 0) {
            toReturn = this.country.compareTo(other.country)
        }

        if(toReturn == 0) {
            toReturn = this.variant.compareTo(other.variant)
        }

        // All Done
        return toReturn
    }
}
