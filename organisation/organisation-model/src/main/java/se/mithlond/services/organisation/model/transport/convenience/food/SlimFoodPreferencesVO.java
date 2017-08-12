package se.mithlond.services.organisation.model.transport.convenience.food;

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.transport.food.FoodPreferenceVO;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Slim transporter carrying a set of FoodPreferenceVOs.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"foodPreferences" })
@XmlAccessorType(XmlAccessType.FIELD)
public class SlimFoodPreferencesVO extends AbstractSimpleTransporter implements Validatable {

    /**
     * The FoodPreferenceVOs for the supplied User.
     */
    @XmlElementWrapper
    @XmlElement(name = "pref")
    private SortedSet<FoodPreferenceVO> foodPreferences;

    /**
     * JAXB-friendly constructor.
     */
    public SlimFoodPreferencesVO() {
        foodPreferences = new TreeSet<>();
    }

    /**
     * Compound constructor creating a SlimFoodPreferencesVO wrapping the supplied FoodPreferenceVOs.
     *
     * @param foodPreferences The FoodPreferenceVOs for the supplied User.
     */
    public SlimFoodPreferencesVO(final FoodPreferenceVO... foodPreferences) {

        // Delegate
        this();

        // Assign internal state
        this.add(foodPreferences);
    }

    /**
     * Adds the supplied FoodPreferenceVOs to this SlimFoodPreferencesVO transporter.
     *
     * @param toAdd the supplied FoodPreferenceVOs to this SlimFoodPreferencesVO transporter.
     */
    public void add(final FoodPreferenceVO... toAdd) {

        if (toAdd != null && toAdd.length > 0) {

            Arrays.stream(toAdd)
                    .filter(Objects::nonNull)
                    .forEach(this.foodPreferences::add);
        }
    }

    /**
     * @return The wrapped FoodPreferenceVOs.
     */
    public SortedSet<FoodPreferenceVO> getFoodPreferences() {
        return foodPreferences;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(foodPreferences, "foodPreferences")
                .endExpressionAndValidate();
    }
}
