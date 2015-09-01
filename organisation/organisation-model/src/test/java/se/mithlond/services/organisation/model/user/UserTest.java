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
package se.mithlond.services.organisation.model.user;

import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.helpers.GroupsAndGuilds;
import se.mithlond.services.organisation.model.helpers.Users;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class UserTest extends AbstractEntityTest {

    private ZonedDateTime firstOfMay2014 = ZonedDateTime.of(2014, 5, 1, 13, 15, 0, 0, TimeFormat.SWEDISH_TIMEZONE);


    // Shared state
    private User[] users;
    private GroupsAndGuilds groupsAndGuilds;

    @Before
    public void setupSharedState() {

        final String data = XmlTestUtils.readFully("testdata/groupsAndGuilds.xml");
        jaxb.add(GroupsAndGuilds.class);
        groupsAndGuilds = unmarshal(GroupsAndGuilds.class, data);

        final List<Organisation> organisations = groupsAndGuilds.getOrganisations();

        users = new User[10];

        // Create some Groups to which we can add Memberships.
        for (int i = 0; i < users.length; i++) {

            final ZonedDateTime currentDate = firstOfMay2014
                    .plusDays(i)
                    .plusHours(i)
                    .plusMinutes(i)
                    .plusSeconds(i);

            final User currentUser = new User("firstName_" + i,
                    "lastName_" + i,
                    currentDate.toLocalDate(),
                    (short) 1234,
                    new Address(
                            "careOfLine_" + i,
                            "departmentName_" + i,
                            "street_" + i,
                            "number_" + i,
                            "city_" + i,
                            "zipCode_" + i,
                            "country_" + i,
                            "description_" + i),
                    new ArrayList<>(),
                    new TreeMap<>()
            );

            final Membership membership = new Membership(
                    "alias_" + i,
                    "subAlias_" + i,
                    "emailAlias_" + i,
                    i % 2 == 0,
                    currentUser,
                    organisations.get(i % organisations.size()),
                    new TreeSet<>(),
                    new TreeSet<>());

            for(Group currentGroupOrGuild : groupsAndGuilds.getGroupsAndGuilds()) {
                if(i % 2 == 0) {
                    if(currentGroupOrGuild instanceof Guild) {
                        membership.addOrUpdateGuildMembership((Guild) currentGroupOrGuild, false, false, false);
                    } else {
                        membership.addOrGetGroupMembership(currentGroupOrGuild);
                    }
                }
            }
            currentUser.getMemberships().add(membership);
            users[i] = currentUser;
        }
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        // final String expected = XmlTestUtils.readFully("testdata/users.xml");
        final Users toMarshal = new Users(users);

        // Act
        final String result = marshal(toMarshal);

        // Assert
        System.out.println("Got: " + result);
        // validateIdenticalContent(expected, result);
    }

    /*
    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final DateTime birthDay = new DateTime(1968, 9, 17, 0, 0, DateTimeZone.UTC);
        final Address address = new Address("careOfLine", "departmentName", "street",
                "number", "city", "zipCode", "country", "description");
        final Map<String, String> contactDetails = new TreeMap<String, String>();
        contactDetails.put(ContactType.HOME_PHONE.toString(), "012345678");

        final Member unitUnderTest = new Member("login", "alias", "subalias", "firstName", "lastName",
                "hashedPassword", birthDay, (short) 6789, address, contactDetails);

        final String data = XmlTestUtils.readFully("testdata/member.xml");

        // Act
        final Member result = binder.unmarshalInstance(new StringReader(data));

        // Assert
        Assert.assertNotSame(unitUnderTest, result);
        Assert.assertEquals(unitUnderTest.getAlias(), result.getAlias());
        Assert.assertEquals(unitUnderTest.getFirstName(), result.getFirstName());
    }

    @Test
    public void validateToString() {

        // Assemble
        final DateTime birthDay = new DateTime(1968, 9, 17, 0, 0, DateTimeZone.UTC);
        final Address address = new Address("careOfLine", "departmentName", "street", "number", "city", "zipCode",
                "country", "description");
        final Map<String, String> contactDetails = new TreeMap<String, String>();
        contactDetails.put("HOME_PHONE", "012345678");

        final Member unitUnderTest = new Member("login", "alias", "subalias", "firstName", "lastName",
                "hashedPassword", birthDay, (short) 6789, address, contactDetails);

        final String expected = "Member: " + unitUnderTest.getAlias() + " (" + unitUnderTest.getFirstName()
                + " - " + unitUnderTest.getLastName() + ")\n" +
                "Contact Details: " + ContactType.HOME_PHONE + ": " + "012345678\n" +
                "\n" + "Home Address: " + address.toString();

        // Act
        final String result = unitUnderTest.toString();

        // Assert
        Assert.assertEquals(expected, result);
    }
    */
}

