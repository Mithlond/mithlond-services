package se.mithlond.services.organisation.domain.model.membership

import se.mithlond.services.organisation.domain.model.Organisation
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import java.time.Year
import java.util.Currency
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
 * @param membership        The Membership for which this MembershipYear applies.
 * @param yearStartDate     The date ofo the start of this Year.
 * @param standardFee       The standard fee of the given Membership
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

        @Basic(optional = false)
        @Column(nullable = false)
        val year: Year,

        @Basic(optional = false)
        @Column(nullable = false)
        val startDate: LocalDate,

        @Basic(optional = false)
        @Column(nullable = false, precision = 10, scale = 2)
        val standardFee: BigDecimal,

        @Basic
        @Column(precision = 10, scale = 2)
        val reducedFee: BigDecimal? = null,

        @Basic
        @Column(precision = 10, scale = 2)
        val expandedFee: BigDecimal? = null,

        @field:ManyToOne(optional = false, cascade = [CascadeType.MERGE, CascadeType.DETACH])
        @field:JoinColumn(
                name = "organisation_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_membershipyear_organisation"))
        var organisation: Organisation

) : Serializable, Comparable<MembershipYear> {
    override fun compareTo(other: MembershipYear): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}