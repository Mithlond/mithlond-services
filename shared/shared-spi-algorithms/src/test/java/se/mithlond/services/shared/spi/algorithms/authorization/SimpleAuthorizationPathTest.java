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
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SimpleAuthorizationPathTest {

    @Test
    public void validateParsingAndSorting() {

        // Assemble
        final SimpleAuthorizationPath path1 = new SimpleAuthorizationPath("realm1", "group1", "qualifier1");
        final SimpleAuthorizationPath path2 = new SimpleAuthorizationPath("realm2", "group2", "qualifier2");
        final SimpleAuthorizationPath path3 = new SimpleAuthorizationPath("realm3", "group3", "qualifier3");
        final AuthorizationPath path3_2 = SimpleAuthorizationPath.parse("realm3/group3/qualifier3").first();

        // Act
        final SortedSet<SimpleAuthorizationPath> apSet = new TreeSet<>();
        apSet.addAll(Arrays.asList(path2, path1, path3));
        final SimpleAuthorizationPath[] sapArray = apSet.toArray(new SimpleAuthorizationPath[apSet.size()]);

        // Assert
        Assert.assertSame(path1, sapArray[0]);
        Assert.assertSame(path2, sapArray[1]);
        Assert.assertSame(path3, sapArray[2]);

        Assert.assertEquals(path1, sapArray[0]);
        Assert.assertEquals(path2, sapArray[1]);
        Assert.assertEquals(path3, sapArray[2]);

        Assert.assertNotSame(path3, path3_2);
        Assert.assertEquals(path3, path3_2);
    }

    @Test
    public void validateAuthorizingPaths() {

        // Assemble
        final List<String> requirements = Arrays.asList("/mithlond/village_idiots", "/forodrim/members");
        final List<AuthorizationPath> paths = new ArrayList<>(SimpleAuthorizationPath.parse(
                "/mithlond/village_idiots/guildmaster,/mithlond/council/allowAny"));

        // Act
        final boolean authorized = SimpleAuthorizationPath.isAuthorized(requirements, paths);

        // Assert
        Assert.assertTrue(authorized);
    }
}
