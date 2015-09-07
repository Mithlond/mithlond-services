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
import se.mithlond.services.shared.authorization.api.Segmenter;
import se.mithlond.services.shared.authorization.model.AuthorizationPath;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AuthorizationPathBuilderTest {

    // Shared state
    private String anySegment = SemanticAuthorizationPath.SEGMENT_SEPARATOR_STRING + Segmenter.ANY;

    @Test
    public void validateInitialState() {

        // Assemble
        final String expectedPattern = anySegment + anySegment + anySegment;
        final AuthorizationPathBuilder unitUnderTest = AuthorizationPathBuilder.create();

        // Act
        final AuthorizationPath result = unitUnderTest.build();
        final Pattern pattern = Pattern.compile(result.toString());

        // Assert
        Assert.assertEquals(expectedPattern, result.toString());
        Assert.assertTrue(pattern.matcher("/foo/bar/baz").matches());
    }

    @Test
    public void validateParsingAuthorizationPaths() {

        // Assemble
        final String patterns = "/mithlond/foo,/mithlond/village_idiots/member";

        // Act
        final SortedSet<SemanticAuthorizationPath> result = AuthorizationPathBuilder.parse(patterns);

        // Assert
        Assert.assertEquals(2, result.size());

    }

    @Test
    public void validateBuildingAuthPath() {

        // Assemble
        final AuthorizationPathBuilder unitUnderTest = AuthorizationPathBuilder.create();
        final SortedMap<String, Boolean> expected = new TreeMap<>();

        expected.put("/mithlond/baz", false);
        expected.put("/foo/mithlond/baz", true);
        expected.put("/foo/mithlond/", true);
        expected.put("//mithlond/", true);
        expected.put("///", false);
        expected.put("/", false);
        expected.put("/uhm", false);

        // Act
        final AuthorizationPath result = unitUnderTest
                .withGroup("mithlond")
                .build();
        final Pattern pattern = Pattern.compile(result.toString());

        // Assert
        for(Map.Entry<String, Boolean> current : expected.entrySet()) {
            Assert.assertEquals("Path [" + current.getKey() + "] did not match pattern [" + result.toString() + "]",
                    current.getValue(), pattern.matcher(current.getKey()).matches());
        }
    }
}
