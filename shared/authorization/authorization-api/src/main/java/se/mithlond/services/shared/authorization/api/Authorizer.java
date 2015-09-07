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
 * Specification for how to perform various pattern matching and Authorization-related tasks.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Authorizer {

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
     * @see AuthorizationPattern#parse(String)
     */
    boolean isAuthorized(final String requiredAuthorizationPatterns,
                         final SortedSet<SemanticAuthorizationPath> possessedPrivileges);
}
