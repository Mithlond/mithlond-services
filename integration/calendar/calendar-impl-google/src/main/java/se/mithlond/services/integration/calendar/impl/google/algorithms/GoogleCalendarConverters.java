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

import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.activity.Admission;
import se.mithlond.services.organisation.model.activity.EventCalendar;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.Amount;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utility class converting Google Calendar-API-related entities to and from corresponding Mithlond entities.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class GoogleCalendarConverters {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(GoogleCalendarConverters.class);

    // Google Calendar API Constants
    private static final String CALENDAR_EVENT_KIND = "calendar#event";
    private static final String CALENDAR_CALENDAR_KIND = "calendar#calendar";
    private static final String ACTIVITY_ID_PROPERTY = "activityId";
    private static final String JPA_ID_PROPERTY = "jpaId";

    /**
     * The standard Google JacksonFactory instance.
     */
    public static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Converts a local EventCalendar to a Google Calendar instance.
     */
    @SuppressWarnings("all")
    public static final Function<EventCalendar, Calendar> EVENTCALENDAR_TO_GOOGLE_CALENDAR = (eventCalendar) -> {

        // Handle nulls
        if (eventCalendar == null) {
            return null;
        }

        // #1) Get the location of this Calendar ... in its own locale.
        final Locale organisationLocale = eventCalendar.getOwningOrganisation().getLocale();
        final String displayCountry = organisationLocale.getDisplayCountry(organisationLocale);

        // #2) Create the google calendar instance.
        final Calendar toReturn = new Calendar()
                .setTimeZone(eventCalendar.getTimeZoneID().getId())
                .setDescription(eventCalendar.getFullDesc())
                .setKind(CALENDAR_CALENDAR_KIND)
                .setLocation(displayCountry)
                .setSummary(eventCalendar.getShortDesc());

        // #3) Map the EventCalendar JPA ID as an UnknownKey
        final Map<String, Object> unknownKeys = toReturn.getUnknownKeys() == null
                ? new TreeMap<>()
                : toReturn.getUnknownKeys();
        unknownKeys.put(JPA_ID_PROPERTY, eventCalendar.getId());
        toReturn.setUnknownKeys(unknownKeys);

        // All Done.
        return toReturn;
    };

    /**
     * Converts a Google DateTime instance to a joda-time DateTime one.
     */
    @SuppressWarnings("all")
    public static final Function<com.google.api.client.util.DateTime, LocalDateTime>
            GOOGLE_TIME_TO_JAVA8TIME = (googleDateTime) -> {

        // Handle nulls
        if (googleDateTime == null) {
            return null;
        }

        // All Done.
        return TimeFormat.XML_TRANSPORT.parse(googleDateTime.toStringRfc3339()).toLocalDateTime();
    };

    /**
     * Converts a Google DateTime instance to a joda-time DateTime one.
     */
    @SuppressWarnings("all")
    public static final Function<LocalDateTime, com.google.api.client.util.DateTime>
            JAVA8TIME_TO_GOOGLE_TIME = (java8Time) -> {

        if (java8Time == null) {
            return null;
        }

        final long epochMilliSecond = java8Time.atZone(TimeFormat.SWEDISH_TIMEZONE).toEpochSecond() * 1000;

        // All done.
        return new com.google.api.client.util.DateTime(epochMilliSecond);
    };

    /**
     * Function converting an Admission into a Google Calendar EventAttendee.
     */
    @SuppressWarnings("all")
    public static final Function<Admission, EventAttendee> ADMISSION_2_ATTENDEE_CONVERTER = (admission) -> {

        // #1) Extract the admitted Membership.
        final Membership admitted = admission.getAdmitted();

        // #2) Create the base EventAttendee object.
        final EventAttendee toReturn = new EventAttendee()
                .setDisplayName(admitted.getAlias())
                .setEmail(admitted.getEmailAlias() + "@" + admitted.getOrganisation().getEmailSuffix())
                .setOrganizer(admission.isResponsible())
                .setResponseStatus("accepted")
                .setComment(admission.getAdmissionNote()); // The Comment can be 'null'.

        // #3) Some Admission properties cannot be represented as standard Google calendar properties.
        //     Instead, convert these properties to 'UnknownKeys' within the EventAttendee object.
        final String keyPrefix = "admission_" + admitted.getId() + "_";
        Map<String, Object> unknownKeys = toReturn.getUnknownKeys();
        if (unknownKeys == null) {
            unknownKeys = new TreeMap<>();
            toReturn.setUnknownKeys(unknownKeys);
        }

        unknownKeys.put(keyPrefix + "admissionTimestamp", convert(admission.getAdmissionTimestamp()).toString());
        unknownKeys.put(keyPrefix + "id", "" + admitted.getId());

        // All Done.
        return toReturn;
    };

    /*
     * Hide constructor for utility classes.
     */
    private GoogleCalendarConverters() {
        // Do Nothing.
    }

    /**
     * Converts the supplied EventCalendar to a corresponding Google Calendar instance.
     *
     * @param eventCalendar The EventCalendar to convert.
     * @return An EventCalendar entity corresponding uniquely to this GoogleEventCalendar.
     */
    public static Calendar convert(final EventCalendar eventCalendar) {
        return EVENTCALENDAR_TO_GOOGLE_CALENDAR.apply(eventCalendar);
    }

    /**
     * Converts the supplied Activity to a Google calendar Event.
     *
     * @param activity The Activity to convert.
     * @return The Event corresponding to the supplied Activity.
     * @see <a href="https://developers.google.com/google-apps/calendar/v3/reference/events/insert">The Google
     * Calendar API, v3</a>
     */
    public static Event convert(final Activity activity) {

        if (activity == null) {
            return null;
        }

        // #0) Create the Google (calendar) Event to return.
        final Event toReturn = new Event()
                .setKind(CALENDAR_EVENT_KIND)
                .setGuestsCanInviteOthers(false)
                .setGuestsCanModify(false)
                .setStart(new EventDateTime().setDateTime(convert(activity.getStartTime())))
                .setEnd(new EventDateTime().setDateTime(convert(activity.getEndTime())))
                .setLocation(convertToGoogleLocationString(activity.getLocation()));

        // #1) Set summary and description of the event.
        final String eventFullDesc = activity.getFullDesc()
                + "\n\nPlats: " + activity.getAddressShortDescription() + "  (" + activity.getAddressCategory() + ")"
                + "\nDresscode: " + activity.getDressCode()
                + "\nKostnad: " + getCostString(activity.getLateAdmissionDate(),
                activity.getCost(),
                activity.getLateAdmissionCost());
        toReturn.setSummary(activity.getShortDesc());
        toReturn.setDescription(eventFullDesc);

        // #2) Set the event status. Possible values are:
        // - "confirmed" - The event is confirmed. This is the default status.
        // - "tentative" - The event is tentatively confirmed. [not used]
        // - "cancelled" - The event is cancelled.
        final String googleEventStatus = activity.isCancelled() ? "cancelled" : "confirmed";
        toReturn.setStatus(googleEventStatus);

        // TODO: Check the sanity of this.
        toReturn.setUpdated(convert(LocalDateTime.now()));

        // #3) Properties which are present within the Activity that have no corresponding
        //     fields within the Google event are added as private extended properties.
        final Event.ExtendedProperties extProps = primeExtendedProperties(toReturn);
        final Map<String, String> privateProperties = extProps.getPrivate();
        privateProperties.put(ACTIVITY_ID_PROPERTY, "" + activity.getId());
        privateProperties.put("activityVersion", "" + activity.getVersion());
        privateProperties.put("cost", "" + activity.getCost().getValue());
        privateProperties.put("currency", "" + activity.getCost().getCurrency());
        privateProperties.put("lateAdmissionCost", "" + activity.getLateAdmissionCost().getValue());

        final Map<String, String> publicProperties = extProps.getShared();
        publicProperties.put("Dresscode", activity.getDressCode());

        // #4) Add the Event attendees
        final List<EventAttendee> convertedAttendees = activity.getAdmissions()
                .stream()
                .map(GoogleCalendarConverters.ADMISSION_2_ATTENDEE_CONVERTER)
                .collect(Collectors.toList());
        toReturn.setAttendees(convertedAttendees);

        // #5) Populate all nonstandard reminders.
        createReminders(toReturn, activity);

        // All done.
        return toReturn;
    }

    /**
     * Simple ID comparison algorithm which verifies that the JPA ID of the supplied activity is equal
     * to the private property with the key {@link GoogleCalendarConverters#ACTIVITY_ID_PROPERTY}.
     *
     * @param googleEvent An Event to compare
     * @param activity    An Activity to compare
     * @return true if the JPA ID of the Activity equates the value of the Event's private property with
     * the key {@link GoogleCalendarConverters#ACTIVITY_ID_PROPERTY}.
     */
    public static boolean isEquivalentState(@NotNull final Event googleEvent,
                                            @NotNull final Activity activity) {

        // Check sanity
        Validate.notNull(googleEvent, "googleEvent");
        Validate.notNull(activity, "activity");

        // Find the activity ID within the Event.
        final Optional<Long> activityID = getActivityID(googleEvent);
        return activityID.isPresent() && activityID.get().equals(activity.getId());
    }

    /**
     * Updates the supplied event with any changes found within the given activity.
     * It is assumed that the event is originally created from the supplied activity.
     *
     * @param existingGoogleEvent A non-null event which was generated from the supplied Activity and converted by the
     *                            {@code GoogleConverter#convert()} method (implying it has a property with the name
     *                            {@code ACTIVITY_ID_PROPERTY} in its private extended properties holding the activity ID).
     * @param activity            A non-null activity which has been used to generate the supplied Event.
     * @return {@code null} if the supplied Event was not in need of updating, or the event updated with
     * the data from the supplied activity if so required.
     */
    public static Event updateGoogleEvent(final Event existingGoogleEvent, final Activity activity) {

        // Check sanity
        Validate.notNull(existingGoogleEvent, "existingGoogleEvent");
        Validate.notNull(activity, "activity");

        if (!isEquivalentState(existingGoogleEvent, activity)) {

            final String msg = "Cowardly refusing to update existing Google Event [ID: "
                    + existingGoogleEvent.getId() + ", Desc: " + existingGoogleEvent.getDescription()
                    + "] with data from a non-corresponding Activity [JpaID: " + activity.getId()
                    + ", ShortDesc: " + activity.getShortDesc() + "]";
            throw new IllegalArgumentException(msg);
        }

        // Create an event for comparison.
        final Event comparison = convert(activity);
        boolean[] updated = new boolean[]{false};

        // Start with the simple properties
        checkEqualityAndUpdateIfRequired(updated, "summary", existingGoogleEvent, comparison);
        checkEqualityAndUpdateIfRequired(updated, "description", existingGoogleEvent, comparison);
        checkEqualityAndUpdateIfRequired(updated, "location", existingGoogleEvent, comparison);
        checkEqualityAndUpdateIfRequired(updated, "start", existingGoogleEvent, comparison);
        checkEqualityAndUpdateIfRequired(updated, "end", existingGoogleEvent, comparison);

        // Check the relevant Maps.
        final SortedMap<String, EventAttendee> originalAttendees = sortAttendees(existingGoogleEvent);
        final SortedMap<String, EventAttendee> comparisonAttendees = sortAttendees(comparison);
        boolean overwriteAttendees = originalAttendees.size() != comparisonAttendees.size();
        if (!overwriteAttendees) {

            // Perform a deep check
            for (Map.Entry<String, EventAttendee> current : originalAttendees.entrySet()) {
                final EventAttendee originalAttendee = current.getValue();
                final EventAttendee comparisonAttendee = comparisonAttendees.get(current.getKey());

                if (comparisonAttendee == null) {
                    overwriteAttendees = true;
                    break;
                }

                String originalComment = originalAttendee.getComment() == null
                        ? ""
                        : originalAttendee.getComment();
                String comparisonComment = comparisonAttendee.getComment() == null
                        ? ""
                        : comparisonAttendee.getComment();
                boolean unchanged = originalAttendee.getDisplayName().equals(comparisonAttendee.getDisplayName())
                        && originalComment.equals(comparisonComment)
                        && originalAttendee.getResponseStatus().equals(comparisonAttendee.getResponseStatus());
                if (!unchanged) {
                    overwriteAttendees = true;
                    break;
                }
            }
        }
        if (overwriteAttendees) {
            existingGoogleEvent.setAttendees(comparison.getAttendees());
            updated[0] = true;
        }

        final SortedMap<String, String> originalProps = new TreeMap<>(
                existingGoogleEvent.getExtendedProperties().getPrivate());
        final SortedMap<String, String> comparisonProps = new TreeMap<>(
                comparison.getExtendedProperties().getPrivate());
        boolean overwritePrivates = originalProps.size() != comparisonProps.size();
        if (!overwritePrivates) {

            // Perform a deep check
            for (Map.Entry<String, String> current : originalProps.entrySet()) {
                final String originalValue = current.getValue();
                final String comparisonValue = comparisonProps.get(current.getKey());

                if (comparisonValue == null) {
                    overwritePrivates = true;
                    break;
                } else if (!originalValue.equals(comparisonValue)) {
                    overwritePrivates = true;
                    break;
                }
            }
        }
        if (overwritePrivates) {
            existingGoogleEvent.getExtendedProperties().setPrivate(comparison.getExtendedProperties().getPrivate());
            updated[0] = true;
        }

        // All done.
        return updated[0] ? existingGoogleEvent : null;
    }

    /**
     * Retrieves the ActivityID from the supplied Event, assuming that the JPA ID of the ActivityID has been written
     * as the value of the private property {@link #ACTIVITY_ID_PROPERTY}. This is done if the Event is created by the
     * conversion method {@link GoogleCalendarConverters#convert(Activity)}.
     *
     * @param event A google Event.
     * @return The ID for the Activity which created the supplied Event, or {@link Optional#empty()} if not found.
     */
    public static Optional<Long> getActivityID(final Event event) throws NullPointerException {

        // Check sanity
        if (event == null) {
            return Optional.empty();
        }

        // Get the private properties of the supplied Event.
        Optional<Long> toReturn = null;

        final Event.ExtendedProperties extendedProperties = event.getExtendedProperties();
        final boolean hasExtendedProperties = extendedProperties != null;
        final boolean hasPrivateProperties = hasExtendedProperties && extendedProperties.getPrivate() != null;

        if (!hasPrivateProperties) {

            if (log.isWarnEnabled()) {
                log.warn("Event [ID: " + event.getId() + ", Desc: " + event.getDescription()
                        + "] was not generated from an Activity. Has ... extendedProperties: "
                        + hasExtendedProperties + ", privateProperties: " + hasPrivateProperties);
            }

            // Nothing to return
            toReturn = Optional.empty();

        } else {

            // The private properties of the Event exist.
            // Attempt to extract an activityID from them.
            final Map<String, String> privateProperties = extendedProperties.getPrivate();

            try {
                toReturn = Optional.of(Long.parseLong(privateProperties.get(ACTIVITY_ID_PROPERTY)));
            } catch (NumberFormatException e) {

                if (log.isWarnEnabled()) {
                    log.warn("Event [ID: " + event.getId() + ", Desc: " + event.getDescription()
                            + "] found inconsistent activityId within Event private properties.", e);
                }

                toReturn = Optional.empty();
            }
        }

        // All Done.
        return toReturn;
    }

    /**
     * Converts a LocalDateTime instance to a Google calendar client DateTime instance.
     *
     * @param java8LocalDateTime a joda-time DateTime instance.
     * @return The corresponding Google DateTime instance.
     */
    public static com.google.api.client.util.DateTime convert(final LocalDateTime java8LocalDateTime) {
        return JAVA8TIME_TO_GOOGLE_TIME.apply(java8LocalDateTime);
    }

    //
    // Private helpers
    //

    private static Event.ExtendedProperties primeExtendedProperties(final Event event) {

        Event.ExtendedProperties extendedProperties = event.getExtendedProperties();
        if (extendedProperties == null) {
            extendedProperties = new Event.ExtendedProperties();
            event.setExtendedProperties(extendedProperties);
        }

        Map<String, String> privateProperties = extendedProperties.getPrivate();
        if (privateProperties == null) {
            privateProperties = new TreeMap<>();
            extendedProperties.setPrivate(privateProperties);
        }

        Map<String, String> sharedProperties = extendedProperties.getShared();
        if (sharedProperties == null) {
            sharedProperties = new TreeMap<>();
            extendedProperties.setShared(sharedProperties);
        }

        // All done.
        return extendedProperties;
    }

    private static String convertToGoogleLocationString(final Address location) {

        return location.getStreet() + " " + location.getNumber() + ", "
                + location.getZipCode() + " " + location.getCity() + ", "
                + location.getCountry();
    }

    /**
     * Retrieves the cost description for an activity given the supplied data.
     *
     * @param lateAdmissionDate The lateAdmissionDate of the Activity.
     * @param cost              The cost of the Activity.
     * @param lateAdmissionCost The cost of the Activity after the lateAdmissionDate.
     * @return the cost description.
     */
    protected static String getCostString(final LocalDate lateAdmissionDate,
                                          final Amount cost,
                                          final Amount lateAdmissionCost) {

        // Method arguments can be null here.
        final double MIN_COST = 0.1d;
        boolean hasCost = cost != null && cost.getValue().doubleValue() > MIN_COST;
        boolean hasLateAdmissionCost = lateAdmissionCost != null
                && lateAdmissionCost.getValue().doubleValue() > MIN_COST;
        boolean noCost = !hasCost && !hasLateAdmissionCost;

        double costValue = hasCost ? cost.getValue().doubleValue() : 0d;
        double lateAdmissionCostValue = hasLateAdmissionCost ? lateAdmissionCost.getValue().doubleValue() : 0d;
        String currencySuffix = "";

        if (cost != null) {
            currencySuffix = " " + cost.getCurrency().getCurrencySymbol();
        } else if (lateAdmissionCost != null) {
            currencySuffix = " " + lateAdmissionCost.getCurrency().getCurrencySymbol();
        }

        if (noCost) {

            // Frequent case of no cost.
            return "" + 0 + currencySuffix;
        }

        // We have a non-zero cost or lateAdmissionCost
        String costText = "" + ((int) costValue) + currencySuffix;
        if (hasLateAdmissionCost && (Math.abs(costValue - lateAdmissionCostValue) > MIN_COST)) {
            costText += " till " + TimeFormat.YEAR_MONTH_DATE.print(lateAdmissionDate)
                    + ", därefter " + ((int) lateAdmissionCostValue) + currencySuffix;
        }

        // All done.
        return costText;
    }

    /**
     * <p>Creates the following Reminders for the supplied Event:</p>
     * <ol>
     * <li><strong>email</strong>: at 09:30 of the Event day</li>
     * <li><strong>popup</strong>: at 2 hours before the given Event</li>
     * <li><strong>email</strong>: at 09:30 of the day before the lateAdmissionCost (should one exist)</li>
     * </ol>
     *
     * @param event    The event in which to assign custom Reminders.
     * @param activity The Activity being converted.
     */
    private static void createReminders(final Event event, final Activity activity) {

        // #0) Create the Reminders container
        final List<EventReminder> eventReminders = new ArrayList<>();
        final Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(eventReminders);

        // #1) Create a Popup event reminder @ 2 hours before the event.
        eventReminders.add(new EventReminder()
                .setMethod("popup")
                .setMinutes(120));

        // #2) Create an Email event reminder @ 0930 on the event day.
        final LocalDateTime emailEventInstant = activity.getStartTime().withHour(9).withMinute(30);
        final long minutesFromEventStartTo0930 = Duration.between(emailEventInstant, activity.getEndTime()).toMinutes();
        eventReminders.add(new EventReminder()
                .setMethod("email")
                .setMinutes((int) minutesFromEventStartTo0930));

        if (activity.getLateAdmissionCost() != null && activity.getLateAdmissionCost().getValue().doubleValue() > 0.0) {

            // #3) Create an Email late admission cost reminder.
            final LocalDateTime reminderEmailAt0930 = activity.getLateAdmissionDate().atTime(9, 30);
            final long minutesFromEvent = Duration.between(reminderEmailAt0930, activity.getEndTime()).toMinutes();
            eventReminders.add(new EventReminder()
                    .setMethod("email")
                    .setMinutes((int) minutesFromEvent));
        }

        // Finally, assign the reminders to the Event.
        event.setReminders(reminders);
    }

    private static SortedMap<String, EventAttendee> sortAttendees(final Event event) {

        final SortedMap<String, EventAttendee> toReturn = new TreeMap<>();
        for (EventAttendee current : event.getAttendees()) {
            toReturn.put(current.getEmail(), current);
        }

        return toReturn;
    }

    private static void checkEqualityAndUpdateIfRequired(final boolean[] updated,
                                                         final String keyName,
                                                         final Event original,
                                                         final Event comparison) {

        final Object originalValue = original.get(keyName);
        final Object comparisonValue = comparison.get(keyName);

        // Weed out the fringe cases where one or both values are null.
        boolean bothValuesAreNull = originalValue == null && comparisonValue == null;
        boolean oneValueIsNull = (originalValue == null && comparisonValue != null)
                || (originalValue != null && comparisonValue == null);

        if (oneValueIsNull) {

            if (log.isDebugEnabled()) {
                log.debug("Property [" + keyName + "] differs; original [" + originalValue + "] and comparison ["
                        + comparisonValue + "]. Updating original.");
            }

            // Update the original event.
            original.set(keyName, comparisonValue);
            updated[0] = true;
        }

        if (bothValuesAreNull || oneValueIsNull) {

            // All done.
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Property [" + keyName + "] has type (original:" + originalValue.getClass().getName()
                    + ") and (comparison:" + comparisonValue.getClass().getName() + ")");
        }

        // We should only compare Strings
        String originalString;
        String comparisonString;
        if (originalValue instanceof String && comparisonValue instanceof String) {

            // Just cast the values.
            originalString = (String) originalValue;
            comparisonString = (String) comparisonValue;

        } else if (originalValue instanceof com.google.api.client.util.DateTime
                && comparisonValue instanceof com.google.api.client.util.DateTime) {

            // Convert to Strings.
            originalString = originalValue.toString();
            comparisonString = comparisonValue.toString();

        } else if (originalValue instanceof EventDateTime && comparisonValue instanceof EventDateTime) {

            final EventDateTime originalTime = (EventDateTime) originalValue;
            final EventDateTime comparisonTime = (EventDateTime) comparisonValue;

            // Convert to Strings
            originalString = TimeFormat.XML_TRANSPORT.print(
                    GOOGLE_TIME_TO_JAVA8TIME.apply(originalTime.getDateTime()));
            comparisonString = TimeFormat.XML_TRANSPORT.print(
                    GOOGLE_TIME_TO_JAVA8TIME.apply(comparisonTime.getDateTime()));

        } else {

            // Complain
            throw new IllegalArgumentException("Could not compare values of type [original: "
                    + originalValue.getClass().getName() + "] and [comparison: "
                    + comparisonValue.getClass().getName() + "]");
        }

        // Compare and update if required
        if (!originalString.equals(comparisonString)) {
            if (log.isDebugEnabled()) {
                log.debug("Property [" + keyName + "] differs; original [" + originalString
                        + "] and comparison [" + comparisonString + "]. Updating original.");
            }

            // Update and return
            original.set(keyName, comparisonValue);
            updated[0] = true;
        }
    }
}
