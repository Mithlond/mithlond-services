package se.mithlond.services.shared.spi.jaxb.adapter;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CDataAdapterTest {

    private CDataAdapter unitUnderTest = new CDataAdapter();
    private String markup = "<div><h1>Title</h1><div style='foo'>content</div></div>";
    private String transportForm = CDataAdapter.CDATA_START + markup + CDataAdapter.CDATA_END;
    private String nonCDataTransportForm = "<div><h1>Title</h1><div style='foo'>content</div></div>";

    @Test
    public void validateConvertingToTransportForm() throws Exception {

        // Assemble

        // Act
        final String result = unitUnderTest.marshal(markup);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertNull(unitUnderTest.marshal(null));
        Assert.assertEquals(transportForm, result);
    }

    @Test
    public void validateConvertingFromTransportForm() throws Exception {

        // Assemble

        // Act
        final String result = unitUnderTest.unmarshal(transportForm);
        final String result2 = unitUnderTest.unmarshal(nonCDataTransportForm);

        // Assert
        Assert.assertNull(unitUnderTest.unmarshal(null));
        Assert.assertEquals(markup, result);
        Assert.assertEquals(markup, result2);
    }
}
