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

import se.mithlond.services.organisation.domain.model.NamedDescription
import se.mithlond.services.organisation.domain.model.Organisation
import se.mithlond.services.organisation.domain.model.Organisational
import java.io.Serializable
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.DiscriminatorColumn
import javax.persistence.DiscriminatorType
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ForeignKey
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * Entity implementation for Groups of Memberships within an Organisation.
 * Groups are structured in a Tree, which means that each Group may have (a single) parent Group.
 * Effective settings for a Group is the aggregation of all settings inherited from all its parent Groups.
 * 
 * @param name    Name of this Group, which must be non-empty and unique within each Organisation.
 * @param description  Description of this Group, which must be non-empty.
 * @param organisation The non-null Organisation in which this Group exists.
 * @param parent       An optional parent to this Group. Groups are structured in a Tree, which means that each
 *                     Group may have (a single) parent Group. Effective settings for a Group is the aggregation
 *                     of all settings inherited from all its parent Groups.
 * @param emailList    An optional electronic mail list which delivers elecronic mail to all members of this Group.
 *                     If the emailList property does not contain a full email address, the email suffix of the
 *                     Organisation will be appended to form the full mail address of
 *                     <code>[emailList]@[organisation email suffix]</code>.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Table(schema = "organisations",
        name = "InternalGroups",
        uniqueConstraints = [UniqueConstraint(name = "unq_groupname_per_org",
                columnNames = arrayOf("groupname", "organisation_id"))])
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discriminator", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("group")
open class Group(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_InternalGroups")
        @field:SequenceGenerator(schema = "organisations", name = "seq_InternalGroups",
                sequenceName = "seq_InternalGroups", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 128)
        override var name: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 1024)
        override var description: String,

        @field:Basic
        @field:Column(length = 128)
        var emailList: String? = null,

        @field:ManyToOne(optional = false,
                fetch = FetchType.EAGER,
                cascade = [CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE])
        @field:JoinColumn(
                name = "organisation_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_group_organisation"))
        override var organisation: Organisation,

        @field:ManyToOne(optional = true)
        @field:JoinColumn(
                name = "parent_group_id",
                foreignKey = ForeignKey(name = "fk_group_parent"))
        var parent: Group?

) : NamedDescription, Organisational, Serializable, Comparable<Group> {

    override fun compareTo(other: Group): Int {

        // Check sanity
        if (this === other) {
            return 0
        }

        // Do we have a parent?
        val thisHasParent = this.parent != null
        val thatHasParent = other.parent != null

        if (thisHasParent && !thatHasParent) {
            return 1
        } else if (!thisHasParent && thatHasParent) {
            return -1
        }

        // Delegate
        var toReturn = this.organisation.compareTo(other.organisation)

        if (toReturn == 0) {
            toReturn = this.name.compareTo(other.name)
        }

        // All Done
        return toReturn
    }

    override fun equals(other: Any?): Boolean {

        // Check sanity
        if (this === other) return true
        if (other !is Group) return false

        // Delegate
        if (this.name != other.name) return false
        if (this.description != other.description) return false
        if ((this.emailList ?: "") != (other.emailList ?: "")) return false
        if (this.organisation != other.organisation) return false
        if (this.parent != other.parent) return false

        // All Done.
        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + (emailList?.hashCode() ?: 0)
        result = 31 * result + organisation.hashCode()
        result = 31 * result + (parent?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {

        return "Group(id=$id, name='$name', description='$description', " +
                "emailList=$emailList, organisation=$organisation, parent=$parent)"
    }
}
