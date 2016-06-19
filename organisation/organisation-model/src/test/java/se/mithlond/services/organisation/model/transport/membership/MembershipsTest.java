/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-api
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
package se.mithlond.services.organisation.model.transport.membership;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;
import se.mithlond.services.shared.test.entity.JpaIdMutator;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MembershipsTest extends AbstractPlainJaxbTest {

    // Shared state
    private Organisation[] organisations;
    private Category[] categories;
    private Group[] groups;
    private Guild[] guilds;
    private Group[] groupsAndGuilds;
    private User[] users;
    private Set<Membership> memberships;

    @Before
    public void setupSharedState() {

        // Create the Organisations
        organisations = new Organisation[3];
        for (int i = 0; i < organisations.length; i++) {
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
                    "emailSuffix_" + i,
                    TimeFormat.SWEDISH_TIMEZONE.normalized(),
                    TimeFormat.SWEDISH_LOCALE);

            JpaIdMutator.setId(organisations[i], 10 + i);
        }


        // Create some categories
        categories = new Category[10];
        for (int i = 0; i < categories.length; i++) {
            categories[i] = new Category("categoryID_" + i, "classification_" + i, "description_" + i);
            JpaIdMutator.setId(categories[i], 20 + i);
        }

        // Create some Groups
        groups = new Group[5];
        guilds = new Guild[groups.length];
        for (int i = 0; i < groups.length; i++) {

            // 'group_name_1_groupName_1'
            groups[i] = new Group(
                    "groupName_" + i,
                    "description_" + i,
                    organisations[i % organisations.length],
                    (i > 1 ? groups[i - 1] : null),
                    "emailList_" + i);

            JpaIdMutator.setId(groups[i], 30 + i);

            guilds[i] = new Guild(
                    "guildName_" + i,
                    "description_" + i,
                    organisations[i % organisations.length],
                    "emailList_" + i,
                    "quenyaName_" + i,
                    "quenyaPrefix_" + i);

            JpaIdMutator.setId(guilds[i], 40 + i);
        }

        groupsAndGuilds = new Group[groups.length + guilds.length];
        for (int i = 0; i < groups.length; i++) {
            groupsAndGuilds[2 * i] = groups[i];
            groupsAndGuilds[(2 * i) + 1] = guilds[i];
        }

        users = new User[5];
        for (int i = 0; i < users.length; i++) {

            final ZonedDateTime birthday = ZonedDateTime.of(
                    1975 + i,
                    5 + i,
                    1 + i,
                    13 + i,
                    15 + i,
                    i,
                    5 + i,
                    TimeFormat.SWEDISH_TIMEZONE);

            final Address homeAddress = new Address(
                    null,
                    null,
                    "homeStreet_" + i,
                    "homeNumber_" + i,
                    "homeCity_" + i,
                    "homeZipCode_" + i,
                    "homeCountry_" + i,
                    "homeAddress_" + i);

            final Map<String, String> contactDetails = new TreeMap<>();
            contactDetails.put("cell_phone", "0312345" + i);

            users[i] = new User(
                    "firstName_" + i,
                    "lastName_" + i,
                    birthday.toLocalDate(),
                    (short) (101 + i),
                    homeAddress,
                    new ArrayList<>(),
                    contactDetails,
                    "userIdentifierToken_" + i);

            JpaIdMutator.setId(users[i], 50 + i);
        }

        memberships = new TreeSet<>();
        for (int i = 0; i < 15; i++) {

            final Membership currentMembership = new Membership(
                    "alias_" + i,
                    "subAlias_" + i,
                    "emailAlias_" + i,
                    !(i % 3 == 0),
                    users[i % users.length],
                    organisations[i % organisations.length]);
            JpaIdMutator.setId(currentMembership, 90 + i);

            memberships.add(currentMembership);

            for (int j = 0; j < i; j += 2) {

                if (j % 3 != 0) {
                    currentMembership.addOrGetGroupMembership(groups[j % groups.length]);
                }

                if (j % 3 == 0) {
                    currentMembership.addOrUpdateGuildMembership(guilds[j % guilds.length], false, false, false);
                }
            }

            currentMembership.getPersonalSettings().put("Skype", "Skype_" + i);
            currentMembership.getPersonalSettings().put("CellPhone", "CellPhone_" + i);
        }
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/membership/memberships.xml");
        final Memberships unitUnderTest = new Memberships();
        unitUnderTest.addOrganisations(organisations);
        unitUnderTest.addGroups(groupsAndGuilds);
        memberships.forEach(unitUnderTest::addMembership);

        // Act
        final String result = marshalToXML(unitUnderTest);
        // System.out.println("Got:  " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/transport/membership/memberships.xml");

        final Memberships expected = new Memberships();
        expected.addOrganisations(organisations);
        expected.addGroups(groupsAndGuilds);
        memberships.forEach(expected::addMembership);

        jaxb.add(Memberships.class);

        // Act
        final Memberships unmarshalled = unmarshalFromXML(Memberships.class, data);

        // Assert
        Assert.assertNotNull(unmarshalled);

        final Map<String, Organisation> actualOrgMap = getOrganisationMap(unmarshalled);
        final Map<String, Organisation> expectedOrgMap = getOrganisationMap(expected);
        Assert.assertEquals(expectedOrgMap.size(), actualOrgMap.size());

        for (Map.Entry<String, Organisation> current : expectedOrgMap.entrySet()) {
            Assert.assertEquals(current.getValue(), actualOrgMap.get(current.getKey()));
        }

        final SortedMap<String, Membership> actualMbMap = getMembershipMap(unmarshalled);
        final SortedMap<String, Membership> expectedMbMap = getMembershipMap(expected);

        for (Map.Entry<String, Membership> current : expectedMbMap.entrySet()) {

            final Membership resurrectedMembership = actualMbMap.get(current.getKey());
            Assert.assertEquals(0, current.getValue().compareTo(resurrectedMembership));
            Assert.assertEquals(2, resurrectedMembership.getPersonalSettings().size());
        }
    }

    //
    // Private helpers
    //

    private SortedMap<String, Membership> getMembershipMap(final Memberships memberships) {

        final SortedMap<String, Membership> toReturn = new TreeMap<>();

        for (Membership current : memberships.getMemberships()) {
            toReturn.put(current.getAlias(), current);
        }

        return toReturn;
    }

    private SortedMap<String, Organisation> getOrganisationMap(final Memberships memberships) {

        final SortedMap<String, Organisation> toReturn = new TreeMap<>();

        for (Organisation current : memberships.getOrganisations()) {
            toReturn.put(current.getOrganisationName(), current);
        }

        // All Done.
        return toReturn;
    }
}
