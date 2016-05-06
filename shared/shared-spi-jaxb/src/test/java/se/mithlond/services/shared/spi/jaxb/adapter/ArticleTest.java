package se.mithlond.services.shared.spi.jaxb.adapter;

import org.junit.Assert;
import org.junit.Test;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ArticleTest extends AbstractPlainJaxbTest {

    private String markup = "<div><h1>Title</h1><div style='foo'>content</div></div>";
    private String transportForm = CDataAdapter.CDATA_START + markup + CDataAdapter.CDATA_END;

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final ArticleExampleVO article = new ArticleExampleVO(markup);

        // Act
        final String marshalled = marshalToXML(article);
        System.out.println("Got: " + marshalled);

        // Assert
        Assert.assertNotNull(marshalled);

    }
}
