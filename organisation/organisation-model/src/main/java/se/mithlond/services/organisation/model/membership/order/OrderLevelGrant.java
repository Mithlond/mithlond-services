/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2014 jGuru Europe AB
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

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.algorithms.authorization.AuthPathBuilder;
import se.mithlond.services.shared.spi.algorithms.authorization.SemanticAuthorizationPath;
import se.mithlond.services.shared.spi.algorithms.authorization.SingleSemanticAuthorizationPathProducer;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * The record of when a particular OrderLevel was granted to a Membership,
 * implying that the OrderLevelGrant class can be used as a historic record for
 * all Orders versus all Memberships.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Access(value = AccessType.FIELD)
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"orderLevel", "dateGranted", "note"})
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderLevelGrant implements Serializable, Comparable<OrderLevelGrant>,
        Validatable, SingleSemanticAuthorizationPathProducer {

    // Internal state
    @Version
    @XmlTransient
    private long version;

    @EmbeddedId
    @XmlTransient
    private OrderLevelGrantId orderLevelGrantId;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @MapsId("orderLevelId")
    @XmlIDREF
    @XmlElement(required = true, nillable = false)
    private OrderLevel orderLevel;

    // This must be XmlTransient to avoid a cyclic graph in the XSD.
    // Handled by a callback method from the Membership side.
    @ManyToOne
    @MapsId("membershipId")
    @XmlTransient
    private Membership membership;

    @Basic(optional = false)
    @Column(nullable = false)
    @Temporal(value = TemporalType.DATE)
    @XmlElement(required = true, nillable = false)
    private Calendar dateGranted;

    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(required = false, nillable = true)
    private String note;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public OrderLevelGrant() {
    }

    /**
     * Compound constructor creating a OrderLevelGrant wrapping the supplied data.
     *
     * @param orderLevel  The OrderLevel granted to the supplied Membership.
     * @param membership  The Membership which is granted an OrderLevel.
     * @param dateGranted The date when the OrderLevelGrant was given.
     * @param note        An optional [short] note for the grant.
     */
    public OrderLevelGrant(final OrderLevel orderLevel,
                           final Membership membership,
                           final ZonedDateTime dateGranted,
                           final String note) {
        // Check some sanity
        Validate.notNull(orderLevel, "orderLevel");
        Validate.notNull(membership, "membership");

        // Assign internal state
        this.orderLevel = orderLevel;
        this.membership = membership;
        this.note = note;

        setDateGranted(dateGranted);

        this.orderLevelGrantId = new OrderLevelGrantId(orderLevel.getId(), membership.getId());
    }

    /**
     * @return the Database-generated version/revision of this Entity.
     */
    public long getVersion() {
        return version;
    }

    /**
     * @return The OrderLevelGrantId which combines the IDs of the OrderLevel and Membership parts.
     */
    public OrderLevelGrantId getOrderLevelGrantId() {
        return orderLevelGrantId;
    }

    /**
     * @return The OrderLevel granted to the supplied Membership.
     */
    public OrderLevel getOrderLevel() {
        return orderLevel;
    }

    /**
     * @return The Membership which is granted an OrderLevel.
     */
    public Membership getMembership() {
        return membership;
    }

    /**
     * @return The date when the OrderLevelGrant was given.
     */
    public ZonedDateTime getDateGranted() {
        return ((GregorianCalendar) this.dateGranted).toZonedDateTime();
    }

    /**
     * @return An optional [short] note for the grant.
     */
    public String getNote() {
        return note;
    }

    /**
     * Assigns the dateGranted of this OrderLevelGrant.
     *
     * @param dateGranted The date when the OrderLevelGrant was given.
     */
    public final void setDateGranted(final ZonedDateTime dateGranted) {

        // Check sanity
        Validate.notNull(dateGranted, "dateGranted");

        // Assign internal state
        this.dateGranted = GregorianCalendar.from(dateGranted);
    }

    /**
     * Assigns - or clears - the order level grant Note.
     *
     * @param note An optional [short] note for the grant.
     */
    public void setNote(final String note) {
        this.note = note;
    }

    /**
     * Assigns the Membership of this OrderLevelGrant. This should only be called during XML unmarshalling,
     * to uphold referential integrity between Membership and OrderLevelGrant. It is not considered part of the
     * public API of the OrderLevelGrant class.
     *
     * @param membership The Membership which should be attached to an OrderLevel.
     */
    public void setMembership(final Membership membership) {

        // Check sanity
        Validate.notNull(membership, "Cannot handle null membership argument.");

        // Assign internal state
        this.membership = membership;

        if (this.orderLevelGrantId == null) {
            orderLevelGrantId = new OrderLevelGrantId(orderLevel.getId(), membership.getId());
        } else {
            this.orderLevelGrantId.membershipId = membership.getId();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final OrderLevelGrant that) {

        // Check sanity
        if (that == null) {
            return -1;
        } else if (that == this) {
            return 0;
        }

        // Start by comparing the obvious stuff.
        final OrderLevel thisOrderLevel = getOrderLevel();
        final OrderLevel thatOrderLevel = that.getOrderLevel();

        int toReturn = thisOrderLevel.getOrder().getOrderName().compareTo(thatOrderLevel.getOrder().getOrderName());
        if (toReturn == 0) {
            toReturn = thisOrderLevel.getIndex() - that.getOrderLevel().getIndex();
        }
        if (toReturn == 0) {
            toReturn = getMembership().getEmailAlias().compareTo(that.getMembership().getEmailAlias());
        }
        if (toReturn == 0) {
            toReturn = getDateGranted().compareTo(that.getDateGranted());
        }

        // All done.
        return toReturn;
    }

    /**
     * The combination of Membership and OrderLevel should be unique, so
     * hashCode and equality calculations use only those properties.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object that) {

        // Check sanity
        if(this == that) {
            return true;
        }
        if(!(that instanceof OrderLevelGrant)) {
            return false;
        }

        // Delegate
        final OrderLevelGrant thatGrant = (OrderLevelGrant) that;
        return this.hashCode() == thatGrant.hashCode();
    }

    /**
     * The combination of Membership and OrderLevel should be unique, so
     * hashCode and equality calculations use only those properties.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return membership.hashCode() + orderLevel.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticAuthorizationPath createPath() {

        final Order myOrder = getOrderLevel().getOrder();
        return AuthPathBuilder.create()
                .withRealm(myOrder.getOwningOrganisation().getOrganisationName())
                .withGroup(myOrder.getOrderName())
                .withQualifier(getOrderLevel().getName())
                .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(dateGranted, "dateGranted")
                .notNull(membership, "membership")
                .notNull(orderLevel, "orderLevel")
                .endExpressionAndValidate();
    }
}
