/*
* #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
 * *
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
package se.mithlond.services.organisation.model.membership.guild;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.membership.GroupMembership;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.authorization.model.AuthorizationPath;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Relates a Membership to a Guild, including some standard guild titles,
 * such as Auditor, GuildMaster or Deputy GuildMaster.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@DiscriminatorValue("guild")
@Access(value = AccessType.FIELD)
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"guildMaster", "deputyGuildMaster", "auditor"})
@XmlAccessorType(XmlAccessType.FIELD)
public class GuildMembership extends GroupMembership {

    /**
     * Simplified representation of GuildMembership for transport purposes.
     */
    public enum GuildRole {

        /**
         * Not a guild member.
         */
        none,

        /**
         * Guild member; no other guild role.
         */
        member,

        /**
         * Deputy GuildMaster, which implies guild membership.
         */
        deputyGuildMaster,

        /**
         * GuildMaster, which implies guild membership.
         */
        guildMaster,

        /**
         * The guild's Auditor, implying someone who validates the proper doings and activities within the guild.
         */
        auditor
    }

    // Internal state
    @Basic
    @XmlAttribute(required = true)
    private boolean guildMaster;

    @Basic
    @XmlAttribute(required = true)
    private boolean deputyGuildMaster;

    @Basic
    @XmlAttribute(required = true)
    private boolean auditor;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public GuildMembership() {
    }

    /**
     * Compound constructor creating a GuildMembership wrapping the supplied data.
     *
     * @param guild             The Guild to which the Membership should be added as a GuildMembership.
     * @param membership        The Membership which should be attached to the Guild.
     * @param guildMaster       If {@code true}, the Membership of this GuildMembership is the guildmaster
     *                          for the given Guild.
     * @param deputyGuildMaster If {@code true}, the Membership of this GuildMembership is the deputy guildmaster
     *                          for the given Guild.
     * @param auditor           If {@code true}, the Membership of this GuildMembership is the auditor
     *                          for the given Guild.
     */
    public GuildMembership(final Guild guild,
                           final Membership membership,
                           final boolean guildMaster,
                           final boolean deputyGuildMaster,
                           final boolean auditor) {

        super(guild, membership);

        // Assign internal state
        this.guildMaster = guildMaster;
        this.deputyGuildMaster = deputyGuildMaster;
        this.auditor = auditor;
    }

    /**
     * @return The Guild to which this GuildMembership relates.
     */
    public Guild getGuild() {
        return (Guild) getGroup();
    }

    /**
     * Assigns the supplied Guild to this GuildMembership.
     *
     * @param guild The non-null guild which should be assigned to this GuildMembership.
     */
    public void setGuild(final Guild guild) {
        super.setGroup(Validate.notNull(guild, "guild"));
    }

    /**
     * @return {@code true} if the Membership of this GuildMembership is the guildmaster
     * for the given Guild.
     */
    public boolean isGuildMaster() {
        return guildMaster;
    }

    /**
     * @return {@code true}, if the Membership of this GuildMembership is the deputy guildmaster
     * for the given Guild.
     */
    public boolean isDeputyGuildMaster() {
        return deputyGuildMaster;
    }

    /**
     * @return {@code true}, if the Membership of this GuildMembership is the auditor
     * for the given Guild.
     */
    public boolean isAuditor() {
        return auditor;
    }

    /**
     * Set the auditor flag to {@code true} if the Membership of this GuildMembership is the Guild's auditor.
     *
     * @param auditor {@code true}, if the Membership of this GuildMembership is the auditor
     *                for the given Guild.
     */
    public void setAuditor(final boolean auditor) {
        this.auditor = auditor;
    }

    /**
     * Set the deputyGuildMaster flag to {@code true} if the Membership of this GuildMembership is the Guild's
     * deputy guild master.
     *
     * @param deputyGuildMaster {@code true}, if the Membership of this GuildMembership is the
     *                          deputy guild master for the given Guild.
     */
    public void setDeputyGuildMaster(final boolean deputyGuildMaster) {
        this.deputyGuildMaster = deputyGuildMaster;
    }

    /**
     * Set the guildMaster flag to {@code true} if the Membership of this GuildMembership is the Guild's guildmaster.
     *
     * @param guildMaster {@code true}, if the Membership of this GuildMembership is the guild master
     *                    for the given Guild.
     */
    public void setGuildMaster(final boolean guildMaster) {
        this.guildMaster = guildMaster;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return super.hashCode()
                + (guildMaster ? 4 : 0)
                + (deputyGuildMaster ? 2 : 0)
                + (auditor ? 1 : 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {
        return super.equals(obj);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final GroupMembership that) {

        // Check sanity
        if (that == null) {
            return -1;
        } else if (that == this) {
            return 0;
        }

        // Delegate
        int toReturn = super.compareTo(that);

        if(that instanceof GuildMembership) {
            final GuildMembership thatMembership = (GuildMembership) that;

            if (toReturn == 0) {
                toReturn = Boolean.compare(isGuildMaster(), thatMembership.isGuildMaster());
            }
            if (toReturn == 0) {
                toReturn = Boolean.compare(isDeputyGuildMaster(), thatMembership.isDeputyGuildMaster());
            }
            if (toReturn == 0) {
                toReturn = Boolean.compare(isAuditor(), thatMembership.isAuditor());
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        final String guildName = getGroup() == null ? "<guild not yet set>" : getGroup().getGroupName();
        String alias = "<membership not yet set>";
        String memberType = "none";

        if (getMembership() != null) {

            alias = getMembership().getAlias();

            memberType = "member";
            if (isGuildMaster()) {
                memberType = "guildMaster";
            } else if (isDeputyGuildMaster()) {
                memberType = "deputyGuildMaster";
            } else if (isAuditor()) {
                memberType = "auditor";
            }
        }

        // All done.
        return "GuildMembership [" + alias + " -> " + guildName + "]: " + memberType;
    }

    /**
     * Factory method to extract a GuildRole for a supplied GuildMembership.
     *
     * @param guildMembership The GuildMembership to convert.
     * @return The GuildRole for the supplied guildMembership; {@code null} GuildMembership implies a 'none' GuildRole.
     */
    public static GuildRole toGuildRole(final GuildMembership guildMembership) {

        if(guildMembership == null) {
            return GuildRole.none;
        }

        GuildRole toReturn = GuildRole.member;
        if(guildMembership.isGuildMaster()) {
            toReturn = GuildRole.guildMaster;
        } else if(guildMembership.isDeputyGuildMaster()) {
            toReturn = GuildRole.deputyGuildMaster;
        } else if(guildMembership.isAuditor()) {
            toReturn = GuildRole.auditor;
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<SemanticAuthorizationPath> getPaths() {

        final SortedSet<SemanticAuthorizationPath> toReturn = new TreeSet<>();
        toReturn.add(new AuthorizationPath(
                getGroup().getOrganisation().getOrganisationName(),
                getGroup().getGroupName(),
                toGuildRole(this).toString()));
        return toReturn;
    }
}
