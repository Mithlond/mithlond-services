/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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

import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.Listable;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity class defining an Order, holding a distinct number of OrderLevels.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = "Order.getByOrganisation",
                query = "select a from Order a where a.owningOrganisation.organisationName like :organisationName"
                        + " order by a.orderName"),
        @NamedQuery(name = "Order.getByOrganisationAndName",
                query = "select a from Order a where a.owningOrganisation.organisationName like :organisationName" +
                        " and a.orderName like :orderName order by a.orderName"),
        @NamedQuery(name = "Order.getOrganisationAndGuild",
                query = "select a from Order a where a.owningOrganisation.organisationName like :organisationName" +
                        " and a.owningGuild.groupName like :guildName order by a.orderName")
})
@Entity
@Table(name = "NazgulOrder")
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"orderName", "levels", "owningGuild"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Order extends Listable {

    // Constants
    private static final long serialVersionUID = 8829990011L;

    // Internal state
    @XmlID
    @Basic
    @Column(nullable = false, length = 1024)
    private String orderName;

    @XmlIDREF
    @ManyToOne(optional = true)
    private Guild owningGuild;

    @XmlElementWrapper(name = "levels", nillable = true, required = true)
    @XmlElement(name = "level")
    @XmlIDREF
    @OneToMany(cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE}, mappedBy = "order")
    private List<OrderLevel> levels = new ArrayList<OrderLevel>();

    /**
     * JPA & JAXB-friendly constructor.
     */
    public Order() {
    }

    /**
     * Compound constructor.
     *
     * @param orderName          The Order Name.
     * @param shortDescription   The short description of this Order.
     * @param fullDescription    The human-readable, full description of this Order.
     * @param owningOrganisation The Organisation owning this order.
     * @param owningGuild        The Guild owning this Order. A null value implies that this is not a Guild order.
     * @param levels             The OrderLevel instances of this Order.
     */
    public Order(final String orderName,
                 final String shortDescription,
                 final String fullDescription,
                 final Organisation owningOrganisation,
                 final Guild owningGuild,
                 final List<OrderLevel> levels) {

        // Delegate
        super(shortDescription, fullDescription, owningOrganisation);

        this.orderName = orderName;
        this.owningGuild = owningGuild;

        // Process the levels to assign this order as their order.
        setLevels(levels);
    }

    //
    // Properties
    //

    /**
     * @return The name of this DefaultOrderVO.
     */
    public String getOrderName() {
        return orderName;
    }

    /**
     * The guild that owns/hands out this order.
     *
     * @return The OwningGuild relation of this Order
     */
    public Guild getOwningGuild() {
        return owningGuild;
    }

    /**
     * Assigns the owningGuild of this Order.
     * The guild that owns/hands out this order.
     *
     * @param owningGuild The owningGuild relation of this Order
     */

    public void setOwningGuild(final Guild owningGuild) {
        // Assign the relation to our internal state
        this.owningGuild = owningGuild;
    }

    /**
     * The levels of this order.
     *
     * @return The KnownLevels relation of this Order
     */
    public List<OrderLevel> getLevels() {
        return levels;
    }

    /**
     * Assigns the knownLevels of this Order.
     * The levels of this order.
     *
     * @param levels The knownLevels relation of this Order
     */
    public final void setLevels(final List<OrderLevel> levels) {

        // Check sanity
        Validate.notNull(levels, "levels");

        // Assign the relation to our internal state
        this.levels = levels;

        // Enforce consistent state.
        for (OrderLevel current : levels) {
            current.setOrder(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateListableEntityState() throws InternalStateValidationException {

        // Check sanity
        InternalStateValidationException.create()
                .notNullOrEmpty(orderName, "orderName")
                .endExpressionAndValidate();
    }
}
