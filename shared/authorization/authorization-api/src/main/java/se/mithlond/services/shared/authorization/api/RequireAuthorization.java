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

/**
 * Specification for how to indicate that content may be protected, and hence require
 * authorization to permit access. This really simplistic authorization model implies
 * that callers must possess at least one of a set of required GroupMemberships to be
 * granted access to a protected resource.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public @interface RequireAuthorization {

    /**
     * @return The AuthorizationPath patterns required to have the required authorization.
     * {@code null} values indicate that no particular AuthorizationPath is required.
     */
    String authorizationPatterns();
}
