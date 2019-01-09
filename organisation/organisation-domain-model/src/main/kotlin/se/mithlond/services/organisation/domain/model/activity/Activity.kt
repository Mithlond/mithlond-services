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
package se.mithlond.services.organisation.domain.model.activity

import se.mithlond.services.organisation.domain.model.NamedDescription
import se.mithlond.services.organisation.domain.model.Organisation
import se.mithlond.services.organisation.domain.model.Organisational
import se.mithlond.services.organisation.domain.model.address.Address
import se.mithlond.services.organisation.domain.model.address.CategorizedAddress
import se.mithlond.services.organisation.domain.model.localization.Category
import se.mithlond.services.organisation.domain.model.membership.Group
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Currency
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.ForeignKey
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * Entity implementation for an Activity within an Organisation.
 *
 * @param id The JPA ID of this Domain Entity.
 * @param shortDesc The mandatory and non-empty short description of this Listable entity.
 * Typically used within short info boxes and pop-ups.
 * @param fullDesc The full description of this Listable entity, visible in detailed listings.
 * May not be null or empty. Typically used within longer info boxes or modal description displays.
 * @param organisation The [Organisation] owning this [CategorizedAddress].
 * @param dressCode The optional dress code of the activity, if applicable.
 * @param startTime The start time of the Activity. Never null.
 * @param endTime The end time of the Activity. Must not be null, and must also be after startTime.
 * @param cost The cost of the activity. Never negative, but may be "0".
 * @param currency The currency for the optional cost of the activity.
 * @param lateAdmissionCost The cost if admission after the lateAdmissionDate. Never negative.
 * Optional, but recommended to be higher than the (standard) cost.
 * @param lateAdmissionDate The optional date before which the activity costs `cost`.
 * After this date, the activity admission costs `lateAdmissionCost`.
 * @param lastAdmissionDate The last date of admissions to the Activity.
 * @param cancelled If 'true', the Activity is cancelled.
 * @param addressCategory The Category of the location where this Activity takes place.
 * @param addressShortDescription The short description of the location for this Activity, such as "Stadsbiblioteket".
 * @param location The location of the Activity. May not be null.
 * @param responsible The Guild organizing this Activity. Optional.
 * @param admissions All current Admissions to this Activity. May be empty - but not null.
 * @param openToGeneralPublic "true" to indicate that the supplied Activity is open to the general public.
 * Otherwise this Activity is open only to Admissions from known Memberships.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations", uniqueConstraints = [
    UniqueConstraint(name = "unq_classification_per_name", columnNames = ["name", "classification"])
])
data class Activity @JvmOverloads constructor(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_Activity")
        @field:SequenceGenerator(schema = "organisations", name = "seq_Activity",
                sequenceName = "seq_Activity", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 254)
        override var name: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 2048)
        override var description: String,

        @field:ManyToOne(optional = false)
        @field:JoinColumn(
                name = "organisation_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_activity_organisation"))
        override var organisation: Organisation,

        @field:ManyToOne(optional = false)
        @field:JoinColumn(
                name = "dresscode_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_activity_dresscode"))
        var dressCode: Dresscode,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var startTime: LocalDateTime,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var endTime: LocalDateTime,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var cost: BigDecimal = BigDecimal.ZERO,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var currency: Currency,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var lateAdmissionCost: BigDecimal = BigDecimal.ZERO,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var lateAdmissionDate: LocalDate,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var lastAdmissionDate: LocalDate,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var cancelled: Boolean = false,

        @field:ManyToOne(optional = false)
        @field:JoinColumn(
                name = "category_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_activity_category"))
        var addressCategory: Category,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var addressShortDescription: String,

        @field:Embedded
        var location: Address,

        @field:ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.REFRESH, CascadeType.MERGE])
        @field:JoinColumn(name = "group_id", foreignKey = ForeignKey(name = "fk_activity_arranging_group"))
        var responsible: Group? = null,

        @field:OneToMany(cascade = [CascadeType.ALL], mappedBy = "activity")
        val admissions: Set<Admission> = mutableSetOf(),

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var openToGeneralPublic: Boolean = false

) : NamedDescription, Organisational, Comparable<Activity> {

    override fun compareTo(other: Activity): Int {

        var toReturn = this.organisation.compareTo(other.organisation)

        if (toReturn == 0) {
            toReturn = startTime.compareTo(other.startTime)
        }

        if (toReturn == 0) {
            toReturn = name.compareTo(other.name)
        }

        // All Done
        return toReturn
    }
}
