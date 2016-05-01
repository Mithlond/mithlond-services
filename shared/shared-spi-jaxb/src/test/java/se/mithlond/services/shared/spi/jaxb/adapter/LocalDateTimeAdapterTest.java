package se.mithlond.services.shared.spi.jaxb.adapter;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalDateTimeAdapterTest {

    private String transportForm = "2015-04-25T15:40:00";
    private LocalDateTime objectForm = LocalDateTime.of(
            LocalDate.of(2015, Month.APRIL, 25),
            LocalTime.of(15, 40, 0));
    private LocalDateTimeAdapter unitUnderTest = new LocalDateTimeAdapter();

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
        final LocalDateTime result = unitUnderTest.unmarshal(transportForm);

        // Assert
        Assert.assertNull(unitUnderTest.unmarshal(null));
        Assert.assertEquals(objectForm, result);
    }
}
