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
import se.mithlond.services.organisation.domain.model.STANDARD_NAMED_ORGANISATIONAL_DESCRIPTION_COMPARATOR
import java.time.DayOfWeek
import java.time.ZoneId
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
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
 * Specification for an EventCalendar, which implies a Calendar containing Activities or Events.
 * 
 * @param zoneId The default [ZoneId] of this EventCalendar. Used when explicit TimeZone information is not available.
 * @param calendarIdentifier An identifier of this EventCalendar, such as "mithlond.official@gmail.com".
 * This identifier can be used to identify this EventCalendar to the corresponding event
 * storage, such as Yahoo or Google; in that sense it works as a primary key of the
 * EventCalendar in the remote system.
 * @param firstDayOfWeek the [DayOfWeek] which is the first day in a week of this [EventCalendar]
 * @param runtimeEnvironment The runtime environment for which this EventCalendar should be visible/accessible.
 * Corresponds to "Deployment.deploymentName".
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations", uniqueConstraints = [
        UniqueConstraint(name = "unq_ec_name_per_organisation", columnNames = ["name", "organisation_id"])
])
data class EventCalendar(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_EventCalendar")
        @field:SequenceGenerator(schema = "organisations", name = "seq_EventCalendar",
                sequenceName = "seq_EventCalendar", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 128)
        override var name: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 1024)
        override var description: String,

        @field:ManyToOne(optional = false)
        @field:JoinColumn(
                name = "organisation_id",
                nullable = false,
                foreignKey = ForeignKey(name = "fk_eventcalendar_organisation"))
        override var organisation: Organisation,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        val zoneId: ZoneId,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        val firstDayOfWeek: DayOfWeek,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        val calendarIdentifier: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        val runtimeEnvironment: String

) : NamedDescription, Organisational, Comparable<EventCalendar> {

    override fun compareTo(other: EventCalendar): Int =
            STANDARD_NAMED_ORGANISATIONAL_DESCRIPTION_COMPARATOR.compare(this, other)
}