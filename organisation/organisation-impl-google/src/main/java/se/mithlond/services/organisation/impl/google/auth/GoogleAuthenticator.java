/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-impl-google
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
package se.mithlond.services.organisation.impl.google.auth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.HttpTransport;
import se.mithlond.services.shared.spi.algorithms.Deployment;
import se.mithlond.services.shared.spi.algorithms.Surroundings;

import java.util.List;
import java.util.SortedMap;

/**
 * Authenticator specification for Google integration, which can be implemented in several ways
 * (OAuth2, ServiceAccountKey, etc.) depending on the authentication method used.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@FunctionalInterface
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
     * Authorizes against Google using the values supplied within the authProperties Map.
     *
     * @param environment               The Deployment of the running application server.
     * @param organisationName          The name of the organisation for which the Credential should be acquired.
     * @param serviceName               The name of the service for which we should authorize.
     * @param requestedGooglePrivileges The access definitions ("access scope" in google lingo) required for the
     *                                  retrieved Credential.
     * @param httpTransport             The active httpTransport. May not be null.
     * @param additionalParameters      Any optional/additional parameters required to generate the Credential.
     * @return A Google Credential for the authenticated connection.
     */
    @SuppressWarnings("all")
    Credential authorize(final Deployment environment,
                         final String organisationName,
                         final String serviceName,
                         final List<String> requestedGooglePrivileges,
                         final HttpTransport httpTransport,
                         final SortedMap<String, String> additionalParameters);

    /**
     * Retrieves the service account email for the supplied organisation name.
     *
     * @param organisationName The name of the organisation whose google Calendar ServiceAccount should be retrieved.
     * @return
     */
    default String getServiceAccountEmail(final String organisationName) {

        return Surroundings.getLocalConfigurationTextFileAsString(
                organisationName,
                GOOGLE_CALENDAR_SERVICE,
                GOOGLE_SERVICEACCOUNT_EMAIL_FILE);
    }
}
