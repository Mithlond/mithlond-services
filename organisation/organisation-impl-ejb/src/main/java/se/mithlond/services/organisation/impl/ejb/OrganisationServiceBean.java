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
import se.mithlond.services.organisation.model.CategoryProducer;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.address.WellKnownAddressType;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.ejb.Stateless;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * OrganisationService Stateless EJB implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Stateless
public class OrganisationServiceBean extends AbstractJpaService implements OrganisationService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(OrganisationServiceBean.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Organisation> getOrganisations() {

        return entityManager.createNamedQuery(Organisation.NAMEDQ_GET_ALL, Organisation.class)
                .getResultList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Organisation> getOrganisation(@NotNull final String organisationName) {

        final List<Organisation> resultList = entityManager.createNamedQuery(
                Organisation.NAMEDQ_GET_BY_NAME, Organisation.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisationName)
                .getResultList();

        if(resultList == null || resultList.isEmpty()) {

            // Warn some.
            log.warn("Found no organisations for the name [" + organisationName + "]");

            // All done.
            return Optional.empty();
        } else if (resultList.size() > 1) {

            // This should really never happen...
            log.warn("Found [" + resultList.size() + "] organisations with the name ["
                    + organisationName + "]. Returning the first one.");
        }

        // All done.
        return Optional.of(resultList.get(0));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Group> getGroups(@NotNull final GroupIdSearchParameters searchParameters) {

        // Check sanity
        Validate.notNull(searchParameters, "searchParameters");

        final List<Group> toReturn = new ArrayList<>();

        // Pad the ID Lists.
        final int groupIDsSize = AbstractJpaService.padAndGetSize(searchParameters.getGroupIDs(), 0L);
        final int organisationIDsSize = AbstractJpaService.padAndGetSize(searchParameters.getOrganisationIDs(), 0L);
        final int classifierIDsSize = AbstractJpaService.padAndGetSize(searchParameters.getClassifierIDs(), 0L);

        // Acquire the padded lists.
        final List<Long> groupIDs = searchParameters.getGroupIDs();
        final List<Long> organisationIDs = searchParameters.getOrganisationIDs();
        final List<Long> classifierIDs = searchParameters.getClassifierIDs();

        // Fire, and add all results to the return List.
        final List<Group> groups = entityManager.createNamedQuery(Group.NAMEDQ_GET_BY_SEARCHPARAMETERS, Group.class)
                .setParameter(OrganisationPatterns.PARAM_NUM_GROUPIDS, groupIDsSize)
                .setParameter(OrganisationPatterns.PARAM_GROUP_IDS, groupIDs)
                .setParameter(OrganisationPatterns.PARAM_NUM_ORGANISATIONIDS, organisationIDsSize)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_IDS, organisationIDs)
                .setParameter(OrganisationPatterns.PARAM_NUM_CLASSIFICATIONIDS, classifierIDsSize)
                .setParameter(OrganisationPatterns.PARAM_CLASSIFICATION_IDS, classifierIDs)
                .getResultList();
        toReturn.addAll(groups);


        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<CategorizedAddress> getCategorizedAddresses(@NotNull final CategorizedAddressSearchParameters searchParameters) {

        // Check sanity
        Validate.notNull(searchParameters, "searchParameters");
        Validate.notNull(searchParameters.getOrganisationID(), "organisationID");

        final List<CategorizedAddress> toReturn = new ArrayList<>();

        // Pad the ID Lists.
        final int classifiersSize = AbstractJpaService.padAndGetSize(searchParameters.getClassifierIDs(), "none");

        // Acquire the padded lists.
        final List<String> classifiers = searchParameters.getClassifierIDs();
        final List<Long> organisationIDs = new ArrayList<>();
        organisationIDs.add(searchParameters.getOrganisationID());

        // Fire, and add all results to the return List.
        final List<CategorizedAddress> categorizedAddresses = entityManager.createNamedQuery(
                CategorizedAddress.NAMEDQ_GET_BY_SEARCHPARAMETERS, CategorizedAddress.class)
                .setParameter(OrganisationPatterns.PARAM_FULL_DESC, searchParameters.getFullDescPattern())
                .setParameter(OrganisationPatterns.PARAM_SHORT_DESC, searchParameters.getShortDescPattern())
                .setParameter(OrganisationPatterns.PARAM_NUM_ORGANISATIONIDS, organisationIDs.size())
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_IDS, organisationIDs)
                .setParameter(OrganisationPatterns.PARAM_NUM_CLASSIFICATIONS, classifiersSize)
                .setParameter(OrganisationPatterns.PARAM_CLASSIFICATIONS, classifiers)
                .setParameter(OrganisationPatterns.PARAM_ADDRESSCAREOFLINE, searchParameters.getAddressCareOfLinePattern())
                .setParameter(OrganisationPatterns.PARAM_CITY, searchParameters.getCityPattern())
                .setParameter(OrganisationPatterns.PARAM_COUNTRY, searchParameters.getCountryPattern())
                .setParameter(OrganisationPatterns.PARAM_DEPARTMENT, searchParameters.getDepartmentNamePattern())
                .setParameter(OrganisationPatterns.PARAM_DESCRIPTION, searchParameters.getDescriptionPattern())
                .setParameter(OrganisationPatterns.PARAM_NUMBER, searchParameters.getNumberPattern())
                .setParameter(OrganisationPatterns.PARAM_STREET, searchParameters.getStreetPattern())
                .setParameter(OrganisationPatterns.PARAM_ZIPCODE, searchParameters.getZipCodePattern())
                .getResultList();
        toReturn.addAll(categorizedAddresses);

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
        final CategorizedAddress existingCA = entityManager.find(CategorizedAddress.class, (Long) nonNull.getId());
        if(existingCA == null) {
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
            final String category,
            final String organisation) {

        final Optional<Organisation> potentialOrganisation = getOrganisation(organisation);
        if(!potentialOrganisation.isPresent()) {
            throw new IllegalArgumentException("No organisation '" + organisation + "' found.");
        }

        final CategoryProducer categoryProducer = WellKnownAddressType.valueOf(category);

        // Create the CategorizedAddress, and persist it.
        final CategorizedAddress toPersist = new CategorizedAddress(
                shortDesc,
                fullDesc,
                categoryProducer,
                potentialOrganisation.get(),
                address);
        entityManager.persist(toPersist);
        entityManager.flush();

        // All done.
        return toPersist;
    }
}
