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
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.JpaIdMutator;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ExcelReportServiceBeanTest {

    // Shared state
    private Organisation organisation;
    private User haxx, erion;
    private Membership memHaxx, memErion;
    private ExcelReportServiceBean unitUnderTest;
    private File targetDir;

    @Before
    public void setupSharedState() {

        final Category addressCategory = new Category("Visiting address", "visiting_address",
                "Address for visiting the organisation");
        final Address location = new Address(null, null, "Foo Street",
                "5",
                "Göteborg",
                "253 54",
                "Sverige",
                "Visiting address");

        organisation = new Organisation("The Organisation",
                "The Tolkien Society of Kinnekulle",
                null,
                "0123-234211",
                "02515-2325232-2323",
                new Address(null, null, "Kinnekullegatan", "54 C", "Kinnekulle", "142 41", "Sverige",
                        "Visiting address"),
                "kinnekulle.tolkien.se",
                TimeFormat.SWEDISH_TIMEZONE,
                TimeFormat.SWEDISH_LOCALE,
                WellKnownCurrency.SEK);
        JpaIdMutator.setId(organisation, 1);

        // Create some users
        haxx = new User(
                "Mr",
                "Häxxmästaren",
                LocalDate.of(1968, Month.SEPTEMBER, 17),
                (short) 1235,
                new Address(null,
                        null,
                        "Testgatan",
                        "45 E",
                        "Grååååbo",
                        "234 54",
                        "Sverige",
                        "Hemma hos Mr Häxx"),
                null,
                null,
                "dasToken");
        JpaIdMutator.setId(haxx, 42);

        erion = new User(
                "Das",
                "Erion",
                LocalDate.of(1922, Month.FEBRUARY, 5),
                (short) 2345,
                new Address(null,
                        null,
                        "Yttertestgatan",
                        "25",
                        "Göteborg",
                        "411 11",
                        "Sverige",
                        "Hemma hos Das Erion"),
                null,
                null,
                "dasErionToken");
        JpaIdMutator.setId(erion, 43);

        memHaxx = new Membership("Häxx",
                "Das Filuro",
                "haxx",
                true,
                haxx,
                organisation);
        memErion = new Membership("Erion",
                null,
                "erion",
                true,
                erion,
                organisation);
        JpaIdMutator.setId(memHaxx, 101);
        JpaIdMutator.setId(memErion, 102);

        unitUnderTest = new ExcelReportServiceBean();

        final String path = getClass().getClassLoader()
                .getResource("testdata")
                .getPath();
        File targetParentDir = new File(path);
        Assert.assertTrue(targetParentDir.exists());
        Assert.assertTrue(targetParentDir.isDirectory());

        this.targetDir = new File(targetParentDir, "generatedExcelFiles");
        if(!targetDir.exists()) {
            targetDir.mkdirs();
        }

        Assert.assertTrue(targetDir.exists());
        Assert.assertTrue(targetDir.isDirectory());
    }

    @Test
    public void validateCreatingWorkbook() {

        // Assemble
        final ExcelReportServiceBean unitUnderTest = new ExcelReportServiceBean();

        // Act
        final Workbook result = unitUnderTest.createWorkbook(memHaxx);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertTrue(result instanceof HSSFWorkbook);

        HSSFWorkbook castResult = (HSSFWorkbook) result;

        final SummaryInformation sInfo = castResult.getSummaryInformation();
        Assert.assertNotNull(sInfo);
        Assert.assertEquals(memHaxx.getAlias(), sInfo.getAuthor());
        Assert.assertNotNull(sInfo.getCreateDateTime());
    }

    @Test
    public void validateCreatingSheetAndContent() throws Exception {

        // Assemble
        final File targetFile = getTargetExcelFile();
        final Workbook workbook = unitUnderTest.createWorkbook(memHaxx);
        final List<String> columnTitles = Arrays.asList("Column_1", "Column_2");

        // Act
        final Sheet firstSheet = unitUnderTest.createStandardExcelSheet(workbook,
                "FooBar",
                "FooBarTitle",
                columnTitles);

        final CellStyle cellStyle = unitUnderTest.getCellStyle(ExcelReportService.ExcelElement.CELL, workbook);
        for(int index = 2; index < 4; index++) {

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

    //
    // Private helpers
    //

    private File getTargetExcelFile() {

        File toReturn = null;
        
        for(int i = 0; true; i++) {
            toReturn = new File(targetDir, "ExcelFile_" + i + ".xls");
            if(!toReturn.exists()) {
                return toReturn;
            }
        }
    }
}
