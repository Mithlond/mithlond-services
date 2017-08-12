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
package se.mithlond.services.organisation.model.transport.food;

import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.food.Food;
import se.mithlond.services.organisation.model.food.FoodPreference;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FoodsAndCategories {

    public Category vegetables, rootsAndBeets, diverse;
    public Food cauliflower, carrot, beetroot;
    public Category milkCategory, meatsCategory;

    public FoodsAndCategories() {

        // #1) Setup some food Categories
        //
        vegetables = Food.getFoodTypeCategory("Grönsaker",
                "Grönsaker och Rotfrukter",
                true);
        AbstractEntityTest.setJpaIDFor(vegetables, 12);

        rootsAndBeets = Food.getFoodTypeCategory("Rotfrukter och Betor",
                "Rotfrukter; växer under jord",
                false);
        AbstractEntityTest.setJpaIDFor(rootsAndBeets, 25);

        diverse = Food.getFoodTypeCategory("Diverse",
                "Diverse / övrigt",
                false);
        AbstractEntityTest.setJpaIDFor(diverse, 20);

        // #2) Create some Food substances within each category
        //
        beetroot = new Food("Rödbeta", "Beetroot", vegetables, rootsAndBeets);
        AbstractEntityTest.setJpaIDFor(beetroot, 215);
        AbstractEntityTest.setJpaIDFor(beetroot.getLocalizedFoodName(), 300);

        carrot = new Food("Morot", "Carrot", vegetables, rootsAndBeets);
        AbstractEntityTest.setJpaIDFor(carrot, 36);
        AbstractEntityTest.setJpaIDFor(carrot.getLocalizedFoodName(), 301);

        cauliflower = new Food("Blomkål", "Cauliflower", vegetables, diverse);
        AbstractEntityTest.setJpaIDFor(cauliflower, 24);
        AbstractEntityTest.setJpaIDFor(cauliflower.getLocalizedFoodName(), 302);

        // #3) Create some Categories for FoodPreferences.
        //
        milkCategory = new Category("Mjölk",
                FoodPreference.FOOD_PREFERENCE_CATEGORY_CLASSIFICATION,
                "Äter inte mjölkprodukter");
        AbstractEntityTest.setJpaIDFor(milkCategory, 43);

        meatsCategory = new Category("Kött",
                FoodPreference.FOOD_PREFERENCE_CATEGORY_CLASSIFICATION,
                "Äter inte något kött");
        AbstractEntityTest.setJpaIDFor(meatsCategory, 39);
    }
}
