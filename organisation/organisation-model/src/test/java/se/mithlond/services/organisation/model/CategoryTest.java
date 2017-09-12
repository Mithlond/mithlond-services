/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 Mithlond
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
package se.mithlond.services.organisation.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.helpers.Categories;
import se.mithlond.services.shared.test.entity.JpaIdMutator;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CategoryTest extends AbstractEntityTest {

    // Shared state
    private SortedSet<Category> categories;
    private Category[] categoryArray;

    @Before
    public void setupSharedState() {

        final String[][] data = {{"1", "Café", "Café eller konditori"},
                {"2", "Pub / Restaurang", "Pub eller Restaurang"},
                {"3", "Affär", "Affär / Butik"},
                {"4", "Samlingslokal", "Lokal för gillesmöten eller fest"},
                {"8", "Hemadress", "Hemma hos Inbyggare"},
                {"44", "Utomhus", "Utomhus eller friluftsliv"}};

        categories = Stream.of(data)
                .map(theArray -> {

                    // Create the Category.
                    final Category toReturn = new Category(theArray[1], "activity_locale", theArray[2]);
                    JpaIdMutator.setId(toReturn, Long.parseLong(theArray[0]));

                    // All Done.
                    return toReturn;
                })
                .collect(Collectors.toCollection(TreeSet::new));

        categoryArray = categories.toArray(new Category[categories.size()]);
    }

    @Test
    public void validateMarshallingToXML() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/categories.xml");

        // Act
        final String result = marshalToXML(new Categories(categoryArray));

        // Assert
        // System.out.println("Got: " + result);
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateMarshallingToJSON() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/categories.json");

        // Act
        final String result = marshalToJSon(new Categories(categoryArray));

        // Assert
        // System.out.println("Got: " + result);
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/categories.xml");
        jaxb.add(Categories.class);

        // Act
        final Categories result = unmarshalFromXML(Categories.class, data);

        // Assert
        Assert.assertNotNull(result);

        final List<Category> resurrected = result.getCategories();
        Assert.assertEquals(categories.size(), resurrected.size());

        for (int i = 0; i < categories.size(); i++) {

            final Category expected = categoryArray[i];
            final Category actual = resurrected.get(i);

            Assert.assertNotSame(expected, actual);
            Assert.assertEquals(expected, actual);
        }
    }

    @Test
    public void validateEqualityAndComparison() {

        // Assemble
        final Category home1 = new Category("home", "At home", "At home");
        final Category home2 = new Category("home", "At home", "At home");
        final Category home3 = new Category("home", "At home", "My Home");

        // Act & Assert
        Assert.assertEquals(home1, home2);
        Assert.assertNotSame(home1, home2);
        Assert.assertEquals(home1.hashCode(), home2.hashCode());
        Assert.assertNotEquals(home1, home3);
        Assert.assertEquals(0, home1.compareTo(home2));
        Assert.assertEquals("At home".compareTo("My Home"), home1.compareTo(home3));
        Assert.assertNotEquals(home3, null);
    }
}
