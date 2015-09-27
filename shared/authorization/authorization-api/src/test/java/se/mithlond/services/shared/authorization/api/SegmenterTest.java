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
