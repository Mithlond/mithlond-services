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
package se.mithlond.services.organisation.domain.model.membership

import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.EmbeddedId
import javax.persistence.Entity
import javax.persistence.ForeignKey
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.MapsId
import javax.persistence.PrePersist
import javax.persistence.Table

/**
 * Compound [GroupMembership] key definition.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Embeddable
@Access(value = AccessType.FIELD)
data class GroupMembershipKey(

        @field:Column(name = "group_id")
        var groupId: Long,

        @field:Column(name = "membership_id")
        var membershipId: Long

) : Serializable

/**
 * Definition for a membership within a Group.
 *
 * @param id The Compound JPA ID of this Entity.
 * @param group      The Group to which this GroupMembership indicates inclusion.
 * @param membership The Membership included in the supplied Group.
 * @param joinedTimestamp The timestamp when this GroupMembership was generated.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations")
open class GroupMembership(

        @field:EmbeddedId
        var id: GroupMembershipKey,

        @field:MapsId("groupId")
        @field:ManyToOne(optional = false, cascade = [CascadeType.MERGE, CascadeType.DETACH])
        @field:JoinColumn(
                name = "group_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_groupmembership_group"))
        var group: Group,

        @field:MapsId("membershipId")
        @field:ManyToOne(optional = false, cascade = [CascadeType.MERGE, CascadeType.DETACH])
        @field:JoinColumn(
                name = "membership_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_groupmembership_membership"))
        var membership: Membership,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var joinedTimestamp: LocalDateTime) : Serializable, Comparable<GroupMembership> {

    /**
     * Compound constructor. Should only be called from within non-framework code.
     */
    constructor(membership: Membership, group: Group, joinedTimestamp: LocalDateTime)
            : this(GroupMembershipKey(-1, -1), group, membership, joinedTimestamp)

    init {
        synchronizeKeyValues()
    }

    /**
     * Copies the ID values of the Membership and Group to the respective PK columns.
     */
    @PrePersist
    fun synchronizeKeyValues() {

        // Harmonize the state of the PK
        if (membership.id != null) {
            this.id.membershipId = membership.id!!
        }
        if (group.id != null) {
            this.id.groupId = group.id!!
        }
    }

    override fun compareTo(other: GroupMembership): Int {

        var toReturn = this.group.compareTo(other.group)

        if (toReturn == 0) {
            toReturn = this.membership.compareTo(other.membership)
        }

        if (toReturn == 0) {
            toReturn = this.joinedTimestamp.compareTo(other.joinedTimestamp)
        }

        // All Done.
        return toReturn
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GroupMembership) return false

        if (id != other.id) return false
        if (joinedTimestamp != other.joinedTimestamp) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + joinedTimestamp.hashCode()
        return result
    }

    override fun toString(): String {
        return "GroupMembership(id=$id, group=$group, membership=$membership, joinedTimestamp=$joinedTimestamp)"
    }
}