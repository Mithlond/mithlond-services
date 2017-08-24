/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model.membership;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.membership.guild.GuildMembership;
import se.mithlond.services.organisation.model.membership.order.OrderLevel;
import se.mithlond.services.organisation.model.membership.order.OrderLevelGrant;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.authorization.model.AuthorizationPath;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPathProducer;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Realization of a Membership, associating a Member with an Organisation.
 * Holds things like login permitted for the user within the Organisation,
 * and the Organisation's email alias for the user.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = Membership.NAMEDQ_GET_BY_ALIAS_ORGANISATION,
                query = "select a from Membership a "
                        + " where a.alias like :" + OrganisationPatterns.PARAM_ALIAS
                        + " and a.organisation.organisationName like :" + OrganisationPatterns.PARAM_ORGANISATION_NAME
                        + " order by a.alias"),
        @NamedQuery(name = Membership.NAMEDQ_GET_BY_NAME_ORGANISATION,
                query = "select a from Membership a "
                        + " where a.user.firstName like :" + OrganisationPatterns.PARAM_FIRSTNAME
                        + " and a.user.lastName like :" + OrganisationPatterns.PARAM_LASTNAME
                        + " and a.organisation.organisationName like :" + OrganisationPatterns.PARAM_ORGANISATION_NAME
                        + " order by a.alias"),
        @NamedQuery(name = Membership.NAMEDQ_GET_BY_GROUP_ORGANISATION_LOGINPERMITTED,
                query = "select a from Membership a, in(a.groupMemberships) groupMemberships "
                        + " where groupMemberships.group.groupName like :" + OrganisationPatterns.PARAM_GROUP_NAME
                        + " and a.organisation.organisationName like :" + OrganisationPatterns.PARAM_ORGANISATION_NAME
                        + " and a.loginPermitted = :" + OrganisationPatterns.PARAM_LOGIN_PERMITTED
                        + " order by a.alias"),
        @NamedQuery(name = Membership.NAMEDQ_GET_BY_ORGANISATION_ID_LOGINPERMITTED,
                query = "select a from Membership a"
                        + " where a.organisation.id = :" + OrganisationPatterns.PARAM_ORGANISATION_ID
                        + " and a.loginPermitted = :" + OrganisationPatterns.PARAM_LOGIN_PERMITTED
                        + " order by a.alias"),
        @NamedQuery(name = Membership.NAMEDQ_GET_BY_ORGANISATION_ID_LOGINPERMITTED_AND_USERID,
                query = "select a from Membership a"
                        + " where a.organisation.id = :" + OrganisationPatterns.PARAM_ORGANISATION_ID
                        + " and a.loginPermitted = :" + OrganisationPatterns.PARAM_LOGIN_PERMITTED
                        + " and a.user.id = :" + OrganisationPatterns.PARAM_USER_ID),
})
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "aliasAndOrganisationIsUnique", columnNames = {"alias", "organisation_id"})})
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"alias", "subAlias", "emailAlias",
        "loginPermitted", "user", "groupMemberships", "orderLevelGrants", "organisation", "personalSettings"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Membership extends NazgulEntity implements Comparable<Membership>, SemanticAuthorizationPathProducer {

    // Our Logger
    @XmlTransient
    private static final Logger log = LoggerFactory.getLogger(Membership.class);

    // Constants
    private static final long serialVersionUID = 8829990028L;

    /**
     * Standard comparator using the Alias and Organisation.OrganisationName properties of Memberships for comparison.
     */
    @XmlTransient
    public static final Comparator<Membership> ALIAS_AND_ORG_COMPARATOR = (l, r) -> {

        final boolean nullLeft = l == null;
        final boolean nullRight = r == null;

        if (nullLeft) {
            return nullRight ? 0 : 1;
        }
        if (nullRight) {
            return -1;
        }

        int toReturn = l.getOrganisation().getOrganisationName()
                .compareTo(r.getOrganisation().getOrganisationName());
        if (toReturn == 0) {
            toReturn = l.getAlias().compareTo(r.getAlias());
        }

        // All Done.
        return toReturn;
    };

    /**
     * NamedQuery for getting Memberships by alias and organisation name.
     * Found Memberships are retrieved irrespective of their LoginPermitted flag.
     */
    public static final String NAMEDQ_GET_BY_ALIAS_ORGANISATION =
            "Membership.getByAliasAndOrganisation";

    /**
     * NamedQuery for getting Memberships by name and organisation name.
     */
    public static final String NAMEDQ_GET_BY_NAME_ORGANISATION =
            "Membership.getByNameAndOrganisation";

    /**
     * NamedQuery for getting Memberships by group name, organisation name and loginPermitted.
     */
    public static final String NAMEDQ_GET_BY_GROUP_ORGANISATION_LOGINPERMITTED =
            "Membership.getByGroupOrganisationAndLoginPermitted";

    /**
     * NamedQuery for getting Memberships by organisation name and loginPermitted.
     */
    public static final String NAMEDQ_GET_BY_ORGANISATION_ID_LOGINPERMITTED =
            "Membership.getByOrganisationAndLoginPermitted";

    /**
     * NamedQuery for getting a single Membership by organisationID, loginPermitted and UserID.
     */
    public static final String NAMEDQ_GET_BY_ORGANISATION_ID_LOGINPERMITTED_AND_USERID =
            "Membership.getByOrganisationIdLoginPermittedAndUserId";


    /**
     * The alias of this Membership. Never null/empty, and unique within the Organisation.
     */
    @Basic(optional = false)
    @Column(nullable = false, length = 64)
    @XmlElement(required = true)
    private String alias;

    /**
     * An optional (i.e. nullable) sub-alias for the Membership within the Organisation.
     */
    @Basic
    @Column(length = 1024)
    @XmlElement
    private String subAlias;

    /**
     * An optional (i.e. nullable) email for the Membership within the Organisation.
     * Should not include the domain name, since that is derived from the organisation.
     */
    @Basic
    @Column(length = 64)
    @XmlElement
    private String emailAlias;

    /**
     * If true, this Membership is permitted login.
     */
    // TODO: Change to map --> Year + (last permissible login date)
    @Basic
    @Column(nullable = false)
    @XmlAttribute(required = true)
    private boolean loginPermitted;

    /**
     * The non-null User of this Membership.
     */
    @ManyToOne(optional = false, cascade = CascadeType.DETACH)
    @XmlIDREF
    private User user;

    /**
     * The non-null Organisation wherein this Membership is part.
     */
    @ManyToOne(optional = false, cascade = {CascadeType.MERGE, CascadeType.DETACH})
    @XmlIDREF
    private Organisation organisation;

    /**
     * Set of {@link GroupMembership} held by this {@link Membership}.
     */
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "membership")
    @XmlElementWrapper(name = "memberships")
    @XmlElements(value = {
            @XmlElement(name = "groupMembership", type = GroupMembership.class),
            @XmlElement(name = "guildMembership", type = GuildMembership.class)
    })
    private Set<GroupMembership> groupMemberships;

    /**
     * Set of {@link OrderLevelGrant} instances held by this {@link Membership}.
     */
    @OneToMany(mappedBy = "membership", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @XmlElementWrapper(name = "orderLevelGrants", nillable = true, required = false)
    @XmlElement(name = "orderLevelGrant")
    private Set<OrderLevelGrant> orderLevelGrants;

    /**
     * A Map relating personal settings, such as contact information.
     */
    @ElementCollection
    @MapKeyColumn(name = "settings_key")
    @Column(name = "settings_value")
    @CollectionTable(name = "membership_personalsettings",
            joinColumns = @JoinColumn(name = "membership_id"),
            uniqueConstraints = {
                    @UniqueConstraint(
                            name = "unq_settingstype_per_membership",
                            columnNames = {"settings_key", "membership_id"})})
    @XmlElementWrapper
    @XmlElement(name = "setting")
    private Map<String, String> personalSettings;

    /**
     * JPA/JAXB-friendly constructor.
     */
    public Membership() {

        // Create internal state
        this.loginPermitted = true;
        this.orderLevelGrants = new TreeSet<>();
        this.groupMemberships = new TreeSet<>();
        this.personalSettings = new TreeMap<>();
    }

    /**
     * Convenience constructor creating a Membership with empty collections for GroupMembership and OrderLevelGrant.
     *
     * @param alias          The alias of this Membership.
     * @param subAlias       The sub-alias of this Membership.
     * @param emailAlias     The emailAlias of this alias.
     * @param loginPermitted Set to <code>true</code> to indicate that
     *                       login is permitted, and false otherwise.
     * @param user           The user of this Membership.
     * @param organisation   The organisation of this Membership, i.e. where the user belongs.
     */
    public Membership(final String alias,
                      final String subAlias,
                      final String emailAlias,
                      final boolean loginPermitted,
                      final User user,
                      final Organisation organisation) {

        this(alias,
                subAlias,
                emailAlias,
                loginPermitted,
                user,
                organisation,
                new TreeSet<>(),
                new TreeSet<>());
    }

    /**
     * Compound constructor.
     *
     * @param alias            The alias of this Membership.
     * @param subAlias         The sub-alias of this Membership.
     * @param emailAlias       The emailAlias of this alias.
     * @param loginPermitted   Set to <code>true</code> to indicate that
     *                         login is permitted, and false otherwise.
     * @param user             The user of this Membership.
     * @param organisation     The organisation of this Membership, i.e. where the user belongs.
     * @param orderLevelGrants The OrderLevels granted to this Membership.
     * @param groupMemberships The groupMemberships within the Organisation to which the
     *                         user of this membership belongs.
     */
    public Membership(final String alias,
                      final String subAlias,
                      final String emailAlias,
                      final boolean loginPermitted,
                      final User user,
                      final Organisation organisation,
                      final Set<OrderLevelGrant> orderLevelGrants,
                      final Set<GroupMembership> groupMemberships) {

        this();

        // Add internal state
        this.alias = alias;
        this.subAlias = subAlias;
        this.emailAlias = emailAlias;
        this.loginPermitted = loginPermitted;
        this.user = user;
        this.organisation = organisation;

        // Add any supplied collection elements to our internal state
        if (orderLevelGrants != null) {
            this.orderLevelGrants.addAll(orderLevelGrants);
        }
        if (groupMemberships != null) {
            this.groupMemberships.addAll(groupMemberships);
        }
    }

    /**
     * Retrieves the alias of this Membership. Aliases must be unique within an Organisation.
     *
     * @return The alias of this Membership.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @return The optional sub-alias of this Membership.
     */
    public String getSubAlias() {
        return subAlias;
    }

    /**
     * Retrieves the email alias for this Membership. The email aliases are normally synthesized on
     * the form {@code alias@organisationEmailSuffix}. However, if special characters such as dashes. apostrophes
     * etc. are present within the alias, this email alias can be a modified version of the alias.
     *
     * @return The email alias for this Membership, exclusive of the email suffix.
     * (I.e. everything before the {@code @} sign in an email address).
     */
    public String getEmailAlias() {
        return emailAlias;
    }

    /**
     * Assigns the email alias for this Membership. The email alias should contain everything before the {@code @}
     * sign in an email address
     *
     * @param emailAlias Assigns the email alias of this Membership. (I.e. everything before the {@code @} sign
     *                   in an email address).
     */
    public void setEmailAlias(final String emailAlias) {

        // Check sanity
        Validate.notEmpty(emailAlias, "emailAlias");

        // Assign internal state
        this.emailAlias = emailAlias;
    }

    /**
     * @return true if this Membership permits login, and false otherwise.
     */
    public boolean isLoginPermitted() {
        return loginPermitted;
    }

    /**
     * Assigns the loginPermitted flag.
     *
     * @param loginPermitted true if this Membership permits login, and false otherwise.
     */
    public void setLoginPermitted(final boolean loginPermitted) {
        this.loginPermitted = loginPermitted;
    }

    /**
     * @return The User of this Membership.
     */
    public User getUser() {
        return user;
    }

    /**
     * Assigns the User of this Membership.
     *
     * @param user The User of this Membership.
     */
    public void setUser(final User user) {

        // Check sanity
        Validate.notNull(user, "user");

        // Assign internal state
        this.user = user;
    }

    /**
     * The Organisation of this Membership.
     *
     * @return The Member of this Membership.
     */
    public Organisation getOrganisation() {
        return organisation;
    }

    /**
     * Assigns the Organisation of this Membership.
     *
     * @param organisation The Organisation of this Membership.
     */
    public void setOrganisation(final Organisation organisation) {

        // Check sanity
        Validate.notNull(organisation, "organisation");

        // Assign internal state
        this.organisation = organisation;
    }

    /**
     * Links the Membership to his/her grants of OrderLevels.
     *
     * @return The OrderLevelGrants of this Membership.
     */
    public Set<OrderLevelGrant> getOrderLevelGrants() {
        return orderLevelGrants;
    }

    /**
     * All GroupMemberships held by this Membership, implying all Groups and Guilds in which this Membership is a part.
     *
     * @return The GroupMemberships held by this Membership, implying all Groups and Guilds in which
     * this Membership is a part.
     */
    public Set<GroupMembership> getGroupMemberships() {
        return groupMemberships;
    }

    /**
     * Assigns the Group (or Guild) memberships of this Membership, replacing existing memberships.
     *
     * @param groupMemberships The groupMemberships relation of this Membership.
     */
    public void setGroupMemberships(final SortedSet<GroupMembership> groupMemberships) {

        // Check sanity
        Validate.notNull(groupMemberships, "groupMemberships");

        // Assign the relation to our internal state
        this.groupMemberships.clear();
        this.groupMemberships.addAll(groupMemberships);
    }

    /**
     * Adds or retrieves a GroupMembership wrapping the supplied data.
     * <strong>Note!</strong> This method cannot create or update {@code GuildMemerships}.
     * Use the {@code #addOrUpdateGuildMembership} method for that.
     *
     * @param group The Group in which this Membership should be a GroupMember.
     * @return The newly created or updated GroupMembership.
     * @see #addOrUpdateGuildMembership(Guild, boolean, boolean, boolean)
     */
    public GroupMembership addOrGetGroupMembership(final Group group) {

        // Check sanity
        Validate.notNull(group, "group");
        Validate.isTrue(!(group instanceof Guild), "Cannot handle Guild groups. (Got type: "
                + group.getClass().getName() + ")");

        // Do we need to define groupMemberships?
        if (groupMemberships == null) {
            groupMemberships = new TreeSet<>();
        }

        // Do we already have a Group membership to the supplied Group?
        for (GroupMembership current : groupMemberships) {
            if (current.getGroup().equals(group)) {
                return current;
            }
        }

        // The GroupMembership was not found.
        final GroupMembership toReturn = new GroupMembership(group, this);
        groupMemberships.add(toReturn);
        return toReturn;
    }

    /**
     * Adds or updates a GuildMembership wrapping the supplied data.
     * <strong>Note!</strong> This method should not be used to acquire or update GroupMemberships.
     * Use the {@code #addOrGetGroupMembership} method for that.
     *
     * @param guild             The Guild in which this Membership should be a GuildMember.
     * @param guildMaster       {@code true} if this Membership is a guild master for the given Guild.
     * @param deputyGuildMaster {@code true} if this Membership is a deputy guild master for the given Guild.
     * @param auditor           {@code true} if this Membership is an auditor for the given Guild.
     * @return The newly created or updated GuildMembership.
     * @see #addOrGetGroupMembership(Group)
     */
    public GuildMembership addOrUpdateGuildMembership(final Guild guild,
                                                      final boolean guildMaster,
                                                      final boolean deputyGuildMaster,
                                                      final boolean auditor) {

        // Check sanity
        Validate.notNull(guild, "guild");

        // Update existing GuildMembership?
        for (GroupMembership current : groupMemberships) {

            if (current instanceof GuildMembership) {
                final GuildMembership currentGuildMembership = (GuildMembership) current;

                if (guild.equals(currentGuildMembership.getGuild())) {

                    // Update this GuildMembership
                    currentGuildMembership.setGuildMaster(guildMaster);
                    currentGuildMembership.setDeputyGuildMaster(deputyGuildMaster);
                    currentGuildMembership.setAuditor(auditor);

                    // All Done.
                    return currentGuildMembership;
                }
            }
        }

        // This Membership did not have a GuildMembership with the given Guild.
        final GuildMembership toReturn = new GuildMembership(guild, this, guildMaster, deputyGuildMaster, auditor);
        this.groupMemberships.add(toReturn);
        return toReturn;
    }

    /**
     * Retrieves the personal settings for this {@link Membership}.
     *
     * @return the non-null personal settings Map for this {@link Membership}.
     */
    public Map<String, String> getPersonalSettings() {
        return personalSettings;
    }

    /**
     * Adds or updates an OrderLevelGrant wrapping the supplied data.
     *
     * @param orderLevel  The OrderLevel which should be granted to this Membership.
     * @param grantedDate The date when the OrderLevel was granted.
     * @param note        An optional note for the OrderLevelGrant.
     * @return The added or updated OrderLevelGrant.
     */
    public OrderLevelGrant addOrUpdateOrderLevelGrant(final OrderLevel orderLevel,
                                                      final ZonedDateTime grantedDate,
                                                      final String note) {

        // Check sanity
        Validate.notNull(orderLevel, "orderLevel");

        // Update existing OrderLevelGrants?
        for (OrderLevelGrant current : orderLevelGrants) {

            OrderLevel currentOrderLevel = current.getOrderLevel();
            if (currentOrderLevel.equals(orderLevel)) {

                // Update this OrderLevelGrant
                current.setDateGranted(grantedDate);
                current.setNote(note);

                // All done.
                return current;
            }
        }

        // Create a new OrderLevelGrant, and add it to this Membership's internal state.
        final OrderLevelGrant toReturn = new OrderLevelGrant(orderLevel, this, grantedDate, note);
        orderLevelGrants.add(toReturn);
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Membership that) {

        // Fail fast
        if (that == null) {
            return -1;
        } else if (that == this) {
            return 0;
        }

        // Delegate.
        return ALIAS_AND_ORG_COMPARATOR.compare(this, that);
    }

    /**
     * The equals and hashCode methods only considers the Member::login and Organisation::organisationName fields,
     * as that combination should be unique.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    public boolean equals(final Object o) {

        // Check sanity
        if (this == o) {
            return true;
        }
        if (!(o instanceof Membership)) {
            return false;
        }

        // Delegate
        final Membership that = (Membership) o;
        if (!(user.hashCode() == that.user.hashCode())) {
            return false;
        }
        if (!organisation.getOrganisationName().equals(that.organisation.getOrganisationName())) {
            return false;
        }

        // All Done.
        return true;
    }

    /**
     * The equals and hashCode methods only considers User and Organisation::organisationName fields,
     * as that combination should be unique.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        return user.hashCode() + organisation.getOrganisationName().hashCode();
    }

    /**
     * @return The user login and organisationName of this Membership.
     */
    @Override
    public String toString() {
        final String userIdAndName = user == null
                ? "<No User Set>"
                : user.getId() + "_" + user.getFirstName() + "_" + user.getLastName();
        final String organisationString = getOrganisation() == null
                ? "<Organisation Not Yet Set>"
                : getOrganisation().getOrganisationName();
        return "Membership [" + this.getAlias() + " -> " + organisationString + " (" + userIdAndName + ")]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<SemanticAuthorizationPath> getPaths() {

        final SortedSet<SemanticAuthorizationPath> toReturn = new TreeSet<>();

        // #1) Add the membership within this Organisation
        toReturn.add(new AuthorizationPath(getOrganisation().getOrganisationName(),
                "member",
                SemanticAuthorizationPath.NO_VALUE));

        // #2) Add the authorization path 
        if (groupMemberships != null) {
            groupMemberships.forEach(current -> toReturn.addAll(current.getPaths()));
        }
        if (orderLevelGrants != null) {
            orderLevelGrants.forEach(current -> toReturn.addAll(current.getPaths()));
        }

        // All done.
        return toReturn;
    }

    /**
     * JAXB callback method invoked after this instance is Unmarshalled.
     * This is the gracious JAXB instantiation sledge hammer...
     *
     * @param unmarshaller The unmarshaller used to perform the unmarshalling.
     */
    @SuppressWarnings("PMD")
    private void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {

        // Re-assign the XmlTransient collections.
        orderLevelGrants.stream().filter(Objects::nonNull).forEach(c -> c.setMembership(this));
        groupMemberships.stream().filter(Objects::nonNull).forEach(c -> c.setMembership(this));

        if (log.isDebugEnabled()) {
            if (parent instanceof User) {
                log.debug("Got parent user: " + parent.toString());
            } else {
                final String parentObjectType = parent == null ? "<null>" : parent.getClass().getName();
                log.debug("Got parent object of type: " + parentObjectType);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        // Ensure that the reference is fully loaded.
        // Workaround for the https://hibernate.atlassian.net/browse/HHH-8839 bug.
        for (GroupMembership current : getGroupMemberships()) {
            current.getGroup().getGroupName();
        }

        InternalStateValidationException.create()
                .notNull(user, "user")
                .notNull(organisation, "organisation")
                .notNull(orderLevelGrants, "orderLevelGrants")
                .notNull(groupMemberships, "groupMemberships")
                .endExpressionAndValidate();
    }
}
