package se.mithlond.services.organisation.model.transport.food;

import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.food.Food;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FoodsAndCategories {

    public Category vegetables, rootsAndBeets, diverse;
    public Food cauliflower, carrot, beetroot;

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
    }
}
