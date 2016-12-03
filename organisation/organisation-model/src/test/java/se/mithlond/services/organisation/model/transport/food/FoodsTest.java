package se.mithlond.services.organisation.model.transport.food;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.food.Food;
import se.mithlond.services.organisation.model.localization.LocaleDefinition;
import se.mithlond.services.organisation.model.localization.Localizable;

import java.util.List;
import java.util.SortedSet;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FoodsTest extends AbstractEntityTest {

    // Shared state
    private Category vegetables, rootsAndBeets, diverse;
    private Food cauliflower, carrot, beetroot;

    @Before
    public void setupSharedState() {

        // #0) Configure the JAXB context
        //
        jaxb.add(Foods.class);

        // #1) Setup some food Categories
        //
        vegetables = Food.getFoodTypeCategory("Grönsaker",
                "Grönsaker och Rotfrukter",
                true);
        setJpaIDFor(vegetables, 12);

        rootsAndBeets = Food.getFoodTypeCategory("Rotfrukter och Betor",
                "Rotfrukter; växer under jord",
                false);
        setJpaIDFor(rootsAndBeets, 25);

        diverse = Food.getFoodTypeCategory("Diverse",
                "Diverse / övrigt",
                false);
        setJpaIDFor(diverse, 20);

        // #2) Create some Food substances within each category
        //
        beetroot = new Food("Rödbeta", "Beetroot", vegetables, rootsAndBeets);
        setJpaIDFor(beetroot, 215);

        carrot = new Food("Morot", "Carrot", vegetables, rootsAndBeets);
        setJpaIDFor(carrot, 36);

        cauliflower = new Food("Blomkål", "Cauliflower", vegetables, diverse);
        setJpaIDFor(cauliflower, 24);
    }

    @Test
    public void validateMarshallingShallowToXML() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/food/shallowFoods.xml");
        final Foods unitUnderTest = new Foods(true,
                LocaleDefinition.SWEDISH_LANGUAGE,
                cauliflower,
                carrot,
                beetroot);

        // Act
        final String result = marshalToXML(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateMarshallingToShallowJSON() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/food/shallowFoods.json");
        final Foods unitUnderTest = new Foods(true,
                LocaleDefinition.SWEDISH_LANGUAGE,
                cauliflower,
                carrot,
                beetroot);

        // Act
        final String result = marshalToJSon(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void validateMarshallingDetailedToXML() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/food/detailedFoods.xml");
        final Foods unitUnderTest = new Foods(false,
                LocaleDefinition.SWEDISH_LANGUAGE,
                cauliflower,
                carrot,
                beetroot);

        // Act
        final String result = marshalToXML(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateMarshallingToDetailedJSON() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/food/detailedFoods.json");
        final Foods unitUnderTest = new Foods(false,
                LocaleDefinition.SWEDISH_LANGUAGE,
                cauliflower,
                carrot,
                beetroot);

        // Act
        final String result = marshalToJSon(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void validateUnmarshallingFromDetailedJSon() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/transport/food/detailedFoods.json");

        // Act
        final Foods resurrected = unmarshalFromJSON(Foods.class, data);

        // Assert
        validateFoods(resurrected, false);
    }

    @Test
    public void validateUnmarshallingFromShallowJSon() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/transport/food/shallowFoods.json");

        // Act
        final Foods resurrected = unmarshalFromJSON(Foods.class, data);

        // Assert
        validateFoods(resurrected, true);
    }

    @Test
    public void validateUnmarshallingFromShallowXML() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/transport/food/shallowFoods.xml");

        // Act
        final Foods resurrected = unmarshalFromXML(Foods.class, data);

        // Assert
        validateFoods(resurrected, true);
    }

    @Test
    public void validateUnmarshallingFromDetailedXML() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/transport/food/detailedFoods.xml");

        // Act
        final Foods resurrected = unmarshalFromXML(Foods.class, data);

        // Assert
        validateFoods(resurrected, false);
    }

    //
    // Private helpers
    //

    private void validateFoods(final Foods foods, final boolean shallow) {

        Assert.assertNotNull(foods);

        // #0) Check the locale definition
        final LocaleDefinition localeDefinition = foods.getLocaleDefinition();
        Assert.assertEquals(Localizable.DEFAULT_LOCALE, localeDefinition);

        // #1) Check the categories
        final SortedSet<Category> categories = foods.getCategories();
        Stream.of(vegetables, rootsAndBeets, diverse)
                .forEach(cat -> Assert.assertTrue("Category [" + cat + "] not found.",
                        categories.contains(cat)));

        // #2) Check the foods
        final Stream<Food> foodStream = Stream.of(this.cauliflower, carrot, beetroot);

        if(shallow) {

            Assert.assertTrue(foods.getDetailedFoods().isEmpty());

            final List<FoodVO> shallowFoods = foods.getFoods();
            foodStream.map(food -> new FoodVO(food, localeDefinition))
                    .forEach(shallowFood -> Assert.assertTrue(shallowFoods.contains(shallowFood)));

        } else {

            Assert.assertTrue(foods.getFoods().isEmpty());

            final List<Food> detailedFoods = foods.getDetailedFoods();
            foodStream.forEach(food -> Assert.assertTrue(detailedFoods.contains(food)));
        }
    }
}
