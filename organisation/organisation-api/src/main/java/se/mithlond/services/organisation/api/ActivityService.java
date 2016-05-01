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
import se.mithlond.services.organisation.api.transport.activity.ActivityVO;
import se.mithlond.services.organisation.api.transport.activity.AdmissionVO;
import se.mithlond.services.organisation.api.transport.activity.Admissions;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.finance.Amount;
import se.mithlond.services.organisation.model.membership.Membership;

import javax.ejb.Local;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    List<Activity> getActivities(final ActivitySearchParameters parameters);

    /**
     * Retrieves the Activity with the supplied activityID primary key.
     *
     * @param activityID The ID of the activity that should be retrieved.
     * @return the Activity with the supplied activityID primary key.
     */
    Activity getActivity(final long activityID);

    /**
     * Adds the given Activity to the database/calendar shared by the organisation.
     *
     * @param organisationName        The name of the Organisation which should own the Activity.
     * @param shortDesc               The mandatory and non-empty short description of this activity, visible in listings.
     * @param fullDesc                The full description of this activity (up to 2048 chars), visible
     *                                in detailed listings. May not be null or empty.
     * @param startTime               The start time of the Activity. Must not be null.
     * @param endTime                 The end time of the Activity. Must not be null, and must also be after startTime.
     * @param cost                    The optional cost of the activity. Must not be negative.
     * @param lateAdmissionCost       The cost if admission after the lateAdmissionDate.
     *                                Optional, but recommended to be higher than the (standard) cost.
     * @param lateAdmissionDate       The date before which the activty costs {@code cost}.
     *                                After this date, the activity admission costs {@code lateAdmissionCost}.
     * @param lastAdmissionDate       The last date of admissions to the Activity.
     * @param cancelled               If <code>true</code>, the Activity is cancelled.
     * @param dressCode               An optional dress code description for the Activity.
     * @param location                The location of the Activity.
     * @param addressCategory         The address-classification category of the address supplied.
     * @param addressShortDescription The short description of the location for this Activity,
     *                                such as "Stadsbiblioteket".
     * @param responsibleGroupName    The name of the Group organizing this Activity. Optional.
     * @param admissions              A Set holding all initial admissions to the activity to create. Optional,
     *                                but should contain at least one responsible AdmissionDetails, unless a Group
     *                                organizes the Activity.
     * @param isOpenToGeneralPublic   If {@code true}, the activity is flagged as being open to the general public
     *                                (as opposed to being available to members of the supplied organisation only).
     * @param activeMembership        The Membership executing this call.
     * @return The created Activity
     */
    @SuppressWarnings("all")
    Activity createActivity(final ActivityVO activityVO, final Membership activeMembership);

    /**
     * Updates the given Activity to the database/calendar shared by the organisation.
     *
     * @param activityId              The id of the Activity that should be updated.
     * @param organisationName        The name of the Organisation which should own the Activity.
     * @param shortDesc               The mandatory and non-empty short description of this activity, visible in listings.
     * @param fullDesc                The full description of this activity (up to 2048 chars), visible
     *                                in detailed listings. May not be null or empty.
     * @param startTime               The start time of the Activity. Must not be null.
     * @param endTime                 The end time of the Activity. Must not be null, and must also be after startTime.
     * @param cost                    The optional cost of the activity. Must not be negative.
     * @param lateAdmissionCost       The cost if admission after the lateAdmissionDate.
     *                                Optional, but recommended to be higher than the (standard) cost.
     * @param lateAdmissionDate       The date before which the activty costs {@code cost}.
     *                                After this date, the activity admission costs {@code lateAdmissionCost}.
     * @param lastAdmissionDate       The last date of admissions to the Activity.
     * @param cancelled               If <code>true</code>, the Activity is cancelled.
     * @param dressCode               An optional dress code description for the Activity.
     * @param location                The location of the Activity.
     * @param addressCategory         The address-classification category of the address supplied.
     * @param responsibleGroupName    The name of the Group organizing this Activity. Optional.
     * @param admissions              A Set holding all initial admissions to the activity to create. Optional,
     *                                but should contain at least one responsible ProtoAdmission, unless a guild
     *                                organizes the Activity.
     * @param addressShortDescription The short description of the location for this Activity,
     *                                such as "Stadsbiblioteket".
     * @param isOpenToGeneralPublic   If {@code true}, the activity is flagged as being open to the general public
     *                                (as opposed to being available to members of the supplied organisation only).
     * @param activeMembership        The Membership executing this call.
     * @return The updated Activity
     * @throws RuntimeException if any required parameter is {@code null} or the activityId did not correspond
     *                          to an existing Activity.
     */
    @SuppressWarnings("all")
    Activity updateActivity(final long activityId,
            final String organisationName,
            final String shortDesc,
            final String fullDesc,
            final ZonedDateTime startTime,
            final ZonedDateTime endTime,
            final Amount cost,
            final Amount lateAdmissionCost,
            final LocalDate lateAdmissionDate,
            final LocalDate lastAdmissionDate,
            final boolean cancelled,
            final String dressCode,
            final String addressCategory,
            final Address location,
            final String addressShortDescription,
            final String responsibleGroupName,
            final Set<AdmissionVO> admissions,
            final boolean isOpenToGeneralPublic,
            final Membership activeMembership);

    /**
     * Modifies the Admissions to a particular Activity, as indicated by the supplied
     * arguments to this method.
     *
     * @param changeType       The type of change to perform.
     * @param admissionDetails The non-null AdmissionDetails instance containing the desired state for an admission.
     * @param actingMembership The acting Membership performing the change.
     * @return {@code true} if the admission was changed as requested, and false otherwise.
     * @throws IllegalStateException if the supplied {@link AdmissionVO} did not contain sufficient information
     * to modify an Admission.
     */
    boolean modifyAdmission(
            final ChangeType changeType,
            final AdmissionVO admissionDetails,
            final Membership actingMembership) throws IllegalStateException;

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
