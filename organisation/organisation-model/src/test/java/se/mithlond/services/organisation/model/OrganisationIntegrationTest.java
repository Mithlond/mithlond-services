package se.mithlond.services.organisation.model;

import org.dbunit.Assertion;
import org.dbunit.dataset.IDataSet;
import org.junit.Assert;
import org.junit.Test;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.shared.test.entity.AbstractIntegrationTest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class OrganisationIntegrationTest extends AbstractIntegrationTest {

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
    public void validateFindingAndUpdatingOrganisations() throws Exception {

        // Assemble
        final IDataSet expected = performStandardTestDbSetup();
        final List<String> organisationNames = new ArrayList<>();
        final List<Long> organisationIDs = new ArrayList<>();

        // Act
        final List<Organisation> resultList = entityManager.createNamedQuery(
                Organisation.NAMEDQ_GET_ALL, Organisation.class).getResultList();
        resultList.forEach(current -> {
            organisationNames.add(current.getOrganisationName());
            organisationIDs.add(current.getId());
        });

        // Assert
        Assert.assertEquals(3, organisationIDs.size());
        Assert.assertEquals(3, organisationNames.size());

        for (int i = 0; i < organisationIDs.size(); i++) {
            Assert.assertEquals((long) 1 + i, (long) organisationIDs.get(i));
        }
        for (int i = 0; i < organisationNames.size(); i++) {
            Assert.assertEquals("name_" + i, organisationNames.get(i));
        }

        // Act #2
        final List<Organisation> resultList2 = entityManager.createNamedQuery(
                Organisation.NAMEDQ_GET_BY_NAME,
                Organisation.class)
                .setParameter(Patterns.PARAM_ORGANISATION_NAME, "%_2")
                .getResultList();

        // Assert #2
        Assert.assertEquals(1, resultList2.size());
        Assert.assertEquals("name_2", resultList2.get(0).getOrganisationName());

        // Act #3
        final Organisation org2 = resultList2.get(0);
        final List<Group> groupsInOrg2 = entityManager.createNamedQuery(Group.NAMEDQ_GET_BY_ORGANISATION, Group.class)
                .setParameter(Patterns.PARAM_ORGANISATION_NAME, org2.getOrganisationName())
                .getResultList();

        /*
        Group [groupName_2] with Parent Group [groupName_1]
        Group [guildName_2] without Parent Group.
         */
        final Map<String, Group> groupName2GroupMap = new TreeMap<>();
        for(Group current : groupsInOrg2) {
            groupName2GroupMap.put(current.getGroupName(), current);
        }
        updateName(groupName2GroupMap.get("groupName_2"), "updatedGroupName_2");
        commitAndStartNewTransaction();

        // Dig out the current database state
        // printCurrentDatabaseState();
        Assertion.assertEquals(expected, iDatabaseConnection.createDataSet());
    }

    //
    // Private helpers
    //

    private void updateName(final Group toUpdate, final String newName) {

        try {
            final Field groupNameField = Group.class.getDeclaredField("groupName");
            groupNameField.setAccessible(true);
            groupNameField.set(toUpdate, newName);
        } catch (Exception e) {
            throw new IllegalStateException("Could not update groupName", e);
        }
    }
}
