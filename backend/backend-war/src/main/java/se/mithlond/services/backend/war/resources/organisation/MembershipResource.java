/*
 * #%L
 * Nazgul Project: mithlond-services-backend-war
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
package se.mithlond.services.backend.war.resources.organisation;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.backend.war.resources.AbstractResource;
import se.mithlond.services.backend.war.resources.RestfulParameters;
import se.mithlond.services.organisation.api.MembershipService;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.transport.membership.Memberships;

import javax.ejb.EJB;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.List;

/**
 * Resource facade to Memberships and Membership management.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Api(description = "Provides information about Memberships to authorized users.")
@Path("/org/{" + RestfulParameters.ORGANISATION_JPA_ID + "}/membership")
public class MembershipResource extends AbstractResource {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(MembershipResource.class);

    // Internal state
    @EJB
    private MembershipService membershipService;

    /**
     * Retrieves a {@link Memberships} wrapper containing all Membership (or MembershipVO), including the
     * OrganisationVO of the Organisation
     *
     * @param orgJpaID                 The JPA ID of the Organisation for which Memberships should be retrieved.
     * @param includeLoginNotPermitted If {@code true}, the returned Memberships holder includes all Memberships -
     *                                 including the ones not permitted login.
     * @return A {@link Memberships} wrapper containing all Membership (or MembershipVO)
     */
    @Path("/all")
    @GET
    public Memberships getMemberships(@PathParam(RestfulParameters.ORGANISATION_JPA_ID) final Long orgJpaID,
            @QueryParam(RestfulParameters.INCLUDE_LOGIN_NOT_PERMITTED)
            @DefaultValue("false")
            final boolean includeLoginNotPermitted) {

        // Fire the JPQL query
        final List<Membership> memberships = membershipService.getMembershipsIn(orgJpaID, includeLoginNotPermitted);

        // Repackage into a Memberships wrapper.
        final Memberships toReturn = new Memberships();
        memberships.stream().forEach(toReturn::addMembership);

        if(log.isDebugEnabled()) {

            final String organisationName = memberships != null && !memberships.isEmpty()
                    ? memberships.get(0).getOrganisation().getOrganisationName()
                    : "<unknown>";

            log.debug("Found " + memberships.size() + " memberships in organisation "
                    + orgJpaID + " (" + organisationName + ")");
        }

        // All Done
        return toReturn;
    }
}
