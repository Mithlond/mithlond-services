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

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import se.mithlond.services.organisation.domain.model.localization.TextSuite.Companion.getRequiredTextSuiteValue
import java.io.Serializable
import java.util.Comparator
import java.util.Locale
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ForeignKey
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table

// Our log
private val log: Logger = LoggerFactory.getLogger(Category::class.java)

/**
 * Entity definition of a Category with a Classification and a trivial Description.
 *
 * @param id The JPA ID of this Domain Entity.
 * @param name The name/short description, typically a single word. Cannot be null or empty.
 * @param classification A classification of this name, such as "Restaurant". This is intended to simplify
 * separating a type of Categories from others.
 * @param description The (fuller/richer) description of this Category. Cannot be null or empty.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations")
data class Category @JvmOverloads constructor(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_Category")
        @field:SequenceGenerator(schema = "organisations", name = "seq_Category",
                sequenceName = "seq_Category", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @field:OneToOne(optional = false)
        @field:JoinColumn(
                name = "texts_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_category_texts"))
        private var texts: TextSuite) : LocalizedComparable<Category>, Serializable {

    override fun compareTo(locale: Locale?, other: Category): Int = getComparator(locale).compare(this, other)

    /**
     * Retrieves the name of this category within the supplied Locale.
     *
     * @param locale The Locale to find the Category name.
     * @return The name of this [Category]
     */
    fun getName(locale: Locale = Locale.getDefault()): String =
            getRequiredTextSuiteValue(texts, "category", Category.NAME_CLASSIFICATION, locale)

    /**
     * Retrieves the classification of this [Category] within the supplied [Locale].
     *
     * @param locale The Locale to find the Category classification.
     * @return The classification of this [Category]
     */
    fun getClassification(locale: Locale = Locale.getDefault()): String =
            getRequiredTextSuiteValue(texts, "category", Category.CLASSIFICATION_CLASSIFICATION, locale)

    /**
     * Retrieves the classification of this [Category] within the supplied [Locale].
     *
     * @param locale The Locale to find the Category classification.
     * @return The classification of this [Category]
     */
    fun getDescription(locale: Locale = Locale.getDefault()): String =
            getRequiredTextSuiteValue(texts, "category", Category.DESCRIPTION_CLASSIFICATION, locale)

    override fun getComparator(locale: Locale?): Comparator<Category> {

        val effectiveLocale = locale ?: let {

            val toUse = texts.standardLocale
            if (log.isInfoEnabled) {
                log.info("Null comparator Locale supplied. Using from internal TextSuite: $toUse")
            }

            // All Done.
            toUse.getLocale()
        }

        // All Done.
        return Comparator { l, r ->

            var toReturn = l.getClassification(effectiveLocale).compareTo(r.getClassification(effectiveLocale))

            if (toReturn == 0) {
                toReturn = l.getName(effectiveLocale).compareTo(r.getName(effectiveLocale))
            }

            if (toReturn == 0) {
                toReturn = l.getDescription(effectiveLocale).compareTo(r.getDescription(effectiveLocale))
            }

            // All Done.
            toReturn
        }
    }

    companion object {

        @JvmStatic
        private val CATEGORY_SUITE_ID = "category";

        /**
         * The ClassifiedLocalizedText classification for the category Name.
         */
        @JvmStatic
        val NAME_CLASSIFICATION = DEFAULT_CLASSIFIER

        /**
         * The ClassifiedLocalizedText classification for the category Classification.
         */
        @JvmStatic
        val CLASSIFICATION_CLASSIFICATION = "Classification"

        /**
         * The ClassifiedLocalizedText classification for the category Description.
         */
        @JvmStatic
        val DESCRIPTION_CLASSIFICATION = "Description"

        /*
        Category(
        name = categoryName,
        classification = classification,
        description = "Address type [$classification :: $categoryName]")
        */
    }
}
