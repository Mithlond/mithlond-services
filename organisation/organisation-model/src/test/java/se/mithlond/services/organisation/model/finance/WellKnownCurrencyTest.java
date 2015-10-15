/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.mithlond.services.organisation.model.finance;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class WellKnownCurrencyTest {

	@Test
	public void validateValues() {

		for (WellKnownCurrency current : WellKnownCurrency.values()) {
			Assert.assertNotNull(current.getCurrencyNamePlural());
			Assert.assertNotNull(current.getCurrencyNameSingular());
			Assert.assertNotNull(current.getCurrencySymbol());
		}
	}

	@Test
	public void validateInternalState() {

		// Assemble
		final WellKnownCurrency unitUnderTest = WellKnownCurrency.SEK;

		// Act
		final String currencySymbol = unitUnderTest.getCurrencySymbol();
		final String currencyNameSingular = unitUnderTest.getCurrencyNameSingular();
		final String currencyNamePlural = unitUnderTest.getCurrencyNamePlural();

		// Assert
		Assert.assertEquals("krona", currencyNameSingular);
		Assert.assertEquals("kronor", currencyNamePlural);
		Assert.assertEquals("kr", currencySymbol);
	}
}
