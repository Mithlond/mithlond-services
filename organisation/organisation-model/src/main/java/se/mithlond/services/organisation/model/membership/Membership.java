package se.mithlond.services.organisation.model.membership;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
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
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

/**
 * Realization of a Membership, associating a Member with an Organisation.
 * Holds things like login permitted for the member within the Organisation,
 * and the Organisation's email alias for the member.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = "getAllMemberships", query = "select a from Membership a order by a.id"),
        @NamedQuery(name = "getMembershipByLoginAndOrganisation",
                query = "select a from Membership a "
                        + "where a.member.login = ?1 and "
                        + "a.organisation.organisationName = ?2 order by a.id"),
        @NamedQuery(name = "getMembershipsByGroupAndOrganisation",
                query = "select a from Membership a, in(a.groups) groups "
                        + "where groups.groupName = ?1 and a.organisation.organisationName = ?2 "
                        + "order by a.member.login"),
        @NamedQuery(name = "getMembershipsByGuildAndOrganisation",
                query = "select a from Membership a, in(a.guildMemberships) guilds "
                        + "where guilds.guild.guildName = ?1 and a.organisation.organisationName = ?2 "
                        + "order by a.member.login"),
        @NamedQuery(name = "getMembershipsByLogin",
                query = "select a from Membership a where a.member.login = ?1 "
                        + "order by a.organisation.organisationName, a.member.login"),
        @NamedQuery(name = "getMembershipsByOrganisationAndLoginPermitted",
                query = "select a from Membership a where a.organisation.organisationName = ?1 "
                        + "and a.loginPermitted = ?2 order by a.member.alias")
})
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "memberAndOrganisationIsUnique",
        columnNames = {"member_id", "organisation_id"})})
@XmlType(propOrder = {"emailAlias", "loginPermitted", "member",
        "guildMemberships", "groups", "orderLevelGrants", "organisation"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Membership extends NazgulEntity implements Comparable<Membership> {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(Membership.class);

    // Constants
    private static final long serialVersionUID = 8829990028L;

    // Internal state
    @Basic(optional = true) @Column(nullable = true, length = 64)
    @XmlElement(required = false, nillable = true)
    private String emailAlias;

    @Basic @Column(nullable = false)
    @XmlAttribute(required = true)
    private boolean loginPermitted = true;

    @ManyToOne(optional = false, cascade = CascadeType.DETACH)
    @XmlIDREF
    private Member member;

    @ManyToOne(optional = false, cascade = {CascadeType.MERGE, CascadeType.DETACH})
    @XmlIDREF
    private Organisation organisation;

    @OneToMany(mappedBy = "membership", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @XmlElementWrapper(name = "guildMemberships", nillable = true, required = false)
    @XmlElement(name = "guildMembership")
    private Set<GuildMembership> guildMemberships = new HashSet<GuildMembership>();

    @OneToMany(mappedBy = "membership", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @XmlElementWrapper(name = "orderLevelGrants", nillable = true, required = false)
    @XmlElement(name = "orderLevelGrant")
    private Set<OrderLevelGrant> orderLevelGrants = new TreeSet<OrderLevelGrant>();

    @XmlIDREF
    @XmlElementWrapper(name = "groups", nillable = true, required = false)
    @XmlElement(name = "group")
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
    @JoinTable(name = "membership_groups",
            joinColumns = @JoinColumn(name = "membership_id", referencedColumnName = "id"),
            inverseJoinColumns = @JoinColumn(name = "group_id", referencedColumnName = "id"))
    private Set<Group> groups = new HashSet<Group>();

    /**
     * JPA/JAXB-friendly constructor.
     */
    public Membership() {
    }

    /**
     * Compound constructor.
     *
     * @param emailAlias       The emailAlias of this alias.
     * @param loginPermitted   Set to <code>true</code> to indicate that
     *                         login is permitted, and false otherwise.
     * @param member           The member of this Membership.
     * @param organisation     The organisation of this Membership, i.e. where the member belongs.
     * @param orderLevelGrants The OrderLevels granted to this Membership.
     * @param groups           The groups within the Organisation to which the
     *                         member of this membership belongs.
     */
    public Membership(final String emailAlias,
                      final boolean loginPermitted,
                      final Member member,
                      final Organisation organisation,
                      final Set<OrderLevelGrant> orderLevelGrants,
                      final Set<Group> groups) {

        this.emailAlias = emailAlias;
        this.loginPermitted = loginPermitted;
        this.member = member;
        this.organisation = organisation;
        this.orderLevelGrants = orderLevelGrants;
        this.groups = groups;
    }

    /**
     * @return The email alias for this Membership, normally being
     * on the form [login@organisation.se] or [login@organisation.org].
     */
    public String getEmailAlias() {
        return emailAlias;
    }

    /**
     * Assigns the email alias for this Membership, normally being
     * on the form [login@organisation.se] or [login@organisation.org].
     *
     * @param emailAlias The email alias for this Membership, normally being
     *                   on the form [login@organisation.se] or
     *                   [login@organisation.org].
     */
    public void setEmailAlias(final String emailAlias) {
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
     * @return The Member of this Membership.
     */
    public Member getMember() {
        return member;
    }

    /**
     * Assigns the Member of this Membership.
     *
     * @param member The Member of this Membership.
     */
    public void setMember(final Member member) {
        this.member = member;
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
     * All groups to which this member belongs.
     *
     * @return The Groups relation of this Member
     */
    public Set<Group> getGroups() {
        return groups;
    }

    /**
     * Assigns the groups of this Member.
     * All groups to which this member belongs.
     *
     * @param groups The groups relation of this Member
     */
    public void setGroups(final Set<Group> groups) {
        // Assign the relation to our internal state
        this.groups = groups;
    }

    /**
     * @return The GuildMemberships of this Membership, implying a listing of all Guilds to
     * which this Membership belongs.
     */
    public Set<GuildMembership> getGuildMemberships() {
        return guildMemberships;
    }

    /**
     * Adds or updates a GuildMembership wrapping the supplied data.
     *
     * @param guild             The Guild in which this Membership should be a GuildMember.
     * @param guildMaster       {@code true} if this Membership is a guild master for the given Guild.
     * @param deputyGuildMaster {@code true} if this Membership is a deputy guild master for the given Guild.
     * @param auditor           {@code true} if this Membership is an auditor for the given Guild.
     * @return The newly created or updated GuildMembership.
     */
    public GuildMembership addOrUpdateGuildMembership(final Guild guild,
                                                      final boolean guildMaster,
                                                      final boolean deputyGuildMaster,
                                                      final boolean auditor) {

        // Check sanity
        Validate.notNull(guild, "Cannot handle null guild argument.");

        // Update existing GuildMembership?
        for (GuildMembership current : guildMemberships) {

            Guild currentGuild = current.getGuild();
            if (currentGuild.equals(guild)) {

                // Update this GuildMembership
                current.setGuildMaster(guildMaster);
                current.setDeputyGuildMaster(deputyGuildMaster);
                current.setAuditor(auditor);

                // All done.
                return current;
            }
        }

        // Create a new GuildMembership
        final GuildMembership toReturn = new GuildMembership(guild, this, guildMaster, deputyGuildMaster, auditor);
        guildMemberships.add(toReturn);
        return toReturn;
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
                                                      final DateTime grantedDate,
                                                      final String note) {

        // Check sanity
        Validate.notNull(orderLevel, "Cannot handle null orderLevel argument.");

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
        return emailAlias.compareTo(that.emailAlias);
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
        if (!member.getLogin().equals(that.member.getLogin())) {
            return false;
        }
        if (!organisation.getOrganisationName().equals(that.organisation.getOrganisationName())) {
            return false;
        }

        // All Done.
        return true;
    }

    /**
     * The equals and hashCode methods only considers the Member::login and Organisation::organisationName fields,
     * as that combination should be unique.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("all")
    public int hashCode() {
        return member.getLogin().hashCode()
                + organisation.getOrganisationName().hashCode();
    }

    /**
     * @return The member login and organisationName of this Membership.
     */
    @Override
    public String toString() {
        return "Membership [" + getMember().getLogin() + " -> " + getOrganisation().getOrganisationName() + "]";
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
        for (GuildMembership current : guildMemberships) {
            current.setMembership(this);
        }
        for (OrderLevelGrant current : orderLevelGrants) {
            current.setMembership(this);
        }

        if (log.isDebugEnabled()) {
            final String parentObjectType = parent == null ? "<null>" : parent.getClass().getName();
            log.debug("Got parent object of type: " + parentObjectType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        // Ensure that the reference is fully loaded.
        // Workaround for the https://hibernate.atlassian.net/browse/HHH-8839 bug.
        for (Group current : getGroups()) {
            current.getGroupName();
        }

        InternalStateValidationException.create()
                .notNull(member, "member")
                .notNull(organisation, "organisation")
                .notNull(guildMemberships, "guildMemberships")
                .notNull(orderLevelGrants, "orderLevelGrants")
                .notNull(groups, "groups")
                .endExpressionAndValidate();
    }
}
