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
package se.mithlond.services.integration.calendar.impl.google;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.integration.calendar.impl.google.algorithms.CalendarAlgorithms;
import se.mithlond.services.integration.calendar.impl.google.algorithms.EventMapper;
import se.mithlond.services.integration.calendar.impl.google.algorithms.GoogleCalendarConverters;
import se.mithlond.services.integration.calendar.impl.google.auth.GoogleAuthenticator;
import se.mithlond.services.organisation.api.EventCalendarService;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.activity.EventCalendar;
import se.mithlond.services.shared.spi.algorithms.Deployment;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.algorithms.diff.DiffHolder;
import se.mithlond.services.shared.spi.algorithms.messages.JmsCompliantMap;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.function.Function;

/**
 * MDB implementation which pushes locally stored events within a particular EventCalendar
 * to a remote Google calendar.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MessageDriven(
        messageListenerInterface = MessageListener.class,
        activationConfig = {
                @ActivationConfigProperty(
                        propertyName = "destinationLookup",
                        propertyValue = EventCalendarService.EVENT_CALENDAR_REQUEST_QUEUE),
                @ActivationConfigProperty(
                        propertyName = "destinationType",
                        propertyValue = "javax.jms.Queue")})
public class EventCalendarSyncMDB implements MessageListener {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(EventCalendarSyncMDB.class);

    private static final MessageAccessor<String> BODY_ACCESSOR = new MessageAccessor<String>("body") {
        @Override
        String getPropertyFrom(final TextMessage msg) throws JMSException {
            return msg.getText();
        }
    };

    // Java-EE-injected state
    @PersistenceContext(unitName = "DataPumpPersistenceUnit")
    protected EntityManager entityManager;

    @Resource(lookup = EventCalendarService.EVENT_CALENDAR_ERROR_QUEUE)
    protected Queue errorMessageQueue;

    @Inject protected JMSContext jmsContext;

    @EJB
    protected GoogleAuthenticator authenticator;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(final Message message) {

        // Check sanity
        if (message instanceof TextMessage) {

            final TextMessage textMessage = (TextMessage) message;

            try {

                // Delegate
                handle(textMessage);

            } catch (Exception e) {

                // Could not handle this request.
                // Stash this message into the DLQ?
                // ... and send an annoying email?
                final JMSProducer producer = jmsContext.createProducer();
                final String inboundBody = getPropertyFrom(textMessage, BODY_ACCESSOR);
                final JmsCompliantMap inboundProperties = JmsCompliantMap.getPropertyMap(textMessage);

                // Copy body + JMS properties to the outbound message.
                final TextMessage outboundMessage = jmsContext.createTextMessage(inboundBody);
                JmsCompliantMap.copyProperties(outboundMessage, inboundProperties);

                // Send the message to the error message queue.
                producer.send(errorMessageQueue, outboundMessage);
            }
        } else {

            // Complain somewhat
            log.error("Cannot handle inbound JMS message of type ["
                    + (message == null ? "<null>" : message.getClass().getName())
                    + "]. Expected TextMessage.");
        }
    }

    /**
     * Handler method accepting an inbound TextMessage which should adhere to the structural
     * requirements as sent within the EventCalendarServiceBean from which it was sent.
     *
     * @param msg A non-null TextMessage
     */
    public void handle(@NotNull final TextMessage msg) {

        // Check sanity
        Validate.notNull(msg, "msg");

        // #1) Get required JMS properties
        final JmsCompliantMap props = JmsCompliantMap.getPropertyMap(msg);
        final String owningOrganisationName = (String) props.get("organisation_name");
        final String startTimeStringForm = (String) props.get("start_time");
        final String endTimeStringForm = (String) props.get("end_time");
        final String activeMembershipAlias = (String) props.get("active_membership_alias");
        final String calendarIdentifier = (String) props.get("calendar_identifier");
        final Long activeMembershipJpaID = (Long) props.get("active_membership_id");

        // #2) Check argument sanity
        Validate.notEmpty(owningOrganisationName, "owningOrganisationName");
        Validate.notEmpty(startTimeStringForm, "startTimeStringForm");
        Validate.notEmpty(endTimeStringForm, "endTimeStringForm");
        Validate.notEmpty(activeMembershipAlias, "activeMembershipAlias");
        Validate.notEmpty(calendarIdentifier, "calendarIdentifier");
        Validate.notNull(activeMembershipJpaID, "activeMembershipJpaID");

        // #3) Find the Organisation whose Activities should be synchronized with Google Calendar.
        final Organisation theOrganisation = entityManager.createNamedQuery(
                Organisation.NAMEDQ_GET_BY_NAME, Organisation.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, owningOrganisationName)
                .getSingleResult();
        if (log.isDebugEnabled()) {
            log.debug("Found organisation [" + theOrganisation.getOrganisationName() + ", JpaID: "
                    + theOrganisation.getId() + "] from owningOrganisationName [" + owningOrganisationName + "]");
        }

        final List<Long> organisationIDs = new ArrayList<>();
        organisationIDs.add(theOrganisation.getId());

        // #4) Find all Calendar events matching the supplied inbound parameters
        final LocalDateTime startTime = TimeFormat.YEAR_MONTH_DATE.parse(startTimeStringForm).toLocalDateTime();
        final LocalDateTime endTime = TimeFormat.YEAR_MONTH_DATE.parse(endTimeStringForm).toLocalDateTime();

        if (log.isDebugEnabled()) {
            log.debug("Found startTime [" + TimeFormat.YEAR_MONTH_DATE.print(startTime)
                    + "] from startTimeStringForm [" + startTimeStringForm + "]");
            log.debug("Found endTime [" + TimeFormat.YEAR_MONTH_DATE.print(endTime)
                    + "] from endTimeStringForm [" + endTimeStringForm + "]");
        }

        final List<Activity> activities = entityManager.createNamedQuery(
                Activity.NAMEDQ_GET_BY_ORGANISATION_IDS_AND_DATERANGE, Activity.class)
                .setParameter(OrganisationPatterns.PARAM_NUM_ORGANISATIONIDS, organisationIDs.size())
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_IDS, organisationIDs)
                .setParameter(OrganisationPatterns.PARAM_START_TIME, startTime)
                .setParameter(OrganisationPatterns.PARAM_END_TIME, endTime)
                .getResultList();

        if (log.isDebugEnabled()) {
            log.debug("Found [" + (activities == null ? "<null>" : "" + activities.size())
                    + "] locally defined Activities between ["
                    + TimeFormat.YEAR_MONTH_DATE.print(startTime) + "] and ["
                    + TimeFormat.YEAR_MONTH_DATE.print(endTime) + "] for organisation ["
                    + owningOrganisationName + "]");
        }

        // #5) Find the EventCalendar for which activities should be synchronized
        final EventCalendar activeEventCalendar = entityManager.createNamedQuery(
                EventCalendar.NAMEDQ_GET_BY_ORGANISATION_AND_RUNTIME, EventCalendar.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, owningOrganisationName)
                .setParameter(OrganisationPatterns.PARAM_EVENT_CALENDAR, calendarIdentifier)
                .setParameter(OrganisationPatterns.PARAM_ENVIRONMENT_ID, Deployment.getDeploymentType())
                .getSingleResult();
        if (activeEventCalendar == null) {
            throw new IllegalArgumentException("Cannot find EventCalendar for calendarIdentifier ["
                    + calendarIdentifier + "]");
        }

        // #6) Find the google Calendar Client for the supplied EventCalendar and Organisation.
        final Calendar calendarClient = authenticator.getCalendarClient(owningOrganisationName);

        if (log.isDebugEnabled()) {
            log.debug("Got non-null Calendar client of type [" + calendarClient.getClass().getCanonicalName() + "]");
        }

        // #7) Find the Events corresponding to the selection criteria interval within the Google Calendar.
        final List<Event> existingEvents = CalendarAlgorithms.getEventsFromGoogle(
                calendarClient,
                calendarIdentifier,
                startTime,
                endTime);

        if (log.isDebugEnabled()) {
            log.debug("Found [" + (existingEvents == null ? "<null>" : "" + existingEvents.size())
                    + "] existing Events within Google Calendar [" + calendarIdentifier + "] between ["
                    + TimeFormat.YEAR_MONTH_DATE.print(startTime) + "] and ["
                    + TimeFormat.YEAR_MONTH_DATE.print(endTime) + "] for organisation ["
                    + owningOrganisationName + "]");
        }

        // #8) Relate the two (potentially disparate) information sets.
        //
        final SortedMap<DiffHolder.Modification, List<EventMapper>> mod2EventMappersMap =
                CalendarAlgorithms.mapEvents(existingEvents, activities);

        // #9) Sort the Events according to their modification status, implying
        //     that the operations to be performed on each of the events are known.
        //
        final List<Event> toCreate = filter(mod2EventMappersMap,
                DiffHolder.Modification.CREATED,
                eventMapper -> GoogleCalendarConverters.convert(eventMapper.getActivity()));

        final List<Event> toUpdate = filter(mod2EventMappersMap,
                DiffHolder.Modification.MODIFIED,
                em -> GoogleCalendarConverters.updateGoogleEvent(em.getEvent(), em.getActivity()));

        final List<Event> toRemove = filter(mod2EventMappersMap,
                DiffHolder.Modification.DELETED,
                EventMapper::getEvent);

        CalendarAlgorithms.synchronizeWithGoogleCalendar(
                calendarClient,
                calendarIdentifier,
                toCreate,
                toUpdate,
                toRemove);
    }

    //
    // Private helpers
    //

    private static List<Event> filter(final SortedMap<DiffHolder.Modification, List<EventMapper>> sourceMap,
                                      @NotNull final DiffHolder.Modification modification,
                                      @NotNull final Function<EventMapper, Event> mapper) {

        final List<Event> toReturn = new ArrayList<>();

        sourceMap.get(modification)
                .stream()
                .filter(Objects::nonNull)
                .map(mapper)
                .forEach(toReturn::add);

        // All done.
        return toReturn;
    }

    /**
     * Trivial holder for TextMessage data.
     *
     * @param <T> The type of property to retrieve from the Message.
     */
    private abstract static class MessageAccessor<T> {

        // Internal state
        private String propertyName;

        /**
         * Compound constructor creating a MessageAccessor wrapping the supplied property name.
         *
         * @param propertyName A non-empty property name.
         */
        public MessageAccessor(@NotNull final String propertyName) {
            this.propertyName = Validate.notEmpty(propertyName, "propertyName");
        }

        /**
         * Retrieves the name of the JMS property which should be gotten.
         *
         * @return the name of the JMS property which should be gotten.
         */
        public String getPropertyName() {
            return propertyName;
        }

        /**
         * Retrieves a property from the supplied non-null TextMessage.
         *
         * @param msg The TextMessage from which to acquire a JMS-hazardous property.
         * @return The property value.
         * @throws JMSException if the operation failed.
         */
        abstract T getPropertyFrom(@NotNull final TextMessage msg) throws JMSException;
    }

    private static <T> T getPropertyFrom(@NotNull final TextMessage jmsMessage,
                                         @NotNull final MessageAccessor<T> accessor) {

        try {
            return (T) accessor.getPropertyFrom(jmsMessage);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not access Message property ["
                    + accessor.getPropertyName() + "]", e);
        }
    }
}
