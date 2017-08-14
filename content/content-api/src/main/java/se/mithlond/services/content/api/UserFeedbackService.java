package se.mithlond.services.content.api;

import se.mithlond.services.content.model.transport.feedback.CharacterizedDescription;

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
     * @return A server response indicating how the server managed to cope with the supplied userFeedback.
     */
    CharacterizedDescription submitUserFeedback(final CharacterizedDescription userFeedback);
}
