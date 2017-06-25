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

import se.jguru.nazgul.core.algorithms.api.Validate;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
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
                query = "select a from Allergy a where a.user.id = :" + OrganisationPatterns.PARAM_USER_ID
                        + " order by a.severity"),
        @NamedQuery(name = Allergy.NAMEDQ_GET_ALL,
                query = "select a from Allergy a order by a.severity, a.user.firstName"),
        @NamedQuery(name = Allergy.NAMEDQ_GET_BY_FOOD_ID,
                query = "select a from Allergy a where a.food.id = :foodID order by a.severity")
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
     * {@link NamedQuery} which retrieves all Allergies for a given {@link Food} (identified by its JPA ID).
     */
    public static final String NAMEDQ_GET_BY_FOOD_ID = "Allergy.getAllergiesByFoodId";

    /**
     * The JPA Version of this Allergy.
     */
    @Version
    @XmlAttribute
    private long version;

    @EmbeddedId
    @XmlTransient
    private AllergyId allergyId;

    /**
     * The Food to which this Allergy refers.
     */
    @ManyToOne
    @MapsId("foodId")
    @XmlElement(required = true)
    private Food food;

    /**
     * The User having this Allergy.
     */
    @ManyToOne(optional = false)
    @MapsId("userId")
    @XmlIDREF
    private User user;

    /**
     * The severity of this Allergy (i.e. of the User towards the Food of this Allergy)
     */
    @OneToOne(optional = false)
    @JoinColumn(nullable = false, name = "severity_id")
    @XmlElement(required = true)
    private AllergySeverity severity;

    /**
     * An optional (i.e. nullable) free-text note of this Allergy.
     */
    @Basic
    @Column
    @XmlElement(nillable = true)
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
    public Allergy(@NotNull final Food food,
                   @NotNull final User user,
                   @NotNull final AllergySeverity severity,
                   final String note) {

        // Check some sanity
        Validate.notNull(food, "food");
        Validate.notNull(user, "user");
        Validate.notNull(severity, "severity");

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

            final String thisFoodName = this.getFood().getLocalizedFoodName().getText() == null
                    ? ""
                    : this.getFood().getLocalizedFoodName().getText();
            final String thatFoodName = that.getFood().getLocalizedFoodName().getText() == null
                    ? ""
                    : that.getFood().getLocalizedFoodName().getText();

            toReturn = thisFoodName.compareTo(thatFoodName);
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
