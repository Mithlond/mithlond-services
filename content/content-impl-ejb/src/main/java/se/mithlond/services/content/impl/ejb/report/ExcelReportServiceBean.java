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

import org.apache.poi.hpsf.DocumentSummaryInformation;
import org.apache.poi.hpsf.SummaryInformation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.mithlond.services.content.api.report.ExcelReportService;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import javax.ejb.Stateless;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * Stateless ExcelReportService implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Stateless
public class ExcelReportServiceBean implements ExcelReportService {

    // Internal constants
    private static final short GREY_25_PERCENT = IndexedColors.GREY_25_PERCENT.getIndex();
    private static final BorderStyle BORDER_THIN = BorderStyle.THIN;

    /**
     * {@inheritDoc}
     */
    @Override
    public Workbook createDocument(@NotNull final Membership activeMembership, @NotNull final String title) {

        // Check sanity
        Validate.notNull(activeMembership, "activeMembership");

        final HSSFWorkbook toReturn = new HSSFWorkbook();
        toReturn.createInformationProperties();

        // Add some comments.
        final SummaryInformation summaryInformation = toReturn.getSummaryInformation();
        summaryInformation.setCreateDateTime(new Date());
        summaryInformation.setTitle(title);
        summaryInformation.setAuthor("Nazgûl Services Excel Report Generator");
        summaryInformation.setSubject("Requested by: " + activeMembership.getAlias());
        summaryInformation.setRevNumber("1");

        // Add some Document summary information as well.
        final DocumentSummaryInformation documentSummaryInformation = toReturn.getDocumentSummaryInformation();
        final String orgName = activeMembership.getOrganisation().getOrganisationName();
        documentSummaryInformation.setCompany(activeMembership.getOrganisation().getOrganisationName());
        documentSummaryInformation.setManager(orgName + " is Da Boss of you!");

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Sheet createStandardExcelSheet(@NotNull final Workbook workbook,
                                          @NotNull final String sheetName,
                                          @NotNull final String sheetTitle,
                                          @NotNull final List<String> columnTitles) {

        // Check sanity
        Validate.notEmpty(sheetName, "sheetName");
        Validate.notEmpty(sheetTitle, "sheetTitle");
        Validate.notEmpty(columnTitles, "columnTitles");
        Validate.notNull(workbook, "workbook");

        // Create a new Workbook if required.
        final LocalDateTime timestamp = LocalDateTime.now();
        final String now = TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(timestamp).replace(":", " ");

        // Create the Sheet to return
        final Sheet toReturn = workbook.createSheet(sheetName + "_" + now);
        toReturn.setFitToPage(true);
        toReturn.setHorizontallyCenter(true);

        // Create a "Title" row containing a single cell (i.e. merged cells)
        // and where the sheet title is presented and centered.
        final Row titleRow = toReturn.createRow(0);
        titleRow.setHeightInPoints(45);
        final Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(sheetTitle + " " + now);
        titleCell.setCellStyle(getCellStyle(ExcelElement.TITLE, workbook));
        // toReturn.addMergedRegion(CellRangeAddress.valueOf("$A$1:$E$1"));
        toReturn.addMergedRegion(CellRangeAddress.valueOf("$A$1:$"
                + CellReference.convertNumToColString(columnTitles.size() - 1) + "1"));

        // Create a header Row with the column names defined above.
        final Row headerRow = toReturn.createRow(1);

        // headerRow.setHeightInPoints(40);
        // This *could* adjust the header row to fit its internal height.
        titleRow.setHeight((short) -1);

        Cell headerCell;

        for (int i = 0; i < columnTitles.size(); i++) {
            headerCell = headerRow.createCell(i);
            headerCell.setCellValue(columnTitles.get(i));
            headerCell.setCellStyle(getCellStyle(ExcelElement.HEADER, workbook));
            toReturn.setDefaultColumnStyle(i, getCellStyle(ExcelElement.NON_WRAPPING, workbook));
            toReturn.autoSizeColumn(i);
        }

        // All done
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    public CellStyle getCellStyle(final ExcelElement el, final Workbook workbook) {

        // Check sanity
        Validate.notNull(workbook, "workbook");
        Validate.notNull(el, "el");

        // Acquire the el and Font as expected
        final CellStyle toReturn = workbook.createCellStyle();
        final Font theFont = workbook.createFont();

        switch (el) {

            case TITLE:
                theFont.setFontHeightInPoints((short) 18);
                theFont.setBold(true);
                theFont.setColor(IndexedColors.BLUE_GREY.getIndex());

                toReturn.setAlignment(HorizontalAlignment.CENTER);
                toReturn.setVerticalAlignment(VerticalAlignment.CENTER);
                break;

            case HEADER:
                theFont.setFontHeightInPoints((short) 11);
                theFont.setColor(IndexedColors.WHITE.getIndex());

                toReturn.setAlignment(HorizontalAlignment.CENTER);
                toReturn.setVerticalAlignment(VerticalAlignment.CENTER);
                toReturn.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
                toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                toReturn.setWrapText(true);
                break;

            case CELL:

                toReturn.setAlignment(HorizontalAlignment.LEFT);
                toReturn.setWrapText(true);
                toReturn.setBorderRight(BORDER_THIN);
                toReturn.setRightBorderColor(GREY_25_PERCENT);
                toReturn.setBorderLeft(BORDER_THIN);
                toReturn.setLeftBorderColor(GREY_25_PERCENT);
                toReturn.setBorderTop(BORDER_THIN);
                toReturn.setTopBorderColor(GREY_25_PERCENT);
                toReturn.setBorderBottom(BORDER_THIN);
                toReturn.setBottomBorderColor(GREY_25_PERCENT);
                break;

            case NON_WRAPPING:
                toReturn.setAlignment(HorizontalAlignment.LEFT);
                toReturn.setWrapText(false);
                toReturn.setBorderRight(BORDER_THIN);
                toReturn.setRightBorderColor(GREY_25_PERCENT);
                toReturn.setBorderLeft(BORDER_THIN);
                toReturn.setLeftBorderColor(GREY_25_PERCENT);
                toReturn.setBorderTop(BORDER_THIN);
                toReturn.setTopBorderColor(GREY_25_PERCENT);
                toReturn.setBorderBottom(BORDER_THIN);
                toReturn.setBottomBorderColor(GREY_25_PERCENT);
                break;

            case FORMULA:
                toReturn.setAlignment(HorizontalAlignment.CENTER);
                toReturn.setVerticalAlignment(VerticalAlignment.CENTER);
                toReturn.setFillForegroundColor(GREY_25_PERCENT);
                toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                toReturn.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                break;

            case ALT_FORMULA:
                toReturn.setAlignment(HorizontalAlignment.CENTER);
                toReturn.setVerticalAlignment(VerticalAlignment.CENTER);
                toReturn.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
                toReturn.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                toReturn.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
                break;

            default:
                throw new IllegalArgumentException("Style [" + el.name() + "] was not defined. "
                        + "Blame the programmer.");
        }

        // All done.
        toReturn.setFont(theFont);
        return toReturn;
    }
}
