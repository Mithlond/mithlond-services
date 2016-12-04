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

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.food.Allergy;
import se.mithlond.services.organisation.model.localization.LocaleDefinition;
import se.mithlond.services.organisation.model.localization.Localizable;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransportable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

/**
 * Shallow-detail Allergy transport object.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE,
        propOrder = {"description", "foodName", "severity", "foodJpaID"})
@XmlAccessorType(XmlAccessType.FIELD)
public class AllergyVO extends AbstractSimpleTransportable {

    /**
     * The (localized) Allergy description of this AllergyVO.
     */
    @XmlElement(required = true)
    private String description;

    /**
     * The AllergySeverity description of this AllergyVO.
     */
    @XmlElement(required = true)
    private String severity;

    /**
     * The Food name of this AllergyVO.
     */
    @XmlElement(required = true)
    private String foodName;

    /**
     * The JPA ID of the Food referred to by this AllergyVO.
     */
    @XmlAttribute(required = true)
    private long foodJpaID;

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
     * @param userJpaID   The JPA ID of the User(VO) having this Allergy.
     */
    public AllergyVO(
            final String description,
            final String severity,
            final String foodName,
            final Long foodJpaID,
            final Long userJpaID) {

        // Delegate
        super(userJpaID);

        // Assign internal state
        this.description = description;
        this.severity = severity;
        this.foodName = foodName;
        this.foodJpaID = foodJpaID;
    }

    /**
     * Compound constructor converting the supplied Allergy and the optional LocaleDefinition to
     * a shallow-detail AllergyVO instance.
     *
     * @param allergy          The Allergy to convert.
     * @param localeDefinition The LocaleDefinition to use for describing the allergy severity.
     */
    public AllergyVO(final Allergy allergy, final LocaleDefinition localeDefinition) {

        // Delegate
        super(allergy.getUser().getId());
        Validate.notNull(localeDefinition, "localeDefinition");

        // Assign internal state
        final String classifier = Localizable.DEFAULT_CLASSIFIER;
        this.description = allergy.getFood().getLocalizedFoodName().getText(localeDefinition, classifier)
                + " : "
                + allergy.getSeverity().getFullDescription().getText(localeDefinition, classifier);
        this.severity = allergy.getSeverity().getShortDescription().getText(localeDefinition, classifier);
        this.foodName = allergy.getFood().getLocalizedFoodName().getText(localeDefinition, classifier);
        this.foodJpaID = allergy.getFood().getId();
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
        return Objects.equals(getDescription(), allergyVO.getDescription())
                && Objects.equals(getSeverity(), allergyVO.getSeverity())
                && Objects.equals(getFoodName(), allergyVO.getFoodName())
                && Objects.equals(getFoodJpaID(), allergyVO.getFoodJpaID())
                && Objects.equals(getJpaID(), allergyVO.getJpaID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getDescription(), getSeverity(), getFoodName(), getJpaID());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final AbstractSimpleTransportable cmp) {

        if (cmp instanceof AllergyVO) {

            final AllergyVO that = (AllergyVO) cmp;

            // Check sanity
            if (that == this) {
                return 0;
            }

            // Delegate to internal state
            final String thisDesc = this.getDescription() == null ? "" : this.getDescription();
            final String thatDesc = that.getDescription() == null ? "" : that.getDescription();
            int toReturn = thisDesc.compareTo(thatDesc);

            if (toReturn == 0) {

                final String thisSeverity = this.getSeverity() == null ? "" : this.getSeverity();
                final String thatSeverity = that.getSeverity() == null ? "" : that.getSeverity();
                toReturn = thisSeverity.compareTo(thatSeverity);
            }

            if (toReturn == 0) {
                toReturn = this.getFoodName().compareTo(that.getFoodName());
            }

            // All Done.
            return toReturn;
        }

        // Delegate
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "AllergyVO{"
                + "description='" + description + '\''
                + ", severity='" + severity + '\''
                + ", foodName='" + foodName + '\''
                + ", foodJpaID='" + foodJpaID + '\''
                + ", userJpaID=" + getJpaID()
                + '}';
    }
}
