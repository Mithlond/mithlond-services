package se.mithlond.services.content.api.transport;

import se.mithlond.services.content.model.Patterns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Holder for a List of markup-sporting Articles.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class Articles implements Serializable {

    // Internal state
    @XmlAttribute(required = true)
    private String realm;
}
