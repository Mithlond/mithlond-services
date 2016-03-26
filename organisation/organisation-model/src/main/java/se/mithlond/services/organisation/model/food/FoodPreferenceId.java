package se.mithlond.services.organisation.model.food;

import se.mithlond.services.organisation.model.OrganisationPatterns;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Many-to-many relation key between Food-related Categories and Members.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Embeddable
@Access(value = AccessType.FIELD)
@XmlTransient
@XmlType(namespace = OrganisationPatterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class FoodPreferenceId implements Serializable {

    private static final long serialVersionUID = 8829999L;

    // Shared state
    @Column(name = "category_id")
    public long categoryId;

    @Column(name = "internaluser_id")
    public long userId;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public FoodPreferenceId() {
    }

    /**
     * Creates a new FoodPreferenceId wrapping the supplied data.
     *
     * @param userID     The id of the User having an allergy to the given FoodId.
     * @param categoryId The id of the Category to which a Member has a FoodPreference.
     */
    public FoodPreferenceId(final long categoryId, final long userID) {
        this.categoryId = categoryId;
        this.userId = userID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if (obj instanceof FoodPreferenceId) {

            final FoodPreferenceId that = (FoodPreferenceId) obj;
            return categoryId == that.categoryId && userId == that.userId;
        }

        // All done.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) (categoryId + userId) % Integer.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "FoodPreferenceId [Category: " + categoryId + ", User: " + userId + "]";
    }
}
