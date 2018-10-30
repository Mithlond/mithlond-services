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
package se.mithlond.services.organisation.domain.model

import se.mithlond.services.organisation.domain.model.address.Address
import java.io.Serializable
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * Entity implementation for Organisations. Each Organisation must have a unique name.
 *
 * @param id The JPA ID of this Domain Entity.
 * @param organisationName The name of this Organisation, such as "Forodrim". Each organisationName must
 * be unique among all other organisation names.
 * @param suffix The suffix of this organisation, such as "Stockholms Tolkiensällskap".
 * @param phone The official phone number to the Organisation.
 * @param bankAccountInfo The bank account number of this organisation.
 * @param postAccountInfo The postal account number of this organisation.
 * @param emailSuffix The email suffix of this organisation, appended to the alias of each membership to
 * receive a non-personal electronic mail address. For example, given the emailSuffix "mithlond.se" and the
 * email alias "foo", the resulting email address is "foo@mithlond.se"
 * @param visitingAddress The visiting address of this organisation.
 * @param country The standard country of this Organisation, as an abbreviated, 2/3-letter string.
 * @param zoneID The standard TimeZoneID of this Organisation.
 * @param language The standard language of this Organisation, as an abbreviated, 2/3-letter string.
 * @param standardCurrency The standard currency of this Organisation.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations", uniqueConstraints = [
    UniqueConstraint(name = "unq_name_per_org", columnNames = ["organisationName"])
])
data class Organisation @JvmOverloads constructor(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_Organisation")
        @field:SequenceGenerator(schema = "organisations", name = "seq_Organisation",
                sequenceName = "seq_Organisation", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        val organisationName: String,

        @field:Column(length = 1024)
        val suffix: String,

        @field:Column(length = 64)
        val phone: String,

        @field:Column(length = 128)
        val bankAccountInfo: String,

        @field:Column(length = 128)
        val postAccountInfo: String,

        @field:Basic(optional = false)
        @field:Column(length = 64, nullable = false)
        val emailSuffix: String,

        @Embedded
        val visitingAddress: Address,

        @field:Basic(optional = false)
        @field:Column(length = 64, nullable = false)
        val zoneID: String,

        @field:Basic(optional = false)
        @field:Column(length = 3, nullable = false)
        val language: String,

        @field:Basic(optional = false)
        @field:Column(length = 3, nullable = false, name = "locale_country")
        val country: String,

        @field:Basic(optional = false)
        @field:Column(length = 5, nullable = false, name = "standard_currency")
        val standardCurrency: String,

        @field:Basic(optional = true)
        @field:Column(nullable = true)
        val foundingYear: Int? = null) : Serializable, Comparable<Organisation> {

    override fun compareTo(other: Organisation): Int = this.organisationName.compareTo(other.organisationName)
}
