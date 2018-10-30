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

import se.mithlond.services.organisation.domain.model.Category
import se.mithlond.services.organisation.domain.model.CategoryProducer
import java.io.Serializable

/**
 * Enum defining a suite of the most commonly used address classifications, also
 * providing corresponding Category objects for each AddressType.
 * 
 * @author [Lennart J&ouml;relid](mailto:lj@jguru.se), jGuru Europe AB
 */
enum class WellKnownAddressType(private val isMailDeliveryAddress: Boolean = false) : Serializable, CategoryProducer {

    /**
     * The address where you live.
     */
    HOME,

    /**
     * The address where you visit an organisation.
     * Office location, or equivalent.
     */
    VISITING,

    /**
     * The delivery address is the address where mail is sent.
     * This could be a postbox address, if applicable.
     */
    DELIVERY(true),

    /**
     * Activity address to a Pub or Restaurant.
     */
    PUB_RESTAURANT,

    /**
     * Activity address to a Shop or equivalent.
     */
    SHOP,

    /**
     * Activity address to an outdoor place.
     */
    OUTDOORS,

    /**
     * Activity addresses to a Cafe.
     */
    CAFE,

    /**
     * Activity address to a site ("Samlingslokal").
     */
    SITE;

    override val category: Category
        get() {

            val classification = if (isMailDeliveryAddress) "mail_delivery_address" else "visitable_address"
            val categoryName = toString().toLowerCase().trim { it <= ' ' }

            // All done.
            return Category(
                    name = categoryName,
                    classification = classification,
                    description = "Address type [$classification :: $categoryName]")
        }
}
