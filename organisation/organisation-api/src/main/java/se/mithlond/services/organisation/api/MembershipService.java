/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-api
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
package se.mithlond.services.organisation.api;

import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.jpa.JpaCudService;

import javax.ejb.Local;
import java.util.List;

/**
 * Service specification for Memberships and corresponding things.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Local
public interface MembershipService extends JpaCudService {

    /**
     * Retrieves all Memberships within the named Organisation, including the Memberships with
     * the "Login not permitted" flag set, if so indicated.
     *
     * @param organisationJpaID        The JpaID of the organisation for which all Memberships should be retrieved.
     * @param includeLoginNotPermitted if {@code true}, all Memberships will be retrieved. if {@code false}, only
     *                                 Memberships with the {@code loginPermitted} flag set to true will be included
     *                                 in the result.
     * @return All Memberships within the supplied organisation. if the {@code includeLoginNotPermitted} flag is set
     * to false only Memberships with the {@code loginPermitted} flag set to true will be included in the result
     */
    List<Membership> getMembershipsIn(final Long organisationJpaID, final boolean includeLoginNotPermitted);

    /**
     * Retrieves the Membership corresponding to the supplied organisation name and alias.
     *
     * @param organisationName The non-empty organisation name in which the retrieved Membership exists.
     * @param alias            The alias of the user for which a Membership should be retrieved.
     * @return The Membership corresponding to the supplied data, or {@code null} if none was found.
     */
    Membership getMembership(final String organisationName, final String alias);

    /**
     * Retrieves the active Memberships (i.e. Memberships permitted login) within the named Organisation for the User
     * with the supplied first and last names.
     *
     * @param organisationName The non-empty name of the Organisation in which the retrieved Membership should exist.
     *                         Accepts JPQL wildcards.
     * @param firstName        The non-empty first name of the User with the retrieved Membership.
     *                         Accepts JPQL wildcards.
     * @param lastName         The non-empty last name of the User with the retrieved Membership.
     *                         Accepts JPQL wildcards.
     * @return The Memberships corresponding to the supplied data, or an empty List if none was found.
     */
    List<Membership> getActiveMemberships(final String organisationName, final String firstName, final String lastName);
}
