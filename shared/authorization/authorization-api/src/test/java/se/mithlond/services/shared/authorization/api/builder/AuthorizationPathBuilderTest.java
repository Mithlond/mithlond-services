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
package se.mithlond.services.shared.authorization.api.builder;

import org.junit.Assert;
import org.junit.Test;
import se.mithlond.services.shared.authorization.api.AuthorizationPattern;
import se.mithlond.services.shared.authorization.model.AuthorizationPath;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;

import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AuthorizationPathBuilderTest {

    @Test
    public void validateInitialState() {

        // Assemble
        final String expectedPattern = "///";
        final AuthorizationPathBuilder unitUnderTest = AuthorizationPathBuilder.create();

        // Act
        final AuthorizationPath result = unitUnderTest.build();

        // Assert
        Assert.assertEquals(expectedPattern, result.toString());
    }

    @Test
    public void validateSplittingStrings() {

        // Assemble
        final String foobar = "/foo/bar/baz";
        final String gnatgnus = "/gnat/gnus/gnarp";
        final String hitme = "/hit/me";
        final String tooLong = "/a/too/long/path";

        final String unitUnderTest = foobar + "," + gnatgnus + "," + tooLong;
        final String shorterString = hitme + "," + foobar + ",";
        final String illegalEmpty = "";

        // Act
        final String[] okSplit = unitUnderTest.split(SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING);
        final String[] empty = illegalEmpty.split(SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING);
        final String[] shorter = shorterString.split(SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING);

        final List<List<String>> spliced = Arrays.asList(shorter).stream()
                .map(current -> Arrays.asList(current.split(SemanticAuthorizationPath.SEGMENT_SEPARATOR_STRING)))
                .collect(Collectors.toList());

        // Assert
        Assert.assertEquals(3, okSplit.length);
        Assert.assertEquals(1, empty.length);
        Assert.assertEquals(2, shorter.length);

        Assert.assertEquals(foobar, okSplit[0]);
        Assert.assertEquals(gnatgnus, okSplit[1]);
        Assert.assertEquals(tooLong, okSplit[2]);

        Assert.assertEquals(spliced.size(), shorter.length);
    }

    @Test
    public void validateParsingAuthorizationPaths() {

        // Assemble
        final String mithlondFooPath = "/mithlond/foo/";
        final String mithlondVillageIdiotsMemberPath = "/mithlond/village_idiots/member";

        final String paths = mithlondFooPath + "," + mithlondVillageIdiotsMemberPath;

        // Act
        final SortedSet<SemanticAuthorizationPath> result = AuthorizationPathBuilder.parse(paths);

        // Assert
        Assert.assertEquals(2, result.size());
    }

    @Test
    public void validateBuildingAuthPath() {

        // Assemble
        final AuthorizationPath unitUnderTest = AuthorizationPathBuilder
                .create()
                .withGroup("mithlond")
                .build();

        final AuthorizationPattern pattern = AuthorizationPattern.parseSingle("//mithlond/");

        final SortedMap<String, Boolean> expected = new TreeMap<>();

        expected.put("/mithlond/baz", false);
        expected.put("/foo/mithlond/baz", true);
        expected.put("/foo/mithlond/", true);
        expected.put("//mithlond/", true);
        expected.put("///", false);
        expected.put("/", false);
        expected.put("/uhm", false);

        // Act
        /*

        // Assert
        for(Map.Entry<String, Boolean> current : expected.entrySet()) {
            Assert.assertEquals("Path [" + current.getKey() + "] did not match pattern [" + result.toString() + "]",
                    current.getValue(), pattern.matcher(current.getKey()).matches());
        }
        */
    }
}
