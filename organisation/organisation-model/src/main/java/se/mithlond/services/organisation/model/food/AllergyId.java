/*
 * #%L
 * Nazgul Project: mithlond-organisation-model
 * %%
 * Copyright (C) 2010 - 2014 jGuru Europe AB
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

import se.jguru.nazgul.mithlond.service.organisation.model.Organisation;

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
 * Many-to-many relation key between Allergy and Member.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Embeddable
@Access(value = AccessType.FIELD)
@XmlTransient
@XmlType(namespace = Organisation.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class AllergyId implements Serializable {

    private static final long serialVersionUID = 8829998L;

    // Shared state
    @Column(name = "food_id")
    public long foodId;

    @Column(name = "member_id")
    public long memberId;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public AllergyId() {
    }

    /**
     * Compound key creating a GuildMembershipId object from the supplied keys.
     *
     * @param foodId   The id of the Food to which a Member has an allergy.
     * @param memberId The id of the Member having an allergy to the given FoodId.
     */
    public AllergyId(final long foodId, final long memberId) {
        this.foodId = foodId;
        this.memberId = memberId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if (obj instanceof AllergyId) {

            final AllergyId that = (AllergyId) obj;
            return foodId == that.foodId && memberId == that.memberId;
        }

        // All done.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) (foodId + memberId) % Integer.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "AllergyId [Food: " + foodId + ", Member: " + memberId + "]";
    }
}
