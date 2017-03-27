package se.mithlond.services.organisation.impl.google;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.organisation.api.EventCalendarService;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Message-Driven EJB which receives events pushing events to the Google Calendar service.
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
public class GoogleEventCalendarMDB implements MessageListener {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(GoogleEventCalendarMDB.class);

    @PersistenceContext(unitName = "DataPumpPersistenceUnit")
    private EntityManager dataSourceEntityManager;

    /**
     * Message listener callback method.
     *
     * @param message A non-null TextMessage, which must contain all required properties.
     */
    @Override
    public void onMessage(final Message message) {

        // We should only respond to TextMessages.
        if (message instanceof TextMessage) {
            handle((TextMessage) message);
        }

        Validate.notEmpty(calendarIdentifier, "calendarIdentifier");
        Validate.notEmpty(owningOrganisationName, "owningOrganisationName");
        Validate.notNull(startTime, "startTime");
        Validate.notNull(endTime, "endTime");
        Validate.notNull(activeMembership, "activeMembership");
        CommonPersistenceTasks.validateInterval(startTime,
                "startTime",
                endTime,
                "endTime");

        try {

            // Create a JMS message to send to the queue
            final TextMessage msg = jmsContext.createTextMessage();
            msg.setStringProperty("organisation_name", owningOrganisationName);
            msg.setStringProperty("start_time", TimeFormat.YEAR_MONTH_DATE.print(startTime));
            msg.setStringProperty("end_time", TimeFormat.YEAR_MONTH_DATE.print(endTime));
            msg.setLongProperty("active_membership_id", activeMembership.getId());
            msg.setStringProperty("active_membership_alias", activeMembership.getAlias());
            msg.setStringProperty("calendar_identifier", calendarIdentifier);

            // Send the message.
            jmsContext.createProducer().send(queue, msg);

        } catch (JMSException e) {
            log.error("Could not send JMS TextMessage", e);
            throw new IllegalArgumentException("Could not send JMS TextMessage", e);
        }
    }

    public void handle(final TextMessage msg) {

        // Check sanity
        Validate.notNull(msg, "msg");
        msg.setStringProperty("organisation_name", owningOrganisationName);
        msg.setStringProperty("start_time", TimeFormat.YEAR_MONTH_DATE.print(startTime));
        msg.setStringProperty("end_time", TimeFormat.YEAR_MONTH_DATE.print(endTime));
        msg.setLongProperty("active_membership_id", activeMembership.getId());
        msg.setStringProperty("active_membership_alias", activeMembership.getAlias());
        msg.setStringProperty("calendar_identifier", calendarIdentifier);
    }
}
