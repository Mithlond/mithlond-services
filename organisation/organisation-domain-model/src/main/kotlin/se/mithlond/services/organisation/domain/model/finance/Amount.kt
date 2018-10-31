/*-
 * #%L
 * Nazgul Project: mithlond-services-organisation-domain-model
 * %%
 * Copyright (C) 2018 jGuru Europe AB
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
package se.mithlond.services.organisation.domain.model.finance

import java.io.Serializable
import java.math.BigDecimal
import java.util.Currency
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Embeddable

/**
 * Amount definition, containing the amount value as well as a currency in which this Amount is given.
 *
 * @param value    The value of this amount.
 * @param currency The currency of this amount.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Embeddable
@Access(AccessType.FIELD)
data class Amount(

        @field:Basic(optional = false)
        @field:Column(nullable = false, precision = 10, scale = 2)
        val value: BigDecimal,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        val currency: Currency

) : Serializable, Comparable<Amount> {


    override fun compareTo(other: Amount): Int {

        if (this.currency != other.currency) {
            throw IllegalArgumentException("Cannot compare amounts in different currencies. ["
                    + this.currency.currencyCode + " <--> " + other.currency.currencyCode + "]")
        }

        // Delegate and return.
        return value.compareTo(other.value)
    }
}
