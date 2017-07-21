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
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.transport.convenience.membership.MembershipListVO;
import se.mithlond.services.organisation.model.transport.food.Allergies;
import se.mithlond.services.organisation.model.transport.membership.Memberships;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
     * Updates the GuildMemberships of the active user.
     *
     * @return The MembershipListVO containing the updated Membership.
     */
    @Path("/guildMemberships/update")
    @POST
    public MembershipListVO updateGuildMemberships(MembershipListVO submittedBodyData) {

        // Check sanity
        if (log.isDebugEnabled()) {
            log.debug("Got submitted MembershipListVO: " + submittedBodyData);
        }

        // Delegate to the service
        final Membership updatedMembership = membershipService.updateGuildMemberships(
                getActiveMembership(),
                submittedBodyData);

        // All Done.
        return new MembershipListVO(updatedMembership.getOrganisation());
    }

    /**
     * Updates the Personal settings of the active user.
     * 
     * @return The MembershipListVO containing the updated Membership.
     */
    @Path("/personalSettings/update")
    @POST
    public MembershipListVO updateMembership(MembershipListVO submittedBodyData) {

        // Check sanity
        if (log.isDebugEnabled()) {
            log.debug("Got submitted MembershipListVO: " + submittedBodyData);
        }

        // Delegate to the service
        final Membership updatedMembership = membershipService.updatePersonalSettings(
                getActiveMembership(),
                submittedBodyData);

        // All Done.
        return new MembershipListVO(updatedMembership.getOrganisation());
    }

    //
    // Private helpers
    //

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
