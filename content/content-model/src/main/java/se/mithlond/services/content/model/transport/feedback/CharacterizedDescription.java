package se.mithlond.services.content.model.transport.feedback;

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.content.model.ContentPatterns;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = ContentPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = ContentPatterns.TRANSPORT_NAMESPACE, propOrder = {"category", "description" })
@XmlAccessorType(XmlAccessType.FIELD)
public class CharacterizedDescription implements Serializable, Validatable, Comparable<CharacterizedDescription> {

    /**
     * The category of this CharacterizedDescription.
     */
    @XmlAttribute(required = true)
    private String category;

    /**
     * The actual description data.
     */
    @XmlElement(required = true)
    private String description;

    /**
     * JAXB-friendly constructor.
     */
    public CharacterizedDescription() {
    }

    /**
     * Compound constructor creating a {@link CharacterizedDescription} wrapping the supplied data.
     *
     * @param category    The category of this CharacterizedDescription.
     * @param description The descriptive text of this CharacterizedDescription.
     */
    public CharacterizedDescription(@NotNull final String category,
                                    @NotNull final String description) {
        this.category = category;
        this.description = description;
    }

    /**
     * @return The category of this CharacterizedDescription.
     */
    public String getCategory() {
        return category;
    }

    /**
     * @return The descriptive text of this CharacterizedDescription.
     */
    public String getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {

        // Fail fast
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        // Delegate to internal state
        final CharacterizedDescription that = (CharacterizedDescription) o;
        return Objects.equals(getCategory(), that.getCategory())
                && Objects.equals(getDescription(), that.getDescription());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getCategory(), getDescription());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final CharacterizedDescription that) {

        // Fail fast
        if (that == this) {
            return 0;
        } else if (that == null) {
            return -1;
        }

        // Delegate to internal state
        int toReturn = this.getCategory().compareTo(that.getCategory());
        if (toReturn == 0) {
            toReturn = this.getDescription().compareTo(that.getDescription());
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CharacterizedDescription{"
                + "category='" + category + '\''
                + ", description='" + description + '\''
                + '}';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(category, "category")
                .notNullOrEmpty(description, "description")
                .endExpressionAndValidate();
    }
}
