/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-impl-ejb
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
package se.mithlond.services.organisation.impl.ejb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.mithlond.services.organisation.api.MembershipService;
import se.mithlond.services.organisation.api.OrganisationService;
import se.mithlond.services.organisation.api.parameters.GroupIdSearchParameters;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.membership.guild.GuildMembership;
import se.mithlond.services.organisation.model.transport.convenience.membership.MembershipListVO;
import se.mithlond.services.organisation.model.transport.convenience.membership.SlimContactInfoVO;
import se.mithlond.services.organisation.model.transport.convenience.membership.SlimGuildMembershipVO;
import se.mithlond.services.organisation.model.transport.convenience.membership.SlimMemberVO;
import se.mithlond.services.organisation.model.transport.membership.Groups;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.StandardAlgorithms;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransportable;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Stateless EJB implementation of the MembershipService specification.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Stateless
public class MembershipServiceBean extends AbstractJpaService implements MembershipService {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(MembershipServiceBean.class);

    @EJB
    private OrganisationService organisationServiceBean;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Membership> getMembershipsIn(final Long orgJpaID, final boolean includeLoginNotPermitted) {

        final List<Membership> toReturn = new ArrayList<>();

        final List<Membership> loginPermittedMemberships = entityManager.createNamedQuery(
                Membership.NAMEDQ_GET_BY_ORGANISATION_ID_LOGINPERMITTED, Membership.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_ID, orgJpaID)
                .setParameter(OrganisationPatterns.PARAM_LOGIN_PERMITTED, true)
                .getResultList();
        toReturn.addAll(loginPermittedMemberships);

        // Include the ones denied Login?
        if (includeLoginNotPermitted) {
            final List<Membership> loginNotPermittedMemberships = entityManager.createNamedQuery(
                    Membership.NAMEDQ_GET_BY_ORGANISATION_ID_LOGINPERMITTED, Membership.class)
                    .setParameter(OrganisationPatterns.PARAM_ORGANISATION_ID, orgJpaID)
                    .setParameter(OrganisationPatterns.PARAM_LOGIN_PERMITTED, false)
                    .getResultList();
            toReturn.addAll(loginNotPermittedMemberships);
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Membership getMembership(final String organisationName, final String alias) {

        final List<Membership> result = entityManager.createNamedQuery(
                Membership.NAMEDQ_GET_BY_ALIAS_ORGANISATION, Membership.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisationName)
                .setParameter(OrganisationPatterns.PARAM_ALIAS, alias)
                .getResultList();

        if (result.size() > 1) {
            log.warn("Got [" + result.size() + "] number of Memberships for organisation ["
                    + organisationName + "] and alias [" + alias + "]. Returning the first result.");
        }

        // All done.
        return result.size() == 0 ? null : result.get(0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Membership> getActiveMemberships(final String organisationName,
                                                 final String firstName,
                                                 final String lastName) {

        final List<Membership> toReturn = new ArrayList<>();

        final List<Membership> allMemberships;
        try {
            allMemberships = entityManager.createNamedQuery(
                    Membership.NAMEDQ_GET_BY_NAME_ORGANISATION, Membership.class)
                    .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisationName)
                    .setParameter(OrganisationPatterns.PARAM_FIRSTNAME, firstName)
                    .setParameter(OrganisationPatterns.PARAM_LASTNAME, lastName)
                    // .setParameter(OrganisationPatterns.PARAM_LOGIN_PERMITTED, true)
                    .getResultList();

        } catch (ConstraintViolationException e) {

            final String exceptionMessage = getConstraintViolationErrorMessage(e);
            log.error(exceptionMessage);

            // Re-throw
            throw e;
        }

        if (allMemberships != null && !allMemberships.isEmpty()) {
            allMemberships.stream().filter(Membership::isLoginPermitted).forEach(toReturn::add);
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Membership updatePersonalSettings(final Membership toUpdate, final MembershipListVO desiredState) {

        // Check sanity
        final Optional<SlimMemberVO> slimMemberVO = this.validateAndGet(desiredState);
        if (!slimMemberVO.isPresent()) {
            return toUpdate;
        }

        Validate.notNull(toUpdate, "toUpdate");

        // #1) Dig out the commonly used data.
        boolean isUpdated = false;
        final User user = toUpdate.getUser();
        final SlimMemberVO receivedData = slimMemberVO.get();

        // #2) Update plain data.
        final LocalDate receivedBirthday = receivedData.getBirthday();
        if (receivedBirthday != null && !user.getBirthday().equals(receivedBirthday)) {
            user.setBirthday(receivedBirthday);
            isUpdated = true;
        }

        // #3) Update ContactInfo details as required.
        final List<SlimContactInfoVO> receivedContactInfo = receivedData.getContactInfo();
        if (receivedContactInfo != null && !receivedContactInfo.isEmpty()) {

            final Map<String, String> existing = user.getContactDetails();

            // #3.1) Convert the receivedContactInfo to a SortedMap.
            final SortedMap<String, String> received = new TreeMap<>();
            receivedContactInfo.stream()
                    .filter(sci -> {
                        final String trimmedKey = StandardAlgorithms.trimAndHarmonizeWhitespace(sci.getMedium());
                        return trimmedKey != null && !trimmedKey.isEmpty();
                    })
                    .forEach(sci -> received.put(
                            StandardAlgorithms.trimAndHarmonizeWhitespace(sci.getMedium()),
                            StandardAlgorithms.trimAndHarmonizeWhitespace(sci.getAddressOrNumber())));

            // #3.2) Remove the ContactInfo entries no longer present
            final Map<String, String> toRemove = existing.entrySet()
                    .stream()
                    .filter(entry -> !received.containsKey(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            toRemove.forEach((key, value) -> existing.remove(key));

            if (!toRemove.isEmpty()) {
                isUpdated = true;

                if (log.isDebugEnabled()) {
                    log.debug("ContactInfo toRemove: " + toRemove);
                }
            }

            // #3.3) Add the new new ContactInfo entries
            final Map<String, String> toAdd = received.entrySet()
                    .stream()
                    .filter(entry -> !existing.containsKey(entry.getKey()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            toAdd.forEach(existing::put);

            if (!toAdd.isEmpty()) {
                isUpdated = true;

                if (log.isDebugEnabled()) {
                    log.debug("ContactInfo toAdd: " + toAdd);
                }
            }

            // Update the rest of the entries.
            received.entrySet().stream()
                    .filter(entry -> !toRemove.containsKey(entry.getKey()))
                    .filter(entry -> !toAdd.containsKey(entry.getKey()))
                    .forEach(entry -> existing.put(
                            StandardAlgorithms.trimAndHarmonizeWhitespace(entry.getKey()),
                            StandardAlgorithms.trimAndHarmonizeWhitespace(entry.getValue())));

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
                    StandardAlgorithms.trimAndHarmonizeWhitespace(receivedAddress.getCareOfLine()),
                    StandardAlgorithms.trimAndHarmonizeWhitespace(receivedAddress.getDepartmentName()),
                    StandardAlgorithms.trimAndHarmonizeWhitespace(receivedAddress.getStreet()),
                    StandardAlgorithms.trimAndHarmonizeWhitespace(receivedAddress.getNumber()),
                    StandardAlgorithms.trimAndHarmonizeWhitespace(receivedAddress.getCity()),
                    StandardAlgorithms.trimAndHarmonizeWhitespace(receivedAddress.getZipCode()),
                    StandardAlgorithms.trimAndHarmonizeWhitespace(receivedAddress.getCountry()),
                    user.getHomeAddress().getDescription()));

            isUpdated = true;
        }

        if (isUpdated) {

            // Update the User data.
            final User updatedUser = update(user);
            toUpdate.setUser(updatedUser);

            if (log.isInfoEnabled()) {
                log.info("Updated User to " + updatedUser);
            }
        }

        // All Done.
        return toUpdate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Membership updateGuildMemberships(final Membership activeMembership, final MembershipListVO desiredState) {

        // Check sanity
        final Optional<SlimMemberVO> slimMemberVO = this.validateAndGet(desiredState);
        if (!slimMemberVO.isPresent()) {
            return activeMembership;
        }

        Validate.notNull(activeMembership, "activeMembership");

        // #1) Dig out the commonly used data.
        final SlimMemberVO receivedData = slimMemberVO.get();

        if (log.isDebugEnabled()) {
            log.debug("Starting processing; received: " + receivedData);
        }

        final Membership[] toReturn = new Membership[]{activeMembership};

        // #2) Find the Groups for which we should have Memberships,
        //     as suggested by the inbound state.
        final List<SlimGuildMembershipVO> desiredGuildVOs = receivedData.getGuilds();

        final Map<Long, GuildMembership.GuildRole> desiredID2RoleMap = desiredGuildVOs.stream()
                .filter(slimVO -> slimVO.getJpaID() != null && slimVO.getMemberType() != null)
                .collect(Collectors.toMap(
                        AbstractSimpleTransportable::getJpaID,
                        slimVO -> MembershipServiceBean.getRoleFrom(slimVO.getMemberType())));

        if (log.isDebugEnabled()) {
            log.debug("Found desired id2RoleMap: " + desiredID2RoleMap.entrySet()
                    .stream()
                    .map(e -> "[JpaID: " + e.getKey() + ", Role: " + e.getValue() + "]")
                    .reduce((l, r) -> l + ", " + r)
                    .orElse("<none>"));
        }

        // #3) Find the existing state.
        final List<GuildMembership> existingGuildMemberships = activeMembership.getGroupMemberships()
                .stream()
                .filter(gr -> gr instanceof GuildMembership)
                .map(gr -> (GuildMembership) gr)
                .collect(Collectors.toList());
        final Map<Long, GuildMembership.GuildRole> existingGuildMembershipMap = existingGuildMemberships
                .stream()
                .sorted()
                .collect(Collectors.toMap(gms -> gms.getGuild().getId(), GuildMembership::toGuildRole));

        if (log.isInfoEnabled()) {
            log.info("Found existing GuildMemberships: " + existingGuildMemberships
                    .stream()
                    .map(GuildMembership::toString)
                    .reduce((l, r) -> l + ", " + r)
                    .orElse("<none>"));
        }

        // #4) Synthesize sets of JpaIDs to remove, add and update.
        final Set<Long> toRemoveGuildIDs = existingGuildMembershipMap.keySet()
                .stream()
                .filter(existingID -> !desiredID2RoleMap.keySet().contains(existingID))
                .collect(Collectors.toSet());
        final Set<Long> toAddGuildIDs = desiredID2RoleMap.keySet()
                .stream()
                .filter(desiredID -> !existingGuildMembershipMap.keySet().contains(desiredID))
                .collect(Collectors.toSet());
        final Set<Long> toUpdateIDs = desiredID2RoleMap.keySet()
                .stream()
                .filter(desiredID -> !toRemoveGuildIDs.contains(desiredID))
                .filter(desiredID -> !toAddGuildIDs.contains(desiredID))
                .collect(Collectors.toSet());

        if (log.isInfoEnabled()) {


            final StringBuilder builder = new StringBuilder(" === Updating GuildMemberships for ["
                    + activeMembership.getAlias() + " in "
                    + activeMembership.getOrganisation().getOrganisationName() + "]");

            final String addedGuildMemberships = "\nAdding GuildMem to: " + toAddGuildIDs
                    .stream()
                    .sorted()
                    .map(c -> "" + c)
                    .reduce((l, r) -> l + ", " + r)
                    .orElse("<none>");
            builder.append(addedGuildMemberships);
            
            final String removedGuildMemberships = "\nRemoving GuildMem from: " + toRemoveGuildIDs
                    .stream()
                    .sorted()
                    .map(c -> "" + c)
                    .reduce((l, r) -> l + ", " + r)
                    .orElse("<none>");
            builder.append(removedGuildMemberships);

            final String updateCandidates = "\nPossibly updating GuildMem type: " + toUpdateIDs
                    .stream()
                    .sorted()
                    .map(c -> "" + c)
                    .reduce((l, r) -> l + ", " + r)
                    .orElse("<none>");
            builder.append(updateCandidates);
            log.info(builder.toString());
        }

        // #5) Remove any undesired guild memberships.
        final Set<GuildMembership> toBeRemoved = activeMembership.getGroupMemberships()
                .stream()
                .filter(gr -> gr instanceof GuildMembership)
                .map(gr -> (GuildMembership) gr)
                .filter(grm -> toRemoveGuildIDs.contains(grm.getGroup().getId()))
                .collect(Collectors.toSet());

        toBeRemoved.forEach(gm -> {

            GuildMembership merged = null;

            // First, delete
            try {
                if(entityManager.contains(gm)) {
                    entityManager.remove(gm);
                } else {
                    merged = entityManager.merge(gm);
                    entityManager.remove(merged);
                }
            } catch (Exception e) {
                log.error("Could not remove " + gm.toString(), e);
            }

            // Second, remove
            activeMembership.getGroupMemberships().remove(gm);

            // Third, update
            toReturn[0] = update(toReturn[0]);
        });
        activeMembership.getGroupMemberships().removeAll(toBeRemoved);

        // #6) Add any new GuildMemberships as requested.
        if (toAddGuildIDs != null && !toAddGuildIDs.isEmpty()) {

            final GroupIdSearchParameters searchParameters = GroupIdSearchParameters
                    .builder()
                    .withDetailedResponsePreferred(true)
                    .withOrganisationIDs(activeMembership.getOrganisation().getId())
                    .withGroupIDs(toAddGuildIDs.toArray(new Long[toAddGuildIDs.size()]))
                    .build();

            final Groups desiredGroups = organisationServiceBean.getGroups(searchParameters);
            final Map<Long, GuildMembership> toAdd = desiredGroups.getGroups()
                    .stream()
                    .filter(g -> g instanceof Guild)
                    .map(g -> (Guild) g)
                    .collect(Collectors.toMap(
                            NazgulEntity::getId,
                            g -> {

                                // 
                                // Map the Guild JPA ID and desired GuildRole
                                // to a (newly constructed) GuildMembership.
                                //
                                final GuildMembership.GuildRole desiredGuildRole = desiredID2RoleMap.get(g.getId());

                                final boolean isGuildMaster = GuildMembership.GuildRole.guildMaster
                                        .name()
                                        .equalsIgnoreCase(desiredGuildRole.name());

                                final boolean isDeputy = GuildMembership.GuildRole.deputyGuildMaster
                                        .name()
                                        .equalsIgnoreCase(desiredGuildRole.name());

                                final boolean isAuditor = GuildMembership.GuildRole.auditor
                                        .name()
                                        .equalsIgnoreCase(desiredGuildRole.name());

                                return new GuildMembership(g,
                                        activeMembership,
                                        isGuildMaster,
                                        isDeputy,
                                        isAuditor);
                            }));

            //
            // Since the groupMemberships Set has CascadeType.ALL set, we should
            // not need to explicitly persist each new GuildMembership ...
            //
            // ... but doing it explicitly has no improper effects, and also prevents this
            // method from breaking should the annotation be changed in the entity.
            //
            toAdd.values().forEach(obj -> {

                // First, persist
                create(obj);

                // Second, add
                activeMembership.getGroupMemberships().add(obj);

                // Now update to persist.
                toReturn[0] = update(toReturn[0]);
            });
        }

        // Update the others GuildMemberships (possibly WRT membership type)
        toUpdateIDs.stream()
                .map(anID -> activeMembership.getGroupMemberships()
                        .stream()
                        .filter(gr -> gr instanceof GuildMembership)
                        .map(gr -> (GuildMembership) gr)
                        .filter(gu -> gu.getGuild().getId() == anID).findFirst().orElse(null))
                .filter(Objects::nonNull)
                .forEach(gu -> {

                    // Find the desired membership type for this Guild.
                    final GuildMembership.GuildRole guildRole = desiredID2RoleMap.get(gu.getGuild().getId());
                    if (guildRole != null) {

                        boolean isGuildMaster = false;
                        boolean isDeputy = false;
                        boolean isAuditor = false;

                        switch (guildRole) {
                            case guildMaster:
                                isGuildMaster = true;
                                break;

                            case deputyGuildMaster:
                                isDeputy = true;
                                break;

                            case auditor:
                                isAuditor = true;
                                break;

                            case member:
                                // Simply use default values
                                break;

                            case none:
                                log.warn("No desired GuildRole 'none' for " + gu.toString()
                                        + ". This implies that the GuildMembership should be removed.");
                                break;
                        }

                        // Update the state of this GuildMembership
                        gu.setGuildMaster(isGuildMaster);
                        gu.setDeputyGuildMaster(isDeputy);
                        gu.setAuditor(isAuditor);

                        // Then, merge it into the
                        entityManager.merge(gu);

                        // Now update to persist.
                        toReturn[0] = update(toReturn[0]);

                    } else {
                        log.warn("No desired GuildRole given for " + gu.toString()
                                + ". Not modifying that GuildMembership.");
                    }
                });

        // All Done.
        return toReturn[0];
    }

    //
    // Private helpers
    //

    private static GuildMembership.GuildRole getRoleFrom(@NotNull final String memberType) {

        // #0) Check sanity
        final String effValue = Validate.notEmpty(memberType, "memberType");

        // #1) The value could be the exact constant of the GuildRole's name().
        GuildMembership.GuildRole toReturn = GuildMembership.GuildRole.none;

        final Optional<GuildMembership.GuildRole> exactMatch = Arrays.stream(GuildMembership.GuildRole.values())
                .filter(gr -> effValue.equalsIgnoreCase(gr.name()))
                .findFirst();
        if (exactMatch.isPresent()) {
            toReturn = exactMatch.get();
        } else {

            final SortedMap<String, GuildMembership.GuildRole> swedishRoleNames = new TreeMap<>();
            swedishRoleNames.put("Medlem", GuildMembership.GuildRole.member);
            swedishRoleNames.put("Gillesmästare", GuildMembership.GuildRole.guildMaster);
            swedishRoleNames.put("Vice Gillesmästare", GuildMembership.GuildRole.deputyGuildMaster);

            final GuildMembership.GuildRole guildRoleOrNull = swedishRoleNames.get(effValue);
            if (guildRoleOrNull != null) {
                toReturn = guildRoleOrNull;
            }
        }

        // All Done.
        return toReturn;
    }

    private Optional<SlimMemberVO> validateAndGet(final MembershipListVO desiredState) {

        // Check sanity
        Validate.notNull(desiredState, "desiredState");

        final List<SlimMemberVO> memberInformation = desiredState.getMemberInformation();
        if (memberInformation == null || memberInformation.size() != 1) {

            final String memberInformationSize = memberInformation == null ? "0 (null)" : "" + memberInformation.size();
            log.warn("Expected 1, but received [" + memberInformationSize + "] SlimMemberVO objects. Aborting update.");
            return Optional.empty();
        }

        // All Done.
        return Optional.of(memberInformation.get(0));
    }
}
