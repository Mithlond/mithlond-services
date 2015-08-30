package se.mithlond.services.organisation.model;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.helpers.Categories;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CategoryTest extends AbstractEntityTest {

    // Shared state
    private List<Category> categories;
    private Category[] categoryArray;

    @Before
    public void setupSharedState() {
        categories = new ArrayList<>();

        for(int i = 0; i < 10; i++) {
            categories.add(new Category("categoryID_" + i, "classification_" + i, "description_" + i));
        }

        categoryArray = categories.toArray(new Category[categories.size()]);
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/categories.xml");

        // Act
        final String result = marshal(new Categories(categoryArray));

        // Assert
        // System.out.println("Got: " + result);
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/categories.xml");
        jaxb.add(Categories.class);

        // Act
        final Categories result = unmarshal(Categories.class, data);

        // Assert
        Assert.assertNotNull(result);

        final List<Category> resurrected = result.getCategories();
        Assert.assertEquals(categories.size(), resurrected.size());

        for(int i = 0; i < categories.size(); i++) {

            final Category expected = categories.get(i);
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
