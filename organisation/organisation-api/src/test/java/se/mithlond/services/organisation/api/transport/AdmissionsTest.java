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
package se.mithlond.services.organisation.api.transport;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AdmissionsTest extends AbstractPlainJaxbTest {

	// Shared state
	private Admissions admissions;

	@Before
	public void setupSharedState() {

		admissions = new Admissions();

		final SortedSet<AdmissionDetails> details = admissions.getDetails();
		for (int i = 0; i < 5; i++) {
			details.add(new AdmissionDetails((long) (20 + i), "alias_" + i, "organisation_" + i, "note_" + i, i % 3 == 0));
		}
	}

	@Test
	public void validateMarshalling() throws Exception {

		// Assemble
		final String expected = XmlTestUtils.readFully("testdata/admissions.xml");

		// Act
		final String result = marshal(admissions);
		// System.out.println("Got: " + result);

		// Assert
		Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
	}

	@Test
	public void validateUnmarshalling() throws Exception {

		// Assemble
		final String data = XmlTestUtils.readFully("testdata/admissions.xml");
		jaxb.add(Admissions.class);

		// Act
		final Admissions resurrected = unmarshal(Admissions.class, data);

		// Assert
		Assert.assertNotNull(resurrected);
		Assert.assertEquals(5, resurrected.getDetails().size());
	}
}
