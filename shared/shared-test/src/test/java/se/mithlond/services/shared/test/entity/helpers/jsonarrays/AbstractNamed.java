package se.mithlond.services.shared.test.entity.helpers.jsonarrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = "mithlond:shared:test:jsonarrays", propOrder = {"name"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractNamed {

    @XmlElement
    private String name;

    public AbstractNamed() {
    }

    public AbstractNamed(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
