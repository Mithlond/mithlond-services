package se.mithlond.services.organisation.model.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Basic;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.MapKeyColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

/**
 * <p>Entity storing Users of Mithlond services. All calls to Mithlond
 * services should be performed by a known User.</p>
 *
 * <p><strong>Note!</strong> No credential information is stored within the User class.
 * Instead, such types of configuration should be managed by an IDM system assumed to handle authentication.
 * The User/Membership/Group/Guild/Organisation structure within the Mithlond service model handles internal
 * Authorization, as these entities better describe the privilege hierarchy of content.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MappedSuperclass
@XmlType(propOrder = {"firstName", "lastName", "birthday", "personalNumberLast4Digits", "homeAddress"})
@XmlAccessorType(XmlAccessType.FIELD)
public class User extends NazgulEntity {

    // Our Logger
    @XmlTransient
    private static final Logger log = LoggerFactory.getLogger(User.class);

    // Constants
    private static final long serialVersionUID = 8829990119L;

    // Internal state
    @Basic(optional = false)
    @Column(nullable = false, length = 64)
    @XmlElement(required = true, nillable = false)
    private String firstName;

    @Basic(optional = false)
    @Column(nullable = false, length = 64)
    @XmlElement(required = true, nillable = false)
    private String lastName;

    @Basic(optional = false)
    @Temporal(value = TemporalType.DATE)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private Calendar birthday;

    @Basic(optional = false)
    @Column(nullable = false, length = 4)
    @XmlAttribute(required = true)
    private short personalNumberLast4Digits;

    @Embedded
    @XmlElement(required = true, nillable = false)
    private Address homeAddress;

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
    @CollectionTable(name = "user_contactdetails")
    private Map<String, String> contactDetails;

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
     * The first name of this AbstractUser.
     *
     * @return The firstName of this AbstractUser entity
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * The last name of this AbstractUser.
     *
     * @return The firstName of this AbstractUser entity
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @return The birthday of this AbstractUser.
     */
    public LocalDate getBirthday() {
        return ((GregorianCalendar) birthday).toZonedDateTime().toLocalDate();
    }

    /**
     * @return The Memberships of this AbstractUser.
     */
    public List<Membership> getMemberships() {
        return memberships;
    }

    /**
     * Assigns the Memberships of this Member.
     *
     * @param memberships the Memberships of this AbstractUser.
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

        // Check sanity
        Validate.notNull(homeAddress, "homeAddress");

        // Assign internal state
        this.homeAddress = homeAddress;
    }

    /**
     * @return The known contact details of this member.
     */
    public Map<String, String> getContactDetails() {
        return contactDetails;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {

        // Check sanity
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }

        // Delegate and return.
        final User that = (User) o;
        boolean toReturn = this.getFirstName().equals(that.getFirstName())
                && this.getLastName().equals(that.getLastName())
                && this.getHomeAddress().equals(that.getHomeAddress());

        if (toReturn) {
            final String thisBirthday = TimeFormat.YEAR_MONTH_DATE.print(this.getBirthday());
            final String thatBirthday = TimeFormat.YEAR_MONTH_DATE.print(that.getBirthday());
            toReturn = thisBirthday.equals(thatBirthday);
        }

        // All done.
        return toReturn;
    }

    /**
     * <p>The User class only considers names (first and last), birthday and homeAddress for hashCode.
     * This data set should prove unique enough for hash sorting.</p>
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        return firstName.hashCode()
                + lastName.hashCode()
                + birthday.hashCode()
                + homeAddress.hashCode();
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

        // All done.
        return "User [" + getId() + ": " + getFirstName() + " " + getLastName() + "\n"
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

            final String nameAndId = getId() + " [" +

            if (log.isDebugEnabled()) {
                log.debug("Found [" + contactDetails.size() + "] contact details for [" + getId() + " ("]");
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

    private String getIdAndName() {
        return "[" + getId() + "]: " + getFirstName() + " " + getLastName();
    }
}
