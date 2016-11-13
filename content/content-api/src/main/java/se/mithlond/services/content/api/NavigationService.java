/*
 * #%L
 * Nazgul Project: mithlond-services-content-api
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
package se.mithlond.services.content.api;

import se.mithlond.services.content.model.navigation.integration.MenuStructure;
import se.mithlond.services.shared.authorization.api.GlobAuthorizationPattern;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPathProducer;
import se.mithlond.services.shared.authorization.api.UnauthorizedException;

import javax.ejb.Local;
import java.util.List;
import java.util.SortedSet;
import java.util.function.Function;

/**
 * Service specification for navigating resources or sites.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Local
public interface NavigationService {

    /**
     * The standard group required to update Navigation structures.
     */
    String UPDATE_GROUP = "editors";

    /**
     * The standard Function returning the AuthorizationPattern required to create or update
     * (i.e. perform write operations on) a MenuStructure. Note that this Function does not
     * gracefully handle {@code null} realmName values.
     */
    Function<String, SortedSet<GlobAuthorizationPattern>> REALM_AUTHORIZATION_PATTERN_FUNCTION =
            realmName -> GlobAuthorizationPattern.parse("/" + realmName + "/" + UPDATE_GROUP);

    /**
     * Retrieves the MenuStructure available to a caller sporting the supplied Memberships.
     *
     * @param realm            The realm (or organisation name) owning the site for which a MenuStructure
     *                         should be retrieved.
     * @param callersAuthPaths The SemanticAuthorizationPathProducer of the caller, used to determine which
     *                         MenuStructure items should be retrieved. Typically, each authPath is a Membership.
     * @return A fully set-up Menu structure.
     * @throws UnknownOrganisationException if the menuOwner was not the name of an existing organisation.
     */
    MenuStructure getMenuStructure(final String realm,
            final List<SemanticAuthorizationPathProducer> callersAuthPaths)
            throws UnknownOrganisationException;

    /**
     * Creates or updates the supplied MenuStructure for the supplied realm.
     * Note that the {@code realm} value must be equal to the {@link MenuStructure#getOrganisationName()}
     * value of the supplied MenuStructure.
     *
     * @param menuStructure    A non-null MenuStructure to set as the MenuStructure for the supplied
     * @param callersAuthPaths The SemanticAuthorizationPathProducer of the caller, used to determine which
     *                         MenuStructure items should be retrieved. Typically, each authPath is a Membership.
     * @return The Persisted/Managed/Updated MenuStructure.
     * @throws UnauthorizedException if the caller did not possess required privileges
     *                               to update the supplied MenuStructure.
     */
    MenuStructure createOrUpdate(final MenuStructure menuStructure,
            final List<SemanticAuthorizationPathProducer> callersAuthPaths)
            throws UnauthorizedException;

}
