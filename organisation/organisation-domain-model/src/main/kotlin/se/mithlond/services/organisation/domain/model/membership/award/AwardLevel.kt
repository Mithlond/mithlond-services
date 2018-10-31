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
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
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
 * A Level description within an Award.
 * Typically, an Award contains several levels - each with different descriptions.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations", uniqueConstraints = [
    UniqueConstraint(name = "unq_name_per_award", columnNames = ["name", "award_id"]),
    UniqueConstraint(name = "unq_index_per_award", columnNames = ["index", "award_id"])
])
data class AwardLevel(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_AwardLevel")
        @field:SequenceGenerator(schema = "organisations", name = "seq_AwardLevel",
                sequenceName = "seq_AwardLevel", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @Basic(optional = false)
        @Column(nullable = false)
        val index: Int,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 255)
        override var name: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 2048)
        override var description: String,

        @field:ManyToOne(optional = false,
                fetch = FetchType.EAGER,
                cascade = [CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE])
        @field:JoinColumn(
                name = "award_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_awardlevel_award"))
        var award: Award

) : NamedDescription, Comparable<AwardLevel> {

    override fun compareTo(other: AwardLevel): Int {

        var toReturn = this.award.compareTo(other.award)

        if (toReturn == 0) {
            toReturn = this.index.compareTo(other.index)
        }

        // All Done.
        return toReturn
    }
}
