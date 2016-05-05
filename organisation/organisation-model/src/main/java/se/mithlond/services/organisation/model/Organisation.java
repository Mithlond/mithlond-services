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
package se.mithlond.services.organisation.model;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import java.time.ZoneId;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Entity implementation for Organisations, all of which are required to have a unique name.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@NamedQueries({
        @NamedQuery(name = Organisation.NAMEDQ_GET_ALL,
                query = "select a from Organisation a"
                        + " order by a.organisationName"),
        @NamedQuery(name = Organisation.NAMEDQ_GET_BY_NAME,
                query = "select a from Organisation a where a.organisationName like :"
                        + OrganisationPatterns.PARAM_ORGANISATION_NAME
                        + " order by a.organisationName")
})
@Table(uniqueConstraints = {@UniqueConstraint(name = "organisationNameIsUnique", columnNames = {"organisationName"})})
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"organisationName", "xmlID", "suffix", "phone", "bankAccountInfo",
                "postAccountInfo", "emailSuffix", "visitingAddress", "timeZoneID", "country", "language"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Organisation extends NazgulEntity implements Comparable<Organisation> {

    // Constants
    private static final long serialVersionUID = 8829990020L;

    /**
     * NamedQuery for getting Organisations by organisationName.
     */
    public static final String NAMEDQ_GET_BY_NAME = "Organisation.getByName";

    /**
     * NamedQuery for getting Organisations by organisationName.
     */
    public static final String NAMEDQ_GET_ALL = "Organisation.getAll";

    // Internal state
    /**
     * The name of this Organisation, such as "Forodrim".
     * Each organisationName must be unique among all other organisation names.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private String organisationName;

    /**
     * Syntetic XML ID for this Organisation, generated immediately before Marshalling.
     * Since whitespace is not permitted in an XML ID, the beforeMarshal listener method generates this field from
     * the organisation name while replacing all whitespace with underscore.
     */
    @XmlID
    @XmlAttribute(required = true)
    @Transient
    @SuppressWarnings("all")
    private String xmlID;

    /**
     * The suffix of this organisation, such as "Stockholms Tolkiensällskap".
     */
    @Basic
    @Column(length = 1024)
    @XmlElement
    private String suffix;

    /**
     * The official phone number to the Organisation.
     */
    @Basic
    @Column(length = 64)
    @XmlElement
    private String phone;

    /**
     * The bank account number of this organisation.
     */
    @Basic
    @XmlElement
    private String bankAccountInfo;

    /**
     * The postal account number of this organisation.
     */
    @Basic
    @XmlElement
    private String postAccountInfo;

    /**
     * The email suffix of this organisation, appended to the alias of each membership to receive a
     * non-personal electronic mail address. For example, given the emailSuffix "mithlond.se" and the
     */
    @Basic(optional = false)
    @Column(length = 64)
    @XmlElement(required = true)
    private String emailSuffix;

    /**
     * The visiting address of this organisation.
     */
    @Embedded
    @XmlElement(required = true)
    private Address visitingAddress;

    /**
     * The standard TimeZoneID of this Organisation.
     *
     * @see ZoneId
     */
    @Basic(optional = false)
    @Column(length = 64)
    @XmlAttribute(required = true)
    private String timeZoneID;

    /**
     * The standard language of this Organisation, as an abbreviated, 2/3-letter string.
     *
     * @see Locale#getLanguage()
     */
    @Basic(optional = false)
    @Column(length = 3)
    @XmlAttribute(required = true)
    private String language;

    /**
     * The standard country of this Organisation, as an abbreviated, 2/3-letter string.
     *
     * @see Locale#getCountry()
     */
    @Basic(optional = false)
    @Column(length = 3, name = "locale_country")
    @XmlAttribute(required = true)
    private String country;

    /**
     * JPA-friendly constructor.
     */
    public Organisation() {
    }

    /**
     * Compound constructor.
     *
     * @param organisationName   The name of this organisation (i.e. "Forodrim")
     * @param suffix             The suffix of this organisation (i.e. "Stockholms Tolkiensällskap")
     * @param phone              The official phone number to this organisation.
     * @param bankAccountInfo    The bank account number of this organisation.
     * @param postAccountInfo    The postal account number of this organisation.
     * @param visitingAddress    The visiting address of this organisation.
     * @param emailSuffix        The email suffix of this organisation (i.e. "mithlond.se").
     * @param standardTimeZoneID The standard TimeZoneID of this organisation.
     * @param standardLocale     The standard Locale of this organisation.
     */
    public Organisation(final String organisationName,
            final String suffix,
            final String phone,
            final String bankAccountInfo,
            final String postAccountInfo,
            final Address visitingAddress,
            final String emailSuffix,
            final ZoneId standardTimeZoneID,
            final Locale standardLocale) {

        // Assign internal state
        this.organisationName = Validate.notEmpty(organisationName, "organisationName");
        this.visitingAddress = Validate.notNull(visitingAddress, "visitingAddress");
        this.timeZoneID = Validate.notNull(standardTimeZoneID, "standardTimeZoneID").toString();
        this.suffix = suffix;
        this.phone = phone;
        this.bankAccountInfo = bankAccountInfo;
        this.postAccountInfo = postAccountInfo;
        this.emailSuffix = emailSuffix;

        final Locale tmp = Validate.notNull(standardLocale, "standardLocale");
        this.country = tmp.getCountry();
        this.language = tmp.getLanguage();
        setXmlID();
    }

    /**
     * @return The name of this Organisation entity
     */
    public String getOrganisationName() {
        return organisationName;
    }

    /**
     * Name suffix, such as "Stockholms Tolkiensallskap".
     *
     * @return The suffix of this Organisation entity
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * @return The phone of this Organisation entity
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @return The bankAccountInfo of this Organisation entity
     */
    public String getBankAccountInfo() {
        return bankAccountInfo;
    }

    /**
     * @return The postAccountInfo of this Organisation entity
     */
    public String getPostAccountInfo() {
        return postAccountInfo;
    }

    /**
     * @return The email suffix of this organisation, such as <code>mithlond.se</code>.
     */
    public String getEmailSuffix() {
        return emailSuffix;
    }

    /**
     * Visiting address for this Organisation.
     * If present, should refer to the standard meeting place.
     *
     * @return The VisitingAddress relation of this Organisation
     */
    public Address getVisitingAddress() {
        return visitingAddress;
    }

    /**
     * Retrieves the standard TimeZone of this Organisation.
     *
     * @return the standard TimeZone of this Organisation.
     */
    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(timeZoneID);
    }

    /**
     * Retrieves the standard Locale of this Organisation.
     *
     * @return the standard Locale of this Organisation.
     */
    public Locale getLocale() {
        return new Locale(language, country);
    }

    /**
     * The OrganisationName is unique per organisation, so will be used in equality
     * comparisons and hashCode calculations.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object that) {

        // Check sanity
        if (this == that) {
            return true;
        }
        if (!(that instanceof Organisation)) {
            return false;
        }

        // Delegate and return.
        final Organisation thatOrganisation = (Organisation) that;
        return organisationName.equals(thatOrganisation.organisationName);
    }

    /**
     * The OrganisationName is unique per organisation, so will be used in equality
     * comparisons and hashCode calculations.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return organisationName.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Organisation that) {
        return that == null ? -1 : organisationName.compareTo(that.organisationName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(organisationName, "organisationName")
                .notNullOrEmpty(suffix, "suffix")
                .notNullOrEmpty(emailSuffix, "emailSuffix")
                .notNullOrEmpty(visitingAddress, "visitingAddress")
                .notNullOrEmpty(language, "language")
                .notNullOrEmpty(country, "country")
                .notNullOrEmpty(timeZoneID, "timeZoneID")
                .endExpressionAndValidate();
    }

    //
    // Private helpers
    //

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

    private void setXmlID() {
        this.xmlID = "organisation_" + this.organisationName.replaceAll("\\s+", "_");
    }
}
