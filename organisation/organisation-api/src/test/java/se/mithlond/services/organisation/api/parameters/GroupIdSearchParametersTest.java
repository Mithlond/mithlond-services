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

import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class GroupIdSearchParametersTest extends AbstractSearchParametersTest {

	@Test
	public void validateInitialState() {

		// Assemble

		// Act
		final GroupIdSearchParameters unitUnderTest = GroupIdSearchParameters
				.builder()
				.build();

		// Assert
		validateEmpty(unitUnderTest.getClassifierIDs());
		validateEmpty(unitUnderTest.getOrganisationIDs());
		validateEmpty(unitUnderTest.getGroupIDs());
	}

	@Test
	public void validateParameterBuilding() {

		// Assemble
		final GroupIdSearchParameters.GroupIdSearchParametersBuilder builder =
				GroupIdSearchParameters.builder();

		// Act
		final GroupIdSearchParameters params = builder
				.withClassifierIDs(2L, 5L, 3L)
				.withGroupIDs(6L, 7L, 8L)
				.withOrganisationIDs(253L)
				.build();

		// Assert
		final String stringRepresentation = params.toString();
		Assert.assertTrue(stringRepresentation.contains("[classifierIDs   : [2, 5, 3]"));

		validateContent(params.getClassifierIDs(), 2, 3, 5);
		validateContent(params.getGroupIDs(), 6, 7, 8);
		validateContent(params.getOrganisationIDs(), 253);
	}
}
