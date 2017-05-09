/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-impl-ejb
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
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
package se.mithlond.services.organisation.impl.ejb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.mithlond.services.organisation.api.EventCalendarService;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.activity.EventCalendar;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.algorithms.Deployment;
import se.mithlond.services.shared.spi.algorithms.Surroundings;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;
import se.mithlond.services.shared.spi.jpa.JpaUtilities;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * EventCalendarService Stateless EJB implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EventCalendarServiceBean extends AbstractJpaService implements EventCalendarService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(EventCalendarServiceBean.class);

    /**
     * A system property key which holds a potential environment name, enabling overriding the default
     * environment used by this EventCalendarServiceBean.
     */
    public static final String ENVIRONMENT_OVERRIDE_PROPERTY = "service_environment";

    /**
     * The JavaEE-injected JMSContext.
     */
    @Inject
    private JMSContext jmsContext;

    /**
     * The JMS Queue used to send EventCalendar requests.
     */
    @Resource(mappedName = EventCalendarService.CALENDAR_REQUEST)
    private Topic queue;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<EventCalendar> getCalendars(final String organisationName, final Membership activeMembership) {

        // Check sanity
        Validate.notEmpty(organisationName, "organisationName");
        Validate.notNull(activeMembership, "activeMembership");

        // Find the environment identifier (i.e. name).
        final String environment = Surroundings.getProperty(ENVIRONMENT_OVERRIDE_PROPERTY) == null
                ? Deployment.getDeploymentType()
                : Surroundings.getProperty(ENVIRONMENT_OVERRIDE_PROPERTY);

        // Find the EventCalendars for the supplied environment.
        final List<EventCalendar> toReturn = new ArrayList<>();
        toReturn.addAll(JpaUtilities.findEntities(
                EventCalendar.class,
                EventCalendar.NAMEDQ_GET_BY_ORGANISATION_AND_RUNTIME,
                true,
                entityManager,
                aQuery -> {
                    aQuery.setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisationName);
                    aQuery.setParameter(OrganisationPatterns.PARAM_ENVIRONMENT_ID, environment);
                }));

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Asynchronous
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
}
