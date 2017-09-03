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
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.mithlond.services.backend.war.resources.AbstractResource;
import se.mithlond.services.backend.war.resources.RestfulParameters;
import se.mithlond.services.organisation.api.ActivityService;
import se.mithlond.services.organisation.api.parameters.ActivitySearchParameters;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.transport.activity.Activities;
import se.mithlond.services.organisation.model.transport.activity.ActivityVO;
import se.mithlond.services.organisation.model.transport.activity.Admissions;
import se.mithlond.services.organisation.model.transport.address.CategoriesAndAddresses;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Resource facade to Activities and Activity management.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Api(description = "Provides information about Activities to authorized users.")
@Path("/org/{" + RestfulParameters.ORGANISATION_JPA_ID + "}/activity")
public class ActivityResource extends AbstractResource {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(ActivityResource.class);

    // Internal state
    @EJB
    private ActivityService activityService;

    /**
     * Retrieves all Activities owned by an Organisation within a DateTime interval.
     *
     * @param organisationID The ID of the Organisation owning the activities extracted.
     * @param fromDate       The String representing the beginning of the interval which should contain the
     *                       {@link Activity#getStartTime()}. The time should be provided in the form 'yyyyMMdd'
     *                       such as {@code 20160203}.
     * @param toDate         The String representing the end of the interval which should contain the
     *                       {@link Activity#getStartTime()}. The time should be provided in the form 'yyyyMMdd'
     *                       such as {@code 20160205}.
     * @return A non-null {@link Activities} transport object containing all Activity instances matching the supplie
     * search criteria.
     */
    @GET
    @Path("/all")
    public Activities getActivities(
            @PathParam(RestfulParameters.ORGANISATION_JPA_ID) final Long organisationID,
            @QueryParam(RestfulParameters.FROM_DATE) final String fromDate,
            @QueryParam(RestfulParameters.TO_DATE) final String toDate) {

        // Debug some.
        if (log.isDebugEnabled()) {
            log.debug("Entered getActivities method. OrgID: " + organisationID
                    + ", fromDate: " + fromDate + ", toDate: " + toDate);
        }

        try {
            // Handle default values for from and to dates
            final LocalDateTime fromDateTime = fromDate == null || fromDate.isEmpty()
                    ? LocalDateTime.now()
                    : ((LocalDate) TimeFormat.COMPACT_LOCALDATE.parse(fromDate)).atStartOfDay();
            final LocalDateTime toDateTime = toDate == null || toDate.isEmpty()
                    ? fromDateTime.plusMonths(2)
                    : ((LocalDate) TimeFormat.COMPACT_LOCALDATE.parse(toDate)).atStartOfDay();

            final ActivitySearchParameters params = ActivitySearchParameters.builder()
                    .withOrganisationIDs(organisationID)
                    .withStartPeriod(fromDateTime)
                    .withEndPeriod(toDateTime)
                    .build();

            // All Done.
            final Activities toReturn = activityService.getActivities(params, getActiveMembership());

            if (log.isInfoEnabled()) {
                log.info("Returning " + toReturn.getActivities());
            }

            // All Done.
            return toReturn;

        } catch (Exception e) {

            log.error("Could not fetch activities from database: ", e);

            throw new IllegalArgumentException("");
        }
    }

    /**
     * Retrieves CategoriesAndAddresses for the supplied organisation.
     *
     * @param organisationID The ID of the Organisation owning the activities extracted.
     * @return The fully populated CategoriesAndAddresses object for the supplied organisation.
     */
    @GET
    @Path("/addresses")
    public CategoriesAndAddresses getActivityLocationAddresses(
            @PathParam(RestfulParameters.ORGANISATION_JPA_ID) final Long organisationID) {

        // Debug some.
        if (log.isInfoEnabled()) {
            log.info("Fetching CategoriesAndAddresses for OrgID: " + organisationID);
        }

        // All Done.
        return activityService.getActivityLocationAddresses(organisationID);
    }

    /**
     * Creates a new Activity in the system, based on the information within the supplied ActivityVO.
     *
     * @param activityVO A non-null {@link ActivityVO} instance to used as a template to create a new Activity.
     * @return An {@link Activities} container with an {@link ActivityVO} containing the data of the
     * newly created {@link Activity}.
     */
    @PUT
    @Path("/create")
    public Activities createActivity(final ActivityVO activityVO) {

        // Debug some.
        if (log.isDebugEnabled()) {
            log.debug("Entered createActivity method. ActivityVO: " + activityVO);
        }

        // Check sanity
        Validate.notNull(activityVO, "Cannot handle null 'activityVO' argument.");

        // Create the activity
        final Activity created = activityService.createActivity(activityVO, getActiveMembership());

        if (log.isDebugEnabled()) {
            log.debug("Created " + created);
        }

        // All Done.
        return new Activities(new ActivityVO(created));
    }

    /**
     * Modifies the admissions of the supplied ActivityVO.
     *
     * @param activityVO A populated - and non-null - {@link ActivityVO}.
     * @return An Activities wrapper containing the ActivityVO of the modified Activity.
     */
    @POST
    @Path("/modify")
    public Activities modifyActivity(final ActivityVO activityVO) {

        // Check sanity
        Validate.notNull(activityVO, "Cannot handle null 'activityVO' argument.");

        final ActivitySearchParameters params = ActivitySearchParameters.builder()
                .withActivityIDs()
                .withOrganisationIDs()
                .build();

        final Activities activities = activityService.getActivities(params, getActiveMembership());
        final List<Activity> activityList = activities.getActivities();

        // There should really only be one activity here
        if (activityList.size() != 1) {
            throw new IllegalArgumentException("Could not get exactly 1 activity for search params " + params);
        }

        final Activity modifiedActivity = activityService.updateActivity(
                activityVO,
                false,
                getActiveMembership());

        // All Done.
        return new Activities(new ActivityVO(modifiedActivity));
    }

    /**
     * Updates the Admissions for the active Membership into the supplied state.
     *
     * @param targetState The target Admissions state.
     * @return The updated Admissions for the active membership.
     */
    @POST
    @Path("/admissions/update")
    public Admissions modifyAdmissions(final Admissions targetState) {

        if (log.isInfoEnabled()) {
            final AtomicInteger index = new AtomicInteger();
            log.info("Received targetState Admissions:\n" + targetState.getDetails()
                    .stream()
                    .map(adm -> index.getAndIncrement() + ": [activityJpaID: " + adm.getActivityID()
                            + ", membershipID: " + adm.getMembershipID()
                            + ", admitted: " + adm.getAdmitted()
                            + ", note: " + adm.getNote().orElse("<none>") + "]")
                    .reduce((l, r) -> l + "\n" + r).orElse("<nothing>"));
        }

        // Delegate to the service
        final Admissions newState = activityService.updateAdmissions(getActiveMembership(), targetState);

        if (log.isDebugEnabled()) {
            log.debug("Returning " + targetState.getDetails()
                    .stream()
                    .map(adm -> "[activityJpaID: " + adm.getActivityID()
                            + ", membershipID: " + adm.getMembershipID()
                            + ", admitted: " + adm.getAdmitted()
                            + ", note: " + adm.getNote().orElse("<none>") + "]")
                    .reduce((l, r) -> l + "\n" + r).orElse("<nothing>"));
        }

        // All Done
        return newState;
    }
}
