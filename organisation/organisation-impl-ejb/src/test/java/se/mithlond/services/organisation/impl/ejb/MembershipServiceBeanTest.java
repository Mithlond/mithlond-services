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

import org.junit.Assert;
import org.junit.Test;
import se.mithlond.services.organisation.model.membership.Membership;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MembershipServiceBeanTest extends AbstractOrganisationIntegrationTest {

    // Shared state
    private MembershipServiceBean unitUnderTest;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCustomSetup() {

        // First, handle the standard setup.
        super.doCustomSetup();

        // Create the test unit
        unitUnderTest = new MembershipServiceBean();

        // Inject the EntityManager connected to the in-memory DB.
        injectEntityManager(unitUnderTest);
    }

    @Test
    public void validateMembershipSelection() throws Exception {

        // Assemble

        // Act
        final List<Membership> allanMithlondMemberships = unitUnderTest.getActiveMemberships(
                ORG_MIFFLOND, "Allan", "Octamac");
        final List<Membership> allanFjodjimMemberships = unitUnderTest.getActiveMemberships(
                ORG_FJODJIM, "Allan", "Octamac");
        Assert.assertEquals(1, allanMithlondMemberships.size());
        Assert.assertEquals(1, allanFjodjimMemberships.size());

        final List<Membership> noonesActiveMithlondMemberships = unitUnderTest.getActiveMemberships(
                ORG_MIFFLOND, "Mr", "Noone");
        final List<Membership> noonesActiveFjodjimsMemberships = unitUnderTest.getActiveMemberships(
                ORG_FJODJIM, "Mr", "Noone");
        Assert.assertEquals(0, noonesActiveMithlondMemberships.size());
        Assert.assertEquals(0, noonesActiveFjodjimsMemberships.size());

        final List<Membership> mifflondActiveMemberships = unitUnderTest.getMembershipsIn(MIFFLOND_JPA_ID, false);
        Assert.assertEquals(3, mifflondActiveMemberships.size());
        final Set<String> activeMifflondAliases = mifflondActiveMemberships
                .stream()
                .map(Membership::getAlias)
                .collect(Collectors.toSet());

        final Membership bilboMifflondMembership = unitUnderTest.getMembership(ORG_MIFFLOND, "Bilbo Baggins");

        // Assert
        Assert.assertEquals("Bilbo Baggins", allanMithlondMemberships.get(0).getAlias());
        Assert.assertEquals("Aragorn", allanFjodjimMemberships.get(0).getAlias());

        Assert.assertTrue(activeMifflondAliases.containsAll(
                Stream.of("Bilbo Baggins", "Dildo Baggins", "Gromp").collect(Collectors.toList())));

        Assert.assertNotNull(bilboMifflondMembership);
        Assert.assertEquals("bilbo", bilboMifflondMembership.getEmailAlias());
    }

    @Test
    public void validateNoExceptionWhenRequestingMembershipsInNonexistentOrganisation() {

        // Act
        final Membership imaginaryMembership = unitUnderTest.getMembership("Nah", "Bilboo");
        final Membership imaginaryFjodjimMembership = unitUnderTest.getMembership(ORG_FJODJIM, "Bilboo");

        final List<Membership> activeImaginaryMemberships = unitUnderTest.getActiveMemberships(
                "Unknown Organisation", "Allan", "Octamac");
        final List<Membership> allImaginaryMemberships = unitUnderTest.getMembershipsIn(-24L, true);

        // Assert
        Assert.assertNotNull(allImaginaryMemberships);
        Assert.assertTrue(allImaginaryMemberships.isEmpty());

        Assert.assertNotNull(activeImaginaryMemberships);
        Assert.assertTrue(activeImaginaryMemberships.isEmpty());

        Assert.assertNull(imaginaryMembership);
        Assert.assertNull(imaginaryFjodjimMembership);
    }
}
