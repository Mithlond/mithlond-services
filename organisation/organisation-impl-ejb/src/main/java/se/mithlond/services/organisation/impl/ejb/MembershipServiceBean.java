/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-impl-ejb
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
package se.mithlond.services.organisation.impl.ejb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.organisation.api.MembershipService;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;

/**
 * Stateless EJB implementation of the MembershipService specification.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Stateless
public class MembershipServiceBean extends AbstractJpaService implements MembershipService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(MembershipServiceBean.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Membership> getMembershipsIn(final Long orgJpaID, final boolean includeLoginNotPermitted) {

        final List<Membership> toReturn = new ArrayList<>();

        final List<Membership> loginPermittedMemberships = entityManager.createNamedQuery(
                Membership.NAMEDQ_GET_BY_ORGANISATION_ID_LOGINPERMITTED, Membership.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_ID, orgJpaID)
                .setParameter(OrganisationPatterns.PARAM_LOGIN_PERMITTED, true)
                .getResultList();
        toReturn.addAll(loginPermittedMemberships);

        // Include the ones denied Login?
        if (includeLoginNotPermitted) {
            final List<Membership> loginNotPermittedMemberships = entityManager.createNamedQuery(
                    Membership.NAMEDQ_GET_BY_ORGANISATION_ID_LOGINPERMITTED, Membership.class)
                    .setParameter(OrganisationPatterns.PARAM_ORGANISATION_ID, orgJpaID)
                    .setParameter(OrganisationPatterns.PARAM_LOGIN_PERMITTED, false)
                    .getResultList();
            toReturn.addAll(loginNotPermittedMemberships);
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Membership getMembership(final String organisationName, final String alias) {

        final List<Membership> result = entityManager.createNamedQuery(
                Membership.NAMEDQ_GET_BY_ALIAS_ORGANISATION, Membership.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisationName)
                .setParameter(OrganisationPatterns.PARAM_ALIAS, alias)
                .getResultList();

        if (result.size() > 1) {
            log.warn("Got [" + result.size() + "] number of Memberships for organisation ["
                    + organisationName + "] and alias [" + alias + "]. Returning the first result.");
        }

        // All done.
        return result.size() == 0 ? null : result.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Membership> getActiveMemberships(final String organisationName,
                                                 final String firstName,
                                                 final String lastName) {

        final List<Membership> toReturn = new ArrayList<>();

        final List<Membership> allMemberships = entityManager.createNamedQuery(
                Membership.NAMEDQ_GET_BY_NAME_ORGANISATION, Membership.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisationName)
                .setParameter(OrganisationPatterns.PARAM_FIRSTNAME, firstName)
                .setParameter(OrganisationPatterns.PARAM_LASTNAME, lastName)
                // .setParameter(OrganisationPatterns.PARAM_LOGIN_PERMITTED, true)
                .getResultList();

        if(allMemberships != null && !allMemberships.isEmpty()) {
            allMemberships.stream().filter(Membership::isLoginPermitted).forEach(toReturn::add);
        }

        // All done.
        return toReturn;
    }
}
