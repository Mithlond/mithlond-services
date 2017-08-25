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
