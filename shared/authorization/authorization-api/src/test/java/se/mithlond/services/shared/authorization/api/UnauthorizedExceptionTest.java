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

import org.junit.Before;
import org.junit.Test;
import se.mithlond.services.shared.authorization.model.AuthorizationPath;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;

import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class UnauthorizedExceptionTest {

    // Shared state
    private String paths1 = "/mithlond/members,/mithlond/editors,/forodrim/members";
    private String paths2 = "/mithlond/members,/forodrim/members";
    private SortedSet<SemanticAuthorizationPath> possessedPaths1, possessedPaths2;

    @Before
    public void setupSharedState() {
        possessedPaths1 = AuthorizationPath.spliceAndParse(paths1);
        possessedPaths2 = AuthorizationPath.spliceAndParse(paths2);
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("all")
    public void validateExceptionOnEmptyOperationsDescription() {

        // Assemble
        final SortedSet<GlobAuthorizationPattern> requiredPatterns = GlobAuthorizationPattern.parse("/foo/bar");

        // Act & Assert
        new UnauthorizedException("", possessedPaths1, requiredPatterns);
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("all")
    public void validateExceptionOnNullOperationsDescription() {

        // Assemble
        final SortedSet<GlobAuthorizationPattern> requiredPatterns = GlobAuthorizationPattern.parse("/foo/bar");

        // Act & Assert
        new UnauthorizedException(null, possessedPaths1, requiredPatterns);
    }
}
