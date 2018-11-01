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

import se.mithlond.services.organisation.domain.model.Category
import se.mithlond.services.organisation.domain.model.localization.DEFAULT_CLASSIFIER
import se.mithlond.services.organisation.domain.model.localization.TextSuite
import java.io.Serializable
import java.util.Comparator
import java.util.Locale
import java.util.SortedSet
import java.util.TreeSet
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ForeignKey
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * A Locale-aware classification of Foods.
 *
 * @param foodNames The [TextSuite] containing localized texts for the name of this food.
 * @param category The top-level Categorisation of this Food.
 * @param subCategory The sub-level Categorisation of this Food.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations")
data class Food(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_Food")
        @field:SequenceGenerator(schema = "organisations", name = "seq_Food",
                sequenceName = "seq_Food", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @field:OneToOne(optional = false)
        @field:JoinColumn(
                name = "foodnames_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_food_foodnames"))
        var foodNames: TextSuite,

        @field:ManyToOne(optional = false, cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH])
        @field:JoinColumn(
                name = "category_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_food_category_textsuite"))
        var category: TextSuite,

        @field:ManyToOne(optional = false, cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH])
        @field:JoinColumn(
                name = "sub_category_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_food_subcategory_textsuite"))
        var subCategory: TextSuite

) : Serializable {

    /**
     * Validates that this Food object contains all required localisations for the supplied Locale.
     *
     * @param theLocale The Locale for which the internal state of this Food obejct should be validated.
     */
    fun validateInternalStateFor(theLocale: Locale) {

        // Ensure that all required texts are available
        getCategoryName(theLocale)
        getCategoryDescription(theLocale)
        getSubCategoryName(theLocale)
        getSubCategoryDescription(theLocale)
        getFoodName(theLocale)
        getFoodDescription(theLocale)
    }

    /**
     * Retrieves a synthetic Category for this Food.
     *
     * @param locale The Locale used to synthesize the category.
     */
    fun getCategory(locale: Locale) = Category(null,
            getCategoryName(locale),
            "category",
            getCategoryDescription(locale))

    /**
     * Retrieves a synthetic SubCategory for this Food.
     *
     * @param locale The Locale used to synthesize the sub-category.
     */
    fun getSubCategory(locale: Locale) = Category(null,
            getSubCategoryName(locale),
            "subCategory",
            getSubCategoryDescription(locale))

    /**
     * Retrieves the Category name of this Food within the supplied [Locale].
     *
     * @param locale The Locale to find the Category name.
     */
    fun getCategoryName(locale: Locale = Locale.getDefault()): String =
            getRequiredTextSuiteValue(category, "category", locale, NAME_CLASSIFICATION)

    /**
     * Retrieves the Category description of this Food within the supplied [Locale].
     *
     * @param locale The Locale to find the Category description.
     */
    fun getCategoryDescription(locale: Locale = Locale.getDefault()): String =
            getRequiredTextSuiteValue(category, "category", locale, DESCRIPTION_CLASSIFICATION)

    /**
     * Retrieves the SubCategory name of this Food within the supplied [Locale].
     *
     * @param locale The Locale to find the SubCategory name.
     */
    fun getSubCategoryName(locale: Locale = Locale.getDefault()): String =
            getRequiredTextSuiteValue(subCategory, "subCategory", locale, NAME_CLASSIFICATION)

    /**
     * Retrieves the SubCategory description of this Food within the supplied [Locale].
     *
     * @param locale The Locale to find the SubCategory description.
     */
    fun getSubCategoryDescription(locale: Locale = Locale.getDefault()): String =
            getRequiredTextSuiteValue(subCategory, "subCategory", locale, DESCRIPTION_CLASSIFICATION)

    /**
     * Retrieves the Food name within the supplied [Locale].
     *
     * @param locale The Locale to find the Food name.
     */
    fun getFoodName(locale: Locale = Locale.getDefault()): String =
            getRequiredTextSuiteValue(foodNames, "foodNames", locale, NAME_CLASSIFICATION)

    /**
     * Retrieves the Food description within the supplied [Locale].
     *
     * @param locale The Locale to find the Food description.
     */
    fun getFoodDescription(locale: Locale = Locale.getDefault()): String =
            getRequiredTextSuiteValue(foodNames, "foodNames", locale, DESCRIPTION_CLASSIFICATION)


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
                    "classification [$NAME_CLASSIFICATION] for locale [${locale.toLanguageTag()}]. " +
                    "This implies a data/database error.")
        }
    }

    companion object {

        /**
         * The ClassifiedLocalizedText classification for the Name of food/category/subCategory.
         */
        @JvmStatic
        val NAME_CLASSIFICATION = DEFAULT_CLASSIFIER

        /**
         * The ClassifiedLocalizedText classification for the Description of food/category/subCategory.
         */
        @JvmStatic
        val DESCRIPTION_CLASSIFICATION = "Description"

        /**
         * Retrieves a Comparator performing a locale-aware comparison of Category/SubCategory/Foodname.
         *
         * @return A [Comparator] for a locale-sensitive Food comparison.
         */
        @JvmStatic
        fun getFoodComparator(locale: Locale): Comparator<Food> = kotlin.Comparator { l, r ->

            // Get required data
            var toReturn = l.getCategoryName(locale).compareTo(r.getCategoryName(locale))

            if (toReturn == 0) {
                toReturn = l.getSubCategoryName(locale).compareTo(r.getSubCategoryName(locale))
            }

            if (toReturn == 0) {
                toReturn = l.getFoodName(locale).compareTo(r.getFoodName(locale))
            }

            // All Done.
            toReturn
        }

        /**
         * @return a SortedSet using the [getFoodComparator] as Comparator for the supplied Locale.
         */
        @JvmStatic
        fun mutableSortedSetOf(locale: Locale): SortedSet<Food> = TreeSet<Food>(getFoodComparator(locale))
    }
}
