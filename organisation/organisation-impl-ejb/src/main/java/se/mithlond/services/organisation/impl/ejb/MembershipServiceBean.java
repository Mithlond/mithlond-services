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
import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.PersonalSettings;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Pojo (EJB) implementation of the MembershipService specification.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MembershipServiceBean extends AbstractJpaService implements MembershipService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(MembershipServiceBean.class);

    @PersistenceContext(name = AbstractJpaService.SERVICE_PERSISTENCE_UNIT)
    private EntityManager entityManager;

    /**
     * {@inheritDoc}
     */
    @Override
    protected EntityManager getEntityManager() {
        return entityManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Membership> getMembershipsIn(final String organisation, final boolean includeLoginNotPermitted) {

        final List<Membership> toReturn = new ArrayList<>();

        final List<Membership> loginPermittedMemberships = entityManager.createNamedQuery(
                Membership.NAMEDQ_GET_BY_ORGANISATION_LOGINPERMITTED, Membership.class)
                .setParameter(Patterns.PARAM_ORGANISATION_NAME, organisation)
                .setParameter(Patterns.PARAM_LOGIN_PERMITTED, true)
                .getResultList();
        toReturn.addAll(loginPermittedMemberships);

        // Include the ones denied Login?
        if(includeLoginNotPermitted) {
            final List<Membership> loginNotPermittedMemberships = entityManager.createNamedQuery(
                    Membership.NAMEDQ_GET_BY_ORGANISATION_LOGINPERMITTED, Membership.class)
                    .setParameter(Patterns.PARAM_ORGANISATION_NAME, organisation)
                    .setParameter(Patterns.PARAM_LOGIN_PERMITTED, false)
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
                .setParameter(Patterns.PARAM_ORGANISATION_NAME, organisationName)
                .setParameter(Patterns.PARAM_ALIAS, alias)
                .getResultList();

        if(result.size() > 1) {
            log.warn("Got [" + result.size() + "] number of Memberships for organisation [" + organisationName +
                    "] and alias [" + alias + "]. Returning the first result.");
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

        final List<Membership> loginPermittedMemberships = entityManager.createNamedQuery(
                Membership.NAMEDQ_GET_BY_NAME_ORGANISATION, Membership.class)
                .setParameter(Patterns.PARAM_ORGANISATION_NAME, organisationName)
                .setParameter(Patterns.PARAM_FIRSTNAME, firstName)
                .setParameter(Patterns.PARAM_LASTNAME, lastName)
                .setParameter(Patterns.PARAM_LOGIN_PERMITTED, true)
                .getResultList();
        toReturn.addAll(loginPermittedMemberships);

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersonalSettings getPersonalSettingsFor(final Membership membership) {

        // Check sanity
        Validate.notNull(membership, "membership");

        // All done.
        return entityManager.createNamedQuery(PersonalSettings.NAMEDQ_GET_BY_MEMBERSHIP_ID, PersonalSettings.class)
                .setParameter(Patterns.PARAM_MEMBERSHIP_ID, membership.getId())
                .getSingleResult();
    }
}
