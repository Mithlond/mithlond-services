package se.mithlond.services.shared.spi.algorithms.introspector;

import org.junit.Assert;
import org.junit.Test;
import se.mithlond.services.shared.spi.algorithms.introspection.SimpleIntrospector;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SimpleIntrospectorTest {

    @Test
    public void validateCopyingSimpleJavaBeanProperties() {

        // Assemble
        final Long twentyFour = 24L;
        final SimplePropertyHolder from = new SimplePropertyHolder("foo", twentyFour, 42);
        final SimplePropertyHolder to = new SimplePropertyHolder();

        // Act
        SimpleIntrospector.copyJavaBeanProperties(from, to);

        // Assert
        Assert.assertEquals("foo", to.getStringProperty());
        Assert.assertEquals(twentyFour, to.getLongProperty());
        Assert.assertEquals((Integer) 42, to.getIntProperty());
    }
}
