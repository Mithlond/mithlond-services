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

import se.mithlond.services.organisation.model.OrganisationPatterns;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Many-to-many relation OrderLevelGrant key between OrderLevel and Membership.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Embeddable
@Access(value = AccessType.FIELD)
@XmlTransient
@XmlType(namespace = OrganisationPatterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class OrderLevelGrantId implements Serializable {

    private static final long serialVersionUID = 88299910L;

    // Shared state
    @Column(name = "orderlevel_id")
    public long orderLevelId;

    @Column(name = "membership_id")
    public long membershipId;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public OrderLevelGrantId() {
    }

    /**
     * Compound key creating a OrderLevelGrantId object from the supplied keys.
     *
     * @param orderLevelId The id of the OrderLevel to which a Membership should be tied.
     * @param membershipId The id of the Membership to add to the Guild given.
     */
    public OrderLevelGrantId(final long orderLevelId, final long membershipId) {
        this.orderLevelId = orderLevelId;
        this.membershipId = membershipId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if (obj instanceof OrderLevelGrantId) {

            final OrderLevelGrantId that = (OrderLevelGrantId) obj;
            return orderLevelId == that.orderLevelId && membershipId == that.membershipId;
        }

        // All done.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) (orderLevelId + membershipId) % Integer.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OrderLevelGrantId [OrderLevel: " + orderLevelId + ", Membership: " + membershipId + "]";
    }
}
