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
import se.mithlond.services.shared.authorization.model.AuthorizationPath;
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

        possessedPrivileges = new TreeSet<>();
        possessedPrivileges.add(AuthorizationPath.parse("/mithlond/village_idiots/member"));
        possessedPrivileges.add(AuthorizationPath.parse("/mithlond/council"));
        possessedPrivileges.add(AuthorizationPath.parse("/mithlond/dwarfGuild/guildMaster"));
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
    public void validateAuthorizationOnGlobPatternMatch() {

        // Assemble
        final SimpleAuthorizer unitUnderTest = SimpleAuthorizer.getInstance();

        final String realm = "/mifflond/";
        final String mifflondVillageIdiotsPattern = realm + "villageIdiots";
        final SortedSet<SemanticAuthorizationPath> villageIdiotsMember = AuthorizationPath
                .spliceAndParse(mifflondVillageIdiotsPattern + "/member");
        final SortedSet<SemanticAuthorizationPath> memberOfSomeOtherGroup = AuthorizationPath
                .spliceAndParse(realm + "someOtherGroup");

        // Act & Assert
        Assert.assertTrue(unitUnderTest.isAuthorized(mifflondVillageIdiotsPattern, villageIdiotsMember));
        Assert.assertFalse(unitUnderTest.isAuthorized(mifflondVillageIdiotsPattern, memberOfSomeOtherGroup));
    }

    @Test
    public void validateAuthorizationUsingSlashTermination() {

        // Assemble
        final String authorizationPattern = "/mithlond/**";
        final SimpleAuthorizer unitUnderTest = SimpleAuthorizer.getInstance();

        final String villageIdiotsMember = "/mithlond/village_idiots/member";
        final String plainMithlondMember = "/mithlond/members";
        final String plainVillageIdiots = "/mithlond/villageIdiots";

        // Act & Assert
        Assert.assertTrue(unitUnderTest.isAuthorized(authorizationPattern,
                wrapSingleAuthPathInSet(villageIdiotsMember)));
        Assert.assertTrue(unitUnderTest.isAuthorized(authorizationPattern,
                wrapSingleAuthPathInSet(plainMithlondMember)));
        Assert.assertTrue(unitUnderTest.isAuthorized(authorizationPattern,
                wrapSingleAuthPathInSet(plainVillageIdiots)));
    }

    @Test
    public void validateAuthorizationUsingPatterns() {

        // Assemble
        final SimpleAuthorizer unitUnderTest = SimpleAuthorizer.getInstance();
        final SortedSet<GlobAuthorizationPattern> patterns1 = GlobAuthorizationPattern.parse(
                "/forodrim/members,/mithlond/members");
        final SortedSet<GlobAuthorizationPattern> patterns2 = GlobAuthorizationPattern
                .parse("/mithlond/village_idiots/");
        final SortedSet<GlobAuthorizationPattern> patterns3 = GlobAuthorizationPattern
                .parse("/mithlond/village_idiots/" + GlobAuthorizationPattern.ANY);

        // Act & Assert
        Assert.assertFalse(unitUnderTest.isAuthorized(patterns1, possessedPrivileges));
        Assert.assertTrue(unitUnderTest.isAuthorized(patterns2, possessedPrivileges));
        Assert.assertTrue(unitUnderTest.isAuthorized(patterns3, possessedPrivileges));
    }

    @Test(expected = UnauthorizedException.class)
    public void validateUnauthorizedExceptionUsingPatterns() {

        // Assemble
        final SimpleAuthorizer unitUnderTest = SimpleAuthorizer.getInstance();
        final SortedSet<GlobAuthorizationPattern> patterns1 = GlobAuthorizationPattern.parse(
                "/forodrim/members,/mithlond/members");

        // Act & Assert
        unitUnderTest.validateAuthorization(patterns1, possessedPrivileges, "Some Operations Description");
    }

    @Test
    public void validateAuthorizedCallYieldsNoUnauthorizedExceptionUsingPatterns() {

        // Assemble
        final SimpleAuthorizer unitUnderTest = SimpleAuthorizer.getInstance();

        // Act & Assert
        unitUnderTest.validateAuthorization("/mithlond/village_idiots/*", possessedPrivileges, "Irrelevant");
    }

    @Test
    public void validateAuthorizationIgnoresInitialSlash() {

        // Assemble
        final String prefix = "mithlond/village_idiots/";
        final String pattern = prefix + "*";
        final String path = prefix + "members";

        // Act
        Assert.assertTrue(GlobAuthorizationPattern.createSinglePattern(pattern).matches(path));
        Assert.assertTrue(GlobAuthorizationPattern.createSinglePattern("/" + pattern).matches("/" + path));
        Assert.assertTrue(GlobAuthorizationPattern.createSinglePattern(pattern).matches("/" + path));
        Assert.assertTrue(GlobAuthorizationPattern.createSinglePattern("/" + pattern).matches(path));
    }

    //
    // Private helpers
    //

    private SortedSet<SemanticAuthorizationPath> wrapSingleAuthPathInSet(final String toParse) {

        SortedSet<SemanticAuthorizationPath> toReturn = new TreeSet<>();
        toReturn.add(AuthorizationPath.parse(toParse));

        return toReturn;
    }
}
