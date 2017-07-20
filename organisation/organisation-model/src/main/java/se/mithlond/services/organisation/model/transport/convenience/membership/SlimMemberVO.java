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
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.guild.GuildMembership;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransportable;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A skinny information type holding data required for listing, searching and filtering Members.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"fullAlias", "emailAlias", "firstName", "lastName", "birthday",
                "homeAddress", "contactInfo", "groups", "guilds"})
@XmlAccessorType(XmlAccessType.FIELD)
// @XmlSeeAlso({SlimContactInfoVO.class, SlimGroupMembershipVO.class, SlimGuildMembershipVO.class})
public class SlimMemberVO extends AbstractSimpleTransportable {

    /**
     * The full alias (including subaliases) of this SlimMemberVO.
     */
    @XmlAttribute(required = true)
    private String fullAlias;

    /**
     * The email alias (without domain part) of this SlimMemberVO.
     */
    @XmlAttribute(required = true)
    private String emailAlias;

    /**
     * The first name of the User with this SlimMemberVO.
     */
    @XmlAttribute(required = true)
    private String firstName;

    /**
     * The last name of the User with this SlimMemberVO.
     */
    @XmlAttribute(required = true)
    private String lastName;

    /**
     * Indicates if this SlimMemberVO is permitted login.
     */
    @XmlAttribute(required = true)
    private boolean loginPermitted;

    /**
     * The birthday of the User with this SlimMemberVO.
     */
    @XmlAttribute(required = true)
    private LocalDate birthday;

    /**
     * The home Address of the User with this SlimMemberVO.
     */
    @XmlElement(required = true)
    private Address homeAddress;

    /**
     * The contact information to the user of this SlimMemberVO.
     */
    @XmlElementWrapper
    @XmlElement(name = "info")
    private List<SlimContactInfoVO> contactInfo;

    // TODO: Add a List of Allergy severities as well.

    /**
     * The Group membership list for this Membership.
     */
    @XmlElementWrapper
    @XmlElement(name = "group")
    private List<SlimGroupMembershipVO> groups;

    /**
     * The Guild membership list for this Membership.
     */
    @XmlElementWrapper
    @XmlElement(name = "guild")
    private List<SlimGuildMembershipVO> guilds;

    // TODO: Add a List of OrderLevelGrants as well.

    /**
     * JAXB-friendly constructor.
     */
    public SlimMemberVO() {
    }

    /**
     * Compound constructor creating a SlimMemberVO wrapping the supplied Membership.
     *
     * @param membership the Membership to wrap.
     */
    public SlimMemberVO(@NotNull final Membership membership) {

        // Delegate
        super(membership.getId());

        this.groups = new ArrayList<>();
        this.guilds = new ArrayList<>();
        this.contactInfo = new ArrayList<>();

        // #1) Add user-based data
        final User theUser = membership.getUser();
        this.firstName = theUser.getFirstName();
        this.lastName = theUser.getLastName();
        this.homeAddress = theUser.getHomeAddress();
        this.birthday = theUser.getBirthday();

        theUser.getContactDetails().entrySet()
                .stream()
                .filter(Objects::nonNull)
                .filter(c -> c.getValue() != null && !c.getValue().isEmpty())
                .filter(c -> c.getKey() != null && !c.getKey().isEmpty())
                .map(c -> new SlimContactInfoVO(c.getKey(), c.getValue()))
                .forEach(contactInfo::add);

        // #2) Add membership-based data
        this.fullAlias = membership.getAlias()
                + (membership.getSubAlias() != null ? ", " + membership.getSubAlias() : "");
        this.emailAlias = membership.getEmailAlias();
        this.loginPermitted = membership.isLoginPermitted();

        membership.getGroupMemberships()
                .stream()
                .filter(Objects::nonNull)
                .filter(c -> c instanceof GuildMembership)
                .map(c -> new SlimGuildMembershipVO((GuildMembership) c))
                .forEach(guilds::add);

        membership.getGroupMemberships()
                .stream()
                .filter(Objects::nonNull)
                .filter(c -> !(c instanceof GuildMembership))
                .map(SlimGroupMembershipVO::new)
                .forEach(groups::add);

        // #3) Clean up somewhat
        Collections.sort(groups);
        Collections.sort(guilds);
        Collections.sort(contactInfo);
    }

    /**
     * @return The full alias (including subaliases) of this SlimMemberVO.
     */
    public String getFullAlias() {
        return fullAlias;
    }

    /**
     * @return The email alias (excluding the domain part) of this SlimMemberVO.
     */
    public String getEmailAlias() {
        return emailAlias;
    }

    /**
     * @return The first name of the user for this SlimMemberVO.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return The last name of the user for this SlimMemberVO.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return The birthday of the user for this SlimMemberVO.
     */
    public LocalDate getBirthday() {
        return birthday;
    }

    /**
     * @return True if this SlimMemberVO is permitted login and false otherwise.
     */
    public boolean getLoginPermitted() {
        return loginPermitted;
    }

    /**
     * @return The home address of the user for this SlimMemberVO.
     */
    public Address getHomeAddress() {
        return homeAddress;
    }

    /**
     * @return The contact information for the user of this SlimMemberVO.
     */
    public List<SlimContactInfoVO> getContactInfo() {
        return contactInfo;
    }

    /**
     * @return The Group membership list for this Membership.
     */
    public List<SlimGroupMembershipVO> getGroups() {
        return groups;
    }

    /**
     * @return The Guild membership list for this Membership.
     */
    public List<SlimGuildMembershipVO> getGuilds() {
        return guilds;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        final String theGroups = getGroups().stream()
                .sorted()
                .map(SlimGroupMembershipVO::toString)
                .reduce((l, r) -> l + ", " + r)
                .orElse("<None>");

        final String theGuilds = getGuilds().stream()
                .sorted()
                .map(c -> "[" + c.getXmlId() + ": " + c.getMemberType() + "]")
                .reduce((l, r) -> l + ", " + r)
                .orElse("<None>");

        final AtomicInteger index = new AtomicInteger();
        final String theContactInfo = getContactInfo().stream()
                .sorted()
                .map(c -> index.incrementAndGet() + ") " + c.toString())
                .reduce((l,r) -> " " + l + "\n " + r)
                .orElse("<None>");

        return "SlimMemberVO [ ..... "
                + "\n jpaID         : " + getJpaID()
                + "\n fullAlias     : " + fullAlias
                + "\n emailAlias    : " + emailAlias
                + "\n name          : " + firstName + " " + lastName
                + "\n loginPermitted: " + getLoginPermitted()
                + "\n birthday      : " + birthday
                + "\n homeAddress   : " + homeAddress
                + "\n contactInfo ...\n" + theContactInfo
                + "\n groups        : " + theGroups
                + "\n guilds        : " + theGuilds
                + "\n ..... ]";
    }
}
