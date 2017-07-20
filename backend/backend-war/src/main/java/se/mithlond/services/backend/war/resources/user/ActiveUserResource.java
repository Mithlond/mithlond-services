/*
 * #%L
 * Nazgul Project: mithlond-services-backend-war
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
package se.mithlond.services.backend.war.resources.user;

import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.backend.war.resources.AbstractResource;
import se.mithlond.services.backend.war.resources.RestfulParameters;
import se.mithlond.services.organisation.api.FoodAndAllergyService;
import se.mithlond.services.organisation.api.MembershipService;
import se.mithlond.services.organisation.api.OrganisationService;
import se.mithlond.services.organisation.api.parameters.GroupIdSearchParameters;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.transport.convenience.membership.MembershipListVO;
import se.mithlond.services.organisation.model.transport.convenience.membership.SlimContactInfoVO;
import se.mithlond.services.organisation.model.transport.convenience.membership.SlimMemberVO;
import se.mithlond.services.organisation.model.transport.food.Allergies;
import se.mithlond.services.organisation.model.transport.membership.Memberships;
import se.mithlond.services.organisation.model.user.User;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Resource tailored to retrieve data for the active user.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Api(description = "Provides information about the Active Membership / User.")
@Path("/activeuser")
public class ActiveUserResource extends AbstractResource {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(ActiveUserResource.class);

    /**
     * Personal settings key for preferred locale.
     */
    public static final String PS_PREFERRED_LOCALE_KEY = "preferred_locale";

    // Internal state
    @EJB
    private OrganisationService organisationService;

    @EJB
    private FoodAndAllergyService foodAndAllergyService;

    @EJB
    private MembershipService membershipService;

    /**
     * Retrieves a {@link Memberships} wrapper containing the full-detail membership information of
     * the currently logged in user.
     *
     * @return A {@link Memberships} wrapper containing the Membership, User, Organisation
     * and Groups of the active Membership.
     * @see Memberships
     */
    @Path("/info")
    @GET
    public Memberships getActiveMembershipInfo() {

        // #1) Find the active Membership, and create the return wrapper.
        final Memberships toReturn = getActiveUserMemberships();

        // #2) All all Groups and Guilds within the active Organisation
        final Organisation activeOrganisation = getActiveMembership().getOrganisation();

        final GroupIdSearchParameters groupSearchParams = GroupIdSearchParameters.builder()
                .withOrganisationIDs(activeOrganisation.getId())
                .withDetailedResponsePreferred(true)
                .build();

        organisationService.getGroups(groupSearchParams)
                .getGroups()
                .forEach(toReturn::addGroups);
        toReturn.getGroups().stream()
                .filter(gr -> gr.getParent() != null)
                .forEach(gr -> log.debug("Group [" + gr.getGroupName() + "] has a parent [" + gr.getParent().getGroupName() + "]"));

        // All Done.
        return toReturn;
    }

    /**
     * Retrieves an {@link Allergies} wrapper containing the full-detail allery information of
     * the currently logged in user.
     *
     * @return An {@link Allergies} wrapper containing the Membership, User, Organisation
     * and Groups of the active Membership.
     * @see Memberships
     */
    @Path("/allergies")
    @GET
    public Allergies getActiveMembershipAllergies() {

        // Find the preferred locale and create the Allergies.
        final Allergies toReturn = new Allergies(getPreferredLocaleForActiveUser());

        // Populate with known, AllergyVOs.
        foodAndAllergyService.getAllergiesFor(getActiveMembership()).forEach(toReturn::add);

        // All Done.
        return toReturn;
    }

    /**
     * Retrieves a MembershipListVO containing all Memberships within the organisation of the active membership.
     *
     * @param includeLoginNotPermitted If true, the result will contain all Memberships - even those without the
     *                                 privilege to login (i.e. former memberships).
     * @return A MembershipListVO with the Memberships within the active organisation.
     */
    @Path("/memberships/in/own/organisation")
    @GET
    public MembershipListVO getMembershipsInOwnOrganisation(
            @QueryParam(RestfulParameters.INCLUDE_LOGIN_NOT_PERMITTED) final Boolean includeLoginNotPermitted) {

        final boolean effectiveInclude = includeLoginNotPermitted == null ? false : includeLoginNotPermitted;
        final Membership activeMembership = getActiveMembership();
        final Organisation activeOrganisation = activeMembership.getOrganisation();

        // Find the Memberships.
        final List<Membership> membershipsInOwnOrganisation = membershipService.getMembershipsIn(
                activeOrganisation.getId(),
                effectiveInclude);

        // Package the retrieved memberships into the MembershipListVO.
        final MembershipListVO toReturn = new MembershipListVO(activeOrganisation);
        membershipsInOwnOrganisation.forEach(toReturn::add);

        // All Done.
        return toReturn;
    }

    /**
     * Updates the Personal settings of the active user.
     * TODO: Move the algorithm into the membershipServiceImpl instead of stashing it here (better re-usability).
     *
     * @return The unmarshalled MembershipListVO submitted from the client.
     */
    @Path("/personalSettings/update")
    @POST
    public Response updateMembership(MembershipListVO submittedBodyData) {

        // Check sanity
        if (log.isDebugEnabled()) {
            log.debug("Got submitted MembershipListVO: " + submittedBodyData);
        }

        // Dig out the commonly used data.
        final Membership activeMembership = getActiveMembership();
        final User user = activeMembership.getUser();
        boolean isUpdated = false;

        // Check inbound data sanity
        final List<SlimMemberVO> memberInformation = submittedBodyData.getMemberInformation();
        if (memberInformation != null && memberInformation.size() == 1) {

            final SlimMemberVO receivedData = memberInformation.get(0);

            if (log.isDebugEnabled()) {
                log.debug("Updating ActiveUser information with: "
                        + receivedData + ". Current activeUser: "
                        + user.toString());
            }

            // #1) Update Birthday if required
            final LocalDate receivedBirthday = receivedData.getBirthday();
            if (receivedBirthday != null && !user.getBirthday().equals(receivedBirthday)) {
                user.setBirthday(receivedBirthday);
                isUpdated = true;
            }

            // #2) Update ContactInfo details as required.
            final List<SlimContactInfoVO> receivedContactInfo = receivedData.getContactInfo();
            if (receivedContactInfo != null && !receivedContactInfo.isEmpty()) {

                final Map<String, String> existing = user.getContactDetails();

                // Convert the receivedContactInfo to a SortedMap.
                final SortedMap<String, String> received = new TreeMap<>();
                receivedContactInfo.stream()
                        .filter(sci -> {
                            final String trimmedKey = trimAndCleanse(sci.getMedium());
                            return trimmedKey != null && !trimmedKey.isEmpty();
                        })
                        .forEach(sci -> received.put(
                                trimAndCleanse(sci.getMedium()),
                                trimAndCleanse(sci.getAddressOrNumber())));

                // Remove the entries no longer present
                final Map<String, String> toRemove = existing.entrySet()
                        .stream()
                        .filter(entry -> !received.containsKey(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                toRemove.forEach((key, value) -> existing.remove(key));

                if(!toRemove.isEmpty()) {
                    isUpdated = true;

                    if(log.isDebugEnabled()) {
                        log.debug("ContactInfo toRemove: " + toRemove);
                    }
                }

                // Add the new entries
                final Map<String, String> toAdd = received.entrySet()
                        .stream()
                        .filter(entry -> !existing.containsKey(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                toAdd.forEach(existing::put);

                if(!toAdd.isEmpty()) {
                    isUpdated = true;

                    if(log.isDebugEnabled()) {
                        log.debug("ContactInfo toAdd: " + toAdd);
                    }
                }

                // Update the rest of the entries.
                received.entrySet().stream()
                        .filter(entry -> !toRemove.containsKey(entry.getKey()))
                        .filter(entry -> !toAdd.containsKey(entry.getKey()))
                        .forEach(entry -> existing.put(
                                trimAndCleanse(entry.getKey()),
                                trimAndCleanse(entry.getValue())));

                if (log.isDebugEnabled()) {
                    log.info("Received ContactInfo: " + received);
                    log.info("Updated ContactInfo: " + existing);
                }

            } else {
                log.warn("Not updating to empty received ContactInfo.");
            }

            // #3) Update homeAddress data, as required
            final Address receivedAddress = receivedData.getHomeAddress();

            if (!user.getHomeAddress().equals(receivedAddress)) {

                user.setHomeAddress(new Address(
                        trimAndCleanse(receivedAddress.getCareOfLine()),
                        trimAndCleanse(receivedAddress.getDepartmentName()),
                        trimAndCleanse(receivedAddress.getStreet()),
                        trimAndCleanse(receivedAddress.getNumber()),
                        trimAndCleanse(receivedAddress.getCity()),
                        trimAndCleanse(receivedAddress.getZipCode()),
                        trimAndCleanse(receivedAddress.getCountry()),
                        user.getHomeAddress().getDescription()));

                isUpdated = true;
            }
        }

        if(isUpdated) {

            // Update the User data.
            final User updatedUser = membershipService.update(user);
            activeMembership.setUser(updatedUser);

            if (log.isInfoEnabled()) {
                log.info("Updated User to " + updatedUser);
            }
        }

        final MembershipListVO toReturn = new MembershipListVO(activeMembership.getOrganisation());
        toReturn.add(activeMembership);

        // All Done.
        return Response.accepted()
                .entity(toReturn)
                .build();
    }

    //
    // Private helpers
    //

    private String trimAndCleanse(final String toTrim) {
        if (toTrim == null) {
            return null;
        }

        return toTrim.trim().replaceAll("\\s+", " ").trim();
    }

    private Locale getPreferredLocaleForActiveUser() {

        // #1) Default value
        Locale toReturn = getActiveMembership().getOrganisation().getLocale();

        // #2) Overridden in personal settings?
        final Map<String, String> personalSettings = getActiveMembership().getPersonalSettings();
        final String preferredLanguageTag = personalSettings.get(PS_PREFERRED_LOCALE_KEY);
        if (preferredLanguageTag != null) {
            try {
                toReturn = Locale.forLanguageTag(preferredLanguageTag);
            } catch (Exception e) {
                log.warn("Could not create a Locale from LanguageTag [" + preferredLanguageTag
                        + "]. Check personal setting [" + PS_PREFERRED_LOCALE_KEY
                        + "] for " + getActiveMembership().toString());
            }
        }

        // All Done.
        return toReturn;
    }

    private Memberships getActiveUserMemberships() {

        final Membership active = getActiveMembership();

        final Set<Membership> membershipSet = new TreeSet<>();
        membershipSet.add(active);

        // All Done.
        return new Memberships(membershipSet);
    }
}
