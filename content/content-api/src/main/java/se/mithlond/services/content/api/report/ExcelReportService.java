/*-
 * #%L
 * Nazgul Project: mithlond-services-content-api
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
package se.mithlond.services.content.api.report;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.mithlond.services.organisation.model.membership.Membership;

import javax.ejb.Local;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Report service assisting in creating Excel reports.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Local
public interface ExcelReportService extends Serializable {

    // Our Logger
    Logger log = LoggerFactory.getLogger(ExcelReportService.class);

    /**
     * The name of the custom HTTP header carrying the suggested filename for downloaded excel reports.
     */
    String SUGGESTED_FILENAME_HEADER = "X-Suggested-Filename";

    /**
     * The content type produced by Excel. Use for a @Produces annotation.
     */
    String EXCEL_CONTENT_TYPE = "application/vnd.ms-excel";

    /**
     * Common element definitions used to create nodes in Excel documents.
     */
    enum ExcelElement {

        /**
         * Header style definition.
         */
        HEADER,

        /**
         * Excel title cell definition.
         */
        TITLE,

        /**
         * Excel standard cell definition.
         */
        CELL,

        /**
         * Like CELL, but no text wrapping occurs.
         */
        NON_WRAPPING,

        /**
         * Formula / money cell definition.
         */
        FORMULA,

        /**
         * Alternative formula / money cell definition.
         */
        ALT_FORMULA
    }

    /**
     * Creates a new and empty Excel Workbook, which is the basis of
     * performing anything Excel-ish.
     *
     * @param activeMembership The active Membership.
     * @return a new and empty Excel Workbook.
     */
    Workbook createWorkbook(@NotNull final Membership activeMembership);

    /**
     * Acquires the given CellStyle for the provided Workbook.
     *
     * @param wb    The workbook to populate
     * @param style The desired style.
     * @return A fully configured Cellstyle.
     */
    CellStyle getCellStyle(@NotNull final ExcelElement style, @NotNull final Workbook wb);

    /**
     * Creates a sheet with the supplied name and title within the supplied targetWorkbook.
     *
     * @param targetWorkbook The Workbook within which the Sheet should be created.
     * @param sheetName      The name of the created Sheet.
     * @param sheetTitle     The title of the created Sheet.
     * @param columnTitles   The titles of the columns created within the Sheet.
     * @return The newly created Sheet.
     */
    Sheet createStandardExcelSheet(@NotNull final Workbook targetWorkbook,
                                   @NotNull final String sheetName,
                                   @NotNull final String sheetTitle,
                                   @NotNull final List<String> columnTitles);

    /**
     * Adds a Cell at the supplied index within the given Row, using the supplied text and CellStyle.
     *
     * @param index The index where to add the given Cell. Must be 0 or greater.
     * @param aRow  The Row in which to add the Cell.
     * @param text  The text which should go into the Cell.
     * @param style The CellStyle to apply to the Cell.
     */
    default void addCell(final int index,
                         @NotNull final Row aRow,
                         @NotNull final String text,
                         @NotNull final CellStyle style) {

        // Check sanity
        Validate.notNull(aRow, "aRow");
        Validate.notNull(text, "text");
        Validate.notNull(style, "style");
        Validate.isTrue(index >= 0, "index >= 0");

        // Create the cell
        final Cell cell = aRow.createCell(index);
        cell.setCellStyle(style);
        cell.setCellValue(text);
    }

    /**
     * Default streaming method, which converts the supplied Workbook to a byte[] suitable for downloading.
     *
     * @param toConvert The Workbook to convert
     * @return a byte[] stream of the Workbook.
     */
    default byte[] convertToByteArray(@NotNull final Workbook toConvert) {

        // Create an Excel resource from the Workbook
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            toConvert.write(baos);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not convert Excel sheet data to bytes.", e);
        }

        // All Done.
        return baos.toByteArray();
    }
}
