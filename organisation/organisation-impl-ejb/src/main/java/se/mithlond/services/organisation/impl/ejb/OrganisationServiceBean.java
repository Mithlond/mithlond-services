/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-impl-ejb
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
package se.mithlond.services.organisation.impl.ejb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.organisation.api.OrganisationService;
import se.mithlond.services.organisation.api.parameters.CategorizedAddressSearchParameters;
import se.mithlond.services.organisation.api.parameters.GroupIdSearchParameters;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.transport.OrganisationVO;
import se.mithlond.services.organisation.model.transport.Organisations;
import se.mithlond.services.organisation.model.transport.address.CategoriesAndAddresses;
import se.mithlond.services.organisation.model.transport.membership.GroupVO;
import se.mithlond.services.organisation.model.transport.membership.Groups;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * OrganisationService Stateless EJB implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
// @SecurityDomain("keycloak")
@Stateless
public class OrganisationServiceBean extends AbstractJpaService implements OrganisationService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(OrganisationServiceBean.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Organisations getOrganisations(final boolean detailedRepresentation) {

        if (log.isDebugEnabled()) {
            log.debug("Fetching organisation data for " + (detailedRepresentation ? "detailed" : "shallow")
                    + " representation");
        }

        final List<Organisation> allOrganisations = entityManager.createNamedQuery(
                Organisation.NAMEDQ_GET_ALL, Organisation.class)
                .getResultList();

        final Organisations toReturn = new Organisations();

        if (detailedRepresentation) {

            // Simply populate the organisations Set within the transport object
            toReturn.getOrganisations().addAll(allOrganisations);
        } else {

            // Convert to OrganizationVOs and add to the OrganisationVO Set within the Transport Object.
            toReturn.getOrganisationVOs().addAll(
                    allOrganisations.stream()
                            .map(c -> new OrganisationVO(
                                    c.getId(),
                                    c.getOrganisationName(),
                                    c.getSuffix()))
                            .collect(Collectors.toList()));
        }

        if (log.isDebugEnabled()) {
            log.debug("All Done. Returning " + toReturn);
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Organisations getOrganisation(final long jpaID, final boolean detailedRepresentation) {

        final Organisation organisation = entityManager.find(Organisation.class, jpaID);
        if (organisation == null) {
            throw new IllegalArgumentException("No organisation with ID " + jpaID + " found.");
        }

        final Organisations toReturn = new Organisations();
        if (detailedRepresentation) {
            toReturn.getOrganisations().add(organisation);
        } else {
            toReturn.getOrganisationVOs().add(new OrganisationVO(organisation));
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Groups getGroups(@NotNull final GroupIdSearchParameters searchParameters) {

        // Check sanity
        Validate.notNull(searchParameters, "searchParameters");

        // Pad the ID Lists.
        final int groupIDsSize = AbstractJpaService.padAndGetSize(searchParameters.getGroupIDs(), 0L);
        final int organisationIDsSize = AbstractJpaService.padAndGetSize(searchParameters.getOrganisationIDs(), 0L);

        // Acquire the padded lists.
        final List<Long> groupIDs = searchParameters.getGroupIDs();
        final List<Long> organisationIDs = searchParameters.getOrganisationIDs();

        // Fire, and add all results to the return List.
        final List<Group> groups = entityManager.createNamedQuery(Group.NAMEDQ_GET_BY_SEARCHPARAMETERS, Group.class)
                .setParameter(OrganisationPatterns.PARAM_NUM_GROUPIDS, groupIDsSize)
                .setParameter(OrganisationPatterns.PARAM_GROUP_IDS, groupIDs)
                .setParameter(OrganisationPatterns.PARAM_NUM_ORGANISATIONIDS, organisationIDsSize)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_IDS, organisationIDs)
                .getResultList();

        final Groups toReturn = new Groups();
        if (searchParameters.isDetailedResponsePreferred()) {

            // Simply add all retrieved groups.
            toReturn.getGroups().addAll(groups);

        } else {

            // Convert to GroupVOs and add.
            toReturn.getGroupVOs().addAll(groups
                    .stream()
                    .map(GroupVO::new)
                    .collect(Collectors.toList()));
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CategoriesAndAddresses getCategorizedAddresses(
            @NotNull final CategorizedAddressSearchParameters searchParameters) {

        // Check sanity
        Validate.notNull(searchParameters, "searchParameters");

        // Pad the ID Lists.
        final int numClassifications = AbstractJpaService.padAndGetSize(searchParameters.getClassifications(), "none");

        // Acquire the padded lists.
        final List<String> classifications = searchParameters.getClassifications();
        final List<Long> organisationIDs = new ArrayList<>();
        organisationIDs.add(searchParameters.getOrganisationID());

        // Fire, and add all results to the return List.
        final TypedQuery<CategorizedAddress> categorizedAddressTypedQuery = entityManager.createNamedQuery(
                CategorizedAddress.NAMEDQ_GET_BY_SEARCHPARAMETERS, CategorizedAddress.class)
                .setParameter(OrganisationPatterns.PARAM_FULL_DESC, searchParameters.getFullDescPattern())
                .setParameter(OrganisationPatterns.PARAM_SHORT_DESC, searchParameters.getShortDescPattern())
                .setParameter(OrganisationPatterns.PARAM_NUM_ORGANISATIONIDS, organisationIDs.size())
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_IDS, organisationIDs)
                .setParameter(OrganisationPatterns.PARAM_NUM_CLASSIFICATIONS, numClassifications)
                .setParameter(OrganisationPatterns.PARAM_CLASSIFICATIONS, classifications)
                .setParameter(OrganisationPatterns.PARAM_ADDRESSCAREOFLINE,
                        makeLikeParameter(searchParameters.getAddressCareOfLinePattern()))
                .setParameter(OrganisationPatterns.PARAM_CITY,
                        makeLikeParameter(searchParameters.getCityPattern()))
                .setParameter(OrganisationPatterns.PARAM_COUNTRY,
                        makeLikeParameter(searchParameters.getCountryPattern()))
                .setParameter(OrganisationPatterns.PARAM_DEPARTMENT,
                        makeLikeParameter(searchParameters.getDepartmentNamePattern()))
                .setParameter(OrganisationPatterns.PARAM_DESCRIPTION,
                        makeLikeParameter(searchParameters.getDescriptionPattern()))
                .setParameter(OrganisationPatterns.PARAM_NUMBER,
                        searchParameters.getNumberPattern())
                .setParameter(OrganisationPatterns.PARAM_STREET,
                        makeLikeParameter(searchParameters.getStreetPattern()))
                .setParameter(OrganisationPatterns.PARAM_ZIPCODE,
                        makeLikeParameter(searchParameters.getZipCodePattern()));


        final List<CategorizedAddress> categorizedAddresses = categorizedAddressTypedQuery.getResultList();
        final CategoriesAndAddresses toReturn = new CategoriesAndAddresses();
        categorizedAddresses.forEach(toReturn::addCategorizedAddress);

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CategoriesAndAddresses getCategorizedAddresses(@NotNull final SortedSet<Long> addressJpaIDs) {

        // Check sanity
        Validate.notNull(addressJpaIDs, "addressJpaIDs");

        final CategoriesAndAddresses toReturn = new CategoriesAndAddresses();

        if (!addressJpaIDs.isEmpty()) {

            // Collect all JPA IDs
            final List<Long> ids = addressJpaIDs.stream().filter(id -> id != null).collect(Collectors.toList());

            // Add all CAs to the transport VO.
            entityManager.createNamedQuery(CategorizedAddress.NAMEDQ_GET_BY_IDS, CategorizedAddress.class)
                    .setParameter(OrganisationPatterns.PARAM_IDS, ids)
                    .getResultList()
                    .forEach(toReturn::addCategorizedAddress);
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CategorizedAddress updateCategorizedAddress(final CategorizedAddress toUpdate) {

        // Ensure that the ID of the supplied CategorizedAddress is not 0
        final CategorizedAddress nonNull = Validate.notNull(toUpdate, "toUpdate");
        final long id = toUpdate.getId();
        Validate.isTrue(id > 0L, "Cannot handle zero or negative JPA ID.");

        // Retrieve an existing CategorizedAddress
        final CategorizedAddress existingCA = entityManager.find(CategorizedAddress.class, nonNull.getId());
        if (existingCA == null) {
            throw new IllegalArgumentException("No existing CategorizedAddress with JPA ID [" + id + "]");
        }

        // Update the existing CA
        existingCA.setAddress(toUpdate.getAddress());
        existingCA.setCategory(toUpdate.getCategory());
        existingCA.setFullDesc(toUpdate.getFullDesc());
        existingCA.setShortDesc(toUpdate.getShortDesc());
        entityManager.flush();

        // All done.
        return existingCA;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CategorizedAddress createCategorizedActivityAddress(final String shortDesc,
            final String fullDesc,
            final Address address,
            final Long categoryID,
            final Long organisationID) {

        // Check sanity
        Validate.notEmpty(shortDesc, "shortDesc");
        Validate.notEmpty(fullDesc, "fullDesc");
        Validate.notNull(address, "address");
        Validate.notNull(categoryID, "categoryID");
        Validate.notNull(organisationID, "organisationID");

        // Find the database objects required to create a CategorizedAddress
        final Organisation organisation = entityManager.find(Organisation.class, organisationID);
        final Category category = entityManager.find(Category.class, categoryID);

        // Handle insanity
        if (organisation == null) {
            throw new IllegalArgumentException("Hittade ingen organisation med ID [" + organisationID + "]");
        }
        if (category == null) {
            throw new IllegalArgumentException("Hittade ingen kategori med ID [" + categoryID + "]");
        }
        if (!CategorizedAddress.ACTIVITY_CLASSIFICATION.equals(category.getClassification())) {
            throw new IllegalArgumentException("Kategori [" + category + "] härrör inte till aktiviteter.");
        }

        // Create the CategorizedAddress, and persist it.
        final CategorizedAddress toPersist = new CategorizedAddress(
                shortDesc,
                fullDesc,
                category,
                organisation,
                address);
        entityManager.persist(toPersist);
        entityManager.flush();

        // All done.
        return toPersist;
    }
}
