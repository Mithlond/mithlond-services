package se.mithlond.services.shared.spi.jaxb.adapter;

import org.eclipse.persistence.oxm.annotations.XmlCDATA;
import se.mithlond.services.shared.spi.jaxb.SharedJaxbPatterns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = SharedJaxbPatterns.NAMESPACE)
@XmlType(namespace = SharedJaxbPatterns.NAMESPACE,
        propOrder = {"markup"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ArticleExampleVO {

    @XmlElement
    @XmlCDATA
    private String markup;

    public ArticleExampleVO() {
    }

    public ArticleExampleVO(final String markup) {
        this.markup = markup;
    }

    public String getMarkup() {
        return markup;
    }
}
