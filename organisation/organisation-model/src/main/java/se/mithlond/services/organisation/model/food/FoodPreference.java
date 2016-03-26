package se.mithlond.services.organisation.model.food;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.user.User;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Food preference entity, adding knowledge about Members' generic food preferences,
 * as opposed to standard Allergies which relates a severity level with a Food and
 * a Member.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = "getAllFoodPreferences",
                query = "select a from Category a where a.classification = '"
                        + FoodPreference.FOOD_PREFERENCE_CATEGORY_CLASSIFICATION + "' order by a.category"),
        @NamedQuery(name = "getFoodPreferencesByMemberLogin",
                query = "select a from FoodPreference a where a.member.login = ?1 order by a.category.category")
})
@Entity
@Access(value = AccessType.FIELD)
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"version", "user", "category"})
@XmlAccessorType(XmlAccessType.FIELD)
public class FoodPreference implements Serializable, Comparable<FoodPreference>, Validatable {

    /**
     * The Category classification of a FoodPreference.
     */
    public static final String FOOD_PREFERENCE_CATEGORY_CLASSIFICATION = "food_preference";

    // Internal state
    @Version
    @XmlAttribute(required = false)
    private long version;

    @EmbeddedId
    @XmlTransient
    private FoodPreferenceId foodPreferenceId;

    @ManyToOne
    @MapsId("categoryId")
    @XmlElement(required = true, nillable = false)
    private Category category;

    @ManyToOne(optional = false)
    @MapsId("userId")
    @XmlIDREF
    private User user;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public FoodPreference() {
    }

    /**
     * Creates a new FoodPreference wrapping the supplied data. Each food preference description is
     * represented as a Category, implying that each FoodPreference object relates a Member to a
     * Category having the classification {@code FOOD_PREFERENCE_CATEGORY_CLASSIFICATION}.
     *
     * @param category The non-null Category for which this FoodPreference should be created.
     * @param user The User having a FoodPreference.
     */
    public FoodPreference(final Category category, final User user) {

        // Check some sanity
        Validate.notNull(user, "Cannot handle null 'user' argument.");
        Validate.notNull(category, "Cannot handle null category argument.");
        Validate.isTrue(category.getClassification().equals(FOOD_PREFERENCE_CATEGORY_CLASSIFICATION),
                "FoodPreference categories must have the classification ["
                        + FOOD_PREFERENCE_CATEGORY_CLASSIFICATION + "]");

        // Assign internal state
        this.category = category;
        this.user = user;

        this.foodPreferenceId = new FoodPreferenceId(category.getId(), user.getId());
    }

    /**
     * @return the Database-generated version/revision of this Entity.
     */
    public long getVersion() {
        return version;
    }

    /**
     * @return The User having a FoodPreference.
     */
    public User getUser() {

        // Handle JAXB unmarshalling
        recreateIdIfRequired();

        // All done
        return user;
    }

    /**
     * @return The non-null Category for which this FoodPreference should be created.
     */
    public Category getCategory() {

        // Handle JAXB unmarshalling
        recreateIdIfRequired();

        // All done
        return category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final FoodPreference that) {

        // Check sanity
        if (that == null) {
            return -1;
        } else if (that == this) {
            return 0;
        }

        // Delegate to internal state comparison.
        int toReturn = getCategory().compareTo(that.getCategory());
        if (toReturn == 0) {
            final long thisUserID = getUser() != null ? getUser().getId() : -1L;
            final long thatUserID = that.getUser() != null ? that.getUser().getId() : -1L;
            toReturn = (int) (thisUserID - thatUserID);
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        // Check sanity
        InternalStateValidationException.create()
                .notNull(category, "category")
                .notNull(user, "user")
                .endExpressionAndValidate();
    }

    //
    // Private helpers
    //

    private void recreateIdIfRequired() {
        if (foodPreferenceId == null && (category != null && user != null)) {
            this.foodPreferenceId = new FoodPreferenceId(category.getId(), user.getId());
        }
    }
}

