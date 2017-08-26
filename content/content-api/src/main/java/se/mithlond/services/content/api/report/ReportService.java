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
