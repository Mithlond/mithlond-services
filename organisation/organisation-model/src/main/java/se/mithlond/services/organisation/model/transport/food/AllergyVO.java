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
package se.mithlond.services.organisation.model.transport.food;

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.food.Allergy;
import se.mithlond.services.organisation.model.localization.Localizable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Locale;
import java.util.Objects;

/**
 * Shallow-detail Allergy transport object.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE,
        propOrder = {"description", "foodName", "severity", "foodJpaID", "note", "userID"})
@XmlAccessorType(XmlAccessType.FIELD)
public class AllergyVO implements Serializable, Validatable, Comparable<AllergyVO> {

    /**
     * The (localized) Allergy description of this AllergyVO.
     * Should be passed from the server (on fetch), but not
     * necessarily from the client (on update).
     */
    @XmlElement
    private String description;

    /**
     * The AllergySeverity description of this AllergyVO.
     */
    @XmlElement(required = true)
    private String severity;

    /**
     * An optional note for this AllergyVO
     */
    @XmlElement
    private String note;

    /**
     * The Food name of this AllergyVO.
     * Should be passed from the server (on fetch), but not
     * necessarily from the client (on update)
     */
    @XmlElement
    private String foodName;

    /**
     * The JPA ID of the Food referred to by this AllergyVO.
     */
    @XmlAttribute(required = true)
    private long foodJpaID;

    /**
     * The JPA ID of the user having this AllergyVO.
     */
    @XmlAttribute(required = true)
    private long userID;

    /**
     * JAXB-friendly constructor.
     */
    public AllergyVO() {
    }

    /**
     * Compound constructor
     *
     * @param description The (localized) Allergy description of this AllergyVO.
     * @param severity    The AllergySeverity description of this AllergyVO.
     * @param foodName    The Food name of this AllergyVO.
     * @param note        An optional note of this AllergyVO.
     * @param foodJpaID   The JPA ID of the food.
     * @param userJpaID   The JPA ID of the User(VO) having this Allergy.
     */
    public AllergyVO(
            final String description,
            final String severity,
            final String foodName,
            final String note,
            final Long foodJpaID,
            final Long userJpaID) {

        // Assign internal state
        this.description = description;
        this.severity = severity;
        this.foodName = foodName;
        this.note = note;
        this.foodJpaID = foodJpaID;
        this.userID = userJpaID;
    }

    /**
     * Compound constructor converting the supplied Allergy and the optional LocaleDefinition to
     * a shallow-detail AllergyVO instance.
     *
     * @param allergy The Allergy to convert.
     * @param locale  The Locale describing the language/localization of the allergy severity.
     */
    public AllergyVO(final Allergy allergy, final Locale locale) {

        this(
                allergy.getSeverity().getFullDescription().getText(locale, Localizable.DEFAULT_CLASSIFIER),
                allergy.getSeverity().getShortDescription().getText(locale, Localizable.DEFAULT_CLASSIFIER),
                allergy.getFood().getLocalizedFoodName().getText(locale, Localizable.DEFAULT_CLASSIFIER),
                allergy.getNote(),
                allergy.getFood().getId(),
                allergy.getUser().getId()
        );
    }

    /**
     * @return The (localized) Allergy description of this AllergyVO.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The AllergySeverity description of this AllergyVO.
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * @return The Food name of this AllergyVO.
     */
    public String getFoodName() {
        return foodName;
    }

    /**
     * @return The JPA ID of the Food referred to by this Allergy.
     */
    public Long getFoodJpaID() {
        return foodJpaID;
    }

    /**
     * @return The JPA ID of the User having this AllergyVO.
     */
    public long getUserID() {
        return userID;
    }

    /**
     * @return An optional/nullable note of this AllergyVO - written by the user, typically.
     */
    public String getNote() {
        return note;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getDescription(), getSeverity(), getNote(), getFoodName(), getFoodJpaID(), userID);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final AllergyVO allergyVO = (AllergyVO) o;
        return Objects.equals(getSeverity(), allergyVO.getSeverity())
                && Objects.equals(getFoodName(), allergyVO.getFoodName())
                && Objects.equals(getFoodJpaID(), allergyVO.getFoodJpaID())
                && Objects.equals(getNote(), allergyVO.getNote())
                && Objects.equals(getUserID(), allergyVO.getUserID());
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(final AllergyVO that) {

        // Check sanity
        if (that == this) {
            return 0;
        } else if (that == null) {
            return -1;
        }

        // Delegate to internal state
        int toReturn = (int) (this.getFoodJpaID() - that.getFoodJpaID());

        if (toReturn == 0) {
            toReturn = (int) (getUserID() - that.getUserID());
        }

        if (toReturn == 0) {

            final String thisSeverity = this.getSeverity() == null ? "" : this.getSeverity();
            final String thatSeverity = that.getSeverity() == null ? "" : that.getSeverity();
            toReturn = thisSeverity.compareTo(thatSeverity);
        }

        if (toReturn == 0) {
            final String thisNote = this.getNote() == null ? "" : this.getNote();
            final String thatNote = that.getNote() == null ? "" : that.getNote();
            toReturn = thisNote.compareTo(thatNote);
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(severity, "severity")
                .endExpressionAndValidate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "AllergyVO{"
                + "description='" + description + '\''
                + ", severity='" + severity + '\''
                + ", note='" + note + '\''
                + ", foodName='" + foodName + '\''
                + ", foodJpaID='" + foodJpaID + '\''
                + ", userID=" + userID
                + '}';
    }
}
