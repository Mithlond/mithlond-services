/*
 * #%L
 * Nazgul Project: mithlond-services-shared-authorization-api
 * %%
 * Copyright (C) 2015 Mithlond
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
package se.mithlond.services.shared.authorization.api;

import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;

import java.util.SortedSet;

/**
 * Trivial Authorizer implementation which performs regular expression pattern matching of
 * AuthorizationPatterns and a set of possessed (by the active user) privileges in the form of
 * SemanticAuthorizationPaths.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class SimpleAuthorizer implements Authorizer {

    // Internal state
    private static final SimpleAuthorizer INSTANCE = new SimpleAuthorizer();

    /**
     * Hide the default constructor in utility classes.
     */
    private SimpleAuthorizer() {
    }

    /**
     * @return The singleton SimpleAuthorizer instance.
     */
    public static SimpleAuthorizer getInstance() {
        return INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthorized(final String requiredAuthorizationPatterns,
                                final SortedSet<SemanticAuthorizationPath> possessedPrivileges) {

        // No requirements == authorized.
        if (requiredAuthorizationPatterns == null || requiredAuthorizationPatterns.isEmpty()) {
            return true;
        }

        // Requirements, but no privileges == not authorized
        if (possessedPrivileges == null || possessedPrivileges.isEmpty()) {
            return false;
        }

        // Match each possessedPrivilege against all of the supplied privileges.
        return isAuthorized(AuthorizationPattern.parse(requiredAuthorizationPatterns), possessedPrivileges);
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAuthorized(final SortedSet<AuthorizationPattern> requiredAuthorizationPatterns,
                                final SortedSet<SemanticAuthorizationPath> possessedPrivileges) {

        // No requirements == authorized.
        if (requiredAuthorizationPatterns == null || requiredAuthorizationPatterns.isEmpty()) {
            return true;
        }

        // Requirements, but no privileges == not authorized
        if (possessedPrivileges == null || possessedPrivileges.isEmpty()) {
            return false;
        }

        for (AuthorizationPattern current : requiredAuthorizationPatterns) {
            for (SemanticAuthorizationPath currentPrivilege : possessedPrivileges) {
                if (current.matches(currentPrivilege.toString())) {
                    return true;
                }
            }
        }

        // Nopes.
        return false;
    }
}
