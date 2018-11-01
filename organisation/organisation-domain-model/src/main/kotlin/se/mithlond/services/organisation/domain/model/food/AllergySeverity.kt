package se.mithlond.services.organisation.domain.model.food

import se.mithlond.services.organisation.domain.model.DESCRIPTION_CLASSIFICATION
import se.mithlond.services.organisation.domain.model.NAME_CLASSIFICATION
import se.mithlond.services.organisation.domain.model.NamedDescription
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
        @Basic(optional = false)
        @Column(nullable = false, name = "severity_sort_order")
        var severitySortOrder: Int,

        /**
         * A localized texts instance containing the short description of this AllergySeverity.
         */
        @OneToOne(optional = false)
        @JoinColumn(name = "short_description_id")
        var names: TextSuite,

        /**
         * A localized texts instance containing the full description of this AllergySeverity.
         */
        @OneToOne(optional = false)
        @JoinColumn(name = "full_description_id")
        var descriptions: TextSuite

) : Serializable, Comparable<AllergySeverity> {

    override fun compareTo(other: AllergySeverity): Int = this.severitySortOrder - other.severitySortOrder

    fun getName(locale: Locale) : String = getRequiredTextSuiteValue(names, "names", locale, NAME_CLASSIFICATION)

    fun getDescription(locale: Locale) : String = getRequiredTextSuiteValue(descriptions,
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