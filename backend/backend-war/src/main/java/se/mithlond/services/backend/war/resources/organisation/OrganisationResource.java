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
import se.mithlond.services.backend.war.resources.AbstractResource;
import se.mithlond.services.backend.war.resources.RestfulParameters;
import se.mithlond.services.organisation.api.OrganisationService;
import se.mithlond.services.organisation.api.parameters.GroupIdSearchParameters;
import se.mithlond.services.organisation.model.transport.Organisations;
import se.mithlond.services.organisation.model.transport.membership.Groups;

import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

/**
 * Organisation-related resources, to retrieve Information about Organisations, Groups and Guilds.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Api(description = "Provides information about Organisations to authorized users.")
@Path("/organisation")
public class OrganisationResource extends AbstractResource {

    // Internal state
    @EJB
    private OrganisationService organisationService;

    /**
     * Retrieves an {@link Organisations} instance containing data for all known Organisations.
     *
     * @param includeDetailsInResponse if {@code true}, the response will contain
     *                                 {@link se.mithlond.services.organisation.model.Organisation} instances, and
     *                                 otherwise
     *                                 {@link se.mithlond.services.organisation.model.transport.OrganisationVO}
     *                                 instances.
     * @return An Organisations holder populated with all Organisations known.
     */
    @PermitAll
    @GET
    @Path("/all")
    public Organisations getOrganisations(
            @QueryParam(RestfulParameters.DETAILS) @DefaultValue("false") final Boolean includeDetailsInResponse) {
        return organisationService.getOrganisations(includeDetailsInResponse);
    }

    /**
     * Retrieves an {@link Organisations} instance containing data for all known Organisations.
     *
     * @param jpaID                    The JPA ID of the Organisation for which details should be retrieved.
     * @param includeDetailsInResponse if {@code true}, the response will contain
     *                                 {@link se.mithlond.services.organisation.model.Organisation} instances, and
     *                                 otherwise
     *                                 {@link se.mithlond.services.organisation.model.transport.OrganisationVO}
     *                                 instances.
     * @return An Organisations holder populated with the Organisation having the supplied jpaID.
     */
    @GET
    @Path("/{" + RestfulParameters.JPA_ID + "}")
    public Organisations getOrganisation(
            @PathParam(RestfulParameters.JPA_ID) final Long jpaID,
            @QueryParam(RestfulParameters.DETAILS) @DefaultValue("false") final Boolean includeDetailsInResponse) {
        return organisationService.getOrganisation(jpaID, includeDetailsInResponse);
    }

    /**
     * Retrieves all Groups for the supplied Organisation.
     *
     * @param jpaID                    The JPA ID of the Organisation for which all Groups should be retrieved.
     * @param includeDetailsInResponse {@code true} to retrieve full Group details, as opposed to GroupVO objects.
     * @return A Groups instance containing all Groups for the supplied Organisation.
     */
    @GET
    @Path("/{" + RestfulParameters.JPA_ID + "}/groups")
    public Groups getAllGroups(
            @PathParam(RestfulParameters.JPA_ID) final Long jpaID,
            @QueryParam(RestfulParameters.DETAILS) @DefaultValue("false") final Boolean includeDetailsInResponse) {

        // Compile the search parameters
        final GroupIdSearchParameters params = GroupIdSearchParameters.builder()
                .withOrganisationIDs(jpaID)
                .withDetailedResponsePreferred(includeDetailsInResponse)
                .build();

        // All Done.
        return organisationService.getGroups(params);
    }
}
