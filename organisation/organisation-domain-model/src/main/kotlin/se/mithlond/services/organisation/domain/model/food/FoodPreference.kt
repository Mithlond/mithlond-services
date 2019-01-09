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

import se.mithlond.services.organisation.domain.model.InternalUser
import se.mithlond.services.organisation.domain.model.localization.TextSuite
import se.mithlond.services.organisation.domain.model.localization.TextSuite.Companion.getRequiredTextSuiteValue
import java.io.Serializable
import java.util.Locale
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.ForeignKey
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.persistence.OneToOne

/**
 * Compound [FoodPreference] key definition.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Embeddable
@Access(value = AccessType.FIELD)
data class FoodPreferenceId(

        @field:Column(name = "name_id")
        var nameId: Long,

        @field:Column(name = "internaluser_id")
        var userId: Long

) : Serializable

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
data class FoodPreference(

        @EmbeddedId
        var foodPreferenceId: FoodPreferenceId? = null,

        /**
         * A localized texts instance containing the name/short description of this FoodPreference.
         */
        @field:MapsId("userId")
        @field:OneToOne(optional = false)
        @field:JoinColumn(
                name = "name_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_food_preference_name"))
        var names: TextSuite,

        /**
         * A localized texts instance containing the (full) description of this FoodPreference.
         */
        @field:OneToOne(optional = false)
        @field:JoinColumn(
                name = "description_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_food_preference_description"))
        var descriptions: TextSuite,

        @field:ManyToOne(optional = false)
        @field:JoinColumn(
                name = "user_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_food_preference_user"))
        @field:MapsId("userId")
        val user: InternalUser

) : Serializable {

    /**
     * Retrieves the Category name of this Food within the supplied [Locale].
     *
     * @param locale The Locale to find the Category name.
     */
    fun getCategoryName(locale: Locale = Locale.getDefault()): String =
            getRequiredTextSuiteValue(descriptions, "category", Food.NAME_CLASSIFICATION, locale)
}
