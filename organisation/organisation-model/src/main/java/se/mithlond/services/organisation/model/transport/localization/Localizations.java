/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.mithlond.services.organisation.model.transport.localization;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.localization.LocaleDefinition;
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
import java.util.SortedSet;

/**
 * Transport object for Localizations and Localized texts.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE,
        propOrder = {"localeDefinitions", "localizedTexts", "localizedTextVOs"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Localizations extends AbstractSimpleTransporter {

    /**
     * A List containing the {@link LocaleDefinition} objects.
     */
    @XmlElementWrapper
    @XmlElement(name = "localization")
    private List<LocaleDefinition> localeDefinitions;

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
        this.localeDefinitions = new ArrayList<>();
        this.localizedTextVOs = new ArrayList<>();
        this.localizedTexts = new ArrayList<>();
    }

    /**
     * Retrieves an unmodifiable List containing the {@link LocaleDefinition} objects transported.
     *
     * @return an unmodifiable List containing the {@link LocaleDefinition} objects transported. Never null.
     */
    public List<LocaleDefinition> getLocaleDefinitions() {
        return Collections.unmodifiableList(localeDefinitions);
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
     * Adds the supplied {@link LocaleDefinition} objects to this transport.
     *
     * @param toAdd one or more objects to add.
     */
    public void addLocalizations(final LocaleDefinition... toAdd) {

        if (toAdd != null) {
            Arrays.asList(toAdd).stream()
                    .filter(c -> c != null)
                    .filter(c -> !localeDefinitions.contains(c))
                    .forEach(c -> localeDefinitions.add(c));
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

                        final SortedSet<LocaleDefinition> containedLocales = c.getContainedLocalizations();
                        if (containedLocales != null && !containedLocales.isEmpty()) {
                            addLocalizations(
                                    containedLocales.toArray(
                                            new LocaleDefinition[containedLocales.size()]));
                        }

                        addLocalizations();
                        localizedTexts.add(c);
                    });
        }
    }
}
