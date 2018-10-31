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
package se.mithlond.services.organisation.domain.model

import se.mithlond.services.organisation.domain.model.address.Address
import se.mithlond.services.organisation.domain.model.membership.Membership
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.MapKeyColumn
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * Mithlond service User definition.
 *
 * @param firstName                 The first name of this User.
 * @param lastName                  The last name of this User.
 * @param birthday                  The birthday of this User.
 * @param personalNumberLast4Digits The last 4 digits in the User's personal number.
 * @param homeAddress               The home address of this User.
 * @param contactDetails            The contact details of this User.
 * @param memberships               A Map containing the Memberships of this User. Can be {@code null}, in which
 *                                  case it is initialized to an empty SortedMap.
 * @param userIdentifierToken       A unique identifier token for this User which permits relating
 *                                  this User to the Authentication system managing User logins.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations",
        name = "InternalUsers",
        uniqueConstraints = [
            UniqueConstraint(name = "unq_alias_per_org", columnNames = ["alias", "organisation_id"]),
            UniqueConstraint(name = "unq_emailalias_per_org", columnNames = ["emailAlias", "organisation_id"]),
            UniqueConstraint(name = "unq_user_per_org", columnNames = ["user_id", "organisation_id"])
        ])
data class InternalUser(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_InternalUser")
        @field:SequenceGenerator(schema = "organisations", name = "seq_InternalUser",
                sequenceName = "seq_InternalUser", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var userIdentifierToken: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 64)
        var firstName: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 64)
        val lastName: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var birthday: LocalDate? = null,

        @field:Basic
        @field:Column(length = 4)
        var personalNumberLast4Digits: Short = 0,

        @field:Embedded
        var homeAddress: Address,

        @field:OneToMany(mappedBy = "user", cascade = [CascadeType.ALL])
        var memberships: MutableList<Membership>,

        //
        // Quoting the EclipseLink documentation:
        //
        // "The type of mapping used is always determined by the value of the Map, not the key.
        // So if the key is a Basic but the value is an Entity a OneToMany mapping is still used.
        // But if the value is a Basic but the key is an Entity a ElementCollection mapping is used."
        //
        // Thus ...
        //
        // a) Mapping of basic/embedded keys to basic/embedded values:
        //    Use @ElementCollection and @MapKeyColumn annotations
        //
        // b) Mapping of basic/embedded keys to entity values:
        //    Use @ElementCollection and @MapKeyJoinColumn annotations
        //
        // c) Mapping of entity keys to entity values:
        //    Use @OneToMany or @ManyToMany and @MapKeyJoinColumn annotations
        //
        // d) Mapping of entity keys to basic/embedded values:
        //    Use @ElementCollection and @JoinTable + @MapKeyJoinColumn
        //
        // MapKeyColumn used for basic types of Map keys
        // MapKeyJoinColumn used for entity types of Map keys
        //
        // If generics are not used in the Map declaration,
        // use @MapKeyClass to define the key type, and
        // targetClass property of @ElementCollection to
        // define value type.
        //
        @field:CollectionTable(
                name = "user_contactdetails",
                uniqueConstraints = [UniqueConstraint(
                        name = "unq_user_contact_type",
                        columnNames = arrayOf("user_id", "contact_type"))])
        @field:Column(name = "address_or_number")
        @field:MapKeyColumn(name = "contact_type")
        @field:ElementCollection(fetch = FetchType.EAGER)
        var contactDetails: MutableMap<String, String> = mutableMapOf()

) : Serializable, Comparable<InternalUser> {

    override fun compareTo(other: InternalUser): Int {

        var toReturn = this.firstName.compareTo(other.firstName)

        if (toReturn == 0) {
            toReturn = this.lastName.compareTo(other.lastName)
        }

        if(toReturn == 0) {

            val thisID = this.id ?: 0L
            val otherID = other.id ?: 0L

            toReturn = (thisID - otherID).toInt()
        }

        // All Done.
        return toReturn
    }
}
