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


/**
 * Specification for a suite of types which may contain (and, hence, produce) a Category.
 *
 * @author [Lennart Jrelid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface CategoryProducer : Serializable {

    /**
     * Retrieves the default Category of this CategoryProducer.
     *
     * @return the default or standard Category of this CategoryProducer.
     */
    val category: Category

    /**
     * Generates a Category with the supplied description.
     *
     * @param description The description of the Category retrieved from this CategoryProducer.
     * @return the standard Category of this CategoryProducer, with the supplied description assigned.
     */
    fun createCategoryWithDescription(description: String): Category {

        // Get the standard Category
        val prototype = category

        // Return a clone with the supplied description.
        return Category(name = prototype.name,
                classification = prototype.classification,
                description = description)
    }
}
