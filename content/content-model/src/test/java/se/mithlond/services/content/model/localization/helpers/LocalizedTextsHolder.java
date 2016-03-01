package se.mithlond.services.content.model.localization.helpers;

import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.content.model.localization.Localization;
import se.mithlond.services.content.model.localization.LocalizedTexts;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"localizations", "localizedTextsList"})
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalizedTextsHolder {

    // Internal state

    @XmlElementWrapper
    @XmlElement(name = "localization")
    private Set<Localization> localizations;

    @XmlElementWrapper
    @XmlElement(name = "localization")
    private List<LocalizedTexts> localizedTextsList;

    public LocalizedTextsHolder() {
        this.localizedTextsList= new ArrayList<>();
        this.localizations = new TreeSet<>();
    }

    public LocalizedTextsHolder(final List<LocalizedTexts> localizedTextsList) {
        this();
        this.localizedTextsList.addAll(localizedTextsList);
    }

    public void addAll(final List<LocalizedTexts> toAdd) {
        if(toAdd != null) {
            for(LocalizedTexts current : toAdd) {

                final Set<Localization> locs = current.getContainedLocalizations()
                        .stream()
                        .distinct()
                        .collect(Collectors.toSet());

                localizations.addAll(locs.stream()
                        .filter(currentLocalization -> !localizations.contains(currentLocalization))
                        .collect(Collectors.toSet()));

                this.localizedTextsList.add(current);
            }
        }
    }

    public void addAll(final LocalizedTexts ... toAdd) {
        if(toAdd != null && toAdd.length > 0) {
            addAll(Arrays.asList(toAdd));
        }
    }

    public List<LocalizedTexts> getLocalizedTextsList() {
        return Collections.unmodifiableList(localizedTextsList);
    }

    public Set<Localization> getLocalizations() {
        return localizations;
    }
}
