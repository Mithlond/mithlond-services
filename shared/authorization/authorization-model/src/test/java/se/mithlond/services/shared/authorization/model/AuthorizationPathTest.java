/*
 * #%L
 * Nazgul Project: mithlond-services-shared-authorization-model
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
package se.mithlond.services.shared.authorization.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.shared.authorization.model.helpers.AuthorizationPaths;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AuthorizationPathTest extends AbstractPlainJaxbTest {

    // Shared state
    private SortedSet<AuthorizationPath> unitUnderTest;

    @Before
    public void setupSharedState() {

        unitUnderTest = new TreeSet<>();

        for (int i = 0; i < 10; i++) {

            final String realm = i % 3 == 0 ? "" : "realm_" + i;
            final String group = i % 5 == 0 ? "" : "group_" + i;
            final String qualifier = i % 2 == 0 ? "" : "qualifier_" + i;

            // Add the AuthorizationPath
            unitUnderTest.add(new AuthorizationPath(realm, group, qualifier));
        }
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/authorizationPaths.xml");
        jaxb.add(AuthorizationPaths.class);

        // Act
        final String result = marshal(new AuthorizationPaths(unitUnderTest));

        // Assert
        // System.out.println("Got: " + result);.
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/authorizationPaths.xml");
        jaxb.add(AuthorizationPaths.class);

        // Act
        final AuthorizationPaths resurrected = unmarshal(AuthorizationPaths.class, data);

        // Assert
        Assert.assertNotNull(resurrected);
        final SortedSet<AuthorizationPath> resultPaths = resurrected.getPaths();

        Assert.assertEquals(unitUnderTest.size(), resultPaths.size());

        for (AuthorizationPath current : unitUnderTest) {
            Assert.assertTrue(resultPaths.contains(current));
        }
    }

    @Test
    public void validateFactoryMethod() {

        // Assemble
        final AuthorizationPath path1 = AuthorizationPath.create("foo/bar/baz");
        final AuthorizationPath path2 = AuthorizationPath.create("/foo/bar/baz");
        final AuthorizationPath path3 = AuthorizationPath.create("      foo/bar/baz     ");

        // Act

        // Assert
        Assert.assertEquals(path1, path2);
        Assert.assertEquals(path1, path3);
        Assert.assertEquals(0, path1.compareTo(path2));
        Assert.assertEquals(0, path1.compareTo(path3));
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyPathToFactoryMethod() {

        // Act & Assert
        AuthorizationPath.create("");
    }

    @Test
    public void validateExceptionOnIncorrectNumberOfArgumentsInFactoryMethod() {

        // Assemble
        final SortedMap<String, Boolean> pathToExpectedSuccess = new TreeMap<>();
        for (int i = 0; i < 10; i++) {

            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < i; j++) {
                builder.append(AuthorizationPath.SEGMENT_SEPARATOR + "segment_" + j);
            }

            pathToExpectedSuccess.put(builder.toString(), i == 3);
        }

        /*
        for(Map.Entry<String, Boolean> current : pathToExpectedSuccess.entrySet()) {
            System.out.println(" Expected [" + current.getKey() + "] -->" + current.getValue());
        }
         */

        final SortedMap<String, Boolean> pathToActualSuccess = new TreeMap<>();

        // Act
        for (Map.Entry<String, Boolean> current : pathToExpectedSuccess.entrySet()) {
            pathToActualSuccess.put(current.getKey(), createMethodReturnedSuccessfully(current.getKey()));
        }

        // Assert
        for (Map.Entry<String, Boolean> current : pathToActualSuccess.entrySet()) {

            final String currentPath = current.getKey();
            Assert.assertEquals(pathToExpectedSuccess.get(currentPath), pathToActualSuccess.get(currentPath));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnPathSeparatorInPathArgument() {

        // Act & Assert
        AuthorizationPath.create("/foo,/bar/baz");
    }

    @Test
    public void validateParsingEmptySegment() {

        // Assemble
        final String emptyGroupPath = "/foo/ /baz";

        // Act
        final AuthorizationPath path1 = AuthorizationPath.create(emptyGroupPath);
        final AuthorizationPath path2 = new AuthorizationPath("foo", "", "baz");

        // Assert
        Assert.assertEquals("foo", path1.getRealm());
        Assert.assertEquals("", path1.getGroup());
        Assert.assertEquals("baz", path1.getQualifier());
        Assert.assertEquals(path1, path2);
    }

    //
    // Private helpers
    //

    private boolean createMethodReturnedSuccessfully(final String path) {
        try {
            AuthorizationPath.create(path);
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
