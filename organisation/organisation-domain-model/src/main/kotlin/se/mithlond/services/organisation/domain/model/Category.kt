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

import java.io.Serializable
import javax.persistence.Access
import javax.persistence.AccessType
import javax.persistence.Basic
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.SequenceGenerator
import javax.persistence.Table
import javax.persistence.UniqueConstraint

/**
 * Entity definition of a Category with a Classification and a trivial Description.
 *
 * @param name The name/short description, typically a single word. Cannot be null or empty.
 * @param classification A classification of this name, such as "Restaurant". This is intended to simplify
 * separating a type of Categories from others.
 * @param description The (fuller/richer) description of this Category. Cannot be null or empty.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
@Entity
@Access(AccessType.FIELD)
@Table(schema = "organisations", uniqueConstraints = [
    UniqueConstraint(name = "unq_classification_per_name", columnNames = ["name", "classification"])
])
data class Category @JvmOverloads constructor(

        @field:Id
        @field:GeneratedValue(strategy = GenerationType.IDENTITY, generator = "seq_Organisation")
        @field:SequenceGenerator(schema = "organisations", name = "seq_Organisation",
                sequenceName = "seq_Organisation", allocationSize = 1)
        @field:Column(name = "id", updatable = false, nullable = false)
        var id: Long? = null,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 128)
        val name: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false, length = 128)
        val classification: String,

        @field:Basic(optional = false)
        @field:Column(nullable = false)
        val description: String) : Serializable, Comparable<Category> {

    override fun compareTo(other: Category): Int {

        // Check sanity
        if (other === this) {
            return 0
        }

        // Compare the state
        var toReturn = classification.compareTo(other.classification)
        if (toReturn == 0) {
            toReturn = name.compareTo(name)
        }
        if (toReturn == 0) {
            toReturn = description.compareTo(description)
        }

        // All done.
        return toReturn
    }
}
