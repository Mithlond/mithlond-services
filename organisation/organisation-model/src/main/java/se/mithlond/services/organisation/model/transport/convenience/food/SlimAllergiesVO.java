package se.mithlond.services.organisation.model.transport.convenience.food;

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SlimAllergiesVO extends AbstractSimpleTransporter implements Validatable {

    @Override
    public void validateInternalState() throws InternalStateValidationException {

    }
}
