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
package se.mithlond.services.organisation.model.finance;

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.Patterns;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Amount definition, containing the amount value as well as a WellKnownCurrency
 * defining the currency in which this Amount is given.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Embeddable
@XmlType(namespace = Patterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class Amount implements Validatable, Comparable<Amount>, Serializable {

    // Internal state
    @Basic(optional = false)
    @Column(nullable = false, precision = 10, scale = 2)
    @XmlAttribute(required = true)
    private BigDecimal value;

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlAttribute(required = true)
    private String currency;

    /**
     * JAXB/JPA-friendly constructor
     */
    public Amount() {
    }

    /**
     * Compound constructor creating an Amount from the given value and currency.
     *
     * @param value    The value of this amount. Cannot be {@code &lt; 0.0}.
     * @param currency The currency of this amount.
     */
    public Amount(final BigDecimal value, final WellKnownCurrency currency) {
        this.value = value;
        this.currency = currency.toString();
    }

    /**
     * @return The value of this Amount.
     */
    public BigDecimal getValue() {
        return value;
    }

    /**
     * @return The currency of this amount.
     */
    public WellKnownCurrency getCurrency() {
        return WellKnownCurrency.valueOf(currency);
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
        if (!(that instanceof Amount)) {
            return false;
        }

        // Delegate
        final Amount thatAmount = (Amount) that;
        return getCurrency().equals(thatAmount.getCurrency())
                && getValue().equals(thatAmount.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return currency.hashCode() + 31 * value.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Amount that) {

        if (getCurrency() != that.getCurrency()) {
            throw new IllegalArgumentException("Cannot compare amounts in different currencies. ["
                    + getCurrency().toString() + " <--> " + that.getCurrency().toString() + "]");
        }

        // Delegate and return.
        return value.compareTo(that.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getValue() + " " + getCurrency().getCurrencyNamePlural();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notTrue(value.compareTo(BigDecimal.ZERO) < 0, "value < 0")
                .notNull(currency, "currency")
                .endExpressionAndValidate();
    }
}
