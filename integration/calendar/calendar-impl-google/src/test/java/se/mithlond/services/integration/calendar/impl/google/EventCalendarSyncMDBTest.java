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

import org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory;
import org.apache.activemq.artemis.junit.EmbeddedJMSResource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import se.mithlond.services.integration.calendar.impl.google.auth.CachingGoogleAuthenticator;
import se.mithlond.services.organisation.api.EventCalendarService;
import se.mithlond.services.shared.spi.algorithms.Deployment;

import javax.jms.CompletionListener;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.util.Hashtable;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EventCalendarSyncMDBTest {

    /**
     * The unittest-unique System property "local.config.dir" must be set within the settings.xml
     * and point to the directory holding the serviceAccountEmail.txt and serviceKey.p12 file
     * structure, that is:
     *
     * [organisationName]/development/google/calendar/serviceAccountEmail.txt    ... and
     * [organisationName]/development/google/calendar/serviceKey.p12
     */
    private static final String UNITTEST_CONFIG_DIR = "local.config.dir";

    /**
     * For this [integration] test, we use the "Development" deployment type.
     */
    private static final String DEPLOYMENT_TYPE = "Development";

    // Shared state
    private File localConfigurationRoot;
    private EventCalendarSyncMDB unitUnderTest;

    private InitialContext initialContext;
    private MessageProducer producer;
    private Session session;
    private Queue eventCalendarRequestQueue;

    @Rule
    public EmbeddedJMSResource resource = new EmbeddedJMSResource();

    @Before
    public void setupSharedState() {

        new TreeMap<>(System.getProperties()).forEach((k, v) -> System.out.println(" [" + k + "]: " + v));

        // Find the local configuration directory.
        final String localConfigDirPath = System.getProperty(UNITTEST_CONFIG_DIR);
        if(localConfigDirPath != null && !localConfigDirPath.isEmpty()) {

            localConfigurationRoot = new File(localConfigDirPath);

            final boolean isDirectory = localConfigurationRoot.exists() && localConfigurationRoot.isDirectory();
            if(!isDirectory) {
                localConfigurationRoot = null;
            } else {

                // Create the system properties overriding default placement of the local configuration root.
                System.setProperty(Deployment.DEPLOYMENT_STORAGE_ROOTDIR_KEY, localConfigurationRoot.getAbsolutePath());
                System.setProperty(Deployment.DEPLOYMENT_TYPE_KEY, DEPLOYMENT_TYPE);

                // Create the test unit and populate its relevant state manually
                unitUnderTest = new EventCalendarSyncMDB();
                unitUnderTest.authenticator = new CachingGoogleAuthenticator();

                // Create the InitialContext for Artemis.
                final Hashtable env = new Hashtable();
                env.put("java.naming.factory.initial","org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
                env.put("connectionFactory.invmConnectionFactory", "vm://0");

                try {

                    resource.getDestinationQueue()

                    initialContext = new InitialContext(env);


                    final ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("ConnectionFactory");
                    eventCalendarRequestQueue = (Queue) initialContext.lookup(
                            EventCalendarService.EVENT_CALENDAR_REQUEST_QUEUE);
                    final Connection connection = cf.createConnection();

                    this.session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                    this.producer = session.createProducer(eventCalendarRequestQueue);
                    final MessageConsumer consumer = session.createConsumer(eventCalendarRequestQueue);
                    consumer.setMessageListener(unitUnderTest);
                    connection.start();

                } catch (Exception e) {
                    throw new IllegalArgumentException("Could not create InitialContext", e);
                }
            }
        }
    }

    @After
    public void teardownSharedState() {

        // Remove any overridden Deployment properties
        System.clearProperty(Deployment.DEPLOYMENT_STORAGE_ROOTDIR_KEY);
        System.clearProperty(Deployment.DEPLOYMENT_TYPE_KEY);
    }

    @Test
    public void validateUnitTestConfigurationDirectoryExists() {

        // Assemble
        final String message = "\n\n#\n# The unittest-unique System property \"local.config.dir\" must be set\n"
                + "# within the settings.xml and point to the directory holding the structure containing the two\n"
                + "# files 'serviceAccountEmail.txt' and 'serviceKey.p12' ...  that is:\n"
                + "# \n"
                + "# [organisationName]/development/google/calendar/serviceAccountEmail.txt    ... and\n"
                + "# [organisationName]/development/google/calendar/serviceKey.p12\n"
                + "#\n";

        // Assert
        Assert.assertNotNull(message, localConfigurationRoot);
    }

    @Test
    public void validateConnectingToGoogleDevelopmentCalendar() throws Exception {

        // Pre-check
        if(localConfigurationRoot == null) {
            return;
        }

        // Assemble
        final TextMessage textMessage = session.createTextMessage("some body");
        producer.send(textMessage);

        // Act

        // Assert
    }
}
