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

import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractSearchParametersTest {

	protected void validateContent(final List<Long> list, final int... expected) {
		Assert.assertNotNull(list);

		for (int current : expected) {
			Assert.assertTrue(list.contains((long) current));
		}
	}

	protected void validateEmpty(final Collection<?> collection) {
		Assert.assertNotNull(collection);
		Assert.assertEquals(0, collection.size());
	}
}
