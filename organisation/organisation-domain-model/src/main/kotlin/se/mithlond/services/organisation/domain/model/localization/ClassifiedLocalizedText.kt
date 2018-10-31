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
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.ForeignKey
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.persistence.PrePersist
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * Compound key definition for ClassifiedLocalizedTexts.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Embeddable
@Access(value = AccessType.FIELD)
data class ClassifiedLocalizedTextId(

        @field:Column(name = "suite_id")
        var textSuiteId: Long,

        @field:Column(name = "locale_definition_id")
        var localeId: Long,

        @field:Column
        var classifier: String) : Serializable

/**
 * # Localized and Classified text
 *
 * Text snippet with a classification and a localization. Part of a [TextSuite].
 *
 * ### Note!
 *
 * Since different DB-native collations cannot be enforced on a single column
 * containing several texts in different localizations, sorting in locale-correct order is the responsibility of
 * the application. This is a classical DB problem for localized texts which is beyond the scope of this service
 * model to solve.
 *
 * @param id The embedded JPA Key.
 * @param localeDefinition The [LocaleDefinition] indicating the language of this [ClassifiedLocalizedText].
 * @param classifier The classifier of this [ClassifiedLocalizedText].
 * @param textSuite The [TextSuite] which contains this [ClassifiedLocalizedText].
 * @param text The text of this LocalizedText object, which should be given in the [LocaleDefinition]
 * given by the [localeDefinition] field.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations", uniqueConstraints = [
    UniqueConstraint(name = "unq_suite_identifier", columnNames = ["suiteIdentifier"])
])
data class ClassifiedLocalizedText(

        @field:EmbeddedId
        var id: ClassifiedLocalizedTextId? = null,

        @field:MapsId("localeId")
        @field:ManyToOne(optional = false, cascade = [CascadeType.DETACH])
        @field:JoinColumn(
                name = "locale_definition_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_classified_text_locale"))
        var localeDefinition: LocaleDefinition,

        @field:MapsId("classifier")
        @field:ManyToOne(optional = false, cascade = [CascadeType.DETACH])
        @field:JoinColumn(
                nullable = false,
                foreignKey = ForeignKey(name = "fk_classified_text_classifier"))
        var classifier: String,

        @field:MapsId("textSuiteId")
        @field:ManyToOne(optional = false, cascade = [CascadeType.DETACH])
        @field:JoinColumn(
                nullable = false,
                foreignKey = ForeignKey(name = "fk_classified_text_suite"))
        var textSuite: TextSuite,

        @Basic(optional = false)
        @Column(nullable = false, length = 2048)
        var text: String

) : Serializable {

    constructor(localeDefinition: LocaleDefinition, classifier: String, textSuite: TextSuite, text: String)
            : this(null, localeDefinition, classifier, textSuite, text)

    init {
        synchronizeKeyValues()
    }

    /**
     * Copies the ID values of the respective PK columns.
     */
    @PrePersist
    fun synchronizeKeyValues() {

        // Harmonize the state of the PK
        val localeDefID = this.localeDefinition.id!!
        val suiteID = this.textSuite.id!!

        when (this.id == null) {
            true -> this.id = ClassifiedLocalizedTextId(suiteID, localeDefID, classifier)
            else -> {

                val theID = this.id!!

                theID.localeId = localeDefID
                theID.textSuiteId = suiteID
                theID.classifier = classifier
            }
        }
    }
}