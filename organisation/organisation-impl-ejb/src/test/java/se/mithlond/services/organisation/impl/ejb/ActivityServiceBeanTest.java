package se.mithlond.services.organisation.impl.ejb;

import org.junit.Assert;
import org.junit.Test;
import se.mithlond.services.organisation.api.parameters.ActivitySearchParameters;
import se.mithlond.services.organisation.model.Listable;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.transport.activity.Activities;
import se.mithlond.services.organisation.model.transport.activity.ActivityVO;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ActivityServiceBeanTest extends AbstractOrganisationIntegrationTest {

    // Shared state
    private ActivityServiceBean unitUnderTest;
    private MembershipServiceBean membershipServiceBean;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCustomSetup() {

        // First, handle the standard setup.
        super.doCustomSetup();

        // Create the test unit
        unitUnderTest = new ActivityServiceBean();
        membershipServiceBean = new MembershipServiceBean();

        // Inject the EntityManager connected to the in-memory DB.
        injectEntityManager(unitUnderTest);
        injectEntityManager(membershipServiceBean);
    }

    @Test
    public void validateAddingAdmissions() throws Exception {

        // Assemble
        // System.out.println("Got: \n" + extractFlatXmlDataSet(iDatabaseConnection.createDataSet()));
        final Membership zap = membershipServiceBean.getMembership(ORG_FJODJIM, "Zap");

        final ActivitySearchParameters shallowSearchParams = ActivitySearchParameters.builder()
                .withOrganisationIDs(FJODJIM_JPA_ID)
                .withStartPeriod(LocalDateTime.of(2016, Month.OCTOBER, 16, 0, 0)) // Should match "Shieldpainting" only.
                .build();
        final ActivitySearchParameters detailSearchParams = ActivitySearchParameters.builder()
                .withOrganisationIDs(FJODJIM_JPA_ID)
                .withStartPeriod(LocalDateTime.of(2016, Month.SEPTEMBER, 16, 0, 0))
                // Should match "Shieldpainting" and "Svärdsfäktning".
                .withDetailedResponsePreferred(true)
                .build();

        // Act
        final Activities shallowActivities = unitUnderTest.getActivities(shallowSearchParams, zap);
        final Activities detailActivities = unitUnderTest.getActivities(detailSearchParams, zap);

        // Assert
        Assert.assertNotNull(shallowActivities);
        Assert.assertEquals(0, shallowActivities.getActivities().size());
        Assert.assertEquals(1, shallowActivities.getActivityVOs().size());

        Assert.assertNotNull(detailActivities);
        Assert.assertEquals(2, detailActivities.getActivities().size());
        Assert.assertEquals(0, detailActivities.getActivityVOs().size());

        final ActivityVO activityVO = shallowActivities.getActivityVOs().get(0);
        final String shallowActivityShortDesc = activityVO.getShortDesc();
        Assert.assertEquals("Sköldmålning", shallowActivityShortDesc);

        final Map<String, Activity> detailedMap = detailActivities.getActivities()
                .stream()
                .collect(Collectors.toMap(Listable::getShortDesc, a -> a));

        Assert.assertTrue(detailedMap.containsKey("Svärdsfäktning"));
        Assert.assertTrue(detailedMap.containsKey("Sköldmålning"));
    }
}
