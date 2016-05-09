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

import se.mithlond.services.backend.war.resources.AbstractResource;
import se.mithlond.services.backend.war.resources.RestfulParameters;
import se.mithlond.services.organisation.api.ActivityService;
import se.mithlond.services.organisation.api.parameters.ActivitySearchParameters;
import se.mithlond.services.organisation.model.transport.activity.Activities;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.time.LocalDateTime;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Path("/org/{" + RestfulParameters.ORGANISATION_JPA_ID + "}/activity")
public class ActivityResource extends AbstractResource {

    // Internal state
    @EJB
    private ActivityService activityService;

    @GET
    @Path("/all")
    public Activities getActivities(
            @PathParam(RestfulParameters.ORGANISATION_NAME) final Long organisationID,
            @QueryParam(RestfulParameters.FROM_DATE) final String fromDate,
            @QueryParam(RestfulParameters.TO_DATE) final String toDate) {

        final LocalDateTime fromDateTime = TimeFormat.COMPACT_LOCALDATE.parse(fromDate).toLocalDateTime();
        final LocalDateTime toDateTime = TimeFormat.COMPACT_LOCALDATE.parse(toDate).toLocalDateTime();

        final ActivitySearchParameters params = ActivitySearchParameters.builder()
                .withOrganisationIDs(organisationID)
                .withStartPeriod(fromDateTime)
                .withEndPeriod(toDateTime)
                .build();

        // All Done.
        return activityService.getActivities(params, getDisconnectedActiveMembership().get());
    }
}
