/*
 * #%L
 * Nazgul Project: mithlond-services-integration-calendar-impl-google
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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
package se.mithlond.services.integration.calendar.impl.google.algorithms;

import com.google.api.client.googleapis.batch.BatchRequest;
import com.google.api.client.googleapis.batch.json.JsonBatchCallback;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.http.HttpHeaders;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.algorithms.diff.DiffHolder;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;

import static se.mithlond.services.integration.calendar.impl.google.algorithms.GoogleCalendarConverters.JSON_FACTORY;

/**
 * Utility class containing stateless algorithms for use with the Google Calendar API.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class CalendarAlgorithms {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(CalendarAlgorithms.class);

    /*
     * Hide constructor for utility classes
     */
    private CalendarAlgorithms() {
        // Do nothing
    }

    /**
     * Reads/receives a List of Events from the Google Calendar with the supplied calendarIdentifier and with
     * event start times being within the interval defined by startInterval -- endInterval.
     *
     * @param calendarClient     The authenticated Google Calendar (client).
     * @param calendarIdentifier The calendar identifier from which Events should be read.
     * @param startInterval      Search criterion, defining the upper bound for the returned Events' start time.
     * @param endInterval        Search criterion, defining the lower bound for the returned Events' end time.
     * @return A List of Events as received from the identified Google Calendar, and sorted on starting time.
     */
    public static List<Event> getEventsFromGoogle(@NotNull final Calendar calendarClient,
                                                  @NotNull final String calendarIdentifier,
                                                  @NotNull final LocalDateTime startInterval,
                                                  @NotNull final LocalDateTime endInterval) {

        // #0) Check sanity
        Validate.notNull(calendarClient, "calendarIdentifier");
        Validate.notEmpty(calendarIdentifier, "calendarIdentifier");
        Validate.notNull(startInterval, "startInterval");
        Validate.notNull(endInterval, "endInterval");

        if (endInterval.isBefore(startInterval)) {

            final String startOfInterval = TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(startInterval);
            final String endOfInterval = TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(endInterval);

            throw new IllegalArgumentException("End of search interval (" + endOfInterval
                    + ") cannot be before the interval start (" + startOfInterval + ")");
        }

        // #1) Create return value.
        if (log.isDebugEnabled()) {
            log.debug("About to fetch Events from Google Calendar [" + calendarIdentifier + "]");
        }
        final List<Event> toReturn = new ArrayList<>();

        try {

            // #2) Compile the Event query.
            final Calendar.Events.List eventListQuery = calendarClient
                    .events()
                    .list(calendarIdentifier)
                    .setTimeMin(GoogleCalendarConverters.convert(startInterval))
                    .setTimeMax(GoogleCalendarConverters.convert(endInterval));

            // #3) Fire the query to the Google Calendar service.
            //     This invokes the actual network traffic.
            final Events foundEvents = eventListQuery.execute();
            if (foundEvents != null) {
                toReturn.addAll(foundEvents.getItems());
            }

            // #4) Log somewhat
            if (log.isDebugEnabled()) {

                final String startOfInterval = TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(startInterval);
                final String endOfInterval = TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(endInterval);

                log.debug("Read " + toReturn.size() + " Events from Google Calendar [" + calendarIdentifier
                        + "] within the interval [" + startOfInterval + "  --  " + endOfInterval + "]");
            }

        } catch (IOException e) {
            log.error("Could not read Events from Google Calendar [" + calendarIdentifier + "]", e);
        }

        // All Done.
        toReturn.sort((l, r) -> (int) (l.getStart().getDate().getValue() - r.getStart().getDate().getValue()));
        return toReturn;
    }

    /**
     * Synchronizes the supplied Event objects (i.e. calendar Events) to the Google Calendar service.
     *
     * @param calendarClient           The fully authorized and connected Google Calendar client.
     * @param calendarIdentifier       The ID of the Calendar into which we should insert all given Events.
     * @param createInGoogleCalendar   The Events to insert into the Google Calendar.
     * @param updateInGoogleCalendar   The Events to update within the Google Calendar.
     * @param removeFromGoogleCalendar The Events to remove from the Google Calendar.
     */
    public static void synchronizeWithGoogleCalendar(@NotNull final Calendar calendarClient,
                                                     @NotNull final String calendarIdentifier,
                                                     @NotNull final List<Event> createInGoogleCalendar,
                                                     @NotNull final List<Event> updateInGoogleCalendar,
                                                     @NotNull final List<Event> removeFromGoogleCalendar) {

        // #0) Log some.
        //
        if (log.isDebugEnabled()) {

            final StringBuilder builder = new StringBuilder();

            builder.append("Creating [" + createInGoogleCalendar.size() + "] events: ")
                    .append(sortAndDescribe(createInGoogleCalendar));

            builder.append("Updating [" + updateInGoogleCalendar.size() + "] events: ")
                    .append(sortAndDescribe(updateInGoogleCalendar));

            builder.append("Removing [" + removeFromGoogleCalendar.size() + "] events: ")
                    .append(sortAndDescribe(removeFromGoogleCalendar));

            // Log somewhat
            log.debug("" + builder.toString());
        }

        try {

            // #1) Get/Create the BatchRequest, and extract the Calendar's Event-collection (i.e. "events").
            //
            final BatchRequest batch = calendarClient.batch();
            final Calendar.Events eventList = calendarClient.events();

            // #2) Create new Events.
            //
            final JsonBatchCallback<Event> eventInsertBatchCallback = createWriteCallback(calendarIdentifier, true);
            for (Event current : createInGoogleCalendar) {

                // Queue the insert in the batch.
                eventList.insert(calendarIdentifier, current).queue(batch, eventInsertBatchCallback);
            }

            // #3) Remove deleted Events.
            //
            final JsonBatchCallback<Void> eventDeleteBatchCallback = createDeleteCallback(calendarIdentifier);
            for (Event current : removeFromGoogleCalendar) {

                // Queue the delete in the batch.
                eventList.delete(calendarIdentifier, current.getId()).queue(batch, eventDeleteBatchCallback);
            }

            // #4) Update modified Events.
            //
            final JsonBatchCallback<Event> eventUpdateBatchCallback = createWriteCallback(calendarIdentifier, false);
            for (Event current : updateInGoogleCalendar) {

                // Queue the update in the batch.
                eventList.update(calendarIdentifier, current.getId(), current).queue(batch, eventUpdateBatchCallback);
            }

            // #5) Fire the Batch
            //
            batch.execute();

        } catch (Exception e) {
            throw new IllegalStateException("Could not synchronize Google calendars.", e);
        }
    }

    /**
     * Relates the supplied Lists containing event information from Google (existingEvents) with the
     * activities read from the local database.
     *
     * @param existingEvents The existing events read from the Google Calendar service.
     * @param activities     The locally created Activities, as fetched from local persistent storage.
     * @return A SortedMap relating the modification to the List of EventMappers representing the Diff
     * of the given kind.
     */
    @NotNull
    public static SortedMap<DiffHolder.Modification, List<EventMapper>> mapEvents(
            @NotNull final List<Event> existingEvents,
            @NotNull final List<Activity> activities) {

        // #0) Check sanity
        Validate.notNull(existingEvents, "existingEvents");
        Validate.notNull(activities, "activities");

        // #1) Map all Activities to their JPA IDs.
        final SortedMap<Long, EventMapper> id2Mapper = new TreeMap<>();
        activities.stream().map(EventMapper::new).forEach(m -> id2Mapper.put(m.getActivity().getId(), m));

        // #2) Find all existing events which contain Activity JPA IDs.
        final SortedMap<Long, Event> id2ExistingEvent = new TreeMap<>();
        existingEvents.forEach(e -> {

            // Only handle valid Events (having a mapped Activity JPA ID).
            GoogleCalendarConverters.getActivityID(e).ifPresent(activityID -> id2ExistingEvent.put(activityID, e));
        });

        // #3) Relate all existingEvents to their corresponding EventMapper
        //     Events which do not correspond to an existing Activity are removed; add EventMappers
        //     for these as well, to enable removing the events from the Google Calendar. 
        id2ExistingEvent.entrySet().stream()
                .filter(e -> id2Mapper.keySet().contains(e.getKey()))
                .forEach(e -> id2Mapper.get(e.getKey()).setActual(e.getValue()));
        id2ExistingEvent.entrySet().stream()
                .filter(e -> !id2Mapper.keySet().contains(e.getKey()))
                .forEach(e -> id2Mapper.put(e.getKey(), new EventMapper(e.getValue())));

        // #4) Re-package grouping on the modification type.
        final SortedMap<DiffHolder.Modification, List<EventMapper>> toReturn = new TreeMap<>();
        for (DiffHolder.Modification current : DiffHolder.Modification.values()) {
            toReturn.put(current, new ArrayList<>());
        }
        id2Mapper.forEach((key, eventMapper) -> toReturn.get(eventMapper.getModification()).add(eventMapper));

        // All Done.
        return toReturn;
    }

    //
    // Private helpers
    //

    private static String sortAndDescribe(final List<Event> events) {

        final Function<Event, String> toIdOrSummary = e -> e.getId() == null ? e.getSummary() : e.getId();

        // All Done.
        return events.stream()
                .map(toIdOrSummary)
                .sorted()
                .reduce((l, r) -> l + ", " + r)
                .orElse("<none>") + "\n";
    }

    private static JsonBatchCallback<Event> createWriteCallback(@NotNull final String calendarIdentifier,
                                                                boolean isInsert) {

        return new JsonBatchCallback<Event>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void onFailure(final GoogleJsonError e, final HttpHeaders responseHeaders) throws IOException {
                final StringBuilder builder = new StringBuilder();
                for (GoogleJsonError.ErrorInfo current : e.getErrors()) {
                    current.setFactory(JSON_FACTORY);
                    builder.append(current.toPrettyString()).append("\n");
                }

                // Log the error, but don't act on it.
                log.error("Failed to " + (isInsert ? "insert" : "update") + " all Events into ["
                        + calendarIdentifier + "].\n" + builder.toString(), e);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onSuccess(final Event event, final HttpHeaders responseHeaders) throws IOException {
                if (log.isInfoEnabled()) {

                    final EventDateTime start = event.getStart();
                    start.setFactory(JSON_FACTORY);

                    log.info("Successfully inserted event [" + event.getSummary() + " (" + event.getId()
                            + ")] at " + start.toPrettyString());
                }
            }
        };
    }

    private static JsonBatchCallback<Void> createDeleteCallback(@NotNull final String calendarIdentifier) {

        return new JsonBatchCallback<Void>() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void onFailure(final GoogleJsonError e, final HttpHeaders responseHeaders) throws IOException {

                final StringBuilder builder = new StringBuilder();
                for (GoogleJsonError.ErrorInfo current : e.getErrors()) {
                    current.setFactory(JSON_FACTORY);
                    builder.append(current.toPrettyString()).append("\n");
                }

                // Log the error, but don't act on it.
                log.error("Failed to delete all Events from [" + calendarIdentifier + "].\n"
                        + builder.toString(), e);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void onSuccess(final Void aVoid, final HttpHeaders responseHeaders) throws IOException {
                if (log.isInfoEnabled()) {

                    // Sort and print the HttpHeaders
                    final SortedMap<String, Object> sortedHeaders = new TreeMap<String, Object>();
                    sortedHeaders.putAll(responseHeaders);

                    final StringBuilder builder = new StringBuilder();
                    for (Map.Entry<String, Object> current : sortedHeaders.entrySet()) {
                        builder.append(" [" + current.getKey() + "]: " + current.getValue() + "\n ");
                    }

                    log.info("Successfully deleted event. Got response headers:\n" + builder.toString());
                }
            }
        };
    }
}
