package se.mithlond.services.content.model.articles;

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Embeddable
public class LayoutableImage implements Serializable, Validatable {

    public enum Placement {

        NORTHWEST,

        NORTH,

        NORTHEAST,

        WEST,

        CENTER,

        EAST,

        SOUTHWEST,

        SOUTH,

        SOUTHEAST
    }

    /**
     * The URL or resource location string for an image.
     */
    private String imageURL;

    /**
     * The placement of this FloatableImage within its context (typically a Section).
     */
    private Placement placement;

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

    }
}
