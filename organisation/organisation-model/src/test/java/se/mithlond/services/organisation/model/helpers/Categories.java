package se.mithlond.services.organisation.model.helpers;

import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.Patterns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class Categories {

    // Internal state
    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = true, required = false, name = "category")
    private List<Category> categories;

    public Categories() {
        this((Category) null);
    }

    public Categories(final Category... cats) {
        categories = new ArrayList<>();
        if (cats != null) {
            Collections.addAll(categories, cats);
        }
    }

    public List<Category> getCategories() {
        return categories;
    }
}
