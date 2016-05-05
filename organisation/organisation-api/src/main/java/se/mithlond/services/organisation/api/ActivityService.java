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
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.transport.activity.Activities;
import se.mithlond.services.organisation.model.transport.activity.ActivityVO;
import se.mithlond.services.organisation.model.transport.activity.AdmissionVO;

import javax.ejb.Local;
import java.util.List;
import java.util.Map;

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
     * @param parameters A non-null ActivitySearchParameters instance detailing which Activities should be retrieved.
     * @return all Activities within the supplied organisationName within the supplied Period.
     */
    Activities getActivities(final ActivitySearchParameters parameters, final Membership activeMembership);

    /**
     * Adds the given Activity to the database/calendar shared by the organisation.
     *
     * @param activityVO       An ActivityVO containing all data for creating an Activity. (Null jpaID required).
     * @param activeMembership The Membership executing this call.
     * @return The created Activity
     * @throws RuntimeException if the Activity could not be created due to the activityVO not containing
     *                          enough data or the activeMembership being null.
     */
    Activity createActivity(final ActivityVO activityVO, final Membership activeMembership)
            throws RuntimeException;

    /**
     * Updates the given Activity to the database/calendar shared by the organisation.
     *
     * @param activityVO                  An ActivityVO containing all data for updating an existing Activity.
     *                                    (non-null JpaID required).
     * @param onlyUpdateNonNullProperties Instructs the update to ignore updating any properties within the Activity
     *                                    for which the ActivityVO contains a null value.
     * @param activeMembership            The Membership executing this call.
     * @return The updated Activity
     * @throws RuntimeException if any required parameter is {@code null} or the activityId did not correspond
     *                          to an existing Activity.
     */
    @SuppressWarnings("all")
    Activity updateActivity(final ActivityVO activityVO,
            final boolean onlyUpdateNonNullProperties,
            final Membership activeMembership);

    /**
     * Modifies the Admissions to a particular Activity, as indicated by the supplied
     * arguments to this method.
     *
     * @param changeType       The type of change to perform.
     * @param actingMembership The acting Membership performing the change.
     * @param admissionDetails The non-null AdmissionDetails instances containing the desired state for an admission.
     * @return {@code true} if the admission was changed as requested, and false otherwise.
     * @throws RuntimeException if the supplied {@link AdmissionVO} did not contain sufficient information
     *                          to modify an Admission.
     */
    boolean modifyAdmission(
            final ChangeType changeType,
            final Membership actingMembership,
            final AdmissionVO... admissionDetails) throws RuntimeException;

    /**
     * Retrieves a Map relating Category to CategorizedAddress for all Location addresses
     * within the given organisation.
     *
     * @param organisationName The name of the organisation owning the Activity-location CategorizedAddress.
     * @return a Map relating Category to a List of CategorizedAddress for all Location addresses
     * within the given organisation.
     */
    Map<Category, List<CategorizedAddress>> getActivityLocationAddresses(final String organisationName);
}
