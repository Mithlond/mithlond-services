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

import se.mithlond.services.organisation.domain.model.finance.Amount
import java.io.Serializable
import java.math.BigDecimal
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
import javax.persistence.PrePersist
import javax.persistence.Table
import javax.persistence.Transient

/**
 * Compound key definition for the YearlyMembership.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Embeddable
@Access(value = AccessType.FIELD)
data class YearlyMembershipKey(

        @field:Column(name = "template_id")
        var templateId: Long,

        @field:Column(name = "membership_id")
        var membershipId: Long

) : Serializable

/**
 * Yearly membership definition.
 *
 * @param id The compound PK of this [YearlyMembership]
 * @param membership The [Membership] of this [YearlyMembership]
 * @param template The [MembershipYear] template of this [YearlyMembership]
 * @param amount The amount payed (if non-null)
 * @param paymentDate The date when the amount was registered
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations")
data class YearlyMembership(

        @field:EmbeddedId
        var id: YearlyMembershipKey,

        @field:MapsId("membershipId")
        @field:ManyToOne(optional = false, cascade = [CascadeType.MERGE, CascadeType.DETACH])
        @field:JoinColumn(
                name = "membership_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_yearlymembership_membership"))
        var membership: Membership,

        @field:MapsId("templateId")
        @field:ManyToOne(optional = false, cascade = [CascadeType.MERGE, CascadeType.DETACH])
        @field:JoinColumn(
                name = "template_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_yearlymembership_template"))
        var template: MembershipYear,

        @field:Basic
        @field:Column(precision = 10, scale = 2)
        val amount: BigDecimal? = null,

        @field:Basic
        @field:Column
        var paymentDate: LocalDate? = null) : Serializable, Comparable<YearlyMembership> {

    /**
     * Compound constructor. Should only be called from within non-framework code.
     */
    constructor(membership: Membership, template: MembershipYear, amount: BigDecimal?, paymentDate: LocalDate?)
            : this(YearlyMembershipKey(-1, -1), membership, template, amount, paymentDate)

    init {
        synchronizeKeyValues()
    }

    /**
     * The amount payed, typecast as an [Amount]
     */
    val amountPayed: Amount
        @Transient
        get() = when (amount == null) {
            true -> Amount(BigDecimal.ZERO, membership.organisation.standardCurrency)
            else -> Amount(amount!!, membership.organisation.standardCurrency)
        }

    /**
     * Copies the ID values of the Membership and Template to the respective PK columns.
     */
    @PrePersist
    fun synchronizeKeyValues() {

        // Harmonize the state of the PK
        if (membership.id != null) {
            this.id.membershipId = membership.id!!
        }
        if (template.id != null) {
            this.id.templateId = template.id!!
        }
    }

    override fun compareTo(other: YearlyMembership): Int {

        var toReturn = this.template.compareTo(other.template)

        if (toReturn == 0) {
            toReturn = this.membership.compareTo(other.membership)
        }

        // All Done.
        return toReturn
    }
}