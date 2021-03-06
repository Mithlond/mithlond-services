/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
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
package se.mithlond.services.organisation.model.address;

import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Objects;

/**
 * Implementation of an embedded Address. The embedded nature of the address implies that
 * it cannot be stored in a table (be part of an entity) where any of the persistent properties
 * have the same name as any member of this Address.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Embeddable
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"careOfLine", "departmentName", "street", "number", "city", "zipCode", "country", "description"})
public class Address implements Validatable, Comparable<Address>, Serializable {

    // Constants
    private static final long serialVersionUID = 8829990015L;

    /**
     * An optional C/O-line. (i.e. "c/o Albert F Neumann")
     */
    @Basic
    @Column(length = 1024)
    @XmlElement
    private String careOfLine;

    /**
     * The optional name of the department (i.e. "Att: Research & Development")
     */
    @Basic
    @Column(length = 1024)
    @XmlElement
    private String departmentName;

    /**
     * The street name of this address, such as "Folsyregatan".
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true)
    private String street;

    /**
     * The optional number on the street or Box, such as "1412" or "18 B".
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true)
    private String number;

    /**
     * The city of this address ("Göteborg").
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true)
    private String city;

    /**
     * The zipCode of this address.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true)
    private String zipCode;

    /**
     * The country of this address.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true)
    private String country;

    /**
     * An arbitrary and optional description of this Address. May be null.
     */
    @Basic
    @Column(length = 1024)
    @XmlElement
    private String description;

    /**
     * JAXB-friendly constructor.
     */
    public Address() {
    }

    /**
     * Compound constructor creating an Address wrapping the supplied properties.
     *
     * @param careOfLine     The optional C/O-line. (i.e. "c/o Albert F Neumann")
     * @param departmentName The optional name of the department (i.e. "Att: Research & Development")
     * @param street         The street name of this address.
     * @param number         The number on the street or Box.
     * @param city           The city of this address.
     * @param zipCode        The zipCode of this address.
     * @param country        The country of this address.
     * @param description    The arbitrary and optional description of this Address. May be null.
     */
    public Address(final String careOfLine,
            final String departmentName,
            final String street,
            final String number,
            final String city,
            final String zipCode,
            final String country,
            final String description) {

        // Assign internal state
        this.careOfLine = careOfLine;
        this.departmentName = departmentName;
        this.street = street;
        this.number = number;
        this.city = city;
        this.zipCode = zipCode;
        this.country = country;
        this.description = description;
    }

    /**
     * @return The careOfLine of this Address entity
     */
    public String getCareOfLine() {
        return careOfLine;
    }

    /**
     * Assigns the careOfLine property of this Address.
     *
     * @param careOfLine The new careOfLine. Can be {@code null}.
     */
    public void setCareOfLine(final String careOfLine) {
        this.careOfLine = careOfLine;
    }

    /**
     * Name of any particular department for which this address is valid.
     *
     * @return The departmentName of this Address entity
     */
    public String getDepartmentName() {
        return departmentName;
    }

    /**
     * @return The street of this Address entity
     */
    public String getStreet() {
        return street;
    }

    /**
     * Can hold letters, such as "18B", or "19, 2 tr".
     *
     * @return The number of this Address entity
     */
    public String getNumber() {
        return number;
    }

    /**
     * @return The city of this Address entity.
     */
    public String getCity() {
        return city;
    }

    /**
     * Holds only the zip code - not any prefices.
     * Therefore, only "43541" would be stored - not
     * any prefices such as "S-43541".
     *
     * @return The zipCode of this Address entity
     */
    public String getZipCode() {
        return zipCode;
    }

    /**
     * @return The country of this Address entity.
     */
    public String getCountry() {
        return country;
    }

    /**
     * @return An arbitrary (and optional) description of this address,
     * such as "Hemma hos Gil-galad" or "Blue Moon Café".
     */
    public String getDescription() {
        if (description == null) {
            return "";
        }
        return description;
    }

    /**
     * Assigns an arbitrary (and optional) description of this address,
     * such as "Hemma hos Gil-galad" or "Blue Moon Café".
     *
     * @param description an arbitrary (and optional) description of this address,
     *                    such as "Hemma hos Gil-galad" or "Blue Moon Café".
     *                    Cannot be null or empty.
     */
    public void setDescription(@NotNull @Size(min = 1) final String description) {

        // Check sanity
        Validate.notEmpty(description, "description");

        // Assign internal state
        this.description = description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Address that) {

        // Fail fast
        if (that == null) {
            return -1;
        } else if (that == this) {
            return 0;
        }

        // Delegate to internal state
        final String thisCoL = this.getCareOfLine() == null ? "" : this.getCareOfLine();
        final String thatCoL = that.getCareOfLine() == null ? "" : that.getCareOfLine();
        int toReturn = thisCoL.compareTo(thatCoL);

        if (toReturn == 0) {
            toReturn = compareStrings(getStreet(), that.getStreet());
        }
        if (toReturn == 0) {
            toReturn = compareStrings(getNumber(), that.getNumber());
        }
        if (toReturn == 0) {
            toReturn = compareStrings(getCity(), that.getCity());
        }
        if (toReturn == 0) {
            toReturn = compareStrings(getZipCode(), that.getZipCode());
        }
        if (toReturn == 0) {
            toReturn = compareStrings(getCountry(), that.getCountry());
        }
        return toReturn;
    }

    private int compareStrings(final String left, final String right) {
        final String leftString = left == null ? "" : left;
        final String rightString = right == null ? "" : right;
        return leftString.compareTo(rightString);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getCareOfLine(),
                getDepartmentName(),
                getStreet(),
                getNumber(),
                getCity(),
                getZipCode(),
                getCountry(),
                getDescription());
    }

    @Override
    public boolean equals(final Object o) {

        // Fail fast
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        // Delegate to internal state
        final Address address = (Address) o;
        return Objects.equals(getCareOfLine(), address.getCareOfLine())
                && Objects.equals(getDepartmentName(), address.getDepartmentName())
                && Objects.equals(getStreet(), address.getStreet())
                && Objects.equals(getNumber(), address.getNumber())
                && Objects.equals(getCity(), address.getCity())
                && Objects.equals(getZipCode(), address.getZipCode())
                && Objects.equals(getCountry(), address.getCountry())
                && Objects.equals(getDescription(), address.getDescription());
    }

    /**
     * Debug printout.
     *
     * @return a debug string of this Address.
     */
    public String toString() {
        final String desc = (getDescription() == null) ? "" : " (" + getDescription() + ")";
        return "Address" + desc + ":\n" + getCareOfLine() + " [" + getDepartmentName()
                + "]\n" + getStreet() + " " + getNumber()
                + "\n" + getZipCode() + " " + getCity()
                + "\n" + getCountry();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        // Don't permit null or empty values unnecessarily.
        InternalStateValidationException.create()
                .notNullOrEmpty(number, "number")
                .notNullOrEmpty(street, "street")
                .notNullOrEmpty(city, "city")
                .notNullOrEmpty(zipCode, "zipCode")
                .notNullOrEmpty(country, "country")
                .endExpressionAndValidate();
    }
}
