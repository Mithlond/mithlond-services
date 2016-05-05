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
package se.mithlond.services.organisation.model.address;

import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.CategoryProducer;
import se.mithlond.services.organisation.model.Listable;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

/**
 * A classified and categorized Address that belongs to a certain Organisation.
 * CategorizedAddresses should be available only within service calls from the Organisation to which they belong.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = CategorizedAddress.NAMEDQ_GET_BY_ORGANISATION_ID_AND_CATEGORY_IDS,
                query = "select a from CategorizedAddress a "
                        + "where a.owningOrganisation.id = :" + OrganisationPatterns.PARAM_ORGANISATION_ID
                        + " and ( 0 = :" + OrganisationPatterns.PARAM_NUM_CATEGORYIDS
                        + " or a.category.categoryID in :" + OrganisationPatterns.PARAM_CATEGORY_IDS + " ) "
                        + " order by a.shortDesc"),
        @NamedQuery(name = "CategorizedAddress.getByOrganisationAndClassification",
                query = "select a from CategorizedAddress a "
                        + "where a.owningOrganisation.organisationName like :organisationName "
                        + "and a.category.classification like :classification order by a.shortDesc"),
        @NamedQuery(name = "CategorizedAddress.getByOrganisationAndShortDesc",
                query = "select a from CategorizedAddress a "
                        + "where a.owningOrganisation.organisationName like :organisationName "
                        + "and a.shortDesc like :shortDesc order by a.shortDesc"),
        @NamedQuery(name = CategorizedAddress.NAMEDQ_GET_BY_SEARCHPARAMETERS,
                query = "select a from CategorizedAddress a "
                        + "where a.fullDesc like :" + OrganisationPatterns.PARAM_FULL_DESC
                        + " and a.shortDesc like :" + OrganisationPatterns.PARAM_SHORT_DESC
                        + " and ( :" + OrganisationPatterns.PARAM_CLASSIFICATION_IDS + " > 0 "
                        + "      and a.category.classification in :" + OrganisationPatterns.PARAM_CATEGORY_IDS + " ) "
                        + " and ( :" + OrganisationPatterns.PARAM_NUM_ORGANISATIONIDS + " > 0 "
                        + "      and a.owningOrganisation.id in :" + OrganisationPatterns.PARAM_ORGANISATION_IDS + " ) "
                        + " and a.address.careOfLine like :" + OrganisationPatterns.PARAM_ADDRESSCAREOFLINE
                        + " and a.address.city like :" + OrganisationPatterns.PARAM_CITY
                        + " and a.address.country like :" + OrganisationPatterns.PARAM_COUNTRY
                        + " and a.address.departmentName like :" + OrganisationPatterns.PARAM_DEPARTMENT
                        + " and a.address.description like :" + OrganisationPatterns.PARAM_DESCRIPTION
                        + " and a.address.number like :" + OrganisationPatterns.PARAM_NUMBER
                        + " and a.address.street like :" + OrganisationPatterns.PARAM_STREET
                        + " and a.address.zipCode like :" + OrganisationPatterns.PARAM_ZIPCODE
                        + " order by a.category.classification, a.shortDesc")

})
@Entity
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"address", "category"})
@XmlAccessorType(XmlAccessType.FIELD)
public class CategorizedAddress extends Listable implements Comparable<CategorizedAddress> {

    /**
     * NamedQuery for getting CategorizedAddresses by information defined within a search parameters.
     */
    public static final String NAMEDQ_GET_BY_SEARCHPARAMETERS =
            "CategorizedAddress.getBySearchParams";

    /**
     * NamedQuery for getting CategorizedAddresses by organisation and category ID.
     */
    public static final String NAMEDQ_GET_BY_ORGANISATION_ID_AND_CATEGORY_IDS =
            "CategorizedAddress.getByOrganisationIdAndCategoryIDs";

    /**
     * The constant value indicating that a {@link CategorizedAddress} is for Activities.
     */
    public static final String ACTIVITY_CLASSIFICATION = "activity_locale";

    // Internal state
    @ManyToOne(optional = false, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @XmlIDREF
    @XmlAttribute(required = true, name = "categoryReference")
    private Category category;

    @Embedded
    @XmlElement(required = true, nillable = false)
    private Address address;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public CategorizedAddress() {
    }

    /**
     * Convenience constructor, creating a CategorizedAddress with a single category.
     *
     * @param shortDesc    The mandatory and non-empty short description of this Listable entity.
     * @param fullDesc     The full description of this entity (up to 2048 chars), visible
     *                     in detailed listings. May not be null or empty.
     * @param organisation The organisation within which this Listable exists.
     * @param address      The address to categorize. Not null.
     * @param producer     The CategoryProducer used to acquire a Category.
     */
    public CategorizedAddress(final String shortDesc,
                              final String fullDesc,
                              final CategoryProducer producer,
                              final Organisation organisation,
                              final Address address) {

        // Delegate
        super(shortDesc, fullDesc, organisation);

        // Check sanity
        Validate.notNull(producer, "producer");

        // Assign internal state
        this.address = address;
        this.category = producer.getCategory();
    }

    /**
     * Assigns the JPA ID of this CategorizedAddress.
     *
     * @param id the JPA ID of this CategorizedAddress.
     */
    @Override
    public void setId(final long id) {
        super.setId(id);
    }

    /**
     * @return The Category of this CategorizedAddress. Not null or empty.
     */
    public Category getCategory() {
        return category;
    }

    /**
     * @return The Address of this CategorizedAddress.
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Assigns the supplied Address to this CategorizedAddress.
     *
     * @param address The address to categorize. Not null.
     */
    public void setAddress(final Address address) {

        // Check sanity
        Validate.notNull(address, "address");

        // Assign internal state
        this.address = address;
    }

    /**
     * Assigns the supplied Category to this CategorizedAddress.
     *
     * @param category The single category of this CategorizedAddress. Not null.
     */
    public void setCategory(final Category category) {

        // Check sanity
        Validate.notNull(category, "Cannot handle null category argument.");

        // Assign internal state
        this.category = category;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final CategorizedAddress that) {

        // Check sanity
        if (that == null) {
            return -1;
        }
        if (that == this) {
            return 0;
        }

        int toReturn = getAddress().compareTo(that.getAddress());
        if (toReturn == 0) {
            toReturn = category.compareTo(that.getCategory());
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object that) {

        // Check sanity
        if (this == that) {
            return true;
        }
        if (!(that instanceof CategorizedAddress)) {
            return false;
        }

        // Delegate
        return this.hashCode() == that.hashCode();
    }

    /**
     * The hashCode implementation only considers the hashCode of the Address and each Category.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 31 * address.hashCode() + category.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "CategorizedAddress [" + getCategory() + ": " + getAddress() + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateListableEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(address, "address")
                .notNull(category, "category")
                .endExpressionAndValidate();
    }
}
