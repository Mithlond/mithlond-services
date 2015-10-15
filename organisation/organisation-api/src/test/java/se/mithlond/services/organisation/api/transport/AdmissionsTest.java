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
			details.add(new AdmissionDetails("alias_" + i, "organisation_" + i, "note_" + i, i % 3 == 0));
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
