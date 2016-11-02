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

import java.util.SortedSet;
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

            final String realm = i % 3 == 0 ? null : "realm_" + i;
            final String group = i % 5 == 0 ? null : "group_" + i;
            final String qualifier = i % 2 == 0 ? null : "qualifier_" + i;

            // Add the AuthorizationPath
            unitUnderTest.add(new AuthorizationPath(realm, group, qualifier));
        }
    }

    @Test
    public void validateSplitUsingStandardLibrary() {

        // Assemble
        final String alt1 = "realm/group/qualifier";
        final String alt2 = "/group/qualifier";
        final String alt3 = "realm//qualifier";

        // Act & Assert
        final String[] split1 = alt1.split(SemanticAuthorizationPath.SEGMENT_SEPARATOR_STRING, -1);
        final String[] split2 = alt2.split(SemanticAuthorizationPath.SEGMENT_SEPARATOR_STRING, -1);
        final String[] split3 = alt3.split(SemanticAuthorizationPath.SEGMENT_SEPARATOR_STRING, -1);

        // Assert
        Assert.assertEquals("realm", split1[0]);
        Assert.assertEquals("group", split1[1]);
        Assert.assertEquals("qualifier", split1[2]);

        Assert.assertEquals("", split2[0]);
        Assert.assertEquals("group", split2[1]);
        Assert.assertEquals("qualifier", split2[2]);

        Assert.assertEquals("realm", split3[0]);
        Assert.assertEquals("", split3[1]);
        Assert.assertEquals("qualifier", split3[2]);
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/authorizationPaths.xml");
        jaxb.add(AuthorizationPaths.class);

        // Act
        final String result = marshalToXML(new AuthorizationPaths(unitUnderTest));

        // Assert
        // System.out.println("Got: " + result);
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/authorizationPaths.xml");
        jaxb.add(AuthorizationPaths.class);

        // Act
        final AuthorizationPaths resurrected = unmarshalFromXML(AuthorizationPaths.class, data);

        // Assert
        Assert.assertNotNull(resurrected);
        final SortedSet<AuthorizationPath> resultPaths = resurrected.getPaths();

        Assert.assertEquals(unitUnderTest.size(), resultPaths.size());

        for (AuthorizationPath current : unitUnderTest) {
            Assert.assertTrue("Path '" + current.getPath() + "' was not found. Got: "
                            + resultPaths.stream()
                            .map(SemanticAuthorizationPath::getPath)
                            .reduce((l, r) -> l + ", " + r)
                            .orElse("<None>"),
                    resultPaths.contains(current));
        }
    }

    @Test
    public void validateFactoryMethod() {

        // Assemble
        final AuthorizationPath path1 = AuthorizationPath.parse("foo/bar/baz");
        final AuthorizationPath path2 = AuthorizationPath.parse("/foo/bar/baz");
        final AuthorizationPath path3 = AuthorizationPath.parse("      foo/bar/baz     ");

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
        AuthorizationPath.parse("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnPathSeparatorInPathArgument() {

        // Act & Assert
        AuthorizationPath.parse("/foo,/bar/baz");
    }

    @Test
    public void validateParsingEmptySegment() {

        // Assemble
        final String emptyGroupPath = "/foo/ /baz";

        // Act
        final AuthorizationPath path1 = AuthorizationPath.parse(emptyGroupPath);
        final AuthorizationPath path2 = new AuthorizationPath("foo", "", "baz");

        // Assert
        Assert.assertEquals("foo", path1.getRealm());
        Assert.assertEquals("", path1.getGroup());
        Assert.assertEquals("baz", path1.getQualifier());
        Assert.assertEquals(path1, path2);
    }
}
