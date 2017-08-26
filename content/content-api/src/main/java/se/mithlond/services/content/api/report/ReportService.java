package se.mithlond.services.content.api.report;

import se.mithlond.services.organisation.model.membership.Membership;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Specification for a generic ReportService.
 * 
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface ReportService<T> extends Serializable {

    /**
     * The name of the custom HTTP header carrying the suggested filename for downloaded excel reports.
     */
    String SUGGESTED_FILENAME_HEADER = "X-Suggested-Filename";

    /**
     * Creates a new and empty Document, which is the basis of
     * performing anything report-ish.
     *
     * @param activeMembership The active Membership.
     * @param title            The document title.
     * @return a new and empty Document.
     */
    T createDocument(@NotNull Membership activeMembership, @NotNull String title);
}
