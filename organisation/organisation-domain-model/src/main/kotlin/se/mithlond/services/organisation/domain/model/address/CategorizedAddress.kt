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
package se.mithlond.services.organisation.domain.model.address

import se.mithlond.services.organisation.domain.model.Category
import se.mithlond.services.organisation.domain.model.Listable
import se.mithlond.services.organisation.domain.model.Organisation
import java.io.Serializable
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.ForeignKey
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * A classified and categorized Address that belongs to a certain Organisation.
 * CategorizedAddresses should be available only within service calls from the
 * Organisation to which they belong.
 *
 * @param id The JPA ID of this Domain Entity.
 * @param shortDesc The mandatory and non-empty short description of this Listable entity.
 * Typically used within short info boxes and pop-ups.
 * @param fullDesc The full description of this Listable entity, visible in detailed listings.
 * May not be null or empty. Typically used within longer info boxes or modal description displays.
 * @param owningOrganisation The [Organisation] owning this [CategorizedAddress].
 * @param category the [Category] of this [CategorizedAddress]
 * @param address the [Address] within this [CategorizedAddress]
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 * @see Listable
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations", uniqueConstraints = [
    UniqueConstraint(
            name = "unq_shortdesc_per_category_org",
            columnNames = ["shortDesc", "category_id", "owningOrganisation_id"])
])
data class CategorizedAddress(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_CategorizedAddress")
        @field:SequenceGenerator(schema = "organisations", name = "seq_CategorizedAddress",
                sequenceName = "seq_CategorizedAddress", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 254)
        override var shortDesc: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 2048)
        override var fullDesc: String,

        @field:ManyToOne(optional = false)
        @field:JoinColumn(
                name = "organisation_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_cataddress_organisation"))
        override var owningOrganisation: Organisation,

        @field:ManyToOne(optional = false, cascade = [CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH])
        @field:JoinColumn(
                name = "category_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_cataddress_category"))
        var category: Category,

        @Embedded
        var address: Address

) : Listable, Serializable, Comparable<CategorizedAddress> {

    override fun compareTo(other: CategorizedAddress): Int {

        var toReturn = this.owningOrganisation.compareTo(other.owningOrganisation)

        if(toReturn == 0) {
            toReturn = this.category.compareTo(other.category)
        }

        if(toReturn == 0) {
            toReturn = this.address.compareTo(other.address)
        }

        // All Done.
        return toReturn
    }

    companion object {

        /**
         * The constant value indicating that a [CategorizedAddress] is for Activities.
         */
        @JvmStatic
        val ACTIVITY_CLASSIFICATION = "activity_locale"
    }
}
