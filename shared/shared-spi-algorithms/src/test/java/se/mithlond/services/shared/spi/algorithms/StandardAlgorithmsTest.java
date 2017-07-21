/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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
package se.mithlond.services.shared.spi.algorithms;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StandardAlgorithmsTest {

    @Test
    public void validateTrimmingAndHarmonizingWhitespace() {

        // Assemble
        final String expected = "This is plain.";
        final List<String> sources = Arrays.asList(" This is plain. ",
                "This   is    plain.",
                "   This    is  plain.",
                " This     is      plain.");

        // Assert
        sources.forEach(str -> Assert.assertEquals(expected, StandardAlgorithms.trimAndHarmonizeWhitespace(str)));
    }

    @Test
    public void validateNullPassThrough() {

        Assert.assertEquals(null, StandardAlgorithms.trimAndHarmonizeWhitespace(null));
    }
}
