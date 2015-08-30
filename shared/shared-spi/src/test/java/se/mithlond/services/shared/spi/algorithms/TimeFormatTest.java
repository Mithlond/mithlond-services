package se.mithlond.services.shared.spi.algorithms;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.AbstractStandardizedTimezoneTest;

import java.time.ZonedDateTime;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TimeFormatTest extends AbstractStandardizedTimezoneTest {

    // Shared stat
    private ZonedDateTime firstOfMay2014 = ZonedDateTime.of(2014, 5, 1, 13, 15, 0, 0, TimeFormat.SWEDISH_TIMEZONE);
    private SortedMap<TimeFormat, String> actual;

    @Before
    public void setupSharedState() {

        actual = new TreeMap<>();

        for(TimeFormat current : TimeFormat.values()) {
            actual.put(current, current.print(firstOfMay2014));
        }
    }

    @Test
    public void validateFormatting() {

        // Assemble
        final SortedMap<TimeFormat, String> expected = new TreeMap<>();
        expected.put(TimeFormat.DAY_OF_WEEK_AND_DATE, "torsdag 2014-05-01");
        expected.put(TimeFormat.YEAR_MONTH_DATE, "2014-05-01");
        expected.put(TimeFormat.HOURS_MINUTES, "13:15");
        expected.put(TimeFormat.XML_TRANSPORT, "2014-05-01T13:15:00+0200");

        // Act

        // Assert
        Assert.assertEquals(expected.size(), actual.size());

        for(Map.Entry<TimeFormat, String> current : expected.entrySet()) {
            Assert.assertEquals(current.getValue(), actual.get(current.getKey()));
        }
    }
}
