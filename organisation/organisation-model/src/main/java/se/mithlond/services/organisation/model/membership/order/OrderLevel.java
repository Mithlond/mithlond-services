/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
package se.mithlond.services.organisation.model.membership.order;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.shared.spi.algorithms.authorization.AuthPathBuilder;
import se.mithlond.services.shared.spi.algorithms.authorization.SemanticAuthorizationPath;
import se.mithlond.services.shared.spi.algorithms.authorization.SingleSemanticAuthorizationPathProducer;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;


/**
 * A Level description within an Order.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = "OrderLevel.getByOrganisationAndOrder",
                query = "select a from OrderLevel a where a.order.orderName like :orderName order by a.index")
})
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "orderNameAndIndexIsUnique",
        columnNames = {"index", "order_id"})})
@XmlType(namespace = Patterns.NAMESPACE,
        propOrder = {"orderLevelXmlID", "index", "name", "shortDesc", "fullDesc", "order"})
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderLevel extends NazgulEntity implements SingleSemanticAuthorizationPathProducer {

    // Constants
    private static final long serialVersionUID = 8829990012L;

    // Internal state
    @XmlID
    @XmlElement(nillable = true, required = false)
    @Transient
    private String orderLevelXmlID;

    @Basic(optional = false) @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private int index;

    @Basic(optional = false) @Column(nullable = false)
    @XmlElement(required = true, nillable = false)
    private String name;

    @Basic(optional = false)
    @Column(nullable = false, length = 255)
    private String shortDesc;

    @Basic(optional = false)
    @Column(nullable = false, length = 2048)
    private String fullDesc;

    /*
    @OneToOne(cascade = {CascadeType.REFRESH, CascadeType.MERGE})
    private Picture distinctiveImage;
    */

    @XmlIDREF
    @XmlElement(required = true, nillable = false)
    @ManyToOne(optional = false,
            fetch = FetchType.EAGER,
            cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
    private Order order;

    /**
     * JPA & JAXB-friendly constructor;
     */
    public OrderLevel() {
    }

    /**
     * Compound constructor that does not assign the Order parameter.
     * Instead, this is done in the compound constructor of Order (unless,
     * of course, you do it manually).
     *
     * @param index     The index in the Order of this OrderLevel. Low orderlevels are given low numbers.
     * @param name      The name of this OrderLevel, such as "Väktare".
     * @param shortDesc The mandatory and non-empty short description of this OrderLevel entity.
     * @param fullDesc  The full description of this entity (up to 2048 chars), visible in detailed listings.
     *                  May not be null or empty.
     */
    public OrderLevel(final int index,
                      final String name,
                      final String shortDesc,
                      final String fullDesc) {

        // Assign internal state
        this.index = index;
        this.name = name;
        this.shortDesc = shortDesc;
        this.fullDesc = fullDesc;
    }

    //
    // Properties
    //

    /**
     * @return The index of this OrderLevel entity within its owning Order
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return The name of this OrderLevel entity
     */
    public String getName() {
        return name;
    }

    /**
     * @return The mandatory and non-empty short description of this entity.
     */
    public String getShortDesc() {
        return shortDesc;
    }

    /**
     * @return The full description of this entity, visible in detailed listings.
     */
    public String getFullDesc() {
        return fullDesc;
    }

    /**
     * Assigns the fullDesc of this Listable.
     *
     * @param fullDesc the non-null fullDesc of this Listable.
     */
    public void setFullDesc(final String fullDesc) {

        // Check sanity
        Validate.notEmpty(fullDesc, "Cannot handle null or empty fullDesc argument.");

        // Assign internal state
        this.fullDesc = fullDesc;
    }

    /**
     * Assigns the shortDesc of this Listable.
     *
     * @param shortDesc the shortDesc of this Listable.
     */
    public void setShortDesc(final String shortDesc) {

        // Check sanity
        Validate.notEmpty(shortDesc, "Cannot handle null or empty shortDesc argument.");

        // Assign internal state
        this.shortDesc = shortDesc;
    }

    /**
     * Updates internal state to provide a unique XmlID for this instance.
     */
    public final void setXmlID() {
        if (order == null) {
            throw new IllegalStateException("Cannot handle null Order for OrderLevel [" + name + "]");
        }

        String tmp = order.getOrderName() + "_" + getName();
        this.orderLevelXmlID = tmp.trim()
                .replaceAll("\\p{javaWhitespace}", "")
                .replace('å', 'a')
                .replace('ä', 'a')
                .replace('ö', 'o')
                .replace('Å', 'A')
                .replace('Ä', 'A')
                .replace('Ö', 'O')
                .replace('/', '_')
                .replace('|', '_');
    }

    /**
     * @return The xmlID of this OrderLevel.
     */
    public String getXmlID() {
        return orderLevelXmlID;
    }

    /*
     * The image of this OrderLevel.
     *
     * @return The DistinctiveImage relation of this OrderLevel

    public Picture getDistinctiveImage() {
        return distinctiveImage;
    }

    /*
     * Assigns the distinctiveImage of this OrderLevel.
     * The image of this OrderLevel.
     *
     * @param distinctiveImage The distinctiveImage relation of this OrderLevel
     *
    public void setDistinctiveImage(Picture distinctiveImage) {
        // Assign the relation to our internal state
        this.distinctiveImage = distinctiveImage;
    }
    */


    /**
     * @return The order of this OrderLevel.
     */
    public Order getOrder() {
        return order;
    }

    /**
     * Assigns the Order of this OrderLevel
     *
     * @param order the Order of this OrderLevel
     */
    public void setOrder(final Order order) {

        // Check sanity
        Validate.notNull(order, "Cannot handle null order argument.");

        // Assign internal state
        this.order = order;
        setXmlID();
    }

    /**
     * The Order::orderName and OrderLevel::index combination is unique, and will be the
     * only data considered for hashCode and equality calculations.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object that) {

        // Check sanity
        if(this == that) {
            return true;
        }
        if(!(that instanceof OrderLevel)) {
            return false;
        }

        // Delegate
        final OrderLevel thatOrderLevel = (OrderLevel) that;
        return this.hashCode() == thatOrderLevel.hashCode();
    }

    /**
     * The Order::orderName and OrderLevel::index combination is unique, and will be the
     * only data considered for hashCode and equality calculations.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return order.getOrderName().hashCode() + index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticAuthorizationPath createPath() {
        final Order myOrder = getOrder();
        return AuthPathBuilder.create()
                .withRealm(myOrder.getOwningOrganisation().getOrganisationName())
                .withGroup(myOrder.getOrderName())
                .withQualifier(getName())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(name, "name")
                .notNullOrEmpty(shortDesc, "shortDesc")
                .notNullOrEmpty(fullDesc, "fullDesc")
                .endExpressionAndValidate();
    }
}
