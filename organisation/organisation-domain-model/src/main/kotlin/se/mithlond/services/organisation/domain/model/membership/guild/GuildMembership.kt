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

import se.mithlond.services.organisation.domain.model.membership.GroupMembership
import se.mithlond.services.organisation.domain.model.membership.GroupMembershipKey
import se.mithlond.services.organisation.domain.model.membership.Membership
import java.time.LocalDateTime
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.ForeignKey
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

/**
 * Definition for a membership within a Guild.
 *
 * @param guild      The Guild to which this GroupMembership indicates inclusion.
 * @param membership The Membership included in the supplied Group.
 * @param joinedTimestamp The timestamp when this GroupMembership was generated.
 * @param id The JPA ID of this Entity.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations")
open class GuildMembership(

        id: GroupMembershipKey,

        guild: Guild,

        membership: Membership,

        joinedTimestamp: LocalDateTime,

        @field:ManyToOne(optional = false, cascade = [CascadeType.MERGE, CascadeType.DETACH])
        @field:JoinColumn(
                name = "guildmembershiptype_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_guildmembership_type"))
        var guildMembershipType: GuildMembershipType
) : GroupMembership(id, guild, membership, joinedTimestamp), Comparable<GroupMembership> {

    /**
     * Convenience property to cast the group of this [GuildMembership] to a [Guild].
     */
    val guild : Guild
        get() = group as Guild

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GuildMembership) return false
        if (!super.equals(other)) return false

        if (guildMembershipType != other.guildMembershipType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + guildMembershipType.hashCode()
        return result
    }

    override fun toString(): String {
        return "GuildMembership(id=$id, guild=$group, membership=$membership, " +
                "joinedTimestamp=$joinedTimestamp, guildMembershipType=$guildMembershipType)"
    }
}