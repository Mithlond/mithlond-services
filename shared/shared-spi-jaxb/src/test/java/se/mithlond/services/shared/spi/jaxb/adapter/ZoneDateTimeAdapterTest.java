package se.mithlond.services.shared.spi.jaxb.adapter;

import org.junit.Assert;
import org.junit.Test;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZonedDateTime;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ZoneDateTimeAdapterTest {

    private String transportForm = "2015-04-25T15:30:00+02:00[Europe/Stockholm]";
    private ZonedDateTime objectForm = ZonedDateTime.of(
            LocalDate.of(2015, Month.APRIL, 25),
            LocalTime.of(15, 30, 0),
            TimeFormat.SWEDISH_TIMEZONE);
    private ZonedDateTimeAdapter unitUnderTest = new ZonedDateTimeAdapter();

    @Test
    public void validateConvertingToTransportForm() throws Exception {

        // Assemble

        // Act
        final String result = unitUnderTest.marshal(objectForm);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertNull(unitUnderTest.marshal(null));
        Assert.assertEquals(transportForm, result);
    }

    @Test
    public void validateConvertingFromTransportForm() throws Exception {

        // Assemble

        // Act
        final ZonedDateTime result = unitUnderTest.unmarshal(transportForm);

        // Assert
        Assert.assertNull(unitUnderTest.unmarshal(null));
        Assert.assertEquals(objectForm, result);
    }
}
