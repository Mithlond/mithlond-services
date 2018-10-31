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

import se.mithlond.services.organisation.domain.model.Organisation
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Month
import java.time.Year
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
import javax.persistence.ManyToOne
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * Template for a year of Membership within an Organisation, including all definitions of a Fee.
 *
 * @param id   The JPA ID of this MembershipYear
 * @param year The Year for which this [MembershipYear] is a template.
 * @param startDate The first date of this [MembershipYear]. Defaults to the 1st of January of the given Year.
 * @param standardFee The standard fee for a Membership.
 * @param reducedFee An optional/reduced fee for a Membership.
 * @param expandedFee An optional/expanded fee for a Membership.
 * @param organisation The organisation of this MembershipYear template.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations", uniqueConstraints = [
    UniqueConstraint(name = "unq_year_per_org", columnNames = ["year", "organisation_id"])
])
data class MembershipYear(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_MembershipYear")
        @field:SequenceGenerator(schema = "organisations", name = "seq_MembershipYear",
                sequenceName = "seq_MembershipYear", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        val year: Year,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        val startDate: LocalDate = LocalDate.of(year.value, Month.JANUARY, 1),

        @field:Basic(optional = false)
        @field:Column(nullable = false, precision = 10, scale = 2)
        val standardFee: BigDecimal,

        @field:Basic
        @field:Column(precision = 10, scale = 2)
        val reducedFee: BigDecimal? = null,

        @field:Basic
        @field:Column(precision = 10, scale = 2)
        val expandedFee: BigDecimal? = null,

        @field:ManyToOne(optional = false, cascade = [CascadeType.MERGE, CascadeType.DETACH])
        @field:JoinColumn(
                name = "organisation_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_membershipyear_organisation"))
        var organisation: Organisation

) : Serializable, Comparable<MembershipYear> {

    override fun compareTo(other: MembershipYear): Int {

        var toReturn = this.year.compareTo(other.year)

        if (toReturn == 0) {
            toReturn = this.organisation.compareTo(other.organisation)
        }

        if (toReturn == 0) {
            toReturn = this.standardFee.compareTo(other.standardFee)
        }

        // All Done.
        return toReturn
    }
}