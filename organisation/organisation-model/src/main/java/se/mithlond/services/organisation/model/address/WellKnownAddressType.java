/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 Mithlond
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
package se.mithlond.services.organisation.model.address;

import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.CategoryProducer;
import se.mithlond.services.organisation.model.OrganisationPatterns;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * Enum defining a suite of the most commonly used address classifications, also
 * providing corresponding Category objects for each AddressType.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.NAMESPACE)
@XmlEnum(String.class)
public enum WellKnownAddressType implements CategoryProducer {

    /**
     * The address where you live.
     */
    HOME(false),

    /**
     * The address where you visit an organisation.
     * Office location, or equivalent.
     */
    VISITING(false),

    /**
     * The delivery address is the address where mail is sent.
     * This could be a postbox address, if applicable.
     */
    DELIVERY(true),

    /**
     * Activity address to a Pub or Restaurant.
     */
    PUB_RESTAURANT(false),

    /**
     * Activity address to a Shop or equivalent.
     */
    SHOP(false),

    /**
     * Activity address to an outdoor place.
     */
    OUTDOORS(false),

    /**
     * Activity addresses to a Cafe.
     */
    CAFE(false),

    /**
     * Activity address to a site ("Samlingslokal").
     */
    SITE(false);

    // Internal state
    private boolean mailDeliveryAddress;

    /**
     * Compound constructor, creating a WellKnownAddressType wrapping the supplied data.
     *
     * @param mailDeliveryAddress if {@code true}, this WellKnownAddressType represents
     *                            a mail delivery address only.
     */
    WellKnownAddressType(final boolean mailDeliveryAddress) {
        this.mailDeliveryAddress = mailDeliveryAddress;
    }

    /**
     * @return {@code true} to indicate that this WellKnownAddressType represents a mail delivery address only.
     */
    public boolean isMailDeliveryAddress() {
        return mailDeliveryAddress;
    }

    /**
     * {@inheritDoc}
     */
    public Category getCategory() {

        final String classification = isMailDeliveryAddress() ? "mail_delivery_address" : "visitable_address";
        final String categoryID = toString().toLowerCase().trim();

        // All done.
        return new Category(
                categoryID,
                classification,
                "Address type [" + classification + " :: " + categoryID + "]");
    }
}
