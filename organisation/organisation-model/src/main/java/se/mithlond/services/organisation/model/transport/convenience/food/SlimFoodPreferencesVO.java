/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.mithlond.services.organisation.model.transport.convenience.food;

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.transport.food.FoodPreferenceVO;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

import javax.validation.constraints.NotNull;
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
    @NotNull
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
