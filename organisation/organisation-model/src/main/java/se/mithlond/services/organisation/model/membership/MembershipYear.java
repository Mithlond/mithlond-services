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
package se.mithlond.services.organisation.model.membership;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.finance.Amount;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * History table relating {@link Membership}s to the years they were active, including the
 * fee amount being paid.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "membershipAndYearIsUnique", columnNames = {"year", "membership_id"})})
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"membership", "year", "fee"})
@XmlAccessorType(XmlAccessType.FIELD)
public class MembershipYear extends NazgulEntity {

    /**
     * The {@link Membership} for which this {@link MembershipYear} applies.
     */
    @ManyToOne(optional = false)
    @Column(name = "membership_id")
    private Membership membership;

    /**
     * The year for which the Membership payed the fee.
     */
    @Basic(optional = false)
    @XmlAttribute(required = true)
    private int year;

    /**
     * The fee payed for the Membership to be active within its Organisation.
     */
    @XmlElement
    private Amount fee;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public MembershipYear() {
    }

    /**
     * Compound constructor creating a {@link MembershipYear} wrapping the supplied data.
     *
     * @param membership The Membership for which this MembershipYear applies.
     * @param year       The year of the Membership.
     * @param fee        The fee submitted.
     */
    public MembershipYear(final Membership membership,
            final int year,
            final Amount fee) {

        this.membership = membership;
        this.year = year;
        this.fee = fee;
    }

    /**
     * Retrieves the Membership for which this {@link MembershipYear} applies.
     *
     * @return the Membership for which this {@link MembershipYear} applies.
     */
    public Membership getMembership() {
        return membership;
    }

    /**
     * <p>Re-assigns the Membership part of this {@link MembershipYear} following a JAXB unmarshalling operation.</p>
     * <p><strong>Note!</strong> This method is for framework use only.</p>
     *
     * @param membership the non-null {@link Membership} of this {@link MembershipYear}.
     */
    public void setMembership(final Membership membership) {
        this.membership = Validate.notNull(membership, "membership");
    }

    /**
     * Retrieves the year for which this {@link MembershipYear} applies.
     *
     * @return The year for which this {@link MembershipYear} applies.
     */
    public int getYear() {
        return year;
    }

    /**
     * Retrieves the fee payed for this {@link MembershipYear}.
     *
     * @return the fee payed for this {@link MembershipYear}.
     */
    public Amount getFee() {
        return fee;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(membership, "membership")
                .endExpressionAndValidate();
    }
}
