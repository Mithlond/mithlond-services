/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
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
