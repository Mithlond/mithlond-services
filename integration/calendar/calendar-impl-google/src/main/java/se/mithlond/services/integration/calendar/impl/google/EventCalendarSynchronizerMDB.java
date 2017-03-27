package se.mithlond.services.integration.calendar.impl.google;

import se.mithlond.services.organisation.api.EventCalendarService;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
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
public class EventCalendarSynchronizerMDB implements MessageListener {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onMessage(final Message message) {

        if (message instanceof TextMessage) {

        }
    }
}
