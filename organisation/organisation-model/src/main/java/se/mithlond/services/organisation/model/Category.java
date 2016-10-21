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

import javax.persistence.Basic;
import javax.persistence.Column;
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
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

/**
 * Definition of a Category, sporting CategoryID, Classification and a trivial Description.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = Category.NAMEDQ_GET_BY_CLASSIFICATION,
                query = "select a from Category a"
                        + " where a.classification like :" + OrganisationPatterns.PARAM_CLASSIFICATION
                        + " order by a.categoryID"),
        @NamedQuery(name = Category.NAMEDQ_GET_BY_ID_CLASSIFICATION,
                query = "select a from Category a"
                        + " where lower(a.categoryID) like lower(:" + OrganisationPatterns.PARAM_CATEGORY_ID
                        + ") and a.classification like :" + OrganisationPatterns.PARAM_CLASSIFICATION
                        + " order by a.categoryID")
})
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "categoryIdAndClassificationIsUnique", columnNames = {"category", "classification"})})
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"xmlID", "classification", "categoryID", "description"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Category extends NazgulEntity implements Comparable<Category>, CategoryProducer {

    // Constants
    private static final long serialVersionUID = 8829990031L;

    /**
     * NamedQuery for getting Category by classification.
     */
    public static final String NAMEDQ_GET_BY_CLASSIFICATION =
            "Category.getByClassification";

    /**
     * NamedQuery for getting Category by CategoryID and classification.
     */
    public static final String NAMEDQ_GET_BY_ID_CLASSIFICATION =
            "Category.getByIdAndClassification";

    /**
     * The category id - comparable to a short description or single word. Cannot be null or empty.
     */
    @Basic(optional = false)
    @Column(nullable = false, name = "category")
    private String categoryID;

    /**
     * A classification of this category, such as "Restaurant".
     * This is intended to simplify separating a type of Categories from others.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    private String classification;

    /**
     * The (fuller/richer) description of this Category. Cannot be null or empty.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    private String description;

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
     * JPA/JAXB-friendly constructor.
     */
    public Category() {
    }

    /**
     * Creates a new Category wrapping the supplied data.
     *
     * @param categoryID     The category id - comparable to a short description or single word.
     *                       Cannot be null or empty.
     * @param classification A classification of this category, such as "Restaurant". This is intended to simplify
     *                       separating a type of Categories from others.
     * @param description    The (fuller/richer) description of this Category. Cannot be null or empty.
     */
    public Category(final String categoryID,
            final String classification,
            final String description) {

        super();

        // Assign internal state
        this.categoryID = categoryID;
        this.description = description;
        this.classification = classification;
        setXmlID();
    }

    /**
     * @return The name of this Category.
     */
    public String getCategoryID() {
        return categoryID;
    }

    /**
     * @return The description (full desc) of this Category.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The classification (metadata group) of this Category.
     */
    public String getClassification() {
        return classification;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Category getCategory() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Category createCategoryWithDescription(final String description) {
        return new Category(this.categoryID, this.classification, description);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return classification.hashCode()
                + categoryID.hashCode()
                + description.hashCode();
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
        if (!(that instanceof Category)) {
            return false;
        }

        // Compare state data
        final Category cat = (Category) that;
        return getCategoryID().equals(cat.getCategoryID())
                && getClassification().equals(cat.getClassification())
                && getDescription().equals(cat.getDescription());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Category that) {

        // Check sanity
        if (that == null) {
            return -1;
        }
        if (that == this) {
            return 0;
        }

        // Compare the state
        int toReturn = getCategoryID().compareTo(that.getCategoryID());
        if (toReturn == 0) {
            toReturn = getClassification().compareTo(that.getClassification());
        }
        if (toReturn == 0) {
            toReturn = getDescription().compareTo(that.getDescription());
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClassification() + " --> " + getCategoryID();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(categoryID, "categoryID")
                .notNullOrEmpty(classification, "classification")
                .notNullOrEmpty(description, "description")
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
        final String xmlIdClassification = classification == null ? "" : classification.trim();
        final String xmlIdCategoryID = categoryID == null ? "" : categoryID.trim();
        this.xmlID = "category_" + (xmlIdClassification + "_" + xmlIdCategoryID).replaceAll("\\s+", "_");
    }
}
