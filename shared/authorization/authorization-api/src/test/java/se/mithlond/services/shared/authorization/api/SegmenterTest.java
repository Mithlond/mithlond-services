/*
 * #%L
 * Nazgul Project: mithlond-services-shared-authorization-api
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
package se.mithlond.services.shared.authorization.api;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SegmenterTest {

    @Test
    public void validateRegexpMatchingTheSuppliedAnyPattern() {

        // Assemble

        // Act & Assert
        Assert.assertTrue("".matches(Segmenter.ANY));
        Assert.assertTrue("åäö".matches(Segmenter.ANY));
        Assert.assertTrue("1".matches(Segmenter.ANY));
        Assert.assertTrue("_".matches(Segmenter.ANY));
        Assert.assertTrue("_högdalen_".matches(Segmenter.ANY));

        Assert.assertFalse("-".matches(Segmenter.ANY));
        Assert.assertFalse(" ".matches(Segmenter.ANY));
        Assert.assertFalse("    ".matches(Segmenter.ANY));
    }

    @Test
    public void validatePatternCreation() {

        // Assemble
        final String prefixPattern = "/forodrim/members/";
        final String patternString = prefixPattern + Segmenter.ANY;
        final Pattern pattern = Pattern.compile(patternString);

        // Act & Assert
        Assert.assertTrue(pattern.matcher(prefixPattern).matches());
        Assert.assertTrue(prefixPattern.matches(patternString));
    }

    @Test
    public void validateSegmentReplacement() {

        // Assemble
        final SortedMap<String, String[]> expected = new TreeMap<>();
        final SortedMap<String, String[]> actual = new TreeMap<>();

        expected.put("/1/2/3", new String[]{"1", "2", "3"});
        expected.put("1/2/3", new String[]{"1", "2", "3"});
        expected.put("//2/3", new String[]{"", "2", "3"});
        expected.put("/ /2/", new String[]{" ", "2", ""});
        expected.put("/ /2", new String[]{" ", "2", ""});
        expected.put("", new String[]{"", "", ""});

        // Act
        for (Map.Entry<String, String[]> current : expected.entrySet()) {
            actual.put(current.getKey(), Segmenter.segment(current.getKey()));
        }

        // Assert
        Assert.assertEquals(expected.size(), actual.size());
        for (Map.Entry<String, String[]> current : expected.entrySet()) {
            Assert.assertArrayEquals(current.getValue(), actual.get(current.getKey()));
        }
    }

    @Test
    public void validatePatternReplacement() {

        // Assemble
        final SortedMap<String, String[]> expected = new TreeMap<>();
        final SortedMap<String, String[]> actual = new TreeMap<>();

        expected.put("/1/2/3", new String[]{"1", "2", "3"});
        expected.put("1/2/3", new String[]{"1", "2", "3"});
        expected.put("//2/3", new String[]{Segmenter.ANY, "2", "3"});
        expected.put("/ /2/", new String[]{Segmenter.ANY, "2", Segmenter.ANY});
        expected.put("/ /2", new String[]{Segmenter.ANY, "2", Segmenter.ANY});
        expected.put("", new String[]{Segmenter.ANY, Segmenter.ANY, Segmenter.ANY});

        // Act
        for (Map.Entry<String, String[]> current : expected.entrySet()) {

            final String[] currentResult =
                    Segmenter.replaceEmptySegmentsWithAnyPattern(
                            Segmenter.segment(current.getKey()), true);

            actual.put(current.getKey(), currentResult);
        }

        // Assert
        Assert.assertEquals(expected.size(), actual.size());
        for (Map.Entry<String, String[]> current : expected.entrySet()) {

            Assert.assertArrayEquals(current.getValue(), actual.get(current.getKey()));
        }
    }

    @Test
    public void validateDetailedPatternReplacement() {

        // Assemble
        final String[] data = new String[]{" ", "2", ""};
        final String[] expectedWithTrimming = new String[]{Segmenter.ANY, "2", Segmenter.ANY};
        final String[] expectedWithoutTrimming = new String[]{" ", "2", Segmenter.ANY};

        // Act
        final String[] resultWithTrimming = Segmenter.replaceEmptySegmentsWithAnyPattern(data, true);
        final String[] resultWithoutTrimming = Segmenter.replaceEmptySegmentsWithAnyPattern(data, false);

        // Assert
        Assert.assertArrayEquals(expectedWithoutTrimming, resultWithoutTrimming);
        Assert.assertArrayEquals(expectedWithTrimming, resultWithTrimming);
    }
}
