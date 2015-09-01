/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 Mithlond
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
package se.mithlond.services.organisation.model.helpers;

import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.organisation.model.address.CategorizedAddress;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlType(propOrder = {"organisations", "categories", "categorizedAddresses"})
@XmlAccessorType(XmlAccessType.FIELD)
public class CategorizedAddresses {

    // Internal state
    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = false, required = true, name = "organisation")
    private List<Organisation> organisations;

    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = false, required = true, name = "category")
    private List<Category> categories;

    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = true, required = false, name = "categorizedAddress")
    private List<CategorizedAddress> categorizedAddresses;

    public CategorizedAddresses() {
        this((CategorizedAddress) null);
    }

    public CategorizedAddresses(final CategorizedAddress... cats) {

        organisations = new ArrayList<>();
        categorizedAddresses = new ArrayList<>();
        categories = new ArrayList<>();

        if (cats != null) {
            Arrays.asList(cats).forEach(current -> {
                if (current != null) {
                    categorizedAddresses.add(current);
                }
            });
        }

        if (categorizedAddresses != null && categorizedAddresses.size() > 0) {

            // Harvest all unique organisations and categories, since they must be
            // written before the CategorizedAddresses that refer them.
            final SortedMap<String, Organisation> organisationMap = new TreeMap<>();
            final SortedMap<String, Category> categoryMap = new TreeMap<>();

            categorizedAddresses.forEach(current -> {
                final Organisation currentOrg = current.getOwningOrganisation();
                final Category currentCategory = current.getCategory();
                organisationMap.put(currentOrg.getOrganisationName(), currentOrg);
                categoryMap.put(currentCategory.toString(), currentCategory);
            });

            for (Map.Entry<String, Organisation> current : organisationMap.entrySet()) {
                organisations.add(current.getValue());
            }

            for (Map.Entry<String, Category> current : categoryMap.entrySet()) {
                categories.add(current.getValue());
            }
        }
    }

    public List<CategorizedAddress> getCategorizedAddresses() {
        return categorizedAddresses;
    }

    public List<Organisation> getOrganisations() {
        return organisations;
    }

    public List<Category> getCategories() {
        return categories;
    }

    /**
     * JAXB callback method invoked after this instance is Unmarshalled.
     * This is the gracious JAXB instantiation sledge hammer...
     *
     * @param unmarshaller The unmarshaller used to perform the unmarshalling.
     */
    @SuppressWarnings("PMD")
    private void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {

        /*
        // Re-assign the XmlTransient collections.
        for (GuildMembership current : guildMemberships) {
            current.setMembership(this);
        }
        for (OrderLevelGrant current : orderLevelGrants) {
            current.setMembership(this);
        }

        if (log.isDebugEnabled()) {
            final String parentObjectType = parent == null ? "<null>" : parent.getClass().getName();
            log.debug("Got parent object of type: " + parentObjectType);
        }
        */
    }
}
