package se.mithlond.services.shared.spi.algorithms.authorization;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SimpleAuthorizationPathTest {

    @Test
    public void validateParsingAndSorting() {

        // Assemble
        final SimpleAuthorizationPath path1 = new SimpleAuthorizationPath("realm1", "group1");
        final SimpleAuthorizationPath path2 = new SimpleAuthorizationPath("realm2", "group2");
        final SimpleAuthorizationPath path3 = new SimpleAuthorizationPath("realm3", "group3");
        final AuthorizationPath path3_2 = SimpleAuthorizationPath.parse("realm3/group3").first();

        // Act
        final SortedSet<SimpleAuthorizationPath> apSet = new TreeSet<>();
        apSet.addAll(Arrays.asList(path2, path1, path3));
        final SimpleAuthorizationPath[] sapArray = apSet.toArray(new SimpleAuthorizationPath[apSet.size()]);

        // Assert
        Assert.assertSame(path1, sapArray[0]);
        Assert.assertSame(path2, sapArray[1]);
        Assert.assertSame(path3, sapArray[2]);

        Assert.assertEquals(path1, sapArray[0]);
        Assert.assertEquals(path2, sapArray[1]);
        Assert.assertEquals(path3, sapArray[2]);

        Assert.assertNotSame(path3, path3_2);
        Assert.assertEquals(path3, path3_2);
    }
}
