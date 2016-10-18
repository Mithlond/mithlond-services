/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-impl-ejb
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
package se.mithlond.services.organisation.impl.ejb;

import com.sun.deploy.perf.PerfHelper;
import org.dbunit.dataset.IDataSet;
import org.joda.time.DateTimeZone;
import org.junit.Assert;
import org.junit.Test;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;
import se.mithlond.services.shared.spi.jpa.PersistenceOperationFailedException;
import se.mithlond.services.shared.test.entity.AbstractIntegrationTest;

import java.lang.reflect.Field;
import java.util.List;
import java.util.TimeZone;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MembershipServiceBeanTest extends AbstractIntegrationTest {

    // Shared state
    private MembershipServiceBean unitUnderTest;

    /**
     * Default constructor, setting a Swedish DateTimeZone.
     */
    public MembershipServiceBeanTest() {
        super(DateTimeZone.forTimeZone(TimeZone.getTimeZone(TimeFormat.SWEDISH_TIMEZONE)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCustomSetup() {

        // Prime the PersistenceHelper, and setup the database
        PersistenceHelper.setEntityManager(entityManager);
        PersistenceHelper.doStandardSetup();
        commitAndStartNewTransaction();
        PersistenceHelper.setEntityManager(entityManager);

        unitUnderTest = new MembershipServiceBean();

        // Inject the EntityManager into the MembershipServiceBean.
        try {
            Field entityManagerField = AbstractJpaService.class.getDeclaredField("entityManager");
            entityManagerField.setAccessible(true);
            entityManagerField.set(unitUnderTest, entityManager);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not inject EntityManager into MembershipServiceBean.", e);
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
    public void validateSelection() throws Exception {

        // Assemble
        final String mifflond = "Mifflond";
        final String fjodjim = "Fjodjim";
        // System.out.println("Got:\n" + extractFlatXmlDataSet(iDatabaseConnection.createDataSet()));

        // Act
        final List<Membership> allanMithlondMemberships = unitUnderTest.getActiveMemberships(mifflond, "Allan", "Octamac");
        final List<Membership> allanFjodjimMemberships = unitUnderTest.getActiveMemberships(fjodjim, "Allan", "Octamac");
        Assert.assertEquals(1, allanMithlondMemberships.size());
        Assert.assertEquals(1, allanFjodjimMemberships.size());

        final List<Membership> noonesMithlondMemberships = unitUnderTest.getActiveMemberships(mifflond, "Mr", "Noone");
        final List<Membership> noonesFjodjimsMemberships = unitUnderTest.getActiveMemberships(fjodjim, "Mr", "Noone");
        Assert.assertEquals(0, noonesMithlondMemberships.size());
        Assert.assertEquals(0, noonesFjodjimsMemberships.size());

        // Assert
        Assert.assertEquals("Bilbo Baggins", allanMithlondMemberships.get(0).getAlias());
        Assert.assertEquals("Aragorn", allanFjodjimMemberships.get(0).getAlias());
    }
}
