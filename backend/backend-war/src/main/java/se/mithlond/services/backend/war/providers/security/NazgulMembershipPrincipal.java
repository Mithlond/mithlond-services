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
package se.mithlond.services.backend.war.providers.security;

import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.GroupMembership;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.membership.guild.GuildMembership;
import se.jguru.nazgul.core.algorithms.api.Validate;

import java.security.Principal;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A Nazgul-flavoured Principal implementation wrapping a Nazgul Membership.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NazgulMembershipPrincipal implements Principal {

    // Internal state
    private Membership membership;

    /**
     * Creates a NazgulMembershipPrincipal wrapping the supplied Membership.
     *
     * @param membership A non-null Membership to wrap in this NazgulMembershipPrincipal.
     */
    public NazgulMembershipPrincipal(final Membership membership) {

        // Check sanity
        Validate.notNull(membership, "membership");

        // Assign internal ste
        this.membership = membership;
    }

    /**
     * @return The alias of the Member for the supplied Membership.
     */
    @Override
    public String getName() {
        return membership.getAlias();
    }

    /**
     * @return The Membership wrapped in this Principal.
     */
    public Membership getMembership() {
        return membership;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        final StringBuilder groupBuilder = new StringBuilder();
        final StringBuilder guildBuilder = new StringBuilder();

        final Set<GroupMembership> groupMemberships = membership.getGroupMemberships();
        if (groupMemberships != null) {

            SortedSet<String> sortedGroupSet = new TreeSet<>();
            SortedSet<String> sortedGuildSet = new TreeSet<>();

            for (GroupMembership current : groupMemberships) {

                if (current instanceof GuildMembership) {

                    final GuildMembership currentGuildMembership = (GuildMembership) current;
                    final Guild currentGuild = currentGuildMembership.getGuild();
                    sortedGuildSet.add(currentGuild.getGroupName());

                } else {

                    final Group currentGroup = current.getGroup();
                    sortedGroupSet.add(currentGroup.getGroupName());
                }
            }

            sortedGroupSet.forEach(current -> {
                groupBuilder.append(current).append(" ");
            });
            sortedGuildSet.forEach(current -> {
                guildBuilder.append(current).append(" ");
            });
        }

        return "======= [Nazgul Membership Principal] =======\n"
                + "  Member Alias : " + membership.getAlias() + "\n"
                + "  Organisation : " + membership.getOrganisation().getOrganisationName() + "\n"
                + "  Groups       : " + groupBuilder.toString() + "\n"
                + "  Guilds       : " + guildBuilder.toString() + "\n"
                + "======= [End Nazgul Membership Principal] =======\n";

    }
}
