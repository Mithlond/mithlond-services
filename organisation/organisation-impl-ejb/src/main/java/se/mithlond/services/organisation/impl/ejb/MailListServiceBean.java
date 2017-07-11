/*-
 * #%L
 * Nazgul Project: mithlond-services-organisation-impl-ejb
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
package se.mithlond.services.organisation.impl.ejb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.organisation.api.MailListService;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.spi.algorithms.Deployment;
import se.mithlond.services.shared.spi.algorithms.messages.JmsCompliantMap;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.Queue;
import javax.jms.TextMessage;

/**
 * MailListService POJO implementation adapting all calls to outbound JMS messages, sent onto the
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MailListServiceBean extends AbstractJpaService implements MailListService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(EventCalendarServiceBean.class);

    /**
     * The JavaEE-injected JMSContext.
     */
    @Inject
    private JMSContext jmsContext;

    /**
     * The JMS Queue used to send MailList service requests.
     */
    @Resource(mappedName = MailListService.MAILING_LIST_REQUEST)
    private Queue queue;

    /**
     * {@inheritDoc}
     */
    @Override
    public void pushEmailsOfMembers(final String owningOrganisationName) {

        // First, find the Organisation
        final Organisation org = entityManager.createNamedQuery(Organisation.NAMEDQ_GET_BY_NAME, Organisation.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, owningOrganisationName)
                .getSingleResult();

        // Compile the properties for the outbound JMS Message
        final JmsCompliantMap propertyMap = new JmsCompliantMap();
        propertyMap.put(ORGANISATION_NAME_PARAM, org.getOrganisationName());
        propertyMap.put(COMMAND_PARAM, "pushEmailAddresses");
        propertyMap.put(Deployment.DEPLOYMENT_TYPE_KEY, Deployment.getDeploymentType());

        // Send the message
        sendJmsMessage(propertyMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pushMailingLists(final String owningOrganisationName) {

        // First, find the Organisation
        final Organisation org = entityManager.createNamedQuery(Organisation.NAMEDQ_GET_BY_NAME, Organisation.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, owningOrganisationName)
                .getSingleResult();

        // Compile the properties for the outbound JMS Message
        final JmsCompliantMap propertyMap = new JmsCompliantMap();
        propertyMap.put(ORGANISATION_NAME_PARAM, org.getOrganisationName());
        propertyMap.put(COMMAND_PARAM, "pushMailingLists");
        propertyMap.put(Deployment.DEPLOYMENT_TYPE_KEY, Deployment.getDeploymentType());

        // Send the message
        sendJmsMessage(propertyMap);
    }

    //
    // Private helpers
    //

    private void sendJmsMessage(final JmsCompliantMap propertyMap) {

        try {
            final TextMessage textMessage = jmsContext.createTextMessage();
            JmsCompliantMap.copyProperties(textMessage, propertyMap);

            jmsContext.createProducer().send(queue, textMessage);
        } catch (Exception e) {
            log.error("Could not send MailList JMS message", e);
        }
    }
}
