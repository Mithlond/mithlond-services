package se.mithlond.services.content.model.localization.helpers;

import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.content.model.localization.Localization;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlType(namespace = Patterns.NAMESPACE, propOrder = "localizations")
@XmlAccessorType(XmlAccessType.FIELD)
public class Localizations {

    // Internal state
    @XmlElementWrapper
    @XmlElement(name = "localization")
    private List<Localization> localizations;

    public Localizations() {
        this.localizations = new ArrayList<>();
    }

    public Localizations(final List<Localization> localizations) {
        this();
        this.localizations.addAll(localizations);
    }

    public List<Localization> getLocalizations() {
        return localizations;
    }
}
