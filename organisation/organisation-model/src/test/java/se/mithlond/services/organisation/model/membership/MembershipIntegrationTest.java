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

import org.dbunit.Assertion;
import org.dbunit.assertion.DiffCollectingFailureHandler;
import org.dbunit.assertion.Difference;
import org.dbunit.dataset.IDataSet;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.helpers.GroupsAndGuilds;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.membership.guild.GuildMembership;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.AbstractIntegrationTest;

import java.lang.reflect.Field;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Ignore("Fixing dbUnit setup files.")
public class MembershipIntegrationTest extends AbstractIntegrationTest {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(MembershipIntegrationTest.class);

    // Shared state
    private ZonedDateTime firstOfMay1974 = ZonedDateTime.of(1974, 5, 1, 13, 15, 0, 0, TimeFormat.SWEDISH_TIMEZONE);
    private Organisation dbOrganisation;
    private List<Group> dbGroups;
    private User[] users;

    public MembershipIntegrationTest() {
        super(DateTimeZone.forID("Europe/Stockholm"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCustomSetup() {

        // Enlighten the JAXB context
        jaxb.add(GroupsAndGuilds.class);
        users = new User[10];

        // Create some Groups to which we can add Memberships.
        for (int i = 0; i < users.length; i++) {

            final ZonedDateTime currentDate = firstOfMay1974
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

            users[i] = currentUser;
        }
    }

    /**
     * Override to ignore Foreign Key violation in dropping all database objects.
     * Also reset the sequences for all tables within the HSQLDB database, or
     * expected values for further tests will be incorrect.
     */
    @Override
    protected void cleanupTestSchema(final boolean shutdownDatabase) {
        try {
            super.cleanupTestSchema(shutdownDatabase);
        } catch (Exception e) {
            // Ignore this
        }
    }

    @Test
    public void validateUpdatingMembershipData() throws Exception {

        // Assemble
        final IDataSet expected = performStandardTestDbSetup();

        // Act #1: Find the Groups.
        List<Group> resultList = entityManager.createNamedQuery(Group.NAMEDQ_GET_BY_ORGANISATION, Group.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, "Mith%")
                .getResultList();

        // Assert #1: Assert groups and organisation
        Assert.assertEquals(3, resultList.size());

        Group administrators = resultList.get(0);
        Group members = resultList.get(1);
        Guild mthFekalion = (Guild) resultList.get(2);
        Organisation mithlond = administrators.getOrganisation();

        Assert.assertEquals("Administratörer", administrators.getGroupName());
        Assert.assertEquals("Skitsnackargillet", mthFekalion.getGroupName());
        Assert.assertFalse(administrators instanceof Guild);

        for (int i = 0; i < users.length; i++) {

            // Persist the user
            entityManager.persist(users[i]);

            final Membership membership = new Membership(
                    "alias_" + i,
                    "subAlias_" + i,
                    "emailAlias_" + i,
                    true,
                    users[i],
                    mithlond);

            // Add some Group memberships
            final Set<GroupMembership> groupMemberships = membership.getGroupMemberships();
            if (i % 2 == 0) {
                groupMemberships.add(new GroupMembership(administrators, membership));
            } else {
                groupMemberships.add(new GroupMembership(members, membership));
            }
            if ((i + 1) % 3 == 0) {
                groupMemberships.add(new GuildMembership(mthFekalion, membership, false, false, false));
            }

            // Persist the membership
            entityManager.persist(membership);
            commitAndStartNewTransaction();

            // Re-acquire the managed versions of Groups and Organisations.
            resultList = entityManager.createNamedQuery(Group.NAMEDQ_GET_BY_ORGANISATION, Group.class)
                    .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, "Mith%")
                    .getResultList();

            administrators = resultList.get(0);
            members = resultList.get(1);
            mthFekalion = (Guild) resultList.get(2);
            mithlond = administrators.getOrganisation();
        }
        commitAndStartNewTransaction();

        final List<Membership> memberships = entityManager.createNamedQuery(
                Membership.NAMEDQ_GET_BY_ORGANISATION_LOGINPERMITTED, Membership.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, "Mit%")
                .setParameter(OrganisationPatterns.PARAM_LOGIN_PERMITTED, true)
                .getResultList();
        Assert.assertEquals(10, memberships.size());

        final Membership toUpdate = memberships.get(2);
        toUpdate.setLoginPermitted(false);
        commitAndStartNewTransaction();

        // printCurrentDatabaseState();

        // Assert #1: Validate full database state.
        // printCurrentDatabaseState();
        final DiffCollectingFailureHandler diffHandler = new DiffCollectingFailureHandler();
        Assertion.assertEquals(expected, iDatabaseConnection.createDataSet(), diffHandler);

        // Handle TravisCI's date/timezone settings
        final List<Difference> diffList = (List<Difference>) diffHandler.getDiffList();
        if(diffList.size() > 0) {

            final Difference difference = diffList.get(0);
            final String columnName = difference.getColumnName();
            if(columnName.equalsIgnoreCase("BIRTHDAY")) {
                log.info("Ignoring Timezone difference for birthday comparison");
            } else {
                Assert.fail("Incorrect database state. Difference: " + difference);
            }
        }
    }

    //
    // Private helpers
    //

    private static void setOrganisation(final Group group, final Organisation org) {

        try {
            final Field orgField = Group.class.getDeclaredField("organisation");
            orgField.setAccessible(true);
            orgField.set(group, org);
        } catch (Exception e) {
            throw new IllegalStateException("Could not assign 'organisation' field.", e);
        }
    }

    private void persistOrganisationAndGroups() {

        // Read some data to originate from
        final String data = XmlTestUtils.readFully("testdata/managedOrganisationAndGroups.xml");
        final GroupsAndGuilds groupsAndGuilds = unmarshalFromXML(GroupsAndGuilds.class, data);

        // Populate the database
        dbGroups = new ArrayList<>();
        final Organisation transportOrganisation = groupsAndGuilds.getOrganisations().get(0);
        entityManager.persist(transportOrganisation);
        commitAndStartNewTransaction();

        log.info("Loaded [" + groupsAndGuilds.getGroupsAndGuilds().size() + "] groups.");

        dbOrganisation = entityManager.createQuery(
                "select o from Organisation o order by o.organisationName", Organisation.class)
                .getSingleResult();
        commitAndStartNewTransaction();

        for (Group currentGroup : groupsAndGuilds.getGroupsAndGuilds()) {

            log.debug("Persisting group: " + currentGroup);

            final Organisation mergedOrganisation = entityManager.merge(dbOrganisation);
            setOrganisation(currentGroup, mergedOrganisation);
            entityManager.persist(currentGroup);
            commitAndStartNewTransaction();
        }
    }
}
