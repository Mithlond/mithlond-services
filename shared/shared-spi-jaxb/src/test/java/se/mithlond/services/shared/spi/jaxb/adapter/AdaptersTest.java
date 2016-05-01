/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-jaxb
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
package se.mithlond.services.shared.spi.jaxb.adapter;

import org.junit.Assert;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.TimeZone;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AdaptersTest extends AbstractPlainJaxbTest {

    // Shared state
    private LocalDate lastAdmissionDate = LocalDate.of(2016, Month.FEBRUARY, 5);
    private LocalTime eventEndTime = LocalTime.of(23, 26);
    private LocalDateTime eventStartTime = LocalDateTime.of(
            LocalDate.of(2016, Month.MARCH, 4),
            LocalTime.of(18, 15));
    private ZonedDateTime admissionTime = ZonedDateTime.of(
            LocalDateTime.of(LocalDate.of(2015, Month.FEBRUARY, 2),
                    LocalTime.of(19,43)),
            TimeFormat.SWEDISH_TIMEZONE);
    private TimeZone SWEDISH_TZ = TimeZone.getTimeZone(TimeFormat.SWEDISH_TIMEZONE);

    private DateExampleVO unitUnderTest = new DateExampleVO(
            lastAdmissionDate,
            eventStartTime,
            eventEndTime,
            admissionTime,
            SWEDISH_TZ);

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/date_example_vo.xml");
        jaxb.add(DateExampleVO.class);

        // Act
        final String result = marshalToXML(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/date_example_vo.xml");
        jaxb.add(DateExampleVO.class);

        // Act
        final DateExampleVO resurrected = unmarshalFromXML(DateExampleVO.class, data);

        // Assert
        Assert.assertNotNull(resurrected);
        Assert.assertEquals(lastAdmissionDate, resurrected.getLastAdmissionDate());
        Assert.assertEquals(admissionTime, resurrected.getAdmissionTime());
        Assert.assertEquals(eventStartTime, resurrected.getEventStartTime());
        Assert.assertEquals(eventEndTime, resurrected.getEventEndTime());
        Assert.assertEquals(SWEDISH_TZ, resurrected.getEventTimeZone());
    }
}
