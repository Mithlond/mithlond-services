package se.mithlond.services.content.model.articles;

import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Markup (HTML, XML etc.) can be stored easily as a String or CLOB within a
 * relational database. However, embedding markup in XML is more tricky, and
 * this is a helper class to assist in wrapping markup in a CDATA block to
 * protect it during transport.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"content"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Markup {

    // Internal state
    @XmlAnyElement
    private String content;

    /**
     * JAXB-friendly constructor.
     */
    public Markup() {
    }

    /**
     * Compound constructor creating a Markup instance wrapping the supplied data.
     *
     * @param content A non-null markup-formatted string.
     */
    public Markup(final String content) {

        // Check sanity
        Validate.notNull(content, "content");

        // Assign internal state
        this.content = content;
    }

    /**
     * @return A non-null markup-formatted string.
     */
    public String getContent() {
        return content;
    }
}
