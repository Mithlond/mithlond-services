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

import se.mithlond.services.organisation.domain.model.Organisation
import se.mithlond.services.organisation.domain.model.membership.Group
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.DiscriminatorValue
import javax.persistence.Entity

/**
 * Entity implementation for a Guild, belonging to an Organisation.
 * Guilds are Groups which have extra internal structure.
 *
 * @param quenyaName   The quenya name of this Guild.
 * @param quenyaPrefix The quenya prefix of this Guild.
 * @param organisation The Organisation where this guild belongs.
 * @param name    The name of this Guild. Identical to group name for Guilds.
 * @param emailList    An optional electronic mail list which delivers elecronic mail to all members of this Group.
 *                     If the emailList property does not contain a full email address, the email suffix of the
 *                     Organisation will be appended to form the full mail address of
 *                     <code>[emailList]@[organisation email suffix]</code>.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@DiscriminatorValue("guild")
open class Guild(

        id: Long? = null,

        name: String,

        description: String,

        emailList: String? = null,

        organisation: Organisation,

        @field:Basic
        @field:Column(length = 64)
        val quenyaName: String,

        @Basic
        @Column(length = 64)
        val quenyaPrefix: String

) : Group(id, name, description, emailList, organisation, null) {

    override fun toString(): String {
        return "Guild(id=$id, name='$name', description='$description', emailList=$emailList, " +
                "organisation=$organisation, parent=$parent, quenyaName='$quenyaName', " +
                "quenyaPrefix='$quenyaPrefix')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Guild) return false
        if (!super.equals(other)) return false

        if (quenyaName != other.quenyaName) return false
        if (quenyaPrefix != other.quenyaPrefix) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + quenyaName.hashCode()
        result = 31 * result + quenyaPrefix.hashCode()
        return result
    }
}