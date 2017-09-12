/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-api
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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

import se.mithlond.services.organisation.api.parameters.ActivitySearchParameters;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.transport.activity.Activities;
import se.mithlond.services.organisation.model.transport.activity.Admissions;
import se.mithlond.services.organisation.model.transport.address.CategoriesAndAddresses;
import se.mithlond.services.shared.authorization.api.RequireAuthorization;

import javax.ejb.Local;

/**
 * Service specification for Activity management, enabling callers
 * to define calendar entries and tasks as required.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Local
public interface ActivityService {

    /**
     * Change type enumeration definition.
     */
    enum ChangeType {

        /**
         * A change implying an addition or modification.
         */
        ADD_OR_MODIFY,

        /**
         * A change implying a deletion.
         */
        DELETE
    }

    /**
     * Retrieves all Activities within the supplied organisationName within the supplied range of dates.
     *
     * @param parameters       A non-null ActivitySearchParameters instance detailing which Activities should be retrieved.
     * @param activeMembership The Membership executing this call (hence being the 'active' {@link Membership}).
     * @return all Activities within the supplied organisationName within the supplied Period.
     */
    @RequireAuthorization(authorizationPatterns = "//Inbyggare/")
    Activities getActivities(final ActivitySearchParameters parameters, final Membership activeMembership);

    /**
     * Adds the given Activities to the database/calendar shared by the organisation.
     *
     * @param activities       An Activities transport wrapper containing all data for creating some Activities.
     *                         This implies only the OrganisationVOs and ActivityVOs should be populated.
     * @param activeMembership The Membership executing this call. If the Membership is considered an Administrator,
     *                         the Activity can be created with other Memberships as responsible (than the
     *                         activeMembership) within the same Organisation.
     *                         Otherwise, only the activeMemberships can be designated as responsible for the newly
     *                         created Activities.
     * @return The created Activities
     * @throws RuntimeException if the Activities could not be created due to the activityVO not containing
     *                          enough data or the activeMembership being null.
     */
    @RequireAuthorization(authorizationPatterns = "//Inbyggare/")
    Activities createActivities(final Activities activities, final Membership activeMembership)
            throws RuntimeException;

    /**
     * Updates the given Activity to the database/calendar shared by the organisation.
     *
     * @param targetState                 An Activities object containing all data for updating existing Activity
     *                                    objects. (Non-null JpaIDs required).
     * @param onlyUpdateNonNullProperties Instructs the update to ignore updating any properties within the Activity
     *                                    for which the ActivityVO contains a null value.
     * @param activeMembership            The Membership executing this call. The Membership executing this call. If
     *                                    the Membership is considered an Administrator, any Activity data within the
     *                                    same Organisation can be updated. Otherwise, only the
     *                                    Activities for which the activeMembership is responsible can be updated.
     * @return The updated Activity
     * @throws RuntimeException if any required parameter is {@code null} or the activityId did not correspond
     *                          to an existing Activity.
     */
    @SuppressWarnings("all")
    @RequireAuthorization(authorizationPatterns = "//Inbyggare/")
    Activities updateActivities(final Activities targetState,
                                final boolean onlyUpdateNonNullProperties,
                                final Membership activeMembership);


    /**
     * Updates the admissions with the supplied data.
     *
     * @param activeMembership The Membership performing this call. If the Membership is considered an Administrator,
     *                         the Admissions are updated even for other Memberships within the same Organisation.
     *                         Otherwise, only the activeMemberships own Admissions are updated.
     * @param admissions       The target state admissions.
     * @return The Admissions, post the update.
     */
    Admissions updateAdmissions(final Membership activeMembership, final Admissions admissions);

    /**
     * Retrieves a Map relating Category to CategorizedAddress for all Location addresses
     * within the given organisation.
     *
     * @param organisationID The JPA ID of the organisation for which addresses should be retrieved
     * @return a Map relating Category to a List of CategorizedAddress for all Location addresses
     * within the given organisation.
     */
    CategoriesAndAddresses getActivityLocationAddresses(final Long organisationID);
}
