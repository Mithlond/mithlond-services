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

import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.shared.spi.jpa.JpaCudService;

import javax.ejb.Local;
import java.util.List;

/**
 * Service specification for queries extracting information from
 * Organisations - without any particular Membership origination
 * or input to the service queries.
 * <p/>
 * For queries related to Memberships and Members, refer to the MemberService.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Local
public interface OrganisationService extends JpaCudService {

    /**
     * Retrieves all guilds within the supplied Organisation.
     *
     * @param organisationName The name of the organisation for which all guilds should be retrieved.
     * @return All guilds within the supplied organisation.
     */
    List<Guild> getGuilds(String organisationName);

    /**
     * Retrieves all groups within the supplied Organisation.
     *
     * @param organisationName The name of the organisation for which all groups should be retrieved.
     * @return All groups within the supplied organisation.
     */
    List<Group> getGroups(String organisationName);

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
     * @return the organisation with the supplied name.
     */
    Organisation getOrganisation(String organisationName);

    /**
     * Retrieves all CategorizedAddresses matching the supplied classification and within the supplid organisation.
     *
     * @param organisationName The name of the organisation for which CategorizedAddresses should be retrieved.
     * @param classification   The classification of the CategorizedAddress to retrieve.
     * @return all CategorizedAddresses matching the supplied classification and within the supplid organisation.
     */
    List<CategorizedAddress> getCategorizedAddresses(String organisationName, String classification);

    /**
     * Retrieves the identified (as in primary key) CategorizedAddress, given the supplied organisation Ownership.
     *
     * @param organisationName The name of the organisation owning the retrieved CategorizedAddress.
     * @param id               The id of the CategorizedAddress to retrieve.
     * @return The CategorizedAddress found.
     */
    CategorizedAddress getCategorizedAddress(String organisationName, int id);

    /**
     * Retrieves all Categories matching the supplied classification.
     *
     * @param classification The classification of the Categories to retrieve.
     * @return all Categories matching the supplied classification.
     */
    List<Category> getCategoriesByClassification(String classification);

    /**
     * Merges and updates the supplied CategorizedAddress within the database.
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
