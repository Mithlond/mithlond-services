package se.mithlond.services.content.impl.ejb.report;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PdfReportServiceBeanTest extends AbstractReportTest {

    // Shared state
    private PdfReportServiceBean unitUnderTest;

    @Before
    public void setupSharedState() {

        createStandardSharedState("generatedPdfFiles");
        unitUnderTest = new PdfReportServiceBean();
    }

    @Test
    public void validateCreatingDocument() {

        // Assemble

        // Act
        final PDDocument result = unitUnderTest.createDocument(memHaxx, "TestDocument");

        // Assert
        Assert.assertNotNull(result);

        final PDDocumentInformation docInfo = result.getDocumentInformation();
        Assert.assertNotNull(docInfo);
        Assert.assertEquals(memHaxx.getAlias(), docInfo.getAuthor());
        Assert.assertNotNull(docInfo.getCreationDate());
    }
}
