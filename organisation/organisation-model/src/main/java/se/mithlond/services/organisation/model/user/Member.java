package se.mithlond.services.organisation.model.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.MapKeyColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDate;
import java.util.GregorianCalendar;
import java.util.Map;

/**
 * Entity implementation for Members.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = "Member.getByOrganisationName",
                query = "select a from Member a, in(a.memberships) ships "
                        + "where ships.organisation.organisationName = :organisationName order by a.alias"),
        @NamedQuery(name = "Member.getMembersByLowercaseAliasAndOrganisation",
                query = "select a from Member a, in(a.memberships) ships "
                        + "where lower(a.alias) like :lcAlias and ships.organisation.organisationName = :orgName "
                        + "order by a.alias")
        /*,
        @NamedQuery(name = "getMembersByGroupAndOrganisation",
                query = "select a from Member a, in(a.memberships) ships, in(ships.groups) grps "
                        + "where grps.groupName like ?1 and ships.organisation.organisationName = ?2 order by a.alias"),
        @NamedQuery(name = "getMembersByGuildAndOrganisation",
                query = "select a from Member a, in(a.memberships) ships, in(ships.guildMemberships) guildmemberships "
                        + "where guildmemberships.guild.guildName like ?1 and ships.organisation.organisationName = ?2 "
                        + "order by a.alias") */
})
@Entity
@XmlType(propOrder = {"alias", "subalias", "firstName", "lastName", "birthday",
                "personalNumberLast4Digits",  "homeAddress", "memberships", "contactDetails"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Member extends User {

    // Our Logger
    @XmlTransient
    private static final Logger log = LoggerFactory.getLogger(Member.class);

    // Constants
    private static final long serialVersionUID = 8829990019L;

    // Internal state
    // TODO: Move to Membership
    @Basic(optional = false)
    @Column(nullable = false, length = 64)
    @XmlElement(required = true, nillable = false)
    private String alias;

    @Basic(optional = true)
    @Column(nullable = true, length = 1024)
    @XmlElement(nillable = true, required = false)
    private String subalias;

    @Basic(optional = false)
    @Column(nullable = false, length = 4)
    @XmlAttribute(required = true)
    private short personalNumberLast4Digits;

    @Embedded
    @XmlElement(required = true, nillable = false)
    private Address homeAddress;

    @XmlElementWrapper(name = "memberships", nillable = false, required = true)
    @XmlElement(name = "membership")
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Membership> memberships = new ArrayList<Membership>();

    //
    // Quoting the EclipseLink documentation:
    //
    // "The type of mapping used is always determined by the value of the Map, not the key.
    // So if the key is a Basic but the value is an Entity a OneToMany mapping is still used.
    // But if the value is a Basic but the key is an Entity a ElementCollection mapping is used."
    //
    // Thus ...
    //
    // a) Mapping of basic/embedded keys to basic/embedded values:
    //    Use @ElementCollection and @MapKeyColumn annotations
    //
    // b) Mapping of basic/embedded keys to entity values:
    //    Use @ElementCollection and @MapKeyJoinColumn annotations
    //
    // c) Mapping of entity keys to entity values:
    //    Use @OneToMany or @ManyToMany and @MapKeyJoinColumn annotations
    //
    // d) Mapping of entity keys to basic/embedded values:
    //    Use @ElementCollection and @JoinTable + @MapKeyJoinColumn
    //
    // MapKeyColumn used for basic types of Map keys
    // MapKeyJoinColumn used for entity types of Map keys
    //
    // If generics are not used in the Map declaration,
    // use @MapKeyClass to define the key type, and
    // targetClass property of @ElementCollection to
    // define value type.
    //
    @Column(name = "address_or_number")
    @MapKeyColumn(name = "contact_type")
    @ElementCollection
    @CollectionTable(name = "member_contactdetails")
    private Map<String, String> contactDetails;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public Member() {
    }

    /**
     * Compound constructor, creating a Member wrapping the supplied data.
     *
     * @param alias                     The Alias of this Member (i.e. "Varg").
     * @param subalias                  The subalias of this Member (i.s. "den förskräcklige")
     * @param firstName                 The first name of this Member.
     * @param lastName                  The last name of this Member.
     * @param birthday                  The birthday of this Member.
     * @param personalNumberLast4Digits The last 4 digits in the Member's personal number.
     * @param homeAddress               The home address of this Member.
     * @param contactDetails            The contact details of this member.
     */
    public Member(final String alias,
                  final String subalias,
                  final String firstName,
                  final String lastName,
                  final LocalDate birthday,
                  final short personalNumberLast4Digits,
                  final Address homeAddress,
                  final Map<String, String> contactDetails) {

        // Check sanity
        Validate.notNull(birthday, "birthday");

        // Assign internal state
        this.alias = alias;
        this.subalias = subalias;
        this.firstName = firstName;
        this.lastName = lastName;
        this.personalNumberLast4Digits = personalNumberLast4Digits;
        this.homeAddress = homeAddress;
        this.contactDetails = contactDetails;

        // Convert the birthday to a Calendar
        this.birthday = GregorianCalendar.from(birthday.atStartOfDay(TimeFormat.SWEDISH_TIMEZONE));
    }

    /**
     * @return The 4 last digits of the personal number.
     */
    public short getPersonalNumberLast4Digits() {
        return personalNumberLast4Digits;
    }

    /**
     * Assigns the 4 last digits of the personal number.
     *
     * @param personalNumberLast4Digits the 4 last digits of the personal number.
     */
    public void setPersonalNumberLast4Digits(final short personalNumberLast4Digits) {
        this.personalNumberLast4Digits = personalNumberLast4Digits;
    }

    /**
     * Only main alias, such as "Theoden" or "Roac".
     *
     * @return The alias of this Member
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Augmentation to the alias, such as "av Dunedain" eller "vindherren".
     *
     * @return The sub-alias of this Member, or an empty string if no sub-alias is present.
     */
    public String getSubalias() {
        return this.subalias == null ? "" : subalias;
    }

    /**
     * The first name of this Member.
     *
     * @return The firstName of this Member entity
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * The last name of this Member.
     *
     * @return The firstName of this Member entity
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return The birthday of this Member.
     */
    public LocalDate getBirthday() {
        return birthday.toZonedDateTime().toLocalDate();
    }

    /**
     * @return The Memberships of this Member.
     */
    public List<Membership> getMemberships() {
        return memberships;
    }

    /**
     * Assigns the Memberships of this Member.
     *
     * @param memberships the Memberships of this Member.
     */
    public void setMemberships(final List<Membership> memberships) {

        // Check sanity
        Validate.notNull(memberships, "Cannot handle null memberships argument.");

        // Handle reverse relationship
        this.memberships = memberships;
        for (Membership current : memberships) {
            current.setMember(this);
        }
    }

    /**
     * @return The HomeAddress relation of this Member.
     */
    public Address getHomeAddress() {
        return homeAddress;
    }

    /**
     * Assigns the homeAddress of this Member.
     *
     * @param homeAddress The homeAddress relation of this Member
     */
    public void setHomeAddress(final Address homeAddress) {
        // Assign the relation to our internal state
        this.homeAddress = homeAddress;
    }

    /**
     * @return The known contact details of this member.
     */
    public Map<String, String> getContactDetails() {
        return contactDetails;
    }

    /**
     * The Equals method - just like the hashCode method - only considers the
     * login field, as logins are unique to each member.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object that) {

        // Check sanity
        if (this == that) {
            return true;
        }
        if (!(that instanceof Member)) {
            return false;
        }

        // Delegate and return.
        final Member thatMember = (Member) that;
        boolean toReturn = this.getAlias().equals(thatMember.getAlias())
                && this.getFirstName().equals(thatMember.getFirstName())
                && this.getLastName().equals(thatMember.getLastName())
                && this.getSubalias().equals(thatMember.getSubalias())
                && this.getHomeAddress().equals(thatMember.getHomeAddress());

        if(toReturn) {
            final String thisBirthday = TimeFormat.YEAR_MONTH_DATE.print(this.getBirthday());
            final String thatBirthday = TimeFormat.YEAR_MONTH_DATE.print(thatMember.getBirthday());
            toReturn = thisBirthday.equals(thatBirthday);
        }

        // All done.
        return toReturn;
    }

    /**
     * The hashCode implementation simply uses the "login"
     * field, which is required to be unique for all Members.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return login.hashCode();
    }

    /**
     * Creates a debug printout of this Member.
     *
     * @return a debug printout of this Member.
     */
    @Override
    public String toString() {

        final StringBuilder contactDetailsBuffer = new StringBuilder();

        // Append all contact details.
        final Map<String, String> details = getContactDetails();
        if (details != null && details.size() > 0) {
            for (String current : details.keySet()) {
                contactDetailsBuffer.append(current).append(": ").append(details.get(current)).append("\n");
            }
        }

        return "Member: " + getAlias() + " (" + getFirstName() + " - " + getLastName() + ")\n"
                + "Contact Details: " + contactDetailsBuffer + "\n"
                + "Home Address: " + getHomeAddress().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        // Ensure that the ElementCollection is fully loaded.
        // Workaround for the https://hibernate.atlassian.net/browse/HHH-8839 bug.
        if (contactDetails != null) {

            if (log.isDebugEnabled()) {
                log.debug("Found [" + contactDetails.size() + "] contact details for Member [" + getLogin() + "]");
            }

            for (Map.Entry<String, String> current : contactDetails.entrySet()) {

                final String currentKey = current.getKey();
                final String currentValue = current.getValue();

                if (log.isDebugEnabled()) {
                    log.debug(" Member [" + getAlias() + "] contactDetails [" + currentKey + "]: " + currentValue);
                }
            }
        }

        final int numMemberships = memberships.size();
        if (log.isDebugEnabled()) {
            log.debug("Member [" + getAlias() + "] has [" + numMemberships + "] memberships.");
        }

        // Validate internal state
        InternalStateValidationException.create()
                .notNullOrEmpty(alias, "alias")
                .notNullOrEmpty(firstName, "firstName")
                .notNullOrEmpty(lastName, "lastName")
                .notNull(birthday, "birthday")
                .notNull(homeAddress, "homeAddress")
                .notNull(memberships, "memberships")
                .notNull(contactDetails, "contactDetails")
                .endExpressionAndValidate();
    }
}