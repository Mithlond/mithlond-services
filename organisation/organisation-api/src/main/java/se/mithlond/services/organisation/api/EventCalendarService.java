/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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

package se.mithlond.services.organisation.api;

import se.mithlond.services.organisation.model.activity.EventCalendar;
import se.mithlond.services.shared.spi.jpa.JpaCudService;

import javax.ejb.Local;
import java.time.LocalDate;
import java.util.List;

/**
 * Specification for how to integrate an organisation with a Calendar service provider.
 * The EventCalendar metadata structure represent a Calendar within a [remote] calendar service provider.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Local
public interface EventCalendarService extends JpaCudService {

    /**
     * Retrieves all EventCalendars owned by the supplied Organisation. Typically, EventCalendars are matched
     * with remove services such as cloud services from Google, Microsoft, Yahoo or the like.
     *
     * @param organisationName   The name of the Organisation for which all remote EventCalendars should be retrieved.
     * @param runtimeEnvironment The runtime environment type which should match all retrieved EventCalendars.
     * @return all Calendars owned by the supplied Organisation and having the supplied runtime environment type.
     */
    List<EventCalendar> getCalendars(String organisationName, String runtimeEnvironment);

    /**
     * <p>Pushes all [locally known] Activities to a [remote] EventCalendar, as follows:</p>
     * <ol>
     * <li>Events only present within the database will be created in the remote EventCalendar.</li>
     * <li>Events present in both local database and remote EventCalendar will be updated with the
     * data in the local database (local master), to cope with Admission changes.</li>
     * <li>Events present only in the remote EventCalendar will be removed from it. Such Events/Activities are
     * assumed to be locally removed, and pushing the activities to the remote EventCalendar will remove the
     * corresponding events from it.</li>
     * <li>Only Activities defined within the Organisation owning the provided EventCalendar will be managed.</li>
     * </ol>
     *
     * @param calendar  The EventCalendar to push activity data to. Cannot be {@code null}.
     * @param startTime Only events that starts after this startTime will be pushed. Cannot be {@code null}.
     * @param endTime   Only events that ends before this endTime will be pushed. Cannot be {@code null}.
     */
    void pushActivities(EventCalendar calendar, LocalDate startTime, LocalDate endTime);
}
