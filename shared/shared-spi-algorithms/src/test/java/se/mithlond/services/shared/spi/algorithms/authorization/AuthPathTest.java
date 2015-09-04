/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
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
package se.mithlond.services.shared.spi.algorithms.authorization;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AuthPathTest {

    /*
    @Test
    public void useCaseAndSpecification() {

        // 1) Read method annotation
        // 2) Is authorization required?
        // 2.1) Find all required authorization paths from the method annotation
        // 2.2) Match all held privileges paths from the active user to all required APs
        // 2.3) Single match ==> authorized
    }
    */

    @Test
    public void validateParsingAndSorting() {

        // Assemble
        final String authPaths = "realm1/group1/qualifier1,realm1/group1/qualifier1,"
                + "realm2/group2/qualifier2,realm3/group3/qualifier3";

        // Act
        final SortedSet<SemanticAuthorizationPath> parsed = AuthPath.parse(authPaths);
        final List<SemanticAuthorizationPath> parsedList = new ArrayList<>(parsed);

        // Assert
        Assert.assertEquals(3, parsed.size());
        final SemanticAuthorizationPath firstPath = parsedList.get(0);
        final SemanticAuthorizationPath secondPath = parsedList.get(1);
        final SemanticAuthorizationPath thirdPath = parsedList.get(2);

        Assert.assertEquals("/realm1/group1/qualifier1", firstPath.toString());
        Assert.assertEquals("/realm2/group2/qualifier2", secondPath.toString());
        Assert.assertEquals("/realm3/group3/qualifier3", thirdPath.toString());

        Assert.assertEquals("realm1", firstPath.getRealm());
        Assert.assertEquals("group1", firstPath.getGroup());
        Assert.assertEquals("qualifier1", firstPath.getQualifier());

        Assert.assertEquals("realm2", secondPath.getRealm());
        Assert.assertEquals("group2", secondPath.getGroup());
        Assert.assertEquals("qualifier2", secondPath.getQualifier());

        Assert.assertEquals("realm3", thirdPath.getRealm());
        Assert.assertEquals("group3", thirdPath.getGroup());
        Assert.assertEquals("qualifier3", thirdPath.getQualifier());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnTooFewNumberOfSegments() {

        // Act & Assert
        AuthPath.parse("realm1/group1");
    }
}
