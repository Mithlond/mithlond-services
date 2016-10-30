package se.mithlond.services.organisation.impl.ejb;

import org.junit.Assert;
import org.junit.Test;
import se.mithlond.services.organisation.model.activity.EventCalendar;
import se.mithlond.services.organisation.model.membership.Membership;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EventCalendarServiceBeanTest extends AbstractOrganisationIntegrationTest {

    // Shared state
    private EventCalendarServiceBean unitUnderTest;
    private MembershipServiceBean membershipServiceBean;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCustomSetup() {

        // First, handle the standard setup.
        super.doCustomSetup();

        // Create the test unit
        unitUnderTest = new EventCalendarServiceBean();
        membershipServiceBean = new MembershipServiceBean();

        // Inject the EntityManager connected to the in-memory DB.
        injectEntityManager(unitUnderTest);
        injectEntityManager(membershipServiceBean);

        System.setProperty("deployment.type", "Development");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown() {

        System.clearProperty("deployment.type");
    }

    @Test
    public void validateFetching() {

        // Assemble
        final List<String> expectedCalendarIDs = Arrays.asList("mifflondDevCalendar", "mifflondStagingCalendar");
        final Membership bilbo = membershipServiceBean.getMembership(ORG_MIFFLOND, "Bilbo Baggins");

        // Act
        final List<EventCalendar> calendars = unitUnderTest.getCalendars(ORG_MIFFLOND, bilbo);

        // Assert
        Assert.assertEquals(2, calendars.size());
        calendars.forEach(c -> Assert.assertTrue(expectedCalendarIDs.contains(c.getCalendarIdentifier())));
    }
}
