package se.mithlond.services.organisation.model.transport.food;

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.food.FoodPreference;

import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;
import java.util.Objects;

/**
 * Slimmed-down FoodPreference VO for JAXB transport.
 * 
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FoodPreferenceVO implements Serializable, Validatable, Comparable<FoodPreferenceVO> {

    /**
     * The category name for this FoodPreferenceVO.
     */
    @XmlAttribute(required = true)
    private String preference;

    /**
     * An optional description of this FoodPreferenceVO.
     */
    @XmlAttribute
    private String description;

    /**
     * The JPA ID of the user having this FoodPreferenceVO.
     */
    @XmlAttribute(required = true)
    private long userID;

    /**
     * JAXB-friendly constructor.
     */
    public FoodPreferenceVO() {
    }

    /**
     * Compound constructor creating a FoodPreferenceVO wrapping the supplied data.
     *
     * @param preference  The preference name/category.
     * @param description The description of this FoodPreference.
     * @param userID      The jpaID of the User for which this FoodPreference is valid.
     */
    public FoodPreferenceVO(final String preference, final String description, final long userID) {

        // Assign internal state
        this.preference = preference;
        this.description = description;
        this.userID = userID;
    }

    /**
     * Convenience constructor converting a FoodPreference to its corresponding VO.
     *
     * @param preference The FoodPreference to convert.
     */
    public FoodPreferenceVO(final FoodPreference preference) {

        // Delegate
        this(preference.getCategory().getCategoryID(),
                preference.getCategory().getDescription(),
                preference.getUser().getId());
    }

    /**
     * @return The category name for this FoodPreferenceVO.
     */
    public String getPreference() {
        return preference;
    }

    /**
     * @return An optional description of this FoodPreferenceVO.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The JPA ID of the user having this FoodPreferenceVO.
     */
    public long getUserID() {
        return userID;
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
        final FoodPreferenceVO that = (FoodPreferenceVO) o;
        return getUserID() == that.getUserID()
                && Objects.equals(getPreference(), that.getPreference());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getPreference(), getUserID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final FoodPreferenceVO that) {

        // Fail fast
        if (that == null) {
            return -1;
        } else if (that == this) {
            return 0;
        }

        // Delegate to internal state
        int toReturn = this.getPreference().compareTo(that.getPreference());
        if (toReturn == 0) {
            toReturn = (int) (this.getUserID() - that.getUserID());
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(preference, "preference")
                .endExpressionAndValidate();
    }
}
