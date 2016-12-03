/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2014 jGuru Europe AB
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
package se.mithlond.services.organisation.model.food;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.localization.LocaleDefinition;
import se.mithlond.services.organisation.model.localization.LocalizedTexts;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

/**
 * Entity implementation for Foods. Foods are classified in a 3-level tree to provide some structure as to
 * where individual Food substances can be located. These three levels are {@code category/subcategory/food},
 * implying that each Food entity has a relation to two category objects - called category and subcategory.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = "Food.getAllFoodByLocalization",
                query = "select f from Food f join LocalizedTexts lt on f.localizedFoodName"),
        @NamedQuery(name = "Food.getFoodByName",
                query = "select a from Food a where a.localizedFoodName like ?1 order by a.localizedFoodName"),
        @NamedQuery(name = "Food.getFoodByCategory",
                query = "select a from Food a where a.category.categoryID like ?1 and a.category.classification = '"
                        + Food.FOOD_CATEGORY_CLASSIFICATION + "' order by a.localizedFoodName"),
        @NamedQuery(name = "Food.getFoodByCategoryAndSubCategory",
                query = "select a from Food a where a.category.categoryID like ?1 and a.category.classification = '"
                        + Food.FOOD_CATEGORY_CLASSIFICATION + "' and a.subCategory.categoryID like ?2 "
                        + "and a.subCategory.classification = '" + Food.FOOD_SUBCATEGORY_CLASSIFICATION + "' "
                        + "order by a.localizedFoodName")
})
@Entity
@Access(value = AccessType.FIELD)
@Table(uniqueConstraints = {@UniqueConstraint(name = "foodNameIsUnique", columnNames = {"foodName"})})
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"localizedFoodName", "category", "subCategory"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Food extends NazgulEntity implements Comparable<Food> {

    /**
     * The classification identifier for a Category of Food. All Food entities have 2 category relations indicated to
     * classify the Food for simple finding. These three levels are {@code category/subcategory/food},
     * and this constant holds the value for a top level categorization for Foods.
     */
    public static final String FOOD_CATEGORY_CLASSIFICATION = "food_category";

    /**
     * The classification identifier for a SubCategory of Food. All Food entities have 2 category relations
     * indicated to classify the Food for simple finding. These three levels are {@code category/subcategory/food},
     * and this constant holds the value for a mid level categorization for Foods.
     */
    public static final String FOOD_SUBCATEGORY_CLASSIFICATION = "food_subcategory";

    /**
     * The LocalizedText suite identifier prefix used for all food name localizations.
     */
    public static final String FOOD_LOCALIZATION_SUITE_PREFIX = "Food_";

    /**
     * A localized texts instance containing the name of this Food.
     */
    @OneToOne(optional = false)
    @JoinColumn(name = "food_name_id")
    @XmlElement(required = true)
    private LocalizedTexts localizedFoodName;

    /**
     * The top-level Categorisation of this Food.
     */
    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @XmlIDREF
    @XmlElement(required = true)
    private Category category;

    /**
     * The sub-level Categorisation of this Food.
     */
    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @XmlIDREF
    @XmlElement(required = true)
    private Category subCategory;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public Food() {
    }

    /**
     * Convenience constructor to create a Food entity with Swedish and English translations,
     * and using the
     *
     * @param swedishFoodName The name of the Food substance in Swedish. Cannot be null or empty.
     * @param englishFoodName The name of this Food substance in English. Cannot be null or empty.
     * @param category        The top-level categorization for this Food (substance).
     * @param subCategory     The second-level categorization for this Food (substance).
     */
    public Food(
            final String swedishFoodName,
            final String englishFoodName,
            final Category category,
            final Category subCategory) {

        // Assign internal state
        this.category = category;
        this.subCategory = subCategory;
        this.localizedFoodName = createFoodNameFor(swedishFoodName, englishFoodName);
    }

    /**
     * Compound constructor wrapping the supplied data.
     *
     * @param localizedFoodName The unique name of this Food.
     * @param category          The top-level categorization for this Food (substance).
     * @param subCategory       The second-level categorization for this Food (substance).
     */
    public Food(final LocalizedTexts localizedFoodName, final Category category, final Category subCategory) {

        // Assign internal state
        this.localizedFoodName = localizedFoodName;
        this.category = category;
        this.subCategory = subCategory;
    }

    /**
     * @return The LocalizedTexts containing the localized name of this Food.
     */
    public LocalizedTexts getLocalizedFoodName() {
        return localizedFoodName;
    }

    /**
     * @return The top-level categorization for this Food (substance).
     */
    public Category getCategory() {
        return category;
    }

    /**
     * @return The second-level categorization for this Food (substance).
     */
    public Category getSubCategory() {
        return subCategory;
    }

    /**
     * Assigns the top-level categorization for this Food (substance).
     *
     * @param category The non-null top-level categorization for this Food (substance).
     */
    public void setCategory(final Category category) {

        // Check sanity
        Validate.notNull(category, "Cannot handle null category argument.");

        // Assign internal state
        this.category = category;
    }

    /**
     * Assigns the second-level categorization for this Food (substance).
     *
     * @param subCategory the second-level categorization for this Food (substance).
     */
    public void setSubCategory(final Category subCategory) {

        // Check sanity
        Validate.notNull(subCategory, "Cannot handle null subCategory argument.");

        // Assign internal state
        this.subCategory = subCategory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Food that) {

        // Check sanity
        if (that == null) {
            return -1;
        } else if (this == that) {
            return 0;
        }

        // Delegate to internal state
        final String thisFoodName = this.getLocalizedFoodName().getText() == null
                ? ""
                : this.getLocalizedFoodName().getText();
        final String thatFoodName = that.getLocalizedFoodName().getText() == null
                ? ""
                : that.getLocalizedFoodName().getText();

        int result = thisFoodName.compareTo(thatFoodName);
        if (result == 0) {
            result = this.getCategory().compareTo(that.getCategory());
        }
        if (result == 0) {
            result = this.getSubCategory().compareTo(that.getSubCategory());
        }

        // All Done.
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        // Check sanity
        InternalStateValidationException.create()
                .notNull(localizedFoodName, "localizedFoodName")
                .notNull(category, "category")
                .notNull(subCategory, "subCategory")
                .endExpressionAndValidate();
    }

    /**
     * Convenience factory method retrieving a LocalizedTexts instance containing texts for
     * 2 LocaleDefinitions (swedish and english). The default LocaleDefinition for the LocalizedTexts retrieved is
     * Swedish and the suite identifier is created using the prefix {@link #FOOD_LOCALIZATION_SUITE_PREFIX} followed
     * by the english food name with all whitespace replaced by '_' characters.
     *
     * @param swedishFoodName The swedish food name.
     * @param englishFoodName The english food name.
     * @return The LocalizedTexts instance to use for the {@link #localizedFoodName} of a Food instance.
     */
    public static LocalizedTexts createFoodNameFor(
            final String swedishFoodName,
            final String englishFoodName) {

        // Check sanity
        Validate.notEmpty(swedishFoodName, "swedishFoodName");
        Validate.notEmpty(englishFoodName, "englishFoodName");

        // Create the LocalizedText, using the englishFoodName as key
        final String key = englishFoodName.trim().replaceAll(" ", "_");

        final LocalizedTexts toReturn = new LocalizedTexts(FOOD_LOCALIZATION_SUITE_PREFIX + key,
                LocaleDefinition.SWEDISH_LANGUAGE,
                swedishFoodName);
        toReturn.setText(LocaleDefinition.ENGLISH_LANGUAGE, englishFoodName);

        // All Done.
        return toReturn;
    }

    /**
     * Convenience factory method retrieving a Category with the {@code FOOD_CATEGORY_CLASSIFICATION} as classification.
     *
     * @param category         The non-empty category.
     * @param description      The non-empty description.
     * @param topLevelCategory if {@code true} the returned Category is a top-level categorization,
     *                         and otherwise a second-level (subCategory) categorization of Food.
     * @return The resulting (and non-managed/persisted) Category with the supplied category and description.
     */
    public static Category getFoodTypeCategory(final String category,
            final String description,
            final boolean topLevelCategory) {

        // Check sanity
        Validate.notEmpty(category, "category");
        Validate.notEmpty(description, "description");

        // All done.
        final String classification = topLevelCategory ? FOOD_CATEGORY_CLASSIFICATION : FOOD_SUBCATEGORY_CLASSIFICATION;
        return new Category(category, classification, description);
    }
}
