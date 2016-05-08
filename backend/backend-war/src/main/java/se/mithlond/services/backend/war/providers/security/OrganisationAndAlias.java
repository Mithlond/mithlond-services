/*
 * #%L
 * Nazgul Project: mithlond-services-backend-war
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
package se.mithlond.services.backend.war.providers.security;

import java.io.Serializable;

/**
 * Holder for an organisation name and an alias.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class OrganisationAndAlias implements Serializable {

    // Internal state
    private String organisationName;
    private String alias;

    /**
     * Creates a new OrganisationAndAlias instance wrapping the supplied data.
     *
     * @param organisationName The name of the organisation.
     * @param alias            The alias of the caller.
     */
    public OrganisationAndAlias(final String organisationName,
                                final String alias) {
        this.organisationName = organisationName;
        this.alias = alias;
    }

    /**
     * @return The organisation name.
     */
    public String getOrganisationName() {
        return organisationName;
    }

    /**
     * @return The alias.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OrganisationAndAlias [Org: " + organisationName + ", Alias: " + alias + "]";
    }
}
