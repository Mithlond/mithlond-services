package se.mithlond.services.content.impl.ejb;

import se.mithlond.services.content.api.UserFeedbackService;
import se.mithlond.services.content.model.transport.feedback.CharacterizedDescription;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.ejb.Stateless;

/**
 * UserFeedbackService Stateless EJB implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Stateless
public class UserFeedbackServiceBean extends AbstractJpaService implements UserFeedbackService {

    /**
     * {@inheritDoc}
     */
    @Override
    public CharacterizedDescription submitUserFeedback(final CharacterizedDescription userFeedback) {
        return null;
    }
}
