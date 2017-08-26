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

import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.mithlond.services.content.api.report.ExcelReportService;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ExcelReportServiceBeanTest extends AbstractReportTest {

    // Shared state
    private ExcelReportServiceBean unitUnderTest;

    @Before
    public void setupSharedState() {

        createStandardSharedState("generatedExcelFiles");
        unitUnderTest = new ExcelReportServiceBean();
    }

    @Test
    public void validateCreatingWorkbook() {

        // Assemble
        final String theTitle = "Da Titul";
        final String author = "Nazgûl Services Excel Report Generator";

        // Act
        final Workbook result = unitUnderTest.createDocument(memHaxx, theTitle);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof HSSFWorkbook);

        HSSFWorkbook castResult = (HSSFWorkbook) result;

        final SummaryInformation sInfo = castResult.getSummaryInformation();
        Assert.assertNotNull(sInfo);
        Assert.assertEquals(author, sInfo.getAuthor());
        Assert.assertNotNull(sInfo.getCreateDateTime());
        Assert.assertEquals(theTitle, sInfo.getTitle());
    }

    @Test
    public void validateCreatingSheetAndContent() throws Exception {

        // Assemble
        final File targetFile = getTargetFile("ExcelFile", "xls");
        final Workbook workbook = unitUnderTest.createDocument(memHaxx, "SomeDocument");
        final List<String> columnTitles = Arrays.asList("Column_1", "Column_2");

        // Act
        final Sheet firstSheet = unitUnderTest.createStandardExcelSheet(workbook,
                "FooBar",
                "FooBarTitle",
                columnTitles);

        final CellStyle cellStyle = unitUnderTest.getCellStyle(ExcelReportService.ExcelElement.CELL, workbook);
        for (int index = 2; index < 4; index++) {

            final Row theRow = firstSheet.createRow(index);

            unitUnderTest.addCell(0, theRow, "Row_" + index + "_Cell0", cellStyle);
            unitUnderTest.addCell(1, theRow, "Row_" + index + "_Cell1", cellStyle);
        }

        final FileOutputStream outputStream = new FileOutputStream(targetFile);
        outputStream.write(unitUnderTest.convertToByteArray(workbook));

        // Assert
        //
        // Read the document, and verify that cell values match ... ?
    }
}
