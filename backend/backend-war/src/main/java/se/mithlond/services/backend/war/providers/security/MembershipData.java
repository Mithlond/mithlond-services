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
public class MembershipData implements Serializable {

    // Internal state
    private String organisationName;
    private String firstName;
    private String lastName;
    private String userIdentifierToken;

    public MembershipData(final String organisationName,
                          final String firstName,
                          final String lastName,
                          final String userIdentifierToken) {

        this.organisationName = organisationName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userIdentifierToken = userIdentifierToken;
    }

    /**
     * Retrieves the Organisation Name.
     *
     * @return the Organisation Name.
     */
    public String getOrganisationName() {
        return organisationName;
    }

    /**
     * Retrieves the First Name of the caller.
     *
     * @return the First Name of the caller.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Retrieves the lastName of the caller.
     *
     * @return the lastName of the caller.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Retrieves the UserIdentificationToken of the caller.
     *
     * @return the UserIdentificationToken of the caller.
     */
    public String getUserIdentifierToken() {
        return userIdentifierToken;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "MembershipData [Org: " + getOrganisationName()
                + ", FirstName: " + getFirstName()
                + ", LastName: " + getLastName()
                + ", UserIdToken: " + getUserIdentifierToken()
                + "]";
    }
}
