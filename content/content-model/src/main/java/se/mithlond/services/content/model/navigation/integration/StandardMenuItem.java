package se.mithlond.services.content.model.navigation.integration;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.content.model.navigation.MenuItem;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class StandardMenuItem extends NazgulEntity implements MenuItem {

    @Override
    protected void validateEntityState() throws InternalStateValidationException {

    }

    @Override
    public String getAnchorHRef() {
        return null;
    }

    @Override
    public String getText() {
        return null;
    }
}
