/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
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
package se.mithlond.services.organisation.model.transport.food;

import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.food.Food;
import se.mithlond.services.organisation.model.localization.LocaleDefinition;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"localeDefinition", "categories", "foods", "detailedFoods"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Foods extends AbstractSimpleTransporter {

    /**
     * The LocaleDefinition used for all FoodVOs transported within this Foods wrapper.
     */
    @XmlElement
    private LocaleDefinition localeDefinition;

    /**
     * The Categories and SubCategories within which the FoodVOs are grouped.
     */
    @XmlElementWrapper
    @XmlElement(name = "category")
    private SortedSet<Category> categories;

    /**
     * The List of (shallow-detail) FoodVOs transported within this Foods wrapper.
     */
    @XmlElementWrapper
    @XmlElement(name = "food")
    private List<FoodVO> foods;

    /**
     * The optional (i.e. nullable) full-detail Food instances transported within this Foods wrapper.
     */
    @XmlElementWrapper
    @XmlElement(name = "detailedFood")
    private List<Food> detailedFoods;

    /**
     * JAXB-friendly constructor.
     */
    public Foods() {

        // Create internal state
        this.foods = new ArrayList<>();
        this.detailedFoods = new ArrayList<>();
        this.categories = new TreeSet<>();
    }

    /**
     * Compound constructor creating a Foods instance wrapping the supplied (full-detail) Food objects.
     *
     * @param foods                 The Food objects to transport.
     * @param localeDefinition      The non-null LocaleDefinition used within this Foods transport wrapper.
     * @param shallowRepresentation if {@code true}, convert all supplied Food objects to FoodVOs before transport
     *                              (i.e. use the shallow-detail representation for transport form).
     */
    public Foods(final boolean shallowRepresentation, final LocaleDefinition localeDefinition, final Food... foods) {

        // Delegate
        this();

        // Check sanity
        this.localeDefinition = Validate.notNull(localeDefinition, "localeDefinition");

        // Add all Foods.
        add(shallowRepresentation, foods);
    }

    /**
     * Adds the supplied Food objects to this Foods transporter, using shallow or detailed representation depending
     * on the value of the shallowRepresentation argument.
     *
     * @param shallowRepresentation if {@code true}, convert all supplied Food objects to FoodVOs before transport
     *                              (i.e. use the shallow-detail representation for transport form).
     * @param foods                 The Food objects to add to this Foods transport.
     */
    public void add(final boolean shallowRepresentation, final Food... foods) {

        if (foods != null) {

            Stream.of(foods).forEach(food -> {

                // Are the Category/Subcategory already extracted?
                final Category category = food.getCategory();
                final Category subCategory = food.getSubCategory();

                if (!categories.contains(category)) {
                    categories.add(category);
                }
                if (!categories.contains(subCategory)) {
                    categories.add(subCategory);
                }

                if (shallowRepresentation) {

                    final FoodVO toAdd = new FoodVO(food, this.localeDefinition);
                    if (!this.foods.contains(toAdd)) {
                        this.foods.add(toAdd);
                    }

                } else {

                    final String localizedFoodName = food.getLocalizedFoodName().getText(this.localeDefinition);
                    Food existingFood = null;
                    if (!this.detailedFoods.isEmpty()) {

                        existingFood = this.detailedFoods.stream()
                                .filter(current -> current.getLocalizedFoodName()
                                        .getText(this.localeDefinition)
                                        .equalsIgnoreCase(localizedFoodName))
                                .findFirst()
                                .orElse(null);
                    }

                    if (existingFood == null) {
                        this.detailedFoods.add(food);
                    }
                }
            });
        }
    }

    /**
     * Retrieves the LocaleDefinition used within this Foods transport wrapper.
     *
     * @return The non-null LocaleDefinition used within this Foods transport wrapper.
     */
    public LocaleDefinition getLocaleDefinition() {
        return localeDefinition;
    }

    /**
     * Retrieves the Set of Category objects used by Food objects within this Foods transport.
     *
     * @return the Set of Category objects used by Food objects within this Foods transport.
     */
    public SortedSet<Category> getCategories() {
        return categories;
    }

    /**
     * Retrieves all shallow-detail FoodVOs transported within this Foods.
     *
     * @return The shallow-detail FoodVOs transported within this Foods.
     */
    public List<FoodVO> getFoods() {
        return foods;
    }

    /**
     * Retrieves all full-detail Food objects transported within this Foods.
     *
     * @return all full-detail Food objects transported within this Foods.
     */
    public List<Food> getDetailedFoods() {
        return detailedFoods;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + " with LocaleDefinition " + localeDefinition
                + " containing " + categories.size() + " categories. Also containing "
                + detailedFoods.size() + " detailed, and " + foods.size()
                + " shallow representations.";
    }
}
