/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
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
package se.mithlond.services.organisation.model.food;

import se.mithlond.services.organisation.model.OrganisationPatterns;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Many-to-many relation key between Food-related Categories and Members.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Embeddable
@Access(value = AccessType.FIELD)
@XmlTransient
@XmlType(namespace = OrganisationPatterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class FoodPreferenceId implements Serializable {

    private static final long serialVersionUID = 8829999L;

    // Shared state
    @Column(name = "category_id")
    public long categoryId;

    @Column(name = "internaluser_id")
    public long userId;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public FoodPreferenceId() {
    }

    /**
     * Creates a new FoodPreferenceId wrapping the supplied data.
     *
     * @param userID     The id of the User having an allergy to the given FoodId.
     * @param categoryId The id of the Category to which a Member has a FoodPreference.
     */
    public FoodPreferenceId(final long categoryId, final long userID) {
        this.categoryId = categoryId;
        this.userId = userID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if (obj instanceof FoodPreferenceId) {

            final FoodPreferenceId that = (FoodPreferenceId) obj;
            return categoryId == that.categoryId && userId == that.userId;
        }

        // All done.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) (categoryId + userId) % Integer.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "FoodPreferenceId [Category: " + categoryId + ", User: " + userId + "]";
    }
}
