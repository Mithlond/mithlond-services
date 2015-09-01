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

import java.time.ZonedDateTime;
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
