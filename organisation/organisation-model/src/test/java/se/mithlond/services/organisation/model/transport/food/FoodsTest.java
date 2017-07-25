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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.food.Food;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FoodsTest extends AbstractEntityTest {

    // Shared state
    private FoodsAndCategories foodsAndCategories;

    @Before
    public void setupSharedState() {

        // #0) Configure the JAXB context
        //
        jaxb.add(Foods.class, FoodVO.class);
        jaxb.mapXmlNamespacePrefix(OrganisationPatterns.NAMESPACE, "organisation");

        // #1) Setup some food Categories
        //
        this.foodsAndCategories = new FoodsAndCategories();
    }

    @Test
    public void validateLocales() {

        // Language [da], Country [DK],  DisplayCountry [Danmark]
        final SortedMap<String, Locale> stringForm2LocaleMap = new TreeMap<>();
        Arrays.stream(DateFormat.getAvailableLocales())
                .forEach(current -> stringForm2LocaleMap.put(current.toString(), current));

        stringForm2LocaleMap.forEach((key, locale) -> System.out.println("Language [" + locale.getLanguage() + "], Country [" + locale.getCountry() + "], " +
                " DisplayCountry [" + locale.getDisplayCountry() + "]"));
    }

    @Test
    public void validateMarshallingShallowToXML() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/food/shallowFoods.xml");
        final Foods unitUnderTest = new Foods(true,
                TimeFormat.SWEDISH_LOCALE,
                foodsAndCategories.cauliflower,
                foodsAndCategories.carrot,
                foodsAndCategories.beetroot);

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
                TimeFormat.SWEDISH_LOCALE,
                foodsAndCategories.cauliflower,
                foodsAndCategories.carrot,
                foodsAndCategories.beetroot);

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
                TimeFormat.SWEDISH_LOCALE,
                foodsAndCategories.cauliflower,
                foodsAndCategories.carrot,
                foodsAndCategories.beetroot);

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
                TimeFormat.SWEDISH_LOCALE,
                foodsAndCategories.cauliflower,
                foodsAndCategories.carrot,
                foodsAndCategories.beetroot);

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

    private static String getArrayIndex(final String[] array, final int index) {
        return array.length > (index - 1) ? array[index] : "";
    }

    private void validateFoods(final Foods foods, final boolean shallow) {

        Assert.assertNotNull(foods);

        // #0) Check the locale definition
        final Locale theLocale = foods.getLocale();
        Assert.assertEquals(TimeFormat.SWEDISH_LOCALE, theLocale);

        // #1) Check the categories
        //
        //          Cat: food_category --> Grönsaker
        //          SubCat: food_subcategory --> Diverse
        //          SubCat: food_subcategory --> Rotfrukter och Betor
        //
        final SortedSet<Category> categories = foods.getCategories();
        final SortedSet<Category> subCategories = foods.getSubCategories();

        Assert.assertEquals(1, categories.size());
        Assert.assertEquals(2, subCategories.size());

        Assert.assertEquals(foodsAndCategories.vegetables, categories.first());
        Stream.of(foodsAndCategories.rootsAndBeets, foodsAndCategories.diverse)
                .forEach(cat -> Assert.assertTrue("SubCategory [" + cat + "] not found.",
                        subCategories.contains(cat)));

        // #2) Check the foods
        final Stream<Food> foodStream = Stream.of(foodsAndCategories.cauliflower,
                foodsAndCategories.carrot,
                foodsAndCategories.beetroot);

        if (shallow) {

            Assert.assertTrue(foods.getDetailedFoods().isEmpty());

            final List<FoodVO> shallowFoods = foods.getFoods();
            foodStream.map(food -> new FoodVO(food, theLocale))
                    .forEach(shallowFood -> Assert.assertTrue(shallowFoods.contains(shallowFood)));

        } else {

            Assert.assertTrue(foods.getFoods().isEmpty());

            final List<Food> detailedFoods = foods.getDetailedFoods();
            foodStream.forEach(food -> Assert.assertTrue(detailedFoods.contains(food)));
        }
    }
}
