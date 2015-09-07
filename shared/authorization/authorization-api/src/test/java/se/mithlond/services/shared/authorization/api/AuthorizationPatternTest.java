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
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AuthorizationPatternTest {

    // Shared state
    private String anySegment = "/" + Segmenter.ANY;

    @Test
    public void validateInitialCreation() {

        // Assemble
        final String realm = "mithlond";
        final String group = "village_idiots";
        final String qualifier = "guildmaster";


        final String fullAnyPath = anySegment + anySegment + anySegment;
        final String realmGroupPath = "/" + realm + "/" + group + anySegment;
        final String qualifiedPath = "/" + realm + "/" + group + "/" + qualifier;

        // Act
        final AuthorizationPattern unitUnderTest1 = new AuthorizationPattern();
        final AuthorizationPattern unitUnderTest2 = new AuthorizationPattern(realm, group);
        final AuthorizationPattern unitUnderTest3 = new AuthorizationPattern(realm, group, qualifier);

        // Assert
        Assert.assertEquals(fullAnyPath, unitUnderTest1.toString());
        Assert.assertEquals(realmGroupPath, unitUnderTest2.toString());
        Assert.assertEquals(qualifiedPath, unitUnderTest3.toString());

        Assert.assertEquals(fullAnyPath.hashCode(), unitUnderTest1.hashCode());
        Assert.assertEquals(realmGroupPath.hashCode(), unitUnderTest2.hashCode());
        Assert.assertEquals(qualifiedPath.hashCode(), unitUnderTest3.hashCode());

        Assert.assertEquals(-1, unitUnderTest1.compareTo(null));
        Assert.assertEquals(0, unitUnderTest1.compareTo(unitUnderTest1));
    }

    @Test
    public void validateParsingSingleAuthorizationPattern() {

        // Assemble
        final String fullPattern = "/realm/group/qualifier";
        final String realmGroupPattern = "/realm/group";
        final String realmPattern = "/realm";

        final String realmPatternWithoutInitialSeparator = "realm";
        final String realmGroupPatternWithoutInitialSeparator = "realm/group";
        final String fullPatternWithoutInitialSeparator = "realm/group/qualifier";

        // Act
        final AuthorizationPattern p1 = AuthorizationPattern.parseSingle(fullPattern);
        final AuthorizationPattern p2 = AuthorizationPattern.parseSingle(fullPatternWithoutInitialSeparator);
        final AuthorizationPattern p3 = AuthorizationPattern.parseSingle(realmGroupPattern);
        final AuthorizationPattern p4 = AuthorizationPattern.parseSingle(realmGroupPatternWithoutInitialSeparator);
        final AuthorizationPattern p5 = AuthorizationPattern.parseSingle(realmPattern);
        final AuthorizationPattern p6 = AuthorizationPattern.parseSingle(realmPatternWithoutInitialSeparator);

        // Assert
        Assert.assertEquals(fullPattern, p1.toString());
        Assert.assertEquals(fullPattern, p2.toString());
        Assert.assertEquals(realmGroupPattern + anySegment, p3.toString());
        Assert.assertEquals(realmGroupPattern + anySegment, p4.toString());
        Assert.assertEquals(realmPattern + anySegment + anySegment, p5.toString());
        Assert.assertEquals(realmPattern + anySegment + anySegment, p6.toString());
    }

    @Test
    public void validateParsingSingleAuthorizationPatterns() {

        // Assemble
        final SortedMap<String, AuthorizationPattern> actual = new TreeMap<>();
        final SortedMap<String, String> expected = new TreeMap<>();

        expected.put("//mithlond/", anySegment + "/mithlond" + anySegment);
        expected.put("///", anySegment + anySegment + anySegment);
        expected.put("/mithlond/baz", "/mithlond/baz" + anySegment);
        expected.put("/foo/mithlond/baz", "/foo/mithlond/baz");
        expected.put("/foo/mithlond/", "/foo/mithlond" + anySegment);
        expected.put("/", anySegment + anySegment + anySegment);
        expected.put("/uhm", "/uhm" + anySegment + anySegment);

        // Act
        for (Map.Entry<String, String> current : expected.entrySet()) {
            actual.put(current.getKey(), AuthorizationPattern.parseSingle(current.getKey()));
        }

        // Assert
        Assert.assertEquals(expected.size(), actual.size());
        for (Map.Entry<String, AuthorizationPattern> current : actual.entrySet()) {
            Assert.assertEquals(expected.get(current.getKey()), current.getValue().toString());
        }
    }

    @Test
    public void validateExceptionOnParsingIncorrectSingleAuthorizationPatterns() {

        // Assemble
        final List<String> incorrectPatterns = Arrays.asList("/a/b/c/d/e", "////", null);

        // Act & Assert
        for(String current : incorrectPatterns) {
            try {
                AuthorizationPattern.parseSingle(current);
                Assert.fail("Expected failure for parsing pattern [" + current + "]");
            } catch (Exception e) {
                // Ignore this.
            }
        }
    }

    @Test
    public void validateParsingMultiplePaths() {

        // Assemble
        final String toParse = "//mithlond/,/,/foo/mithlond/baz";

        final SortedSet<String> expected = new TreeSet<>();
        expected.add("/foo/mithlond/baz");
        expected.add(anySegment + "/mithlond" + anySegment);
        expected.add(anySegment + anySegment + anySegment);

        // Act
        final SortedSet<AuthorizationPattern> parsed = AuthorizationPattern.parse(toParse);

        // Assert
        final Iterator<AuthorizationPattern> patternIterator = parsed.iterator();

        for (final String currentExpectedPattern: expected) {
            final AuthorizationPattern actual = patternIterator.next();
            Assert.assertEquals(currentExpectedPattern, actual.toString());
        }
    }
}
