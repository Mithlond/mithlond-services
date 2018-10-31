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
package se.mithlond.services.organisation.domain.model.membership.guild

import se.mithlond.services.organisation.domain.model.NamedDescription
import se.mithlond.services.organisation.domain.model.STANDARD_NAMED_DESCRIPTION_COMPARATOR
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 *
 * @param id The JPA ID of this GuildMembershipType
 * @param name The name of this GuildMembershipType. Unique property.
 * @param description A description of this GuildMembershipType. 
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations", uniqueConstraints = [
    UniqueConstraint(name = "unq_name_per_guildmemshiptype", columnNames = ["name"])
])
data class GuildMembershipType(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_GuildMembershipType")
        @field:SequenceGenerator(schema = "organisations", name = "seq_GuildMembershipType",
                sequenceName = "seq_GuildMembershipType", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 254)
        override var name: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 2048)
        override var description: String

) : NamedDescription, Comparable<GuildMembershipType> {

    override fun compareTo(other: GuildMembershipType): Int = STANDARD_NAMED_DESCRIPTION_COMPARATOR.compare(this, other)
}