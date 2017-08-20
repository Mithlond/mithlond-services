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
import se.mithlond.services.content.api.UserFeedbackService;
import se.mithlond.services.content.model.transport.feedback.CharacterizedDescription;
import se.mithlond.services.organisation.api.FoodAndAllergyService;
import se.mithlond.services.organisation.api.MembershipService;
import se.mithlond.services.organisation.api.OrganisationService;
import se.mithlond.services.organisation.api.parameters.GroupIdSearchParameters;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.membership.guild.GuildMembership;
import se.mithlond.services.organisation.model.transport.convenience.food.SlimFoodPreferencesVO;
import se.mithlond.services.organisation.model.transport.convenience.membership.MembershipListVO;
import se.mithlond.services.organisation.model.transport.food.Allergies;
import se.mithlond.services.organisation.model.transport.food.FoodPreferenceVO;
import se.mithlond.services.organisation.model.transport.membership.Memberships;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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

    @EJB
    private UserFeedbackService userFeedbackService;

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
     * the currently logged in user, including known Food Preferences.
     *
     * @return An {@link Allergies} wrapper containing the Membership, User, Organisation
     * and Groups of the active Membership.
     * @see Memberships
     */
    @Path("/allergies")
    @GET
    public Allergies getActiveMembershipAllergiesAndFoodPrefs() {

        // Find the preferred locale and create the Allergies.
        final Allergies toReturn = new Allergies(getPreferredLocaleForActiveUser());

        // Populate with known AllergyVOs and FoodPreferences.
        foodAndAllergyService.getAllergiesFor(getActiveMembership()).forEach(toReturn::add);
        foodAndAllergyService.getPreferencesFor(getActiveMembership()).forEach(toReturn::add);

        // Log somewhat.
        log.info("Returning: " + toReturn.toString());

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
     * Updates the Food preferences of the active user.
     *
     * @param submittedBodyData The SlimFoodPreferencesVO containing the updated FoodPreferences.
     * @return The SlimFoodPreferencesVO containing the resulting FoodPreferences.
     */
    @Path("/foodPreferences/update")
    @POST
    public SlimFoodPreferencesVO updateFoodPreferences(final SlimFoodPreferencesVO submittedBodyData) {

        // Check sanity
        if (log.isDebugEnabled()) {

            final String prefsMsg = submittedBodyData != null && submittedBodyData.getFoodPreferences() != null
                    ? "[" + submittedBodyData.getFoodPreferences().size() + "] preferences: "
                    + submittedBodyData.getFoodPreferences().stream().map(FoodPreferenceVO::getPreference)
                    .reduce((l, r) -> l + ", " + r).orElse("<none>")
                    : "<none found; null encountered>";

            log.debug("Got submitted SlimFoodPreferencesVO: " + prefsMsg);
        }

        // All Done.
        return foodAndAllergyService.updateFoodPreferences(getActiveMembership(), submittedBodyData);
    }

    /**
     * Updates the allergies for the activeUser.
     *
     * @param submittedBodyData The Allergies containing the expected/desired target state.
     * @return The Allergies containing the resulting (server-side) state.
     */
    @Path("/allergies/update")
    @POST
    public Allergies updateAllergies(final Allergies submittedBodyData) {

        // Check sanity
        if (log.isDebugEnabled()) {
            log.debug("Got submitted Allergies: " + submittedBodyData.toString());
        }

        // All Done.
        return foodAndAllergyService.updateAllergies(getActiveMembership(), submittedBodyData);
    }

    /**
     * Updates the GuildMemberships of the active user.
     *
     * @param submittedBodyData The List of MembershipVOs considered the expected/desired target state.
     * @return The MembershipListVO containing the updated Membership.
     */
    @Path("/guildMemberships/update")
    @POST
    public MembershipListVO updateGuildMemberships(final MembershipListVO submittedBodyData) {

        // Check sanity
        if (log.isDebugEnabled()) {
            log.debug("Got submitted MembershipListVO: " + submittedBodyData);
        }

        // Delegate to the service
        final Membership updatedMembership = membershipService.updateGuildMemberships(
                getActiveMembership(),
                submittedBodyData);

        if (log.isInfoEnabled()) {
            log.info("Updated GuildMemberships. Got " + updatedMembership.getGroupMemberships()
                    .stream()
                    .filter(Objects::nonNull)
                    .filter(gr -> gr instanceof GuildMembership)
                    .map(gr -> (GuildMembership) gr)
                    .map(gu -> {

                        final Guild theGuild = gu.getGuild();

                        return "[" + theGuild.getId() + " (" + theGuild.getGroupName() + "): "
                                + GuildMembership.toGuildRole(gu) + "]";
                    })
                    .reduce((l, r) -> l + ", " + r)
                    .orElse("<none>"));
        }

        // All Done.
        final MembershipListVO toReturn = new MembershipListVO(updatedMembership.getOrganisation());
        toReturn.add(updatedMembership);
        return toReturn;
    }

    /**
     * Updates the Personal settings of the active user.
     *
     * @return The MembershipListVO containing the updated Membership.
     */
    @Path("/personalSettings/update")
    @POST
    public MembershipListVO updateMembership(final MembershipListVO submittedBodyData) {

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

    /**
     * Accepts the supplied CharacterizedDescription and processes it according to business rules.
     *
     * @param submittedBodyData The non-null CharacterizedDescription received.
     * @return A response describing how the server handled the feedback from the user.
     */
    @Path("/ideaOrBug/submit")
    @POST
    public CharacterizedDescription submitIdeaOrBug(final CharacterizedDescription submittedBodyData) {

        // Check sanity
        if (log.isDebugEnabled()) {
            log.debug("Got submitted CharacterizedDescription: " + submittedBodyData);
        }

        //
        // Handle any internal errors and ensure that we always respond with
        // a properly formed CharacterizedDescription.
        //
        final CharacterizedDescription toReturn;
        
        try {
            toReturn = userFeedbackService.submitUserFeedback(
                    getActiveMembership(),
                    submittedBodyData);
        } catch (Exception e) {
            log.error("Could not deliver feedback", e);
            return new CharacterizedDescription("Unsuccessful", "Failed handling user notification.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Returning: " + toReturn);
        }
        return toReturn;
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
