/*-
 * #%L
 * Nazgul Project: mithlond-services-organisation-impl-ejb
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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
import org.junit.Ignore;
import org.junit.Test;
import se.mithlond.services.organisation.api.parameters.FoodAndAllergySearchParameters;
import se.mithlond.services.organisation.model.food.Allergy;
import se.mithlond.services.organisation.model.membership.Membership;

import java.util.SortedMap;
import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Ignore("... for now ...")
public class FoodAndAllergyIntegrationTest extends AbstractOrganisationIntegrationTest {

    private FoodAndAllergyServiceBean unitUnderTest;
    private MembershipServiceBean membershipServiceBean;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCustomSetup() {

        // First, handle the standard setup.
        super.doCustomSetup();

        // Create the test unit
        unitUnderTest = new FoodAndAllergyServiceBean();
        membershipServiceBean = new MembershipServiceBean();

        // Inject the EntityManager connected to the in-memory DB.
        injectEntityManager(unitUnderTest);
        injectEntityManager(membershipServiceBean);
    }

    @Test
    public void validateFindingAllergies() {

        // Assemble
        final FoodAndAllergySearchParameters params = FoodAndAllergySearchParameters.builder()
                .withOrganisationIDs(MIFFLOND_JPA_ID)
                .withLoginOnly(true)
                .build();

        // Act
        final SortedMap<Membership, SortedSet<Allergy>> result = unitUnderTest.getAllergiesFor(params);

        // Assert
        printCurrentDatabaseState();
        Assert.assertNotNull(result);
    }
}
