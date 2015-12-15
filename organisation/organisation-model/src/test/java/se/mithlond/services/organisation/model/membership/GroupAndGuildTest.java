/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model.membership;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.helpers.GroupsAndGuilds;
import se.mithlond.services.organisation.model.membership.guild.Guild;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class GroupAndGuildTest extends AbstractEntityTest {

    // Shared state
    private Organisation[] organisations;
    private Category[] categories;
    private Group[] groups;
    private Guild[] guilds;
    private Group[] groupsAndGuilds;

    @Before
    public void setupSharedState() {

        // Create the Organisations
        organisations = new Organisation[3];
        for(int i = 0; i < organisations.length; i++) {
            final Address currentAddress = new Address(
                    "careOfLine_" + i,
                    "departmentName_" + i,
                    "street_" + i,
                    "number_" + i,
                    "city_" + i,
                    "zipCode_" + i,
                    "country_" + i,
                    "description_" + i);

            organisations[i] = new Organisation(
                    "name_" + i,
                    "suffix_" + i,
                    "phone_" + i,
                    "bankAccountInfo_" + i,
                    "postAccountInfo_" + i,
                    currentAddress,
                    "emailSuffix_" + i);
        }


        // Create some categories
        categories = new Category[10];
        for(int i = 0; i < categories.length; i++) {
            categories[i] = new Category("categoryID_" + i, "classification_" + i, "description_" + i);
        }

        // Create some Groups
        groups = new Group[5];
        guilds = new Guild[groups.length];
        for(int i = 0; i < groups.length; i++) {

            groups[i] = new Group(
                    "groupName_" + i,
                    organisations[i % organisations.length],
                    (i > 1 ? groups[i-1] : null),
                    "emailList_" + i);

            guilds[i] = new Guild(
                    "guildName_" + i,
                    organisations[i % organisations.length],
                    "emailList_" + i,
                    "quenyaName_" + i,
                    "quenyaPrefix_" + i);
        }

        groupsAndGuilds = new Group[groups.length + guilds.length];
        for(int i = 0; i < groups.length; i++) {
            groupsAndGuilds[2 * i] = groups[i];
            groupsAndGuilds[(2 * i) + 1] = guilds[i];
        }
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/groupsAndGuilds.xml");
        final GroupsAndGuilds toMarshal = new GroupsAndGuilds(groupsAndGuilds);

        // Act
        final String result = marshalToXML(toMarshal);

        // Assert
        // System.out.println("Got: " + result);
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/groupsAndGuilds.xml");
        jaxb.add(GroupsAndGuilds.class);

        // Act
        final GroupsAndGuilds result = unmarshalFromXML(GroupsAndGuilds.class, data);

        // Assert
        Assert.assertNotNull(result);

        final List<Group> resurrected = result.getGroupsAndGuilds();
        Assert.assertEquals(groupsAndGuilds.length, resurrected.size());

        final SortedMap<String, Group> actual = new TreeMap<>();
        final SortedMap<String, Group> expected = new TreeMap<>();
        resurrected.forEach(x -> actual.put(x.getGroupName(), x));
        Arrays.asList(groupsAndGuilds).forEach(x -> expected.put(x.getGroupName(), x));

        for(Map.Entry<String, Group> current : actual.entrySet()) {

            final Group currentExpected = expected.get(current.getKey());

            Assert.assertNotSame(currentExpected, current.getValue());
            Assert.assertEquals(currentExpected, current.getValue());
        }
    }
}
