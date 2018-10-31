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

import se.mithlond.services.organisation.domain.model.InternalUser
import se.mithlond.services.organisation.domain.model.Organisation
import java.io.Serializable
import java.util.SortedSet
import java.util.TreeSet
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
import javax.persistence.JoinTable
import javax.persistence.ManyToMany
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * Realization of a Membership, associating a Member with an Organisation.
 *
 * @param alias The alias of this Membership. Never null/empty, and unique within the Organisation.
 * @param subAlias An optional (i.e. nullable) sub-alias for the Membership within the Organisation.
 * @param emailAlias An optional (i.e. nullable) email for the Membership within the Organisation.
 * Should not include the domain name, since that is derived from the organisation.
 * @param loginPermitted Set to <code>true</code> to indicate that login is permitted, and false otherwise.
 * @param user           The user of this Membership.
 * @param organisation   The organisation of this Membership, i.e. where the user belongs.
 * @param yearlyMemberships The organisation of this Membership, i.e. where the user belongs.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations", uniqueConstraints = [
    UniqueConstraint(name = "unq_alias_per_org", columnNames = ["alias", "organisation_id"]),
    UniqueConstraint(name = "unq_emailalias_per_org", columnNames = ["emailAlias", "organisation_id"]),
    UniqueConstraint(name = "unq_user_per_org", columnNames = ["user_id", "organisation_id"])
])
data class Membership(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_Membership")
        @field:SequenceGenerator(schema = "organisations", name = "seq_Membership",
                sequenceName = "seq_Membership", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 64)
        var alias: String,

        @field:Basic
        @field:Column(length = 1024)
        var subAlias: String,

        @field:Basic
        @field:Column(length = 64)
        var emailAlias: String? = null,

        @field:Basic
        @field:Column(nullable = false)
        var loginPermitted: Boolean = false,

        @field:ManyToOne(optional = false, cascade = [CascadeType.DETACH])
        @field:JoinColumn(
                name = "user_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_membership_user"))
        var user: InternalUser,

        @field:ManyToOne(optional = false, cascade = [CascadeType.MERGE, CascadeType.DETACH])
        @field:JoinColumn(
                name = "organisation_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_membership_organisation"))
        var organisation: Organisation,

        @field:ManyToMany(cascade = [CascadeType.MERGE, CascadeType.DETACH])
        @field:JoinTable(schema = "organisations",
                name = "yearly_membership",
                joinColumns = [JoinColumn(
                        name = "membership_id",
                        foreignKey = ForeignKey(name = "fk_yearlymembership_membership"))],
                inverseJoinColumns = [JoinColumn(
                        name = "membershipyear_id",
                        foreignKey = ForeignKey(name = "fk_yearlymembership_template"))])
        var yearlyMemberships: SortedSet<MembershipYear> = TreeSet()

) : Serializable, Comparable<Membership> {

    override fun compareTo(other: Membership): Int {

        var toReturn = this.organisation.compareTo(other.organisation)

        if (toReturn == 0) {
            toReturn = this.alias.compareTo(other.alias)
        }

        // All Done.
        return toReturn
    }
}
