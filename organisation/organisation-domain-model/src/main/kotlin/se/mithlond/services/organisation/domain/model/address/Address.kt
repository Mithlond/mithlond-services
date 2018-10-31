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
package se.mithlond.services.organisation.domain.model.address

import java.io.Serializable
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Embeddable

/**
 * Implementation of an embedded Address. The embedded nature of the address implies that
 * it cannot be stored in a table (be part of an entity) where any of the persistent properties
 * have the same name as any member of this Address.
 *
 * @param careOfLine     The optional C/O-line. (i.e. "c/o Albert F Neumann")
 * @param departmentName The optional name of the department (i.e. "Att: Research & Development")
 * @param street         The street name of this address.
 * @param number         The optional number on the street or Box, such as "1412" or "18 B".
 * @param city           The city of this address.
 * @param zipCode        The zipCode of this address.
 * @param country        The country of this address.
 * @param description    The arbitrary and optional description of this Address.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Embeddable
@Access(AccessType.FIELD)
data class Address @JvmOverloads constructor(

        @field:Basic(optional = true)
        @field:Column(nullable = true, length = 1024)
        val careOfLine: String? = null,

        @field:Basic(optional = true)
        @field:Column(nullable = true)
        val departmentName: String? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var street: String? = null,

        @field:Basic(optional = true)
        @field:Column(nullable = true)
        var number: String? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var city: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var zipCode: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        var country: String,

        @field:Basic(optional = true)
        @field:Column(nullable = true, length = 1024)
        var description: String? = null) : Serializable, Comparable<Address> {

    override fun compareTo(other: Address): Int {

        var toReturn = this.country.compareTo(other.country)

        if(toReturn == 0) {
            toReturn = this.city.compareTo(other.city)
        }

        if(toReturn == 0) {

            val leftStreet = this.street ?: ""
            val rightStreet = other.street ?: ""
            toReturn = leftStreet.compareTo(rightStreet)
        }

        if(toReturn == 0) {

            val leftNum = this.number?: ""
            val rightNum = other.number?: ""
            toReturn = leftNum.compareTo(rightNum)
        }

        // All Done.
        return toReturn
    }

    /**
     * @return True if this Address sports non-null [street] and [number] properties
     */
    val hasVisitingAddress: Boolean
        get() = street != null && number != null
}
