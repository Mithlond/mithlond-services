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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.mithlond.services.shared.authorization.api.builder.AuthorizationPathBuilder;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SimpleAuthorizerTest {

    // Shared state
    private SortedSet<SemanticAuthorizationPath> possessedPrivileges;

    @Before
    public void setupSharedState() {
        possessedPrivileges = AuthorizationPathBuilder.parse(
                "/mithlond/village_idiots/member,/mithlond/council,/mithlond/dwarfGuild/guildMaster");
    }

    @Test
    public void validateAuthorizedOnNoRequiredPatterns() {

        // Assemble
        final String emptyPattern = "";
        final String nullPattern = null;

        // Act
        final boolean result1 = SimpleAuthorizer.getInstance().isAuthorized(emptyPattern, possessedPrivileges);
        final boolean result2 = SimpleAuthorizer.getInstance().isAuthorized(nullPattern, possessedPrivileges);

        // Assert
        Assert.assertTrue(result1);
        Assert.assertTrue(result2);
    }

    @Test
    public void validateNotAuthorizedOnNoPossessedPrivileges() {

        // Assemble
        final String authorizationPattern = "/mithlond/village_idiots";
        final SortedSet<SemanticAuthorizationPath> nullPrivileges = null;
        final SortedSet<SemanticAuthorizationPath> emptyPrivileges = new TreeSet<>();

        // Act
        final boolean result1 = SimpleAuthorizer.getInstance().isAuthorized(authorizationPattern, nullPrivileges);
        final boolean result2 = SimpleAuthorizer.getInstance().isAuthorized(authorizationPattern, emptyPrivileges);

        // Assert
        Assert.assertFalse(result1);
        Assert.assertFalse(result2);
    }

    @Test
    public void validateAuthorization() {

        // Assemble
        final String authorizationPattern = "/mithlond/village_idiots";
        final SimpleAuthorizer unitUnderTest = SimpleAuthorizer.getInstance();

        final String villageIdiotsMember = "/mithlond/village_idiots/member";
        final String plainMithlondMember = "/mithlond/members";
        final String plainVillageIdiots = "/mithlond/village_idiots";

        // Act & Assert
        Assert.assertTrue(unitUnderTest.isAuthorized(authorizationPattern, getAuthPaths(villageIdiotsMember)));
        Assert.assertFalse(unitUnderTest.isAuthorized(authorizationPattern, getAuthPaths(plainMithlondMember)));
        Assert.assertTrue(unitUnderTest.isAuthorized(authorizationPattern, getAuthPaths(plainVillageIdiots)));
    }

    @Test
    public void validateAuthorizationUsingSlashTermination() {

        // Assemble
        final String authorizationPattern = "/mithlond/";
        final SimpleAuthorizer unitUnderTest = SimpleAuthorizer.getInstance();

        final String villageIdiotsMember = "/mithlond/village_idiots/member";
        final String plainMithlondMember = "/mithlond/members";
        final String plainVillageIdiots = "/mithlond/villageIdiots";


        // Act & Assert
        Assert.assertTrue(unitUnderTest.isAuthorized(authorizationPattern, getAuthPaths(villageIdiotsMember)));
        Assert.assertTrue(unitUnderTest.isAuthorized(authorizationPattern, getAuthPaths(plainMithlondMember)));
        Assert.assertTrue(unitUnderTest.isAuthorized(authorizationPattern, getAuthPaths(plainVillageIdiots)));
    }

    @Test
    public void validateAuthorizationUsingPatterns() {

        // Assemble
        final SimpleAuthorizer unitUnderTest = SimpleAuthorizer.getInstance();
        final SortedSet<AuthorizationPattern> patterns1 = AuthorizationPattern.parse("/forodrim/members," +
                "/mithlond/members");
        final SortedSet<AuthorizationPattern> patterns2 = AuthorizationPattern.parse("/mithlond/village_idiots/");
        final SortedSet<AuthorizationPattern> patterns3 = AuthorizationPattern.parse("/mithlond/village_idiots/"
                + Segmenter.ANY);

        // Act & Assert
        Assert.assertFalse(unitUnderTest.isAuthorized(patterns1, possessedPrivileges));
        Assert.assertTrue(unitUnderTest.isAuthorized(patterns2, possessedPrivileges));
        Assert.assertTrue(unitUnderTest.isAuthorized(patterns3, possessedPrivileges));
    }

    //
    // Private helpers
    //

    private SortedSet<SemanticAuthorizationPath> getAuthPaths(final String toParse) {
        return AuthorizationPathBuilder.parse(toParse);
    }
}
