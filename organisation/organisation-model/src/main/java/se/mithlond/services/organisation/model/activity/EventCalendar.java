/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
package se.mithlond.services.organisation.model.activity;

import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.Listable;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.time.ZoneId;

/**
 * Specification for an EventCalendar, which implies a Calendar containing Activities or Events.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
		@NamedQuery(name = EventCalendar.NAMEDQ_GET_BY_ORGANISATION_RUNTIME_AND_IDENTIFIER,
				query = "select a from EventCalendar a "
						+ " where a.owningOrganisation.organisationName like :"
                        + OrganisationPatterns.PARAM_ORGANISATION_NAME
						+ " and a.runtimeEnvironment like :" + OrganisationPatterns.PARAM_ENVIRONMENT_ID
						+ " and a.calendarIdentifier like :" + OrganisationPatterns.PARAM_EVENT_CALENDAR
						+ " order by a.fullDesc")
})
@Entity
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"timeZoneID", "isMondayFirstDayOfWeek", "calendarIdentifier", "runtimeEnvironment"})
@Table(uniqueConstraints = {
        @UniqueConstraint(
                name = "calendarIdAndEnvironmentIsUnique",
                columnNames = {"calendarIdentifier", "runtimeEnvironment"})})
@XmlAccessorType(XmlAccessType.FIELD)
public class EventCalendar extends Listable {

    /**
     * NamedQuery for getting EventCalendars by organisation name, as well as runtimeIdentifier and eventCalendarID.
     */
    public static final String NAMEDQ_GET_BY_ORGANISATION_RUNTIME_AND_IDENTIFIER =
            "EventCalendar.getByOrganisationRuntimeAndIdentifier";

    /**
     * The default TimeZone of this EventCalendar, used whenever
     * explicit TimeZone information is not available.
     */
    @NotNull
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private String timeZoneID;

    /**
     * "true" to indicate that monday is the first day of the week,
     * and "false" to indicate that sunday is the first day of the week
     * for this EventCalendar.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlAttribute(required = true)
    private boolean isMondayFirstDayOfWeek;

    /**
     * An identifier of this EventCalendar, such as "mithlond.official@gmail.com".
     * This identifier can be used to identify this EventCalendar to the corresponding event
     * storage, such as Yahoo or Google; in that sense it works as a primary key of the
     * EventCalendar in the remote system.
     */
    @NotNull
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private String calendarIdentifier;

    /**
     * The runtime environment for which this EventCalendar should be visible/accessible.
     * Corresponds to "Deployment.deploymentName".
     */
    @NotNull
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private String runtimeEnvironment;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public EventCalendar() {
    }

    /**
     * Convenience constructor, creating a Calendar containing the supplied data,
     * and using {@code TimeFormat.SWEDISH_TIMEZONE} for timezone identifier, and using monday
     * as the first day within the week.
     *
     * @param shortDesc          The short description of this Calendar.
     * @param fullDesc           The full/long description of this Calendar.
     * @param organisation       The Organisation owning this Calendar.
     * @param calendarIdentifier The remote identifier (or type) of the calendar used,
     *                           such as "mithlond.official@gmail.com".
     * @param runtimeEnvironment The runtime environment for which this EventCalendar should be visible/accessible.
     * @see TimeFormat#SWEDISH_TIMEZONE
     */
    public EventCalendar(final String shortDesc,
                         final String fullDesc,
                         final Organisation organisation,
                         final String calendarIdentifier,
                         final String runtimeEnvironment) {

        this(shortDesc,
                fullDesc,
                organisation,
                TimeFormat.SWEDISH_TIMEZONE,
                true,
                calendarIdentifier,
                runtimeEnvironment);
    }

    /**
     * Compound constructor, creating a Calendar containing the supplied data.
     *
     * @param shortDesc              The short description of this EventCalendar.
     * @param fullDesc               The full/long description of this EventCalendar.
     * @param organisation           The Organisation owning this EventCalendar.
     * @param timeZoneID             The default TimeZone of this EventCalendar.
     * @param calendarIdentifier     The remote identifier (or type) of the calendar used,
     *                               such as "mithlond.official@gmail.com".
     * @param isMondayFirstDayOfWeek {@code true} to indicate that the weeks in this EventCalendar starts with Monday.
     *                               Otherwise, weeks are assumed to start with Sunday.
     * @param runtimeEnvironment     The runtime environment for which this EventCalendar should be visible/accessible.
     */
    public EventCalendar(final String shortDesc,
                         final String fullDesc,
                         final Organisation organisation,
                         final ZoneId timeZoneID,
                         final boolean isMondayFirstDayOfWeek,
                         final String calendarIdentifier,
                         final String runtimeEnvironment) {

        super(shortDesc, fullDesc, organisation);

        // Assign internal state
        this.timeZoneID = timeZoneID.getId();
        this.isMondayFirstDayOfWeek = isMondayFirstDayOfWeek;
        this.calendarIdentifier = calendarIdentifier;
        this.runtimeEnvironment = runtimeEnvironment;
    }

    /**
     * @return The default TimeZone of this Calendar.
     */
    public ZoneId getTimeZoneID() {
        return ZoneId.of(timeZoneID);
    }

    /**
     * @return {@code true} if monday is the first day of the week, and {@code false} to
     * indicate that sunday is the first day of the week.
     */
    public boolean isMondayFirstDayOfWeek() {
        return isMondayFirstDayOfWeek;
    }

    /**
     * @return The type of calendar used, such as "Google Calendar".
     */
    public String getCalendarIdentifier() {
        return calendarIdentifier;
    }

    /**
     * @return The runtime environment for which this EventCalendar should be visible/accessible.
     * Must match the system or environment property defining the runtime environment type of a
     * web application.
     */
    public String getRuntimeEnvironment() {
        return runtimeEnvironment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateListableEntityState() throws InternalStateValidationException {

        // Check sanity
        InternalStateValidationException.create()
                .notNullOrEmpty(timeZoneID, "timeZoneID")
                .notNullOrEmpty(calendarIdentifier, "calendarIdentifier")
                .notNullOrEmpty(runtimeEnvironment, "runtimeEnvironment")
                .endExpressionAndValidate();
    }
}
