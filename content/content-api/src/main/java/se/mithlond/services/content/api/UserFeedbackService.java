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
package se.mithlond.services.content.api;

import se.mithlond.services.content.model.transport.feedback.CharacterizedDescription;
import se.mithlond.services.organisation.model.membership.Membership;

import javax.ejb.Local;

/**
 * Specification for how to handle feedback from users.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Local
public interface UserFeedbackService {

    /**
     * Submits the supplied user Feedback (typically a Bug Report or a Suggestion for improvement).
     *
     * @param userFeedback The bug or idea supplied by the user.
     * @param submitter    The Membership who submitted the bug or idea.
     * @return A server response indicating how the server managed to cope with the supplied userFeedback.
     */
    CharacterizedDescription submitUserFeedback(final Membership submitter,
                                                final CharacterizedDescription userFeedback);
}
