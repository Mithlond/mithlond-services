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
