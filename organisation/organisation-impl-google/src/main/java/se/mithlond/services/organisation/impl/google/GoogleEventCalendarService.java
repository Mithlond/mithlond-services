/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-impl-google
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
package se.mithlond.services.organisation.impl.google;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.organisation.api.EventCalendarService;
import se.mithlond.services.organisation.impl.google.auth.GoogleAuthenticator;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.activity.EventCalendar;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.algorithms.Deployment;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.ejb.EJB;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * EventCalendarService implementation which pushes Activities to Google Calendars
 * using the Google calendar API.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class GoogleEventCalendarService extends AbstractJpaService implements EventCalendarService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(GoogleEventCalendarService.class);

    // Global JSON factory instance.
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * The mandatory prefix of the Google Calendar Application in the development environment,
     * which must be followed by one of the permitted environments to form a fully configured application name.
     * (Each application name must be defined in the Google Development Console).
     */
    public static final String APPLICATION_PREFIX = "CalendarApp";

    /**
     * The key for the environment variable (or java property) holding the absolute path to
     * the directory containing the Google certificate storage.
     */
    public static final String GOOGLE_ENVIRONMENT = "environment.type";

    /**
     * Standard directory name for the (Google) Nazgul certificate store.
     */
    public static final String DEFAULT_CERTSTORE_DIRNAME = ".nazgulCertStore";

    /**
     * The name of the ServiceAccount p12 file.
     */
    public static final String SERVICE_ACCOUNT_P12_FILE = "serviceKey.p12";

    /**
     * The name of the file containing the ServiceAccount email.
     */
    public static final String SERVICE_ACCOUNT_EMAIL_FILE = "serviceAccountEmail.txt";

    /**
     * The access definition ("access scope" in google lingo) required by this GoogleCalendarService
     * for interaction with the Google calendar API.
     */
    public static final String CALENDARDATA_WRITE_SCOPE = "https://www.googleapis.com/auth/calendar";


    // Internal state
    @EJB(beanInterface = ServiceAccountOauth2Authenticator.class)
    private GoogleAuthenticator authenticator;
    private HttpTransport httpTransport;
    private String applicationName;


    /**
     * JPA-friendly constructor.
     */
    @SuppressWarnings("all")
    public GoogleEventCalendarService() {

        // Use the service account authenticator.
        /*
        if(authenticator == null) {
            authenticator = new ServiceAccountOauth2Authenticator();
        }
        */
    }

    /**
     * DI-aware constructor, to be used only during testing.
     *
     * @param authenticator The GoogleAuthenticator to use.
     * @param httpTransport The HttpTransport to use.
     */
    public GoogleEventCalendarService(final GoogleAuthenticator authenticator,
                                      final HttpTransport httpTransport) {

        // Check sanity
        Validate.notNull(authenticator, "authenticator");
        Validate.notNull(httpTransport, "httpTransport");

        // Assign internal state
        this.authenticator = authenticator;
        this.httpTransport = httpTransport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EventCalendar> getCalendars(final String organisationName, final Membership activeMembership) {

        // Check sanity
        Validate.notEmpty(organisationName, "organisationName");
        Validate.notNull(activeMembership, "activeMembership");

        // Find the current Runtime Environment.

        // Find all relevant calendars within the local database.
        final List<EventCalendar> resultList = entityManager.createNamedQuery(
                EventCalendar.NAMEDQ_GET_BY_ORGANISATION_AND_RUNTIME,
                EventCalendar.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisationName)
                .setParameter(OrganisationPatterns.PARAM_ENVIRONMENT_ID, Deployment.getDeploymentType())
                .getResultList();

        // All Done.
        return resultList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pushActivities(final String calendarIdentifier,
                               final String owningOrganisationName,
                               final LocalDate startTime,
                               final LocalDate endTime,
                               final Membership activeMembership) {

        // Check sanity
        Validate.notEmpty(calendarIdentifier, "calendarIdentifier");
        Validate.notEmpty(owningOrganisationName, "owningOrganisationName");
        Validate.notNull(startTime, "startTime");
        Validate.notNull(endTime, "endTime");
        Validate.isTrue(startTime.isBefore(endTime), "startTime.isBefore(endTime)");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pushActivities(final EventCalendar calendar, final DateTime startTime, final DateTime endTime) {

        // Check sanity
        Validate.notNull(calendar, "Cannot handle null calendar argument.");
        Validate.notNull(startTime, "Cannot handle null startTime argument.");
        Validate.notNull(endTime, "Cannot handle null endTime argument.");
        Validate.isTrue(startTime.isBefore(endTime), "startTime must be before endTime.");

        // Acquire all locally stored Activities matching the search criteria.
        final String organisationName = calendar.getOwningOrganisation().getOrganisationName();
        final List<Activity> localActivities = fireNamedQuery("getActivitesByDateRange",
                organisationName,
                startTime.toGregorianCalendar(),
                endTime.toGregorianCalendar());

        if (log.isDebugEnabled()) {
            final StringBuilder builder = new StringBuilder("\n\n ==== Found [" + localActivities.size()
                    + "] activities ====\n\n");
            int i = 0;
            for (Activity current : localActivities) {
                builder.append("[" + (i++) + "]: " + current.toString());
            }

            // Log somewhat
            log.debug(builder.toString());
        }

        // Connect to Google calendar service, and retrieve all activities
        // between startTime and endTime for the relevant environment calendar.
        final Calendar calendarClient = getCalendarClient(
                organisationName,
                calendar.getRuntimeEnvironment(),
                calendar.getCalendarIdentifier());

        final Events events = getGoogleEvents(calendarClient,
                calendar.getCalendarIdentifier(),
                startTime,
                endTime);

        final List<Event> eventList = events == null ? new ArrayList<Event>() : events.getItems();

        if (log.isDebugEnabled()) {
            log.debug("Got [" + eventList.size() + "] events from Google calendar [" + calendar.getShortDesc() + "]");
            for (Event current : eventList) {

                current.setFactory(JSON_FACTORY);
                try {
                    log.debug("" + current.toPrettyString());
                } catch (IOException e) {
                    log.error("Could not prettyPrint Google event", e);
                }
            }
        }

        // Map the Google Events to our Activity entities, to find data that should be
        // altered within our database or within the Google Calendar.
        final ActivityEventMatchHolder holder = mapActivitiesToGoogleEvents(localActivities, eventList);

        // Convert the Activities to Google Events.
        final List<Event> addToGoogleCalendar = new ArrayList<Event>();
        for (Activity current : holder.getUnmatchedActivities()) {
            addToGoogleCalendar.add(GoogleConverter.convert(current));
        }

        // Update the Google calendar.
        pushEventsToGoogleCalendar(
                calendarClient,
                calendar.getCalendarIdentifier(),
                addToGoogleCalendar,
                holder.getUpdatedEvents(),
                holder.getUnmatchedEvents());
    }

    /**
     * Sorts and maps the provided Activity instances to Google Event instances.
     *
     * @param activities The Activities to sort and map.
     * @param events     The Events to sort and map.
     * @return An ActivityEventMatchHolder holder instance.
     */
    public ActivityEventMatchHolder mapActivitiesToGoogleEvents(final List<Activity> activities,
                                                                final List<Event> events) {

        // Check sanity
        Validate.notNull(activities, "Cannot handle null activities argument.");
        Validate.notNull(events, "Cannot handle null events argument.");

        final ActivityEventMatchHolder toReturn = new ActivityEventMatchHolder();
        final List<Activity> addToGoogleCalendar = toReturn.getUnmatchedActivities();
        final List<Event> removeFromGoogleCalendar = toReturn.getUnmatchedEvents();
        final List<Event> updatedEvents = toReturn.getUpdatedEvents();
        final Map<Activity, Event> updateCandidates = new HashMap<Activity, Event>();

        final SortedMap<Long, Activity> key2Activities = getActivityId2ActivityMap(activities);
        final SortedMap<Long, Event> key2Events = getActivityId2EventMap(events);

        final Set<Long> activityKeys = key2Activities.keySet();
        final Set<Long> eventKeys = key2Events.keySet();

        // Activities whose keys match that of a retrieved Event are update candidates.
        // Activities whose keys do not match that of a retrieved Event should be created in the Google calendar.
        for (Map.Entry<Long, Activity> current : key2Activities.entrySet()) {

            final Long currentKey = current.getKey();

            if (eventKeys.contains(currentKey)) {

                if (log.isDebugEnabled()) {
                    log.debug("CurrentKey [" + currentKey + "] was found in key2Events [" + key2Events.keySet() + "]");
                }

                updateCandidates.put(current.getValue(), key2Events.get(currentKey));
            } else {

                if (log.isDebugEnabled()) {
                    log.debug("CurrentKey [" + currentKey + "] was NOT found in key2Events keySet ["
                            + key2Events.keySet() + "]");
                }

                addToGoogleCalendar.add(current.getValue());
            }
        }

        // Events whose keys do not match that of an existing Activity
        // should be removed from the Google calendar.
        for (Map.Entry<Long, Event> current : key2Events.entrySet()) {
            if (!activityKeys.contains(current.getKey())) {
                removeFromGoogleCalendar.add(current.getValue());
            }
        }

        // Now, find if any of the update candidates should actually be updated.
        for (Map.Entry<Activity, Event> current : updateCandidates.entrySet()) {
            final Event updatedEvent = GoogleConverter.getUpdatedEvent(current.getValue(), current.getKey());
            if (updatedEvent != null) {
                updatedEvents.add(updatedEvent);
            }
        }

        // All done.
        return toReturn;
    }

    class ActivityEventMatchHolder implements Serializable {

        // Internal state
        private List<Activity> unmatchedActivities;
        private List<Event> unmatchedEvents;
        private List<Event> updatedEvents;

        public ActivityEventMatchHolder() {

            unmatchedActivities = new ArrayList<Activity>();
            unmatchedEvents = new ArrayList<Event>();
            updatedEvents = new ArrayList<Event>();
        }

        public List<Event> getUnmatchedEvents() {
            return unmatchedEvents;
        }

        public List<Activity> getUnmatchedActivities() {
            return unmatchedActivities;
        }

        public List<Event> getUpdatedEvents() {
            return updatedEvents;
        }
    }

    //
    // Private helpers
    //

    /**
     * Sorts the Activities into a map using the activity ID.
     *
     * @param activities The Activities to sort and map.
     * @return A SortedMap relating the activity ID to the Activity.
     */
    private SortedMap<Long, Activity> getActivityId2ActivityMap(final List<Activity> activities) {

        final SortedMap<Long, Activity> toReturn = new TreeMap<Long, Activity>();

        for (Activity current : activities) {

            final Activity previousActivityWithSameKey = toReturn.put(current.getId(), current);
            if (previousActivityWithSameKey != null) {

                // This should *really* not happen.
                throw new IllegalStateException("Two activities held the same key. [" + current.getFullDesc()
                        + "] and [" + previousActivityWithSameKey.getFullDesc() + "]");
            }
        }

        // All done
        return toReturn;
    }

    /**
     * Sorts the Google Events into a map using the corresponding Activity ID as key.
     *
     * @param events The Events to sort and map.
     * @return A SortedMap relating the Activity ID of the activity which was used to
     * create the Event to the Event itself.
     */
    private SortedMap<Long, Event> getActivityId2EventMap(final List<Event> events) {

        final SortedMap<Long, Event> toReturn = new TreeMap<Long, Event>();

        for (Event current : events) {

            // Get the activityID from the supplied Event.
            final Event previousEventWithSameKey = toReturn.put(GoogleConverter.getActivityID(current), current);
            if (previousEventWithSameKey != null) {
                throw new IllegalStateException("Two events held the same key. ['"
                        + current.getDescription() + "' with id '" + current.getId() + "'] and ['"
                        + previousEventWithSameKey.getDescription() + "' with id '"
                        + previousEventWithSameKey.getId() + "']");
            }
        }

        // All done
        return toReturn;
    }

    /**
     * Adds all the supplied Events to the calendar with the supplied Name.
     *
     * @param calendarClient                   The fully authorized and connected Google Calendar client.
     * @param calendarIdentifier               The ID of the Calendar into which we should insert all given Events.
     * @param eventsToCreateInGoogleCalendar   The Events to insert into the Google Calendar.
     * @param eventsToUpdateInGoogleCalendar   The Events to update within the Google Calendar.
     * @param eventsToRemoveFromGoogleCalendar The Events to remove from the Google Calendar.
     */
    protected void pushEventsToGoogleCalendar(final com.google.api.services.calendar.Calendar calendarClient,
                                              final String calendarIdentifier,
                                              final List<Event> eventsToCreateInGoogleCalendar,
                                              final List<Event> eventsToUpdateInGoogleCalendar,
                                              final List<Event> eventsToRemoveFromGoogleCalendar) {

        if (log.isDebugEnabled()) {

            StringBuilder builder = new StringBuilder();
            builder.append("Creating [" + eventsToCreateInGoogleCalendar.size() + "] events.\n");
            builder.append("Updating [" + eventsToUpdateInGoogleCalendar.size() + "] events.\n");
            builder.append("Removing [" + eventsToRemoveFromGoogleCalendar.size() + "] events.\n");

            for (Event current : eventsToCreateInGoogleCalendar) {
                final String idOrSummary = current.getId() == null ? current.getSummary() : current.getId();
                builder.append("   Create [" + idOrSummary + "]\n");
            }
            for (Event current : eventsToUpdateInGoogleCalendar) {
                final String idOrSummary = current.getId() == null ? current.getSummary() : current.getId();
                builder.append("   Update [" + idOrSummary + "]\n");
            }
            for (Event current : eventsToRemoveFromGoogleCalendar) {
                final String idOrSummary = current.getId() == null ? current.getSummary() : current.getId();
                builder.append("   Remove [" + idOrSummary + "]\n");
            }

            // Log somewhat
            log.debug("" + builder.toString());
        }

        try {

            // Batch the calls to increase efficiency and reduce network latency.
            final JsonBatchCallback<Event> eventInsertBatchCallback = new JsonBatchCallback<Event>() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onFailure(final GoogleJsonError e, final HttpHeaders responseHeaders) throws IOException {
                    final StringBuilder builder = new StringBuilder();
                    for (GoogleJsonError.ErrorInfo current : e.getErrors()) {
                        current.setFactory(GoogleAuthenticator.JSON_FACTORY);
                        builder.append(current.toPrettyString()).append("\n");
                    }

                    // Log the error, but don't act on it.
                    log.error("Failed to insert all Events into [" + calendarIdentifier + "].\n"
                            + builder.toString(), e);
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onSuccess(final Event event, final HttpHeaders responseHeaders) throws IOException {
                    if (log.isInfoEnabled()) {

                        final EventDateTime start = event.getStart();
                        start.setFactory(GoogleAuthenticator.JSON_FACTORY);

                        log.info("Successfully inserted event [" + event.getSummary() + "] at "
                                + start.toPrettyString());
                    }
                }
            };

            final JsonBatchCallback<Void> eventDeleteBatchCallback = new JsonBatchCallback<Void>() {
                @Override
                public void onFailure(final GoogleJsonError e, final HttpHeaders responseHeaders) throws IOException {

                }

                @Override
                public void onSuccess(final Void aVoid, final HttpHeaders responseHeaders) throws IOException {
                    if (log.isInfoEnabled()) {

                        // Sort and print the HttpHeaders
                        final SortedMap<String, Object> sortedHeaders = new TreeMap<String, Object>();
                        sortedHeaders.putAll(responseHeaders);

                        final StringBuilder builder = new StringBuilder();
                        for (Map.Entry<String, Object> current : sortedHeaders.entrySet()) {
                            builder.append(" [" + current.getKey() + "]: " + current.getValue() + "\n");
                        }

                        log.info("Successfully deleted event. Got response headers:\n" + builder.toString());
                    }
                }
            };

            final BatchRequest batch = calendarClient.batch();
            for (Event current : eventsToCreateInGoogleCalendar) {

                // Queue the insert in the batch.
                calendarClient.events().insert(calendarIdentifier, current)
                        .queue(batch, eventInsertBatchCallback);
            }
            for (Event current : eventsToRemoveFromGoogleCalendar) {

                // Queue the delete in the batch.
                calendarClient.events().delete(calendarIdentifier, current.getId())
                        .queue(batch, eventDeleteBatchCallback);
            }
            for (Event current : eventsToUpdateInGoogleCalendar) {

                // Queue the update in the batch.
                calendarClient.events().update(calendarIdentifier, current.getId(), current)
                        .queue(batch, eventInsertBatchCallback);
            }

            // Fire the Batch
            batch.execute();

        } catch (Exception e) {
            throw new IllegalStateException("Could not synchronize Google calendars.", e);
        }
    }

    /**
     * Retrieves all Events for the given data.
     *
     * @param calendarClient The fully authorized and connected Google Calendar client.
     * @param calendarId     The remote identifier of the Calendar from which we should retrieve all activities,
     *                       such as {@code mithlond.official@gmail.com}.
     * @param startTime      Only events that starts after this startTime will be retrieved.
     * @param endTime        Only events that ends before this endTime will be retrieved.
     */
    protected Events getGoogleEvents(final com.google.api.services.calendar.Calendar calendarClient,
                                     final String calendarId,
                                     final DateTime startTime,
                                     final DateTime endTime) {

        try {

            // Fire the rest query to the Google server, and handle the results.
            final Events result = calendarClient.events().list(calendarId)
                    .setTimeMin(GoogleConverter.convert(startTime))
                    .setTimeMax(GoogleConverter.convert(endTime))
                    .execute();

            if (log.isDebugEnabled()) {

                final String stringFormat = result.getUpdated() != null
                        ? result.getUpdated().toStringRfc3339()
                        : "<unknown>";
                log.debug("Events updated time: " + stringFormat);
            }

            // All done.
            return result;

        } catch (Exception e) {
            throw new IllegalStateException("Could not retrieve Google Events.", e);
        }
    }

    /**
     * Assigns the Google application name, for test purposes.
     * <strong>Note!</strong> This should only be invoked in testing, and should be removed when
     * defined as correctly implemented.
     *
     * @param applicationName the Google application name, for test purposes. If used, should be set to
     *                        something like [app][environment], such as "FooDevelopment".
     * @deprecated This should only be invoked in testing, and should be removed when the Google
     * application names are re-generated in the standardized form now assumed by this
     * service implementation.
     */
    protected void setApplicationName(final String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * @return The Google environment used. The google environment parameter defines which
     * security credentials are used and which google application name is connected to by
     * this GoogleCalendarService.
     */
    private String getEnvironment() {

        // Harvest value from the environment or Java system properties.
        String envName = getEnvOrProperty(GOOGLE_ENVIRONMENT);

        // Check sanity
        if (envName == null) {
            envName = RuntimeEnvironment.DEVELOPMENT.getCamelHumpName();
        } else {
            try {
                final RuntimeEnvironment selected = RuntimeEnvironment.valueOf(RuntimeEnvironment.class, envName.toUpperCase());
                envName = selected.getCamelHumpName();
            } catch (Exception e) {
                log.error("RuntimeEnvironment [" + envName.toUpperCase() + "] is unknown. Known environments: "
                        + Arrays.asList(RuntimeEnvironment.values()) + ". Reverting to "
                        + RuntimeEnvironment.DEVELOPMENT.getCamelHumpName());
                envName = RuntimeEnvironment.DEVELOPMENT.getCamelHumpName();
            }
        }

        // All done.
        if (log.isDebugEnabled()) {
            log.debug("Retrieved environment name [" + envName + "]");
        }
        return envName;
    }

    /**
     * Trivial helper method which retrieves the value for the given key, when searching
     * through (in order) 1. RuntimeEnvironment properties, and 2. Java System properties.
     * The first non-null value found will be returned.
     *
     * @param key The key for which an environment or java system property should be found.
     * @return The found value, or {@code null} if none was found.
     */

    private String getEnvOrProperty(final String key) {

        String toReturn = null;
        boolean foundInEnvironment = true;

        toReturn = System.getenv(key);
        if (toReturn == null) {
            toReturn = System.getProperty(key);
            foundInEnvironment = false;
        }

        // Debug somewhat
        if (log.isDebugEnabled()) {
            final String location = foundInEnvironment ? " env " : " java props ";
            final String msg = "Property [" + key + "] "
                    + (toReturn == null ? "not found." : "found in [" + location + "]: " + toReturn);
            log.debug(msg);
        }

        // All done.
        return toReturn;
    }

    /**
     * Authorizes and retrieves the Google Calendar client.
     *
     * @param organisationName          The name of the Organisation for which we should authorize.
     * @param environment               The name of the environment in which this GoogleCalendarService is running.
     * @param googleServiceAccountEmail The email of the serviceAccount for the EventCalendar we want to sync data for.
     * @return The fully authorized Google Calendar client.
     */
    public com.google.api.services.calendar.Calendar getCalendarClient(final String organisationName,
                                                                       final String environment,
                                                                       final String googleServiceAccountEmail) {

        try {
            // Provide the google serviceAccountEmail to the additionalProperties.
            final SortedMap<String, String> additionalProperties = new TreeMap<String, String>();
            additionalProperties.put(GoogleAuthenticator.PROPERTY_SERVICE_ACCOUNT_USER, googleServiceAccountEmail);

            // Open an Http connection to the Google calendar service, if required.
            if (httpTransport == null) {
                httpTransport = GoogleNetHttpTransport.newTrustedTransport();
            }

            final Credential credentials = authenticator.authorize(
                    RuntimeEnvironment.valueOf(environment.toUpperCase().trim()),
                    organisationName,
                    "calendarservice",
                    Arrays.asList(CALENDARDATA_WRITE_SCOPE),
                    httpTransport,
                    additionalProperties);

            // Build the google application name.
            final String appName = this.applicationName == null
                    ? APPLICATION_PREFIX + getEnvironment()
                    : this.applicationName;

            // All done.
            return new Calendar.Builder(httpTransport, JSON_FACTORY, credentials)
                    .setApplicationName(appName)
                    .build();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not acquire the Google Calendar client.", e);
        }
    }
}
