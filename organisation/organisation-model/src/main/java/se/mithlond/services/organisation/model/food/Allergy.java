/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.user.User;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Allergy entity, relating a Food and a Severity to a Member.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = Allergy.NAMEDQ_GET_BY_USERID,
                query = "select a from Allergy a where a.user.id = ?1 order by a.severity"),
        @NamedQuery(name = Allergy.NAMEDQ_GET_ALL,
                query = "select a from Allergy a order by a.severity"),
        @NamedQuery(name = Allergy.NAMEDQ_GET_BY_FOODNAME,
                query = "select a from Allergy a where a.food.foodName like ?1 order by a.severity")
})
@Entity
@Access(value = AccessType.FIELD)
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"food", "severity", "note", "user", "version"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Allergy implements Serializable, Comparable<Allergy>, Validatable {

    /**
     * {@link NamedQuery} which retrieves all Allergies for a specific {@link User} (identified by the UserID).
     */
    public static final String NAMEDQ_GET_BY_USERID = "Allergy.getAllergiesByUserID";

    /**
     * {@link NamedQuery} which retrieves all Allergies.
     */
    public static final String NAMEDQ_GET_ALL = "Allergy.getAll";

    /**
     * {@link NamedQuery} which retrieves all Allergies for a given {@link Food} (identified by its name).
     */
    public static final String NAMEDQ_GET_BY_FOODNAME = "Allergy.getAllergiesByFoodName";

    // Internal state
    @Version
    @XmlAttribute(required = false)
    private long version;

    @EmbeddedId
    @XmlTransient
    private AllergyId allergyId;

    @ManyToOne
    @MapsId("foodId")
    @XmlElement(required = true, nillable = false)
    private Food food;

    @ManyToOne(optional = false)
    @MapsId("userId")
    @XmlIDREF
    private User user;

    @OneToOne(optional = false)
    @XmlElement(required = true)
    private AllergySeverity severity;

    @Basic(optional = true) @Column(nullable = true)
    @XmlElement(required = false, nillable = true)
    private String note;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public Allergy() {
    }

    /**
     * Compound constructor creating an Allergy wrapping the supplied data.
     *
     * @param food     The Food to which this Allergy pertains.
     * @param user     The User being allergic.
     * @param severity The severity of this allergy.
     * @param note     An optional note for this Allergy.
     */
    public Allergy(final Food food, final User user, final AllergySeverity severity, final String note) {

        // Check some sanity
        Validate.notNull(food, "Cannot handle null food argument.");
        Validate.notNull(user, "Cannot handle null user argument.");
        Validate.notNull(severity, "Cannot handle null severity argument.");

        // Assign internal state
        this.food = food;
        this.user = user;
        this.severity = severity;
        this.note = note;

        this.allergyId = new AllergyId(food.getId(), user.getId());
    }

    /**
     * @return the Database-generated version/revision of this Entity.
     */
    public long getVersion() {
        return version;
    }

    /**
     * @return The AllergyId which combines the IDs of the Food and Member parts.
     */
    public AllergyId getAllergyId() {
        return allergyId;
    }

    /**
     * @return The Food to which this Allergy pertains.
     */
    public Food getFood() {
        return food;
    }

    /**
     * @return The Member being allergic.
     */
    public User getUser() {
        return user;
    }

    /**
     * @return The severity of this Allergy.
     */
    public AllergySeverity getSeverity() {
        return severity;
    }

    /**
     * Assigns the severity of this Allergy.
     *
     * @param severity A non-null severity for this Allergy.
     */
    public void setSeverity(final AllergySeverity severity) {

        // Check sanity
        Validate.notNull(severity, "Cannot handle null severity argument.");

        // Assign internal state
        this.severity = severity;
    }

    /**
     * @return An optional note for this Allergy.
     */
    public String getNote() {
        return note;
    }

    /**
     * Assigns a new note to this Allergy - or removes the supplied one by assigning {@code null}.
     *
     * @param note An optional note for this Allergy.
     */
    public void setNote(final String note) {
        this.note = note;
    }

    /**
     * Compares the Severity, Food name and Member login - but ignores the note for the sake of comparing.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Allergy that) {

        // Check sanity
        if (that == null) {
            return -1;
        } else if (that == this) {
            return 0;
        }

        // Delegate to internal state comparison.
        // Ignore the note for the purposes of the comparison.
        int toReturn = getSeverity().compareTo(that.getSeverity());
        if (toReturn == 0) {
            toReturn = getFood().getFoodName().compareTo(that.getFood().getFoodName());
        }
        if (toReturn == 0) {
            final long thisUserID = getUser() != null ? getUser().getId() : -1L;
            final long thatUserID = that.getUser() != null ? that.getUser().getId() : -1L;
            toReturn = (int) (thisUserID - thatUserID);
        }

        // All done.
        return toReturn;
    }

    /**
     * The combination of Food and Member should be unique, so
     * hashCode and equality calculations use only those properties.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object that) {

        // Check sanity
        if (this == that) {
            return true;
        }
        if (!(that instanceof Allergy)) {
            return false;
        }

        // Delegate
        final Allergy thatAllergy = (Allergy) that;
        return this.hashCode() == thatAllergy.hashCode();
    }

    /**
     * The combination of Food and Member should be unique, so
     * hashCode and equality calculations use only those properties.
     * <p/>
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return food.hashCode() + user.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(food, "food")
                .notNull(user, "user")
                .notNull(severity, "severity")
                .endExpressionAndValidate();
    }
}
