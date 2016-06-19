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

import org.junit.Assert;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class UserTest extends AbstractEntityTest {

    private ZonedDateTime firstOfMay2014 = ZonedDateTime.of(
            LocalDate.of(2014, Month.MAY, 1),
            LocalTime.of(13, 15, 0),
            TimeFormat.SWEDISH_TIMEZONE);

    // Shared state
    private User[] users;
    private GroupsAndGuilds groupsAndGuilds;

    @Before
    public void setupSharedState() {

        // jaxb.addIgnoreClassPatterns("ch.");

        final String data = XmlTestUtils.readFully("testdata/groupsAndGuilds.xml");
        jaxb.add(GroupsAndGuilds.class);
        groupsAndGuilds = unmarshalFromXML(GroupsAndGuilds.class, data);

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
                    new TreeMap<>(),
                    "userIdentiferToken_" + i
            );

            final Membership membership = new Membership(
                    "alias_" + i,
                    "subAlias_" + i,
                    "emailAlias_" + i,
                    i % 3 == 0,
                    currentUser,
                    organisations.get(i % organisations.size()),
                    new TreeSet<>(),
                    new TreeSet<>());

            membership.getPersonalSettings().put("Foo", "Foo_" + i);

            for (Group currentGroupOrGuild : groupsAndGuilds.getGroupsAndGuilds()) {
                if (i % 2 == 0) {
                    if (currentGroupOrGuild instanceof Guild) {
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

    /**
     * NOTE!
     *
     * It is vital that the package-info.java file is present in the helpers directory
     * to inform the JAXB engine that the locally crafted XML type converters for Java 8 should be used.
     *
     * @see se.mithlond.services.shared.spi.jaxb.adapter.LocalDateAdapter
     * @see se.mithlond.services.shared.spi.jaxb.adapter.LocalTimeAdapter
     * @see se.mithlond.services.shared.spi.jaxb.adapter.LocalDateTimeAdapter
     * @see se.mithlond.services.shared.spi.jaxb.adapter.ZonedDateTimeAdapter
     */
    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/someUsers.xml");
        final Users toMarshal = new Users(users);

        // Act
        final String result = marshalToXML(toMarshal);

        // Assert
        // System.out.println("Got: " + result);
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/someUsers.xml");
        jaxb.add(Users.class);

        // Act
        final Users resurrected = unmarshalFromXML(Users.class, data);

        // Assert
        Assert.assertNotNull(resurrected);

        final List<User> users = resurrected.getUsers();
        Assert.assertNotNull(users);
        Assert.assertEquals(this.users.length, users.size());

        for (int i = 0; i < this.users.length; i++) {

            final User expectedUser = this.users[i];
            final User actualUser = users.get(i);

            Assert.assertNotSame(expectedUser, actualUser);
            Assert.assertEquals(expectedUser, actualUser);
        }
    }
}

