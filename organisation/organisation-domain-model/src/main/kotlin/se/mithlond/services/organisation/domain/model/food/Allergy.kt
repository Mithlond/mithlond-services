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
import java.io.Serializable
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.ForeignKey
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.persistence.OneToOne
import javax.persistence.PrePersist

/**
 * Compound [Allergy] key definition.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Embeddable
@Access(value = AccessType.FIELD)
data class AllergyId(

        @Column(name = "food_id")
        var foodId: Long,

        @Column(name = "user_id")
        var userId: Long) : Serializable

/**
 * Entity specification of an Allergy.
 * 
 * @param id The JPA ID of this Entity.
 * @param food The Food of this Allergy
 * @param user The [InternalUser] having this Allergy
 * @param severity The severity of this Allergy.
 * @param note An optional (i.e. nullable) free-text note of this Allergy.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
data class Allergy(

        @field:EmbeddedId
        var id: AllergyId? = null,

        @field:MapsId("foodId")
        @field:ManyToOne(optional = false, cascade = [CascadeType.MERGE, CascadeType.DETACH])
        @field:JoinColumn(
                name = "food_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_allergy_food"))
        var food: Food,

        @field:MapsId("userId")
        @field:ManyToOne(optional = false, cascade = [CascadeType.MERGE, CascadeType.DETACH])
        @field:JoinColumn(
                name = "user_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_allergy_user"))
        var user: InternalUser,

        @field:OneToOne(optional = false)
        @field:JoinColumn(nullable = false, name = "severity_id")
        var severity: AllergySeverity,

        @field:Basic
        @field:Column
        var note: String? = null

) : Serializable {

    constructor(food: Food, user: InternalUser, severity: AllergySeverity, note: String? = null)
            : this(null, food, user, severity, note)

    init {
        synchronizeKeyValues()
    }

    /**
     * Copies the ID values of the respective PK columns.
     */
    @PrePersist
    fun synchronizeKeyValues() {

        // Harmonize the state of the PK
        val foodID = this.food.id!!
        val userID = this.user.id!!

        when (this.id == null) {
            true -> this.id = AllergyId(foodID, userID)
            else -> {

                val theID = this.id!!

                theID.foodId = foodID
                theID.userId = userID
            }
        }
    }
}
