/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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
package se.mithlond.services.organisation.model.transport.convenience.membership;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.guild.GuildMembership;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"memberType"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SlimGuildMembershipVO extends SlimGroupMembershipVO {

    /**
     * The type of membership within this Guild.
     */
    @XmlAttribute(required = true)
    private String memberType;

    /**
     * JAXB-friendly constructor.
     */
    public SlimGuildMembershipVO() {
    }

    /**
     * Compound constructor converting the supplied GuildMembership to a SlimGuildMembershipVO.
     *
     * @param guildMembership The GuildMembership to convert.
     */
    public SlimGuildMembershipVO(@NotNull final GuildMembership guildMembership) {

        // Delegate
        super(guildMembership);

        // Assign internal state
        this.memberType = GuildMembership.toGuildRole(guildMembership).name();
    }

    /**
     * @return The type of membership within this Guild.
     */
    public String getMemberType() {
        return memberType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + ", memberType: " + getMemberType();
    }
}
