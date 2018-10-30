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
 * Specification for entities which should be able to be listed, holding standard descriptions.
 *
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
interface Listable : Serializable {

    /**
     * The non-empty short description of this Listable entity.
     * Typically used within short info boxes and pop-ups.
     *
     * @return The short description of this [Listable]
     */
    var shortDesc: String

    /**
     * The full description of this Listable entity.
     * May not be empty. Typically used within information detail or
     * longer info boxes or modal description displays.
     *
     * @return The full description of this [Listable]
     */
    var fullDesc: String

    /**
     * The reference to the organisation within which this Listable exists.
     *
     * @return The [Organisation] owning this [Listable]
     */
    var owningOrganisation: Organisation
}
