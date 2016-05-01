package se.mithlond.services.shared.spi.jaxb.adapter;

import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.Month;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalDateAdapterTest {

    private String transportForm = "2015-04-25";
    private LocalDate objectForm = LocalDate.of(2015, Month.APRIL, 25);
    private LocalDateAdapter unitUnderTest = new LocalDateAdapter();

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
        final LocalDate result = unitUnderTest.unmarshal(transportForm);

        // Assert
        Assert.assertNull(unitUnderTest.unmarshal(null));
        Assert.assertEquals(objectForm, result);
    }
}
