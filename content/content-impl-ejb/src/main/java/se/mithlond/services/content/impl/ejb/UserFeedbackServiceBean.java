/*-
 * #%L
 * Nazgul Project: mithlond-services-content-impl-ejb
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
package se.mithlond.services.content.impl.ejb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.content.api.UserFeedbackService;
import se.mithlond.services.content.model.transport.feedback.CharacterizedDescription;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.Surroundings;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.ejb.Stateless;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * UserFeedbackService Stateless EJB implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Stateless
public class UserFeedbackServiceBean extends AbstractJpaService implements UserFeedbackService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(UserFeedbackServiceBean.class);

    private static final String NEWLINE = System.getProperty("line.separator");

    /**
     * {@inheritDoc}
     */
    @Override
    public CharacterizedDescription submitUserFeedback(final Membership submitter,
                                                       final CharacterizedDescription userFeedback) {

        // Check sanity
        final boolean okWrapper = userFeedback != null;
        final boolean okCategory = okWrapper
                && userFeedback.getCategory() != null
                && !userFeedback.getCategory().isEmpty();
        final boolean okDescription = okWrapper
                && userFeedback.getDescription() != null
                && !userFeedback.getDescription().isEmpty();
        final boolean okSubmitter = submitter != null;
        final boolean okDataReceived = okSubmitter && okWrapper && okCategory && okDescription;

        // Fail fast
        if (!okDataReceived) {

            final String prefix = "Null or empty ";
            final StringBuilder stateDesc = new StringBuilder("Insufficient data received: ");

            if (!okSubmitter) {
                stateDesc.append(prefix + "submitter.");
            }

            if (!okWrapper) {
                stateDesc.append(prefix + "userFeedback wrapper.");
            } else {

                if (!okCategory) {
                    stateDesc.append(prefix + "category.");
                }

                if (!okDescription) {
                    stateDesc.append(prefix + "description.");
                }
            }

            // All Done.
            return new CharacterizedDescription("Insufficient", "Insufficient data received: "
                    + stateDesc.toString());
        }

        // Compile the inbound data as a (plaintext) String.
        final String body = compileFeedbackMessage(submitter, userFeedback);

        // Compile the email recipients for the feedback mail message.
        final String organisationName = submitter.getOrganisation().getOrganisationName();
        final String emailSuffix = "@" + submitter.getOrganisation().getEmailSuffix();
        final List<Membership> theDevelopers = entityManager.createNamedQuery(Membership
                .NAMEDQ_GET_BY_GROUP_ORGANISATION_LOGINPERMITTED, Membership.class)
                .setParameter(OrganisationPatterns.PARAM_GROUP_NAME, "Utvecklare")
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisationName)
                .setParameter(OrganisationPatterns.PARAM_LOGIN_PERMITTED, true)
                .getResultList();

        final List<String> recipients = new ArrayList<>();
        recipients.add(submitter.getEmailAlias() + emailSuffix);
        theDevelopers.forEach(m -> recipients.add(m.getEmailAlias() + emailSuffix));

        // Send the email message.
        sendPlaintextEmail(
                organisationName,
                submitter.getOrganisation().getEmailSuffix(),
                recipients,
                "User Feedback",
                body);

        // All Done.
        return new CharacterizedDescription("Success",
                "UserFeedback message successfully sent to [" + recipients + "] recipients.");
    }

    public void sendPlaintextEmail(final String orgName,
                                   final String orgEmailSuffix,
                                   final List<String> recipients,
                                   final String subject,
                                   final String body) {

        // Get the session
        final Session mailSession = getMailSessionForOrganisation(orgName);

        if (mailSession != null) {

            // Create the message and the status tracker
            final MimeMessage message = new MimeMessage(mailSession);
            final boolean[] foundProblems = new boolean[]{false};

            try {

                // #1) Add static properties to the message.
                message.setSender(new InternetAddress("intranet@" + orgEmailSuffix));
                message.setReplyTo(new InternetAddress[]{new InternetAddress("do_not_reply@" + orgEmailSuffix)});
                message.setSubject(subject, "UTF-8");
                message.setSentDate(new Date());
                message.setHeader("Content-Transfer-Encoding", "8bit");

                // #2) Add recipients
                recipients.forEach(rec -> {
                    try {
                        message.addRecipients(Message.RecipientType.TO, rec);
                    } catch (MessagingException e) {
                        log.error("Could not add Mail recipient [" + rec + "]", e);
                    }
                    foundProblems[0] = true;
                });


                // #3) Add the payload / message body
                final MimeBodyPart bodyPart = new MimeBodyPart();
                bodyPart.setDescription("Nazgûl Mail");
                bodyPart.setContent(body, "UTF-8");

                final Multipart multipart = new MimeMultipart();
                multipart.addBodyPart(bodyPart);
                message.setContent(multipart);

                // Send the message
                sendMessage(mailSession, message);
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not send email message", e);
            }
        }
    }

    //
    // Private helpers
    //

    private void sendMessage(final Session mailSession,
                             final MimeMessage msg) throws MessagingException {

        // Save any changes to the message.
        // Then connect via SMTP and send the Message.
        msg.saveChanges();
        final Transport trans = mailSession.getTransport("smtp");

        // Add a transport listener to acquire state change log messages.
        trans.addTransportListener(getTransportListener());

        // Connect and send the message.
        trans.connect();
        trans.sendMessage(msg, msg.getAllRecipients());
        trans.close();
    }

    private synchronized Session getMailSessionForOrganisation(final String organisationName) {

        final Context ctx;
        try {

            ctx = new InitialContext();
            return (Session) Surroundings.lookupInJndi(ctx, "java:global/mail/" + organisationName);

        } catch (NamingException e) {
            log.error("Could not acquire Mail Session for organisation [" + organisationName + "]", e);
        }

        // All Done.
        return null;
    }

    private static TransportListener getTransportListener() {

        return new TransportListener() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void messageDelivered(final TransportEvent transportEvent) {
                if (log.isDebugEnabled()) {
                    log.debug("Mail delivered fully.");
                }
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void messageNotDelivered(final TransportEvent transportEvent) {

                String mailSubject;
                try {
                    mailSubject = transportEvent.getMessage().getSubject();
                } catch (Exception ex) {
                    mailSubject = "<unknown>";
                    log.error("Could not acquire Message Subject.", ex);
                }

                // Complain.
                log.warn("Mail not delivered. Mail subject: " + mailSubject);
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void messagePartiallyDelivered(final TransportEvent transportEvent) {

                if (log.isWarnEnabled()) {
                    log.warn("Mail partly delivered. (Success: "
                            + Arrays.stream(transportEvent.getValidSentAddresses())
                            .map(Address::toString)
                            .reduce((l, r) -> l + ", " + r).orElse("<none>")
                            + "), Failure: "
                            + Arrays.stream(transportEvent.getValidUnsentAddresses())
                                    .map(Address::toString)
                                    .reduce((l, r) -> l + ", " + r).orElse("<none>")
                            + ")");
                }
            }
        };
    }

    private static String compileFeedbackMessage(@NotNull final Membership submitter,
                                                 @NotNull final CharacterizedDescription userFeedback) {

        // Extract relevant data
        final User submittingUser = submitter.getUser();

        return "User Feedback Message" + NEWLINE
                + "From: " + submitter.getAlias() + " (" + submittingUser.getFirstName()
                + " " + submittingUser.getLastName() + ")" + NEWLINE
                + "Organisation: " + submitter.getOrganisation().getOrganisationName() + NEWLINE
                + "Received at: " + TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(LocalDateTime.now()) + NEWLINE
                + "Feedback Type: " + userFeedback.getCategory() + NEWLINE
                + "Feedback Message: " + userFeedback.getDescription() + NEWLINE;
    }
}
