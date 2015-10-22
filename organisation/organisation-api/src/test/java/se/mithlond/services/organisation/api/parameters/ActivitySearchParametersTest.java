package se.mithlond.services.organisation.api.parameters;

import org.junit.Assert;
import org.junit.Test;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import java.time.ZonedDateTime;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ActivitySearchParametersTest extends AbstractSearchParametersTest {

	@Test
	public void validateInitialState() {

		// Assemble

		// Act
		final ActivitySearchParameters unitUnderTest = ActivitySearchParameters.builder().build();

		// Assert
		validateEmpty(unitUnderTest.getOrganisationIDs());
		validateEmpty(unitUnderTest.getActivityIDs());
		validateEmpty(unitUnderTest.getMembershipIDs());

		Assert.assertNotNull(unitUnderTest.getEndPeriod());
		Assert.assertEquals(unitUnderTest.getStartPeriod(),
				unitUnderTest.getEndPeriod().minus(ActivitySearchParameters.ActivitySearchParametersBuilder.DEFAULT_PERIOD_SIZE));
		Assert.assertEquals("%", unitUnderTest.getFreeTextSearch());
	}

	@Test
	public void validateParameterBuilding() {

		// Assemble
		final ZonedDateTime endPeriod = TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.parse("2014-02-13 14:23");
		final ActivitySearchParameters.ActivitySearchParametersBuilder builder =
				ActivitySearchParameters.builder();

		// Act
		final ActivitySearchParameters params = builder
				.withOrganisationIDs(5L)
				.withActivityIDs(25L, 65L)
				.withFreeText("foobar")
				.withEndPeriod(endPeriod)
				.build();

		// Assert
		final String stringRepresentation = params.toString();
		Assert.assertTrue(stringRepresentation.contains("[endPeriod       : 2014-02-13 14:23"));
		Assert.assertTrue(stringRepresentation.contains("[startPeriod     : 2013-11-13 14:23"));
		Assert.assertTrue(stringRepresentation.contains("[freeTextSearch  : foobar"));

		Assert.assertEquals(0, params.getMembershipIDs().size());
		validateContent(params.getOrganisationIDs(), 5);
		validateContent(params.getActivityIDs(), 25, 65);
	}
}
