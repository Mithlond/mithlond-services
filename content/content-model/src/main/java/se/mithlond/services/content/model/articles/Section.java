package se.mithlond.services.content.model.articles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.mithlond.services.content.model.ContentPatterns;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * Section within an Article or document.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = ContentPatterns.NAMESPACE, propOrder = {"heading", "text"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Section extends NazgulEntity {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(Section.class);

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement
    private String heading;

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlAttribute
    private boolean showHeading;

    @Basic(optional = false)
    @Column(nullable = false, length = 4096)
    @XmlElement
    private String text;

    @ManyToOne(optional = false)
    @XmlTransient
    private Article article;

    @ManyToMany
    @XmlElementWrapper
    @XmlElement(name = "image")
    private List<LayoutableImage> images;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public Section() {
    }

    /**
     * Compound constructor creating a Section wrapping the supplied data.
     *
     * @param article The article to which this Section belongs.
     * @param heading The heading of this Section.
     * @param text
     */
    public Section(final Article article,
            final String heading,
            final String text) {

        super(article.getCreatedAt(), createdBy);
        this.heading = heading;
        this.text = text;
    }
}
