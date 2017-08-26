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
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.mithlond.services.content.api.report.PdfReportService;
import se.mithlond.services.organisation.model.membership.Membership;

import javax.ejb.Stateless;
import javax.validation.constraints.NotNull;
import java.util.Calendar;

/**
 * Stateless PdfReportService EJB implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Stateless
public class PdfReportServiceBean implements PdfReportService {

    /**
     * {@inheritDoc}
     */
    @Override
    public PDDocument createDocument(@NotNull final Membership activeMembership, @NotNull final String title) {

        // Check sanity
        Validate.notNull(activeMembership, "activeMembership");
        Validate.notEmpty(title, "title");

        // Create the document and add some metadata to it.
        final PDDocument toReturn = new PDDocument();
        final PDDocumentInformation pdd = toReturn.getDocumentInformation();

        pdd.setAuthor("" + activeMembership.getAlias());
        pdd.setProducer("Nazgûl Services Excel Report Generator");
        pdd.setCreationDate(Calendar.getInstance());
        pdd.setTitle(title);

        // All Done.
        return toReturn;
    }
}
