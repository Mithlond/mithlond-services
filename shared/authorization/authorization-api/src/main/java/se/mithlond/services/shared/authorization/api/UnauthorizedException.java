/*
 * #%L
 * Nazgul Project: mithlond-services-shared-authorization-api
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
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
import se.mithlond.services.shared.spi.algorithms.Validate;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Exception indicating that a certain operation was not performed due to the
 * caller lacking required privileges and hence being Unauthorized for performing
 * the operation. This UnauthorizedException type should only be used on methods
 * where failed authorization must be thoroughly communicated to the calling client,
 * typically by aborting the normal execution path and reverting to some other action -
 * typically when aborting a Login or an update-some-data-in-the-database operation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class UnauthorizedException extends RuntimeException implements Serializable {

    static final long serialVersionUID = -882999901L;

    // Internal state
    private String operationDescription;
    private SortedSet<SemanticAuthorizationPath> possessedPaths = new TreeSet<>();
    private SortedSet<GlobAuthorizationPattern> requiredAuthorizationPatterns = new TreeSet<>();

    /**
     * Compound constructor creating an UnauthorizedException indicating that a
     * particular operation could not be performed due to lack of proper authorization.
     *
     * @param operationDescription A non-empty and free-form description of the operation to be performed.
     * @param possessedPaths The SemanticAuthorizationPaths possessed by the caller.
     * @param requiredAuthorizationPatterns The required AuthorizationPattern
     */
    public UnauthorizedException(final String operationDescription,
            final SortedSet<SemanticAuthorizationPath> possessedPaths,
            final SortedSet<GlobAuthorizationPattern> requiredAuthorizationPatterns) {

        super();

        // Check sanity
        Validate.notEmpty(operationDescription, "Cannot handle null or empty 'operationDescription' argument.");

        // Assign internal state
        this.operationDescription = operationDescription;
        if(possessedPaths != null) {
            this.possessedPaths.addAll(possessedPaths);
        }
        if(requiredAuthorizationPatterns != null) {
            this.requiredAuthorizationPatterns.addAll(requiredAuthorizationPatterns);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getMessage() {
        return "Unauthorized: " + operationDescription
                + "\n  Required AuthorizationPatterns      : " + requiredAuthorizationPatterns
                + "\n  Possessed SemanticAuthorizationPaths: " + possessedPaths;
    }
}
