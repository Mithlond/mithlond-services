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
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.services.calendar.Calendar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.shared.spi.algorithms.Deployment;
import se.mithlond.services.shared.spi.algorithms.Surroundings;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.ejb.Singleton;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static se.mithlond.services.integration.calendar.impl.google.algorithms.GoogleCalendarConverters.JSON_FACTORY;

/**
 * Abstract GoogleAuthenticator implementation which provides in-memory caching of credential data.
 * This implementation assumes that the number of active authentication credentials used is small
 * enough to properly use in-memory caching.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Singleton
public class CachingGoogleAuthenticator implements Serializable, GoogleAuthenticator {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(CachingGoogleAuthenticator.class);

    // Internal state
    private Map<String, Credential> cachedAuthData = new TreeMap<>();
    private Map<String, String> serviceAccountEmailData = new TreeMap<>();
    private Map<String, File> serviceAccountP12Data = new TreeMap<>();
    private HttpTransport preferredTransport;

    /**
     * {@inheritDoc}
     */
    @Override
    public HttpTransport getTransport() {

        // Cached already?
        if (this.preferredTransport != null) {
            return this.preferredTransport;
        }

        try {
            // Re-create and cache the preferred transport.
            this.preferredTransport = GoogleNetHttpTransport.newTrustedTransport();

            // Log somewhat
            if (log.isDebugEnabled()) {
                log.debug("Created and cached HttpTransport ["
                        + preferredTransport.getClass().getCanonicalName() + "]");
            }

        } catch (Exception e) {
            throw new IllegalStateException("Could not create a NetHttpTransport", e);
        }

        // All Done.
        return this.preferredTransport;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServiceAccountEmail(@NotNull final String organisationName) {

        // Check sanity
        Validate.notEmpty(organisationName, "organisationName");

        // #1) Is the email data already cached?
        final String cacheKey = getCacheKey(organisationName, null);
        if (serviceAccountEmailData.get(cacheKey) != null) {
            return serviceAccountEmailData.get(cacheKey);
        }

        // #2) Read the email data from local configuration file, and cache it.
        final String toReturn = Surroundings.getLocalConfigurationTextFileAsString(
                organisationName,
                GOOGLE_CALENDAR_SERVICE,
                GOOGLE_SERVICEACCOUNT_EMAIL_FILE).trim();
        this.serviceAccountEmailData.put(cacheKey, toReturn);

        // #3) Log somewhat
        if (log.isDebugEnabled()) {
            log.debug("Created and cached serviceAccountEmail for key [" + cacheKey + "]");
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File getServiceP12File(@NotNull final String organisationName) {

        // Check sanity
        Validate.notEmpty(organisationName, "organisationName");

        // #1) Is the File already cached?
        final String cacheKey = getCacheKey(organisationName, null);
        if (serviceAccountP12Data.get(cacheKey) != null) {
            return serviceAccountP12Data.get(cacheKey);
        }

        // #2) Read the email data from local configuration file, and cache it.
        final File toReturn = Surroundings.getLocalConfigFile(organisationName,
                GOOGLE_CALENDAR_SERVICE,
                GOOGLE_SERVICE_ACCOUNT_P12_FILE);
        this.serviceAccountP12Data.put(cacheKey, toReturn);

        // #3) Log somewhat
        if (log.isDebugEnabled()) {
            log.debug("Created and cached serviceAccountP12File for key [" + cacheKey + "]");
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Credential authorize(@NotNull final String organisationName) {

        // Check sanity
        Validate.notEmpty(organisationName, "organisationName");

        // #1) Is the Credential already cached?
        final List<String> requestedGooglePrivileges = GoogleAuthenticator.WRITE_PRIVILEGES;
        final String cacheKey = getCacheKey(organisationName, requestedGooglePrivileges);
        if (cachedAuthData.get(cacheKey) != null) {
            return cachedAuthData.get(cacheKey);
        }

        // #2 Define the Google credential arguments.
        final String serviceAccountEmail = getServiceAccountEmail(organisationName);
        final File serviceAccountP12File = getServiceP12File(organisationName);
        final HttpTransport transport = getTransport();

        // #2) We will not use OAuth2 with redirection to the local server for
        //     callback from Google OAuth2 servers. Instead, we use a service account
        //     P12 key for a serviceAccount access.
        //
        try {

            // It seems that Google's OAuth servers do not like the serviceAccountUser property to be set.
            final GoogleCredential toReturn = new GoogleCredential.Builder()
                    .setTransport(transport)
                    .setJsonFactory(JSON_FACTORY)
                    .setServiceAccountId(serviceAccountEmail)
                    .setServiceAccountScopes(requestedGooglePrivileges)
                    .setServiceAccountPrivateKeyFromP12File(serviceAccountP12File)
                    // .setServiceAccountUser(serviceAccountUser)
                    .build();

            cachedAuthData.put(cacheKey, toReturn);
            return toReturn;

        } catch (Exception e) {
            String msg = "\n\nCould not build GoogleCredential from:"
                    + "\n serviceAccountEmail: " + serviceAccountEmail
                    + "\n serviceAccountScopes: " + requestedGooglePrivileges
                    + "\n serviceAccountP12File: " + serviceAccountP12File.getAbsolutePath()
                    + "\n transport: " + transport;
            throw new IllegalArgumentException(msg, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Calendar getCalendarClient(final String organisationName) {

        // First, retrieve the authorized Credentials.
        final Credential credentials = authorize(organisationName);

        // Build the google application name.
        final String appName = organisationName + "CalendarApp" + Deployment.getDeploymentType();

        try {

            // All Done.
            return new Calendar.Builder(getTransport(), JSON_FACTORY, credentials)
                    .setApplicationName(appName)
                    .build();
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not acquire the Google Calendar client.", e);
        }
    }

    //
    // Private helpers
    //

    private static String getCacheKey(@NotNull final String organisationName,
                                      final List<String> privilegesRequested) {

        final String deploymentType = Deployment.getDeploymentType();

        StringBuilder builder = new StringBuilder();
        builder.append(organisationName).append("_");
        builder.append(deploymentType).append("_");
        builder.append(GOOGLE_CALENDAR_SERVICE);

        if (privilegesRequested != null) {

            builder.append("_");
            for (String current : privilegesRequested) {
                builder.append(current).append("_");
            }
        }

        // All done.
        return builder.toString();
    }
}
