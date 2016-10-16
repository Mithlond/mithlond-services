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
package se.mithlond.services.organisation.model.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * <p>Entity storing Users of Mithlond services. All services calls should be performed by a known User.</p>
 * <p><strong>Note!</strong> No credential information is stored within the User class.
 * Instead, such types of configuration should be managed by an IDM system assumed to handle authentication.
 * The User/Membership/Group/Guild/Organisation structure within the Mithlond service model handles internal
 * Authorization, as these entities better describe the privilege hierarchy of content.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"xmlID", "userIdentifierToken",
        "firstName", "lastName", "birthday", "personalNumberLast4Digits",
        "homeAddress", "memberships", "contactDetails"})
@XmlAccessorType(XmlAccessType.FIELD)
@Table(name = "InternalUsers", uniqueConstraints = {
        @UniqueConstraint(name = "userIdentifierTokenIsUnique", columnNames = "userIdentifierToken")
})
public class User extends NazgulEntity {

    // Our Logger
    @XmlTransient
    private static final Logger log = LoggerFactory.getLogger(User.class);

    // Constants
    private static final long serialVersionUID = 8829990119L;

    /**
     * A non-empty token used to connect this User to a userID within an external Identity Management system.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true)
    private String userIdentifierToken;

    /**
     * The first name of this User.
     */
    @Basic(optional = false)
    @Column(nullable = false, length = 64)
    @XmlElement(required = true)
    private String firstName;

    /**
     * The last name of this User.
     */
    @Basic(optional = false)
    @Column(nullable = false, length = 64)
    @XmlElement(required = true)
    private String lastName;

    /**
     * The birthday of this User.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true)
    private LocalDate birthday;

    /**
     * The optional personal number (SSN-related, swedish thing) of this User.
     */
    @Basic
    @Column(length = 4)
    @XmlAttribute
    private short personalNumberLast4Digits;

    /**
     * The home address of this User.
     */
    @Embedded
    @XmlElement(required = true)
    private Address homeAddress;

    /**
     * The {@link Membership}s held by this User.
     */
    @XmlElementWrapper(name = "memberships", required = true)
    @XmlElement(name = "membership")
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Membership> memberships;

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
    @CollectionTable(name = "user_contactdetails", uniqueConstraints = {
            @UniqueConstraint(name = "unq_user_contact_type",
            columnNames = {"user_id", "contact_type"})
    })
    @Column(name = "address_or_number")
    @MapKeyColumn(name = "contact_type")
    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, String> contactDetails;

    /**
     * Syntetic XML ID for this User, generated immediately before Marshalling.
     * The method ensures that whitespace is weeded not found in the xmlID as per the XML specification.
     */
    @XmlID
    @XmlAttribute(required = true)
    @Transient
    @SuppressWarnings("all")
    private String xmlID;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public User() {

        // Assign internal state
        contactDetails = new TreeMap<>();
        memberships = new ArrayList<>();
    }

    /**
     * Compound constructor, creating a User wrapping the supplied data.
     *
     * @param firstName                 The first name of this User.
     * @param lastName                  The last name of this User.
     * @param birthday                  The birthday of this User.
     * @param personalNumberLast4Digits The last 4 digits in the User's personal number.
     * @param homeAddress               The home address of this User.
     * @param contactDetails            The contact details of this User.
     * @param memberships               A Map containing the Memberships of this User. Can be {@code null}, in which
     *                                  case it is initialized to an empty SortedMap.
     * @param userIdentifierToken       A unique identifier token for this User which permits relating
     *                                  this User to the Authentication system managing User logins.
     */
    public User(final String firstName,
            final String lastName,
            final LocalDate birthday,
            final short personalNumberLast4Digits,
            final Address homeAddress,
            final List<Membership> memberships,
            final Map<String, String> contactDetails,
            final String userIdentifierToken) {

        // Delegate
        this();

        // Check sanity
        Validate.notNull(birthday, "birthday");

        this.firstName = firstName;
        this.lastName = lastName;
        this.personalNumberLast4Digits = personalNumberLast4Digits;
        this.homeAddress = homeAddress;
        this.userIdentifierToken = userIdentifierToken;
        this.birthday = birthday;

        if(memberships != null) {
            this.memberships.addAll(memberships);
        }
        if(contactDetails != null) {
            this.contactDetails.putAll(contactDetails);
        }
        setXmlID();
    }

    /**
     * @return A unique identifier token which permits relating this User to the
     * Authentication system managing User logins. Typically a UUID, LDAP identifier or similar.
     */
    public String getUserIdentifierToken() {
        return userIdentifierToken;
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
        return birthday;
    }

    /**
     * @return The Memberships of this AbstractUser.
     */
    public List<Membership> getMemberships() {
        return memberships;
    }

    /**
     * Assigns the Memberships of this User.
     *
     * @param memberships the Memberships of this AbstractUser.
     */
    public void setMemberships(final List<Membership> memberships) {

        // Check sanity
        Validate.notNull(memberships, "Cannot handle null memberships argument.");

        // Handle reverse relationship
        this.memberships = memberships;
        for (Membership current : memberships) {
            current.setUser(this);
        }
    }

    /**
     * @return The HomeAddress relation of this User.
     */
    public Address getHomeAddress() {
        return homeAddress;
    }

    /**
     * Assigns the homeAddress of this User.
     *
     * @param homeAddress The homeAddress relation of this User
     */
    public void setHomeAddress(final Address homeAddress) {

        // Check sanity
        Validate.notNull(homeAddress, "homeAddress");

        // Assign internal state
        this.homeAddress = homeAddress;
    }

    /**
     * @return The known contact details of this User.
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
     * Creates a debug printout of this User.
     *
     * @return a debug printout of this User.
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
        return "User " + getIdAndName() + "\n"
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
                log.debug("Found [" + contactDetails.size() + "] contact details for " + getIdAndName());
            }

            for (Map.Entry<String, String> current : contactDetails.entrySet()) {

                final String currentKey = current.getKey();
                final String currentValue = current.getValue();

                if (log.isDebugEnabled()) {
                    log.debug(" User " + getIdAndName() + " contactDetails [" + currentKey + "]: " + currentValue);
                }
            }
        }

        final int numMemberships = memberships.size();
        if (log.isDebugEnabled()) {
            log.debug("User " + getIdAndName() + " has [" + numMemberships + "] memberships.");
        }

        // Validate internal state
        InternalStateValidationException.create()
                .notNullOrEmpty(firstName, "firstName")
                .notNullOrEmpty(lastName, "lastName")
                .notNull(birthday, "birthday")
                .notNull(homeAddress, "homeAddress")
                .notNull(memberships, "memberships")
                .notNull(contactDetails, "contactDetails")
                .notNullOrEmpty(userIdentifierToken, "userIdentifierToken")
                .endExpressionAndValidate();
    }

    //
    // Private helpers
    //

    private String getIdAndName() {
        return "[" + getId() + "]: " + getFirstName() + " " + getLastName();
    }

    /**
     * Standard JAXB class-wide listener method, automagically invoked
     * immediately before this object is Marshalled.
     *
     * @param marshaller The active Marshaller.
     */
    @SuppressWarnings("all")
    private void beforeMarshal(final Marshaller marshaller) {
        setXmlID();
    }

    /**
     * Note that all XML IDs must start with letters.
     * ... because otherwise it is considered an XML number
     * ... glorious framework implementation....
     */
    private void setXmlID() {

        final String hopefullyUniqueString = "user_" + getId()
                + "_" + firstName
                + "_" + lastName
                + "_" + DateTimeFormatter.ISO_LOCAL_DATE.format(birthday).replaceAll("-", "_");
        this.xmlID = hopefullyUniqueString.replaceAll("\\s+", "_");
    }
}
