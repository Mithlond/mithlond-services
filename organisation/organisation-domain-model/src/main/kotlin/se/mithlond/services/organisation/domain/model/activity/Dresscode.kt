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
package se.mithlond.services.organisation.domain.model.activity

import se.mithlond.services.organisation.domain.model.Listable
import se.mithlond.services.organisation.domain.model.Organisation
import se.mithlond.services.organisation.domain.model.address.CategorizedAddress
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.Column
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
 * Entity class defining an DressCode within a particular organisation
 *
 * @param id The JPA ID of this Domain Entity.
 * @param shortDesc The mandatory and non-empty short description of this Listable entity.
 * Typically used within short info boxes and pop-ups.
 * @param fullDesc The full description of this Listable entity, visible in detailed listings.
 * May not be null or empty. Typically used within longer info boxes or modal description displays.
 * @param owningOrganisation The [Organisation] owning this [CategorizedAddress].
 * @param dressCode The Dresscode name, such as "Midgårda dräkt". A single word (or two...) unique to this DressCode.
 * Mostly used in all GUI-type of selections. Refer to shortDesc and fullDesc for richer descriptions on this DressCode.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations", uniqueConstraints = [
    UniqueConstraint(name = "unq_dresscode_per_organisation", columnNames = ["dressCode", "organisation_id"])
])
data class Dresscode(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_Dresscode")
        @field:SequenceGenerator(schema = "organisations", name = "seq_Dresscode",
                sequenceName = "seq_Dresscode", allocationSize = 1)
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
                foreignKey = ForeignKey(name = "fk_dresscode_organisation"))
        override var owningOrganisation: Organisation,

        @Basic(optional = false)
        @Column(length = 64, nullable = false)
        private val dressCode: String
) : Listable, Comparable<Dresscode> {

    override fun compareTo(other: Dresscode): Int {

        var toReturn = this.owningOrganisation.compareTo(other.owningOrganisation)

        if(toReturn == 0) {
            toReturn = this.dressCode.compareTo(other.dressCode)
        }

        // All Done.
        return toReturn
    }
}
