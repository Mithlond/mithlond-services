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
package se.mithlond.services.organisation.model.localization.helpers;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.localization.LocaleDefinition;
import se.mithlond.services.organisation.model.localization.LocalizedTexts;

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
@XmlRootElement(namespace = OrganisationPatterns.NAMESPACE)
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"localeDefinitions", "localizedTextsList"})
@XmlAccessorType(XmlAccessType.FIELD)
public class LocalizedTextsHolder {

    // Internal state

    @XmlElementWrapper
    @XmlElement(name = "localization")
    private Set<LocaleDefinition> localeDefinitions;

    @XmlElementWrapper
    @XmlElement(name = "localization")
    private List<LocalizedTexts> localizedTextsList;

    public LocalizedTextsHolder() {
        this.localizedTextsList = new ArrayList<>();
        this.localeDefinitions = new TreeSet<>();
    }

    public LocalizedTextsHolder(final List<LocalizedTexts> localizedTextsList) {
        this();
        this.localizedTextsList.addAll(localizedTextsList);
    }

    public void addAll(final List<LocalizedTexts> toAdd) {
        if (toAdd != null) {
            for (LocalizedTexts current : toAdd) {

                final Set<LocaleDefinition> locs = current.getContainedLocalizations()
                        .stream()
                        .distinct()
                        .collect(Collectors.toSet());

                localeDefinitions.addAll(locs.stream()
                        .filter(currentLocalization -> !localeDefinitions.contains(currentLocalization))
                        .collect(Collectors.toSet()));

                this.localizedTextsList.add(current);
            }
        }
    }

    public void addAll(final LocalizedTexts... toAdd) {
        if (toAdd != null && toAdd.length > 0) {
            addAll(Arrays.asList(toAdd));
        }
    }

    public List<LocalizedTexts> getLocalizedTextsList() {
        return Collections.unmodifiableList(localizedTextsList);
    }

    public Set<LocaleDefinition> getLocaleDefinitions() {
        return localeDefinitions;
    }
}
