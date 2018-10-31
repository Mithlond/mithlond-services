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
package se.mithlond.services.organisation.domain.model.food

import se.mithlond.services.organisation.domain.model.Category
import java.io.Serializable
import javax.persistence.CascadeType
import javax.persistence.ManyToOne

/**
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
data class Food(

        /**
         * The top-level Categorisation of this Food.
         */
        @ManyToOne(optional = false, cascade = arrayOf(CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH))
        var category: Category,

        /**
         * The sub-level Categorisation of this Food.
         */
        @ManyToOne(optional = false, cascade = arrayOf(CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH))
        var subCategory: Category

) : Serializable, Comparable<Food> {
    override fun compareTo(other: Food): Int {

        var toReturn = this.category.compareTo(other.category)

        if (toReturn == 0) {
            toReturn = this.subCategory.compareTo(other.subCategory)
        }

        // TODO: Add the locale

        // All Done.
        return toReturn
    }
}
