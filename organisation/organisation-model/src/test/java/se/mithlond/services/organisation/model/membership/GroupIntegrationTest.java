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
import org.dbunit.dataset.IDataSet;
import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.organisation.model.helpers.GroupsAndGuilds;
import se.mithlond.services.shared.test.entity.AbstractIntegrationTest;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class GroupIntegrationTest extends AbstractIntegrationTest {

    // Shared state
    private List<Group> receivedFromClient;

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

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCustomSetup() {

        jaxb.add(GroupsAndGuilds.class);

        // Fake reading some transported data.
        // This occurs when reading / unmarshalling transport model entities from the network.
        final GroupsAndGuilds transported = unmarshal(
                GroupsAndGuilds.class,
                XmlTestUtils.readFully("testdata/managedOrganisationAndGroups.xml"));
        receivedFromClient = transported.getGroupsAndGuilds();
    }

    @Test
    public void validateEntityManagerMergingAndUpdating() throws Exception {

        // Assemble
        final IDataSet expected = performStandardTestDbSetup();

        // Act
        for(Group current : receivedFromClient) {

            // Can't use EntityManager.merge since data was changed during transport.
            // Instead, re-read from database and update using supplied data if required.
            //
            // This illustrates one of the bigger design flaws of JPA - EntityManager.merge
            // cannot be used when data has been changed during transport.
            //
            // *sigh* - pointless much, yes?
            // managedGroups.add(entityManager.merge(current));

            // a) Get the managed Group corresponding to the received/current one.
            final Group currentManaged = entityManager.find(Group.class, current.getId());

            // b) Check the version of the existing and the received object.
            //    Was the database version updated while the transported version was
            boolean notUpdatedInDatabase = currentManaged.getVersion() <= current.getVersion();
            boolean emailListUpdated = !currentManaged.getEmailList().equals(current.getEmailList());

            // c) Update the internal state of the managed object with the state of the transported one.
            if(notUpdatedInDatabase && emailListUpdated) {
                updateEmailList(currentManaged, current.getEmailList());
            }

            // d) Commit the transaction to flush the EntityManager to DB.
            commitAndStartNewTransaction();
        }

        final Group modifiedGroup = entityManager.createNamedQuery(Group.NAMEDQ_GET_BY_NAME_ORGANISATION, Group.class)
                .setParameter(Patterns.PARAM_GROUP_NAME, "groupName_0")
                .setParameter(Patterns.PARAM_ORGANISATION_NAME, "name_0")
                .getSingleResult();

        // Assert
        Assert.assertEquals("modifiedEmailList", modifiedGroup.getEmailList());
        Assert.assertEquals(2, modifiedGroup.getVersion());

        Assertion.assertEquals(expected, iDatabaseConnection.createDataSet());
    }

    //
    // Private helpers
    //

    private void updateEmailList(final Group toUpdate, final String emailList) {

        try {
            final Field emailListField = Group.class.getDeclaredField("emailList");
            emailListField.setAccessible(true);
            emailListField.set(toUpdate, emailList);
        } catch (Exception e) {
            throw new IllegalStateException("Could not updated emailList", e);
        }
    }
}
