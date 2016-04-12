package se.mithlond.services.shared.spi.algorithms.diff;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DiffHolderTest {

    @Test
    public void validateDiffAlgorithm() {

        // Assemble
        final DebugDiffHolder createdHolder = new DebugDiffHolder(null, new StringBuffer("comparison2"));
        final DebugDiffHolder deletedHolder = new DebugDiffHolder("actual", null);
        final DebugDiffHolder unknownHolder = new DebugDiffHolder(null, null);
        final DebugDiffHolder modifiedHolder = new DebugDiffHolder("something", new StringBuffer("somewhat"));

        // Act & Assert
        Assert.assertEquals(DiffHolder.Modification.MODIFIED, modifiedHolder.getModification());
        Assert.assertEquals(DiffHolder.Modification.CREATED, createdHolder.getModification());
        Assert.assertEquals(DiffHolder.Modification.DELETED, deletedHolder.getModification());
        Assert.assertEquals(DiffHolder.Modification.UNKNOWN, unknownHolder.getModification());
    }
}
