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

import se.mithlond.services.organisation.domain.model.localization.DEFAULT_CLASSIFIER
import java.io.Serializable

/**
 * Standard Comparator sorting [NamedDescription]s by name and description.
 */
val STANDARD_NAMED_DESCRIPTION_COMPARATOR = Comparator<NamedDescription> { l, r ->

    var toReturn = l.name.compareTo(r.name)

    if(toReturn == 0) {
        toReturn = l.description.compareTo(r.description)
    }

    // All Done
    toReturn
}

/**
 * Standard Comparator sorting [NamedDescription]s which are also [Organisational] by name and description.
 */
val STANDARD_NAMED_ORGANISATIONAL_DESCRIPTION_COMPARATOR = Comparator<NamedDescription> { l, r ->

    var toReturn = 0
    if(l is Organisational && r is Organisational) {
        toReturn = (l as Organisational).organisation.compareTo((r as Organisational).organisation)
    }

    if(toReturn == 0) {
        toReturn = l.name.compareTo(r.name)
    }

    if(toReturn == 0) {
        toReturn = l.description.compareTo(r.description)
    }

    // All Done
    toReturn
}

/**
 * The ClassifiedLocalizedText classification for the Name.
 */
const val NAME_CLASSIFICATION = DEFAULT_CLASSIFIER

/**
 * The ClassifiedLocalizedText classification for the Description of food/category/subCategory.
 */
const val DESCRIPTION_CLASSIFICATION = "Description"

/**
 * Specification for entities with a name and description.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface NamedDescription : Serializable {

    /**
     * The Name of named description objects.
     * Typically a single word, typically used in human-readable menus or listings.
     */
    var name : String

    /**
     * The Description of named description objects.
     * Typically used in human-readable detailed descriptions, and hence longer than a name.
     */
    var description : String
}
