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

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.localization.LocalizedTexts;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Definition of the severity of an Allergy. Severities come in 4 degrees:
 * <ol>
 * <li><strong>NONE</strong>. This implies no allergy.</li>
 * <li><strong>MINOR</strong>. While the substance causes discomfort for the Member, it can occur in food and be
 * managed by the Member him/herself (such as not eating carrots, placed on the served dish).</li>
 * <li><strong>CANNOT_BE_INGESTED</strong>. Major allergy. This substance cannot occur in the food of the
 * Member for any reason. The Member may need medication to handle the conditions arising from this severity.</li>
 * <li><strong>NOT_IN_THE_SAME_ROOM</strong>. Fatal allergy. The substance cannot occur in the same room as the
 * Member, for risk of medical conditions.</li>
 * </ol>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = AllergySeverity.NAMEDQ_GET_ALL,
                query = "select a from AllergySeverity a order by a.severitySortOrder")
})
@Entity
@Access(value = AccessType.FIELD)
@Table(name = "allergy_severity",
        uniqueConstraints = {
                @UniqueConstraint(name = "unique_sort_order", columnNames = {"severity_sort_order"})
        })
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"severitySortOrder", "shortDescription", "fullDescription"})
@XmlAccessorType(XmlAccessType.FIELD)
public class AllergySeverity extends NazgulEntity implements Comparable<AllergySeverity> {

    /**
     * {@link NamedQuery} which retrieves all persisted AllergySeverity objects.
     */
    public static final String NAMEDQ_GET_ALL = "AllergySeverity.getAll";

    /**
     * The sort order of the severity, with less severe allergies having lower severitySortOrder values.
     */
    @Basic(optional = false)
    @Column(nullable = false, name = "severity_sort_order")
    @XmlAttribute(required = true)
    private int severitySortOrder;

    /**
     * A localized texts instance containing the short description of this AllergySeverity.
     */
    @OneToOne(optional = false)
    @JoinColumn(name = "short_description_id")
    @XmlElement(required = true)
    private LocalizedTexts shortDescription;

    /**
     * A localized texts instance containing the full description of this AllergySeverity.
     */
    @OneToOne(optional = false)
    @JoinColumn(name = "full_description_id")
    @XmlElement(required = true)
    private LocalizedTexts fullDescription;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public AllergySeverity() {
    }

    /**
     * Compound constructor creating an {@link AllergySeverity} wrapping the supplied data.
     *
     * @param severitySortOrder The sort order of the severity, with less severe allergies
     *                          having lower severitySortOrder values.
     * @param shortDescription       A localized texts instance containing the description of this AllergySeverity.
     */
    public AllergySeverity(final int severitySortOrder,
            final LocalizedTexts shortDescription,
            final LocalizedTexts fullDescription) {

        this.severitySortOrder = severitySortOrder;
        this.shortDescription = shortDescription;
        this.fullDescription = fullDescription;
    }

    /**
     * @return The sort order of this {@link AllergySeverity}.
     * Less severe allergies have lower severitySortOrder value. Will always contain a unique value greater than zero.
     */
    public int getSeveritySortOrder() {
        return severitySortOrder;
    }

    /**
     * @return A localized texts instance containing the description of this AllergySeverity.
     */
    public LocalizedTexts getShortDescription() {
        return shortDescription;
    }

    /**
     * @return A localized texts instance containing the description of this AllergySeverity.
     */
    public LocalizedTexts getFullDescription() {
        return fullDescription;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final AllergySeverity o) {

        if (o == null) {
            return -1;
        }
        if (o == this) {
            return 0;
        }

        // Handle sorting
        return severitySortOrder - o.severitySortOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException
                .create()
                .notTrue(severitySortOrder <= 0, "Cannot handle zero or negative 'severitySortOrder'. (Got: "
                        + severitySortOrder + ")")
                .notNull(shortDescription, "shortDescription")
                .notNull(fullDescription, "fullDescription")
                .endExpressionAndValidate();
    }
}
