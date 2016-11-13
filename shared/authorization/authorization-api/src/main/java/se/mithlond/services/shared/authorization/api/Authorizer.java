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

import java.io.Serializable;
import java.util.SortedSet;

/**
 * Specification for how to perform various pattern matching and Authorization-related tasks.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Authorizer extends Serializable {

    /**
     * Parses the supplied requiredAuthorizationPatterns, and then validates if any of the
     * provided SemanticAuthorizationPaths matches at least one of the generated corresponding
     * AuthorizationPattern instances.
     *
     * @param requiredAuthorizationPatterns A concatenated string of valid AuthorizationPatterns.
     * @param possessedPrivileges           A Set of SemanticAuthorizationPaths to be verified against the parsed
     *                                      AuthorizationPattern instances from the requiredAuthorizationPatterns.
     * @return {@code true} if the possessedPrivileges contained at least one SemanticAuthorizationPath which matched
     * one of the requiredAuthorizationPatterns.
     * @see GlobAuthorizationPattern#parse(String)
     */
    default boolean isAuthorized(final String requiredAuthorizationPatterns,
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
        return isAuthorized(GlobAuthorizationPattern.parse(requiredAuthorizationPatterns), possessedPrivileges);
    }

    /**
     * Checks if any of the provided SemanticAuthorizationPaths matches at least one of the
     * supplied AuthorizationPattern instances.
     *
     * @param requiredAuthorizationPatterns A SortedSet of AuthorizationPatterns.
     * @param possessedPrivileges           A Set of SemanticAuthorizationPaths to be verified against the parsed
     *                                      AuthorizationPattern instances from the requiredAuthorizationPatterns.
     * @return {@code true} if the possessedPrivileges contained at least one SemanticAuthorizationPath which matched
     * one of the requiredAuthorizationPatterns.
     */
    boolean isAuthorized(final SortedSet<GlobAuthorizationPattern> requiredAuthorizationPatterns,
            final SortedSet<SemanticAuthorizationPath> possessedPrivileges);

    /**
     * Checks if any of the provided SemanticAuthorizationPaths matches at least one of the supplied
     * AuthorizationPattern instances. If not, throws an UnauthorizedException. This type of invocation should only
     * be used if the normal caller's execution should be terminated and some other action taken (rather than the
     * result of the invocation simply being ignored) by the caller. In all other cases, use the
     * {@link #isAuthorized(SortedSet, SortedSet)} method instead.
     *
     * @param requiredAuthorizationPatterns A comma-separated string containing required AuthorizationPatterns.
     * @param possessedPrivileges           A Set of SemanticAuthorizationPaths to be verified against the parsed
     *                                      AuthorizationPattern instances from the requiredAuthorizationPatterns.
     * @param operationDescription          A non-empty description of the operation which was potentially unauthorized.
     *                                      This will be delivered into the
     *                                      {@link UnauthorizedException#operationDescription} property in case the
     *                                      underlying operation was unauthorized.
     * @throws UnauthorizedException if the underlying operation was unauthorized given the supplied patterns and
     *                               privileges.
     */
    default void validateAuthorization(final String requiredAuthorizationPatterns,
            final SortedSet<SemanticAuthorizationPath> possessedPrivileges,
            final String operationDescription) throws UnauthorizedException {

        // No requirements == authorized.
        if (requiredAuthorizationPatterns == null || requiredAuthorizationPatterns.isEmpty()) {
            return;
        }

        // From here on, we need a SortedSet of AuthorizationPatterns.
        final SortedSet<GlobAuthorizationPattern> requiredPatterns =
                GlobAuthorizationPattern.parse(requiredAuthorizationPatterns);

        // Requirements, but no privileges == not authorized
        if (possessedPrivileges == null || possessedPrivileges.isEmpty()) {
            throw new UnauthorizedException(
                    operationDescription,
                    possessedPrivileges,
                    requiredPatterns);
        }

        // Delegate
        validateAuthorization(
                GlobAuthorizationPattern.parse(requiredAuthorizationPatterns),
                possessedPrivileges,
                operationDescription);
    }

    /**
     * Checks if any of the provided SemanticAuthorizationPaths matches at least one of the supplied
     * AuthorizationPattern instances. If not, throws an UnauthorizedException. This type of invocation should only
     * be used if the normal caller's execution should be terminated and some other action taken (rather than the
     * result of the invocation simply being ignored) by the caller. In all other cases, use the
     * {@link #isAuthorized(SortedSet, SortedSet)} method instead.
     *
     * @param requiredAuthorizationPatterns A SortedSet of AuthorizationPatterns.
     * @param possessedPrivileges           A Set of SemanticAuthorizationPaths to be verified against the parsed
     *                                      AuthorizationPattern instances from the requiredAuthorizationPatterns.
     * @param operationDescription          A non-empty description of the operation which was potentially unauthorized.
     *                                      This will be delivered into the
     *                                      {@link UnauthorizedException#operationDescription} property in case the
     *                                      underlying operation was unauthorized.
     * @throws UnauthorizedException if the underlying operation was unauthorized given the supplied patterns and
     *                               privileges.
     */
    void validateAuthorization(
            final SortedSet<GlobAuthorizationPattern> requiredAuthorizationPatterns,
            final SortedSet<SemanticAuthorizationPath> possessedPrivileges,
            final String operationDescription) throws UnauthorizedException;
}
