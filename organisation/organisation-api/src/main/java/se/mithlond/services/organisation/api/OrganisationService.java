/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-api
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

import se.mithlond.services.organisation.api.parameters.GroupIdSearchParameters;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.shared.spi.jpa.JpaCudService;

import javax.ejb.Local;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Optional;

/**
 * <p>Service specification for queries extracting information from Organisations - without
 * any particular Membership origination or input to the service queries.</p>
 * <p>For queries related to Memberships and Members, refer to the MemberService.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see MembershipService
 */
@Local
public interface OrganisationService extends JpaCudService {

    /**
     * Retrieves all known Organisations.
     *
     * @return all known Organisations.
     */
    List<Organisation> getOrganisations();

    /**
     * Retrieves the organisation with the supplied name.
     *
     * @param organisationName The name of the organisation that should be retrieved.
     * @return the organisation with the supplied name, or an empty Optional if no organisation
     * with the supplied name was found.
     */
    Optional<Organisation> getOrganisation(@NotNull String organisationName);

    /**
     * Retrieves all Groups matching the supplied searchParameters.
     *
     * @param searchParameters The GroupIdSearchParameters populated with the IDs of the
     *                         groups (or organisations) which should be retrieved.
     * @return all Groups matching the supplied searchParameters.
     */
    List<Group> getGroups(@NotNull GroupIdSearchParameters searchParameters);

    /**
     * Retrieves all CategorizedAddresses matching the supplied searchParameters.
     *
     * @param searchParameters The GroupIdSearchParameters populated with the IDs of the
     *                         groups (or organisations) which should be retrieved.
     * @return all CategorizedAddresses matching the supplied searchParameters.
     */
    List<CategorizedAddress> getCategorizedAddresses(@NotNull GroupIdSearchParameters searchParameters);

    /**
     * Retrieves all Categories matching the supplied classification.
     *
     * @param classification The classification of the Categories to retrieve.
     * @return all Categories matching the supplied classification.
     */
    List<Category> getCategoriesByClassification(@NotNull String classification);

    /**
     * Updates the supplied CategorizedAddress within the database.
     *
     * @param toUpdate The CategorizedAddress to update.
     * @return The updated/merged CategorizedAddress.
     */
    CategorizedAddress updateCategorizedAddress(final CategorizedAddress toUpdate);

    /**
     * Creates a CategorizedAddress from supplied Address and categories.
     * The categories are assumed to have the {@code CategorizedAddress.ACTIVITY_LOCALE_CLASSIFICATION}
     * classification.
     *
     * @param shortDesc    The categorized address short description.
     * @param fullDesc     The full description of the CatgorizedAddress.
     * @param address      The address to wrap to a CategorizedAddress.
     * @param category     The Category (assumed to be present in the database already) of
     *                     the created CategorizedAddress.
     * @param organisation The name of the owning organisation for the CategorizedAddress.
     * @return The created CategorizedAddress instance.
     */
    CategorizedAddress createCategorizedActivityAddress(final String shortDesc,
            final String fullDesc,
            final Address address,
            final String category,
            final String organisation);
}
