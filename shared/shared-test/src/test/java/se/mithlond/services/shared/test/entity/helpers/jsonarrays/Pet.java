package se.mithlond.services.shared.test.entity.helpers.jsonarrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = "mithlond:shared:test:jsonarrays")
@XmlAccessorType(XmlAccessType.FIELD)
public class Pet extends AbstractNamed {

    public Pet() {
    }

    public Pet(final String name) {
        super(name);
    }
}
