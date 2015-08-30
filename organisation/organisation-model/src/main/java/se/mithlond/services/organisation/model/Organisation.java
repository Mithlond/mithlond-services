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
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

/**
 * Entity implementation for Organisations, all of which are required to have a unique name.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "organisationNameIsUnique", columnNames = {"organisationName"})})
@XmlType(propOrder = {"organisationName", "suffix", "phone", "bankAccountInfo",
        "postAccountInfo", "emailSuffix", "visitingAddress"})
public class Organisation extends NazgulEntity {

    // Constants
    private static final long serialVersionUID = 8829990020L;

    // Internal state
    @XmlID
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private String organisationName;

    @Basic
    @Column(length = 1024)
    @XmlElement(required = false, nillable = true)
    private String suffix;

    @Basic
    @Column(length = 64)
    @XmlElement(required = false, nillable = true)
    private String phone;

    @Basic
    @XmlElement(required = false, nillable = true)
    private String bankAccountInfo;

    @Basic
    @XmlElement(required = false, nillable = true)
    private String postAccountInfo;

    @Basic(optional = false)
    @Column(length = 64)
    @XmlElement(required = true, nillable = false)
    private String emailSuffix;

    @Embedded
    @XmlElement(required = true, nillable = false)
    private Address visitingAddress;

    /**
     * JPA-friendly constructor.
     */
    public Organisation() {
    }

    /**
     * Compound constructor.
     *
     * @param organisationName The name of this organisation (i.e. "Forodrim")
     * @param suffix           The suffix of this organisation (i.e. "Stockholms Tolkiensällskap")
     * @param phone            The official phone number to this organisation.
     * @param bankAccountInfo  The bank account number of this organisation.
     * @param postAccountInfo  The postal account number of this organisation.
     * @param visitingAddress  The visiting address of this organisation.
     * @param emailSuffix      The email suffix of this organisation (i.e. "mithlond.se").
     */
    public Organisation(final String organisationName,
                        final String suffix,
                        final String phone,
                        final String bankAccountInfo,
                        final String postAccountInfo,
                        final Address visitingAddress,
                        final String emailSuffix) {

        // Check sanity
        Validate.notEmpty(organisationName, "organisationName");
        Validate.notNull(visitingAddress, "visitingAddress");

        // Assign internal state
        this.organisationName = organisationName;
        this.suffix = suffix;
        this.phone = phone;
        this.bankAccountInfo = bankAccountInfo;
        this.postAccountInfo = postAccountInfo;
        this.visitingAddress = visitingAddress;
        this.emailSuffix = emailSuffix;
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
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(organisationName, "organisationName")
                .notNullOrEmpty(suffix, "suffix")
                .notNullOrEmpty(emailSuffix, "emailSuffix")
                .notNullOrEmpty(visitingAddress, "visitingAddress")
                .endExpressionAndValidate();
    }
}
