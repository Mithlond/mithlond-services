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

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.food.Food;
import se.mithlond.services.organisation.model.localization.LocaleDefinition;
import se.mithlond.services.organisation.model.localization.Localizable;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransportable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * The SimpleTransportable version of a Food entity.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE, propOrder = {"foodName", "categoryID", "subCategoryID"})
@XmlAccessorType(XmlAccessType.FIELD)
public class FoodVO extends AbstractSimpleTransportable {

    /**
     * The food name (within one explicit LocaleDefinition).
     */
    @XmlAttribute(required = true)
    private String foodName;

    /**
     * The identifier of the top-level Categorisation for this FoodVO.
     */
    @XmlAttribute(required = true)
    private long categoryID;

    /**
     * The identifier of the sub-level Categorisation for this FoodVO.
     */
    @XmlAttribute(required = true)
    private long subCategoryID;

    /**
     * JAXB-friendly constructor.
     */
    public FoodVO() {
    }

    /**
     * Compound constructor creating a FoodVO wrapping the supplied data.
     *
     * @param foodJpaID     The JPA ID of the Food represented by this FoodVO.
     * @param foodName      The (localized) name of the food.
     * @param categoryID    The JPA ID of the top-level Categorisation for this FoodVO.
     * @param subCategoryID The JPA ID of the sub-level Categorisation for this FoodVO.
     */
    public FoodVO(final long foodJpaID, final String foodName, final long categoryID, final long subCategoryID) {

        // Delegate
        super(foodJpaID);

        // Assign internal state
        this.foodName = Validate.notEmpty(foodName, "foodName");
        this.categoryID = categoryID;
        this.subCategoryID = subCategoryID;
    }

    /**
     * Creates a FoodVO wrapping the data of the supplied objects.
     *
     * @param food             The Food entity to convert to a lightweight FoodVO object.
     * @param localeDefinition The LocaleDefinition used to extract the foodName for this FoodVO.
     */
    public FoodVO(final Food food, final LocaleDefinition localeDefinition) {

        // Delegate
        super(food.getId());

        // Check sanity
        Validate.notNull(localeDefinition, "localeDefinition");

        // Assign internal state
        this.foodName = food.getLocalizedFoodName().getText(localeDefinition, Localizable.DEFAULT_CLASSIFIER);
        this.categoryID = food.getCategory().getId();
        this.subCategoryID = food.getSubCategory().getId();
    }

    /**
     * @return The food name for this FoodVO.
     */
    public String getFoodName() {
        return foodName;
    }

    /**
     * @return The identifier of the top-level Categorisation for this FoodVO.
     */
    public long getCategoryID() {
        return categoryID;
    }

    /**
     * @return The identifier of the sub-level Categorisation for this FoodVO.
     */
    public long getSubCategoryID() {
        return subCategoryID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final AbstractSimpleTransportable cmp) {

        if (cmp instanceof FoodVO) {

            final FoodVO that = (FoodVO) cmp;

            // Check sanity
            if (that == this) {
                return 0;
            }

            // Delegate to internal state
            int toReturn = (int) (this.getCategoryID() - that.getCategoryID());

            if (toReturn == 0) {
                toReturn = (int) (this.getSubCategoryID() - that.getSubCategoryID());
            }

            if (toReturn == 0) {
                toReturn = this.getFoodName().compareTo(that.getFoodName());
            }

            // All Done.
            return toReturn;
        }

        // Delegate
        return super.compareTo(cmp);
    }
}
