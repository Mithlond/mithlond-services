/*
 * #%L
 * Nazgul Project: mithlond-services-shared-entity-test
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
package se.mithlond.services.shared.test.entity;

import org.junit.Assert;
import org.junit.Test;
import se.mithlond.services.shared.test.entity.helpers.FooBar;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class JpaIdMutatorTest {

	@Test
	public void validateMutatingJpaID() {

		// Assemble
		final FooBar fooBar1 = new FooBar("foobar1");
		final FooBar fooBar2 = new FooBar("foobar1");

		// Act
		JpaIdMutator.setId(fooBar1, 4);

		// Assert
		Assert.assertEquals(0, fooBar2.getId());
		Assert.assertEquals(4, fooBar1.getId());
	}
}
