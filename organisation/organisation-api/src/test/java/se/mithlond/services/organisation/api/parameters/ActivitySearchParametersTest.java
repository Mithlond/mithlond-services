/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-api
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
				.withEndPeriod(endPeriod.toLocalDateTime())
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
