/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-impl-google
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
package se.mithlond.services.organisation.impl.google;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.activity.EventCalendar;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import java.time.LocalDateTime;
import java.time.Month;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class GoogleCalendarConvertersTest {

    // Shared state
    private JsonFactory factory;
    private Organisation mifflond;
    private Address address;
    private LocalDateTime secondNovember2016;

    @Before
    public void setupSharedState() {

        // Create the JsonFactory 
        this.factory = new JacksonFactory();

        // Create data
        address = new Address("careOfLine",
                "departmentName",
                "street",
                "number",
                "city",
                "zipCode",
                "country",
                "description");
        
        mifflond = new Organisation("name",
                "suffix",
                "phone",
                "bankAccountInfo",
                "postAccountInfo",
                address,
                "emailSuffix",
                TimeFormat.SWEDISH_TIMEZONE.normalized(),
                TimeFormat.SWEDISH_LOCALE,
                WellKnownCurrency.SEK);

        secondNovember2016 = LocalDateTime.of(2016, Month.NOVEMBER, 2, 15, 32);
    }

    @Test
    public void validateConvertingEventCalendar() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/syntheticGoogleCalendar.json");
        final EventCalendar eventCalendar = new EventCalendar(
                "eventCalendarShortDesc",
                "eventCalendarFullDesc",
                mifflond,
                "someCalendar",
                "Staging");

        // Act
        final Calendar calendar = GoogleCalendarConverters.convert(eventCalendar);
        calendar.setFactory(factory);
        final String result = calendar.toPrettyString();
        // System.out.println("Got: " + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void validateConvertingLocalDates() throws Exception {

        // Assemble
        final long expectedNumberOfMillis = 1478097120000L;

        // Act
        final DateTime dateTime = GoogleCalendarConverters.convert(secondNovember2016);
        final long numberOfMilliseconds = dateTime.getValue();
        final String rfc3339 = dateTime.toStringRfc3339();

        // System.out.println("Got: " + rfc3339);

        // Assert
        Assert.assertEquals(expectedNumberOfMillis, numberOfMilliseconds);
        Assert.assertTrue(rfc3339.startsWith("2016-11-02T15:32:00"));
    }
}
