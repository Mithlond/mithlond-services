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
package se.mithlond.services.organisation.domain.model.food

import se.mithlond.services.organisation.domain.model.DESCRIPTION_CLASSIFICATION
import se.mithlond.services.organisation.domain.model.NAME_CLASSIFICATION
import se.mithlond.services.organisation.domain.model.localization.TextSuite
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
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * Defines the severity levels Allergies.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations", uniqueConstraints = [
    UniqueConstraint(name = "unq_severity_sort_order", columnNames = ["severitySortOrder"])
])
data class AllergySeverity(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_AllergySeverity")
        @field:SequenceGenerator(schema = "organisations", name = "seq_AllergySeverity",
                sequenceName = "seq_AllergySeverity", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        /**
         * The sort order of the severity, with less severe allergies having lower severitySortOrder values.
         */
        @field:Basic(optional = false)
        @field:Column(nullable = false, name = "severity_sort_order")
        var severitySortOrder: Int,

        /**
         * A localized texts instance containing the short description of this AllergySeverity.
         */
        @field:OneToOne(optional = false)
        @field:JoinColumn(name = "short_description_id")
        var names: TextSuite,

        /**
         * A localized texts instance containing the full description of this AllergySeverity.
         */
        @field:OneToOne(optional = false)
        @field:JoinColumn(name = "full_description_id")
        var descriptions: TextSuite

) : Serializable, Comparable<AllergySeverity> {

    /**
     * Validates that this AllergySeverity object contains all required localisations for the supplied Locale.
     *
     * @param theLocale The Locale for which the internal state of this AllergySeverity object should be validated.
     */
    fun validateInternalStateFor(theLocale: Locale) {

        // Ensure that all required texts are available
        getName(theLocale)
        getDescription(theLocale)
    }

    override fun compareTo(other: AllergySeverity): Int = this.severitySortOrder - other.severitySortOrder

    /**
     * Retrieves the name of this AllergySeverity within the supplied Locale.
     *
     * @return The name of this AllergySeverity in the given Locale.
     * @throws IllegalArgumentException if the name was not provided within the supplied Locale.
     */
    @Throws(IllegalArgumentException::class)
    fun getName(locale: Locale): String = getRequiredTextSuiteValue(names, "names", locale, NAME_CLASSIFICATION)

    /**
     * Retrieves the description of this AllergySeverity within the supplied Locale.
     *
     * @return The description of this AllergySeverity in the given Locale.
     * @throws IllegalArgumentException if the description was not provided within the supplied Locale.
     */
    fun getDescription(locale: Locale): String = getRequiredTextSuiteValue(descriptions,
            "descriptions",
            locale,
            DESCRIPTION_CLASSIFICATION)

    companion object {

        @JvmStatic
        @Throws(IllegalStateException::class)
        private fun getRequiredTextSuiteValue(textSuite: TextSuite,
                                              suiteName: String,
                                              locale: Locale,
                                              classifier: String): String {

            // Retrieve the value ... which should be non-null
            val expectedText = textSuite.getText(locale, classifier)

            return when (expectedText != null) {
                true -> expectedText!!
                false -> throw IllegalStateException("TextSuite [$suiteName] lacks " +
                        "classification [${Food.NAME_CLASSIFICATION}] for locale [${locale.toLanguageTag()}]. " +
                        "This implies a data/database error.")
            }
        }
    }
}
