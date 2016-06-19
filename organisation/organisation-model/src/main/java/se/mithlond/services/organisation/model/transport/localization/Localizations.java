package se.mithlond.services.organisation.model.transport.localization;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.localization.Localization;
import se.mithlond.services.organisation.model.localization.LocalizedTexts;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

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

/**
 * Transport object for Localizations and Localized texts.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE,
        propOrder = {"localizations", "localizedTexts", "localizedTextVOs"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Localizations extends AbstractSimpleTransporter {

    /**
     * A List containing the {@link Localization} objects.
     */
    @XmlElementWrapper
    @XmlElement(name = "localization")
    private List<Localization> localizations;

    /**
     * A List holding shallow/VO {@link LocalizedTextVO} objects.
     */
    @XmlElementWrapper
    @XmlElement(name = "localizedText")
    private List<LocalizedTextVO> localizedTextVOs;

    /**
     * A List holding full-state {@link LocalizedTexts} objects.
     */
    @XmlElementWrapper
    @XmlElement(name = "localization")
    private List<LocalizedTexts> localizedTexts;

    /**
     * JAXB-friendly constructor
     */
    public Localizations() {
        this.localizations = new ArrayList<>();
        this.localizedTextVOs = new ArrayList<>();
        this.localizedTexts = new ArrayList<>();
    }

    /**
     * Retrieves an unmodifiable List containing the {@link Localization} objects transported.
     *
     * @return an unmodifiable List containing the {@link Localization} objects transported. Never null.
     */
    public List<Localization> getLocalizations() {
        return Collections.unmodifiableList(localizations);
    }

    /**
     * Retrieves an unmodifiable List containing the {@link LocalizedTextVO} objects transported.
     *
     * @return an unmodifiable List containing the {@link LocalizedTextVO} objects transported. Never null.
     */
    public List<LocalizedTextVO> getLocalizedTextVOs() {
        return Collections.unmodifiableList(localizedTextVOs);
    }

    /**
     * Retrieves an unmodifiable List containing the {@link LocalizedTexts} objects transported.
     *
     * @return an unmodifiable List containing the {@link LocalizedTexts} objects transported. Never null.
     */
    public List<LocalizedTexts> getLocalizedTexts() {
        return Collections.unmodifiableList(localizedTexts);
    }

    /**
     * Adds the supplied {@link Localization} objects to this transport.
     *
     * @param toAdd one or more objects to add.
     */
    public void addLocalizations(final Localization... toAdd) {

        if (toAdd != null) {
            Arrays.asList(toAdd).stream()
                    .filter(c -> c != null)
                    .filter(c -> !localizations.contains(c))
                    .forEach(c -> localizations.add(c));
        }
    }

    /**
     * Adds the supplied {@link LocalizedTextVO} objects to this transport.
     *
     * @param toAdd one or more objects to add.
     */
    public void addLocalizedTextVOs(final LocalizedTextVO... toAdd) {

        if (toAdd != null) {
            Arrays.asList(toAdd).stream()
                    .filter(c -> c != null)
                    .filter(c -> !localizedTextVOs.contains(c))
                    .forEach(c -> localizedTextVOs.add(c));
        }
    }

    /**
     * Adds the supplied {@link LocalizedTexts} objects to this transport.
     *
     * @param toAdd one or more objects to add.
     */
    public void addLocalizedTexts(final LocalizedTexts... toAdd) {

        if (toAdd != null) {
            Arrays.asList(toAdd).stream()
                    .filter(c -> c != null)
                    .filter(c -> !localizedTexts.contains(c))
                    .forEach(c -> {

                        final List<Localization> containedLocalizations = c.getContainedLocalizations();
                        if (containedLocalizations != null && !containedLocalizations.isEmpty()) {
                            addLocalizations(
                                    containedLocalizations.toArray(
                                            new Localization[containedLocalizations.size()]));
                        }

                        addLocalizations();
                        localizedTexts.add(c);
                    });
        }
    }
}
