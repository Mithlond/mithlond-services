/*
 * #%L
 * Nazgul Project: backend-jaxrs-web
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
/*
 * Copyright (c) jGuru Europe AB.
 * All rights reserved.
 */

package se.mithlond.services.backend.war.producers.security;

import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.security.Principal;

/**
 * Identity data wrapper which doubles as a Principal.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class IdentityWrapper implements Serializable, Principal {

    // Internal state
    private String userId;
    private String credential;
    private String organisation;

    /**
     * Compound constructor creating an IdentityWrapper, wrapping the supplied data.
     *
     * @param userId       The non-empty userId/login of an Identity.
     * @param credential   The non-null credential of an Identity.
     * @param organisation The non-empty organisation of an Identity.
     */
    public IdentityWrapper(final String userId,
                           final String credential,
                           final String organisation) {

        // Check sanity
        Validate.notEmpty(userId, "Cannot handle null or empty 'userId' argument.");
        Validate.notEmpty(organisation, "Cannot handle null or empty 'organisation' argument.");
        Validate.notNull(credential, "Cannot handle null 'credential' argument.");

        // Assign internal state
        this.userId = userId;
        this.credential = credential;
        this.organisation = organisation;
    }

    /**
     * @return The non-empty userId/login of an Identity.
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @return The non-empty userId/login of an Identity.
     */
    @Override
    public String getName() {
        return getUserId();
    }

    /**
     * @return The non-null credential of an Identity.
     */
    public String getCredential() {
        return credential;
    }

    /**
     * @return The non-empty organisation of an Identity.
     */
    public String getOrganisation() {
        return organisation;
    }
}
