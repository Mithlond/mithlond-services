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
package se.mithlond.services.organisation.domain.model.membership.award

import se.mithlond.services.organisation.domain.model.NamedDescription
import se.mithlond.services.organisation.domain.model.Organisation
import se.mithlond.services.organisation.domain.model.Organisational
import java.io.Serializable
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ForeignKey
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
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
    UniqueConstraint(name = "unq_name_per_organisation", columnNames = ["name", "organisation_id"])
])
data class Award(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_Award")
        @field:SequenceGenerator(schema = "organisations", name = "seq_Award",
                sequenceName = "seq_Award", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 128)
        override var name: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 1024)
        override var description: String,

        @field:ManyToOne(optional = false)
        @field:JoinColumn(
                name = "awardtype_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_award_awardtype"))
        var awardType: AwardType,

        @field:ManyToOne(optional = false)
        @field:JoinColumn(
                name = "organisation_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_award_organisation"))
        override var organisation: Organisation,

        @field:OneToMany(mappedBy = "award",
                cascade = [CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE])
        var levels: List<AwardLevel> = mutableListOf()

) : Serializable, NamedDescription, Organisational, Comparable<Award> {

    override fun compareTo(other: Award): Int {

        var toReturn = this.organisation.compareTo(other.organisation)

        if(toReturn == 0) {
            toReturn = this.awardType.compareTo(other.awardType)
        }

        if(toReturn == 0) {
            toReturn = this.name.compareTo(other.name)
        }

        // All Done
        return toReturn
    }
}
