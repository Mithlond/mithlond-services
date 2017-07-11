/*-
 * #%L
 * Nazgul Project: mithlond-services-organisation-api
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
package se.mithlond.services.organisation.api;

import javax.ejb.Asynchronous;
import javax.ejb.Local;
import javax.validation.constraints.NotNull;

/**
 * Specification for how to integrate an organisation with a mailing list service provider.
 * The Mailing list metadata structure represent a mailing list within a [remote] service provider.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Local
public interface MailListService {

    /**
     * The JMS message parameter for the Organisation (name).
     */
    String ORGANISATION_NAME_PARAM = "organisation";

    /**
     * The JMS message parameter for the command to issue.
     */
    String COMMAND_PARAM = "command";

    /**
     * The JNDI name of the MailingList request Destination, to which push commands are sent for further processing
     * within another thread/transaction.
     */
    String MAILING_LIST_REQUEST = "java:global/jms/nazgul/services/mailinglist/request";

    /**
     * The JNDI name of the EventCalendar error Destination, to which error JMS Messages are sent for
     * further processing within another thread/transaction.
     */
    String MAILING_LIST_RESPONSE = "java:global/jms/nazgul/services/mailinglist/response";

    /**
     * <p>Pushes all [locally known] email addresses to a remote MailingList service, to serve as a whitelist of
     * originating email addresses.</p>
     *
     * @param owningOrganisationName The name of the Organisation owning the EventCalendar to which activity
     *                               information should be pushed. Cannot be {@code null} or empty.
     */
    @Asynchronous
    void pushEmailsOfMembers(@NotNull String owningOrganisationName);

    /**
     * <p>Pushes all mailing lists (including their metadata, such as descriptions) to a remote MailingList service, to
     * create or update existing mailing lists.</p>
     *
     * @param owningOrganisationName The name of the Organisation owning the EventCalendar to which activity
     *                               information should be pushed. Cannot be {@code null} or empty.
     */
    @Asynchronous
    void pushMailingLists(@NotNull String owningOrganisationName);
}
