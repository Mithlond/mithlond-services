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
