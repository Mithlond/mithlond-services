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
package se.mithlond.services.integration.calendar.impl.google.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.calendar.Calendar;

import javax.ejb.Local;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Authenticator specification for Google integration, which can be implemented in several ways
 * (OAuth2, ServiceAccountKey, etc.) depending on the authentication method used.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Local
public interface GoogleAuthenticator {

    /**
     * The unique identifier of the Google calendar service.
     */
    String GOOGLE_CALENDAR_SERVICE = "google/calendar";

    /**
     * The name of the file containing the email for a Google calendar ServiceAccount.
     */
    String GOOGLE_SERVICEACCOUNT_EMAIL_FILE = "serviceAccountEmail.txt";

    /**
     * The name of the ServiceAccount p12 file.
     */
    String GOOGLE_SERVICE_ACCOUNT_P12_FILE = "serviceKey.p12";

    /**
     * Property for the serviceAccount user (email).
     */
    String PROPERTY_SERVICE_ACCOUNT_USER = "serviceAccountUser";

    /**
     * The write access definition ("access scope" in google lingo) required by
     * this GoogleCalendarService for updating the remote Google Calendar using the API.
     */
    List<String> WRITE_PRIVILEGES = Collections.singletonList("https://www.googleapis.com/auth/calendar");

    /**
     * Retrieves the preferred HttpTransport used by this GoogleAuthenticator.
     *
     * @return the preferred HttpTransport used by this GoogleAuthenticator.
     */
    @NotNull
    HttpTransport getTransport();

    /**
     * Retrieves the service account email for the supplied organisation name.
     *
     * @param organisationName The non-empty organisation name of the organisation whose
     *                         service account email should be retrieved.
     * @return The email of the serviceAccount for the supplied Organisation (name).
     */
    String getServiceAccountEmail(@NotNull final String organisationName);

    /**
     * Retrieves the Service account P12 file for the supplied organisation name.
     *
     * @param organisationName The non-empty organisation name of the organisation whose
     *                         service account P12 private key should be retrieved.
     * @return The File containing the P12 key of the serviceAccount for the supplied Organisation (name).
     */
    File getServiceP12File(@NotNull final String organisationName);

    /**
     * Authorizes against the Google Calendar Service.
     *
     * @param organisationName The name of the organisation for which the Credential should be acquired.
     * @return A Google Credential for the authenticated connection.
     */
    Credential authorize(@NotNull final String organisationName);

    /**
     * Retrieves an authorized Google Calendar client, fully ready for use.
     *
     * @param organisationName The name of the organisation for which the Calendar client should be acquired.
     * @return An authorized Google Calendar client object, ready for use.
     */
    Calendar getCalendarClient(@NotNull final String organisationName);
}
