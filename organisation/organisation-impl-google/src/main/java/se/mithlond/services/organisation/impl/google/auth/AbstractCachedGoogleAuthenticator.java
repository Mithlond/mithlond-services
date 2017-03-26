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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.shared.spi.algorithms.Deployment;
import se.mithlond.services.shared.spi.algorithms.Validate;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Abstract GoogleAuthenticator implementation which provides in-memory
 * caching of credential data. This implementation assumes that the number
 * of active authentication credentials used is small enough to properly
 * use in-memory caching.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractCachedGoogleAuthenticator implements Serializable, GoogleAuthenticator {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(AbstractCachedGoogleAuthenticator.class);

    /**
     * The cached authentication credentials.
     */
    protected Map<String, Credential> cachedAuthData = new TreeMap<>();

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("all")
    @Override
    public Credential authorize(final Deployment deployment,
                                final String organisationName,
                                final String serviceName,
                                final List<String> requestedGooglePrivileges,
                                final HttpTransport httpTransport,
                                final SortedMap<String, String> additionalParameters) {

        // Check sanity
        Validate.notNull(httpTransport, "httpTransport");
        Validate.notNull(deployment, "deployment");
        Validate.notEmpty(organisationName, "organisationName");
        Validate.notEmpty(serviceName, "serviceName");
        Validate.notNull(requestedGooglePrivileges, "requestedGooglePrivileges");

        final SortedMap<String, String> optionalParams = additionalParameters == null
                ? new TreeMap<String, String>()
                : additionalParameters;
        Collections.sort(requestedGooglePrivileges);

        // Cached?
        final String cacheKey = getCacheKey(organisationName, deployment,
                serviceName, requestedGooglePrivileges, optionalParams);
        Credential toReturn = cachedAuthData.get(cacheKey);
        if (toReturn == null) {

            // Not cached. (Re-)generate the credentials.
            toReturn = generateCredentials(organisationName,
                    deployment,
                    serviceName,
                    requestedGooglePrivileges,
                    httpTransport,
                    optionalParams);

            // Cache the result
            cachedAuthData.put(cacheKey, toReturn);

            if (log.isDebugEnabled()) {
                log.debug("Created new authentication data for key [" + cacheKey + "]: " + toReturn);
            }

        } else {

            if (log.isDebugEnabled()) {
                log.debug("Retrieved cached authentication data for key [" + cacheKey + "]: " + toReturn);
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * Concrete generation method, where Credentials are made from the supplied data.
     *
     * @param organisationName          The name of the organisation for which the Credential should be acquired.
     * @param deployment                The active Deploiyment
     * @param serviceName               The name of the service we are accessing.
     * @param requestedGooglePrivileges The access definitions ("access scope" in google lingo) required for the
     *                                  retrieved Credential.
     * @param httpTransport             The active httpTransport; never null.
     * @param additionalParameters      Any extra parameters required for the authentication.
     *                                  May be empty, but never {@code null}.
     * @return A Google Credential for the authenticated connection.
     */
    @SuppressWarnings("all")
    protected abstract Credential generateCredentials(final String organisationName,
                                                      final Deployment deployment,
                                                      final String serviceName,
                                                      final List<String> requestedGooglePrivileges,
                                                      final HttpTransport httpTransport,
                                                      final SortedMap<String, String> additionalParameters);

    /**
     * Retrieves the directory where credentials for a specific service are found.
     *
     * @param organisationName The name of the organisation for which we should connect/authenticate.
     * @param deployment       The runtime environment of the active application server.
     * @param serviceName      The name of the service we are accessing.
     * @return A File wrapping the directory where credentials for a specific service are found.
     */
    protected final File getCredentialStoreDirectory(final String organisationName,
                                                     final Deployment deployment,
                                                     final String serviceName) {

        // Check sanity
        Validate.notNull(deployment, "Cannot handle null runtimeEnvironment argument.");
        Validate.notEmpty(organisationName, "Cannot handle null or empty organisationName argument.");
        Validate.notEmpty(serviceName, "Cannot handle null or empty serviceName argument.");

        // Validate that the directory actually exists.
        final File toReturn = new File(
                getNazgulCredentialStoreRootDir(),
                organisationName + "/" + deployment.getCamelHumpName() + "/" + serviceName);

        if (!(toReturn.exists() && toReturn.isDirectory())) {
            throw new IllegalArgumentException("CredentialStoreDirectory [" + toReturn.getAbsolutePath()
                    + "] for organisation [" + organisationName + "], runtimeEnvironment ["
                    + deployment.getCamelHumpName() + "] and serviceName [" + serviceName
                    + "] is not a directory.");
        }

        // All done.
        return toReturn;
    }

    //
    // Private helpers
    //

    private String getCacheKey(final String organisationName,
                               final Deployment deployment,
                               final String serviceName,
                               final List<String> privilegesRequested,
                               final SortedMap<String, String> additionalParams) {

        StringBuilder builder = new StringBuilder();
        builder.append(organisationName).append("_");
        builder.append(deployment).append("_");
        builder.append(serviceName).append("_");

        for (String current : privilegesRequested) {
            builder.append(current).append("_");
        }

        for (Map.Entry<String, String> current : additionalParams.entrySet()) {
            builder.append("_").append(current.getKey()).append("_").append(current.getValue());
        }

        // All done.
        return builder.toString();
    }
}
