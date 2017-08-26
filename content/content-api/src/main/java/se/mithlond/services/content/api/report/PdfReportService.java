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

import org.apache.pdfbox.pdmodel.PDDocument;

import javax.ejb.Local;

/**
 * Report service assisting in creating PDF reports.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Local
public interface PdfReportService extends ReportService<PDDocument> {

    /**
     * The content type produced by Excel. Use for a @Produces annotation.
     */
    String PDF_CONTENT_TYPE = "application/vnd.ms-excel";
}
