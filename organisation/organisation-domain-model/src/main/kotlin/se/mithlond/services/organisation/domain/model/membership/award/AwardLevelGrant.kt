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

import se.mithlond.services.organisation.domain.model.membership.Membership
import java.io.Serializable
import java.time.LocalDate
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
import javax.persistence.Table

/**
 * Compound key definition for the [AwardLevelGrant].
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Embeddable
@Access(value = AccessType.FIELD)
data class AwardLevelGrantKey(

        @field:Column(name = "awardlevel_id")
        var awardLevelId: Long,

        @field:Column(name = "membership_id")
        var membershipId: Long

) : Serializable

/**
 * Entity defining the grant of an AwardLevel to a [Membership]
 *
 * @param awardLevel  The [AwardLevel] granted to the supplied [Membership].
 * @param membership  The [Membership] which is granted an [AwardLevel].
 * @param dateGranted The date when the AwardLevel was granted to the Membership.
 * @param note        An optional short note for the grant.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations")
data class AwardLevelGrant(

        @field:EmbeddedId
        var id: AwardLevelGrantKey,

        @field:MapsId("membershipId")
        @field:ManyToOne(optional = false, cascade = [CascadeType.MERGE, CascadeType.DETACH])
        @field:JoinColumn(
                name = "membership_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_awardlevelgrant_membership"))
        var membership: Membership,

        @field:MapsId("awardLevelId")
        @field:ManyToOne(optional = false, cascade = [CascadeType.MERGE, CascadeType.DETACH])
        @field:JoinColumn(
                name = "awardlevel_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_awardlevelgrant_awardlevel"))
        var awardLevel: AwardLevel,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var dateGranted : LocalDate,

        @field:Basic
        @field:Column(length = 1024)
        var note : String? = null) : Serializable, Comparable<AwardLevelGrant> {

    override fun compareTo(other: AwardLevelGrant): Int {

        var toReturn = this.awardLevel.compareTo(other.awardLevel)

        if(toReturn == 0) {
            toReturn = this.membership.compareTo(other.membership)
        }

        // All Done
        return toReturn
    }
}