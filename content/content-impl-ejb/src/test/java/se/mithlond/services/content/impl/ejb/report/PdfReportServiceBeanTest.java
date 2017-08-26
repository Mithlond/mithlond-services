/*-
 * #%L
 * Nazgul Project: mithlond-services-content-impl-ejb
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
