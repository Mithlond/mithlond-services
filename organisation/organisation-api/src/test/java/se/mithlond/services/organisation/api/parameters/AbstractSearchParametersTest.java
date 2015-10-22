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
