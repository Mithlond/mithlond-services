/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
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
package se.mithlond.services.shared.spi.algorithms;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.AbstractStandardizedTimezoneTest;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TimeFormatTest extends AbstractStandardizedTimezoneTest {

    // Shared state
    private ZonedDateTime firstOfMay2014 = ZonedDateTime.of(2014, 5, 1, 13, 15, 0, 0, TimeFormat.SWEDISH_TIMEZONE);
    private SortedMap<TimeFormat, String> actual;

    @Before
    public void setupSharedState() {

        actual = new TreeMap<>();

        for (TimeFormat current : TimeFormat.values()) {
            actual.put(current, current.print(firstOfMay2014));
        }
    }

    @Test
    public void validateFormatting() {

        // Assemble
        final SortedMap<TimeFormat, String> expected = new TreeMap<>();
        expected.put(TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES, "2014-05-01 13:15");
        expected.put(TimeFormat.DAY_OF_WEEK_AND_DATE, "torsdag 2014-05-01");
        expected.put(TimeFormat.YEAR_MONTH_DATE, "2014-05-01");
        expected.put(TimeFormat.HOURS_MINUTES, "13:15");
        expected.put(TimeFormat.XML_TRANSPORT, "2014-05-01T13:15:00+0200");
        expected.put(TimeFormat.COMPACT_LOCALDATE, "20140501");
        expected.put(TimeFormat.COMPACT_LOCALDATETIME, "20140501131500");

        // Act

        // Assert
        Assert.assertEquals(expected.size(), actual.size());

        for (Map.Entry<TimeFormat, String> current : expected.entrySet()) {
            Assert.assertEquals(current.getValue(), actual.get(current.getKey()));
        }
    }

    @Test
    public void validateParsing() {

        // Assemble
        final SortedMap<TimeFormat, String> data = new TreeMap<>();
        data.put(TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES, "2014-05-01 13:15");
        data.put(TimeFormat.DAY_OF_WEEK_AND_DATE, "torsdag 2014-05-01");
        data.put(TimeFormat.YEAR_MONTH_DATE, "2014-05-01");
        data.put(TimeFormat.HOURS_MINUTES, "13:15");
        data.put(TimeFormat.XML_TRANSPORT, "2014-05-01T13:15:00+0200");
        data.put(TimeFormat.COMPACT_LOCALDATE, "20140501");
        data.put(TimeFormat.COMPACT_LOCALDATETIME, "20140501131500");

        final SortedMap<TimeFormat, String> expected = new TreeMap<>();
        expected.put(TimeFormat.DAY_OF_WEEK_AND_DATE, "2014-05-01");
        expected.put(TimeFormat.YEAR_MONTH_DATE, "2014-05-01");
        expected.put(TimeFormat.COMPACT_LOCALDATE, "2014-05-01");

        expected.put(TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES, "2014-05-01T13:15");
        expected.put(TimeFormat.COMPACT_LOCALDATETIME, "2014-05-01T13:15");
        expected.put(TimeFormat.XML_TRANSPORT, "2014-05-01T13:15+02:00[Europe/Stockholm]");
        expected.put(TimeFormat.HOURS_MINUTES, "13:15");

        // Act
        final SortedMap<TimeFormat, ? extends TemporalAccessor> result = new TreeMap<>();
        data.forEach((k, v) -> result.put(k, k.parse(v)));

        // Assert
        Assert.assertEquals(data.size(), result.size());
        result.forEach((k, v) -> Assert.assertTrue("Expected type [" + k.getExpectedParseResultType().getSimpleName()
                        + "], but got [" + v.getClass().getSimpleName() + "]",
                k.getExpectedParseResultType().isAssignableFrom(v.getClass())));

        result.forEach((k, v) -> Assert.assertEquals(v.toString(), expected.get(k)));
    }
}
