/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
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
package se.mithlond.services.content.model.articles;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.core.xmlbinding.api.XmlBinder;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDateTime;

/**
 * Abstract superclass of all text which should be timestamped in a simple way, defining
 * both when (and by whom) the text was created. Also sporting an optional last updated time and Membership.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MappedSuperclass
@XmlType(namespace = XmlBinder.CORE_NAMESPACE, propOrder = {"created", "createdBy", "lastUpdated", "lastUpdatedBy"})
@XmlAccessorType(XmlAccessType.FIELD)
@Access(value = AccessType.FIELD)
public abstract class AbstractTimestampedText extends NazgulEntity {

    /**
     * The timestamp when this AbstractTimestampedText was created.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @XmlAttribute(required = true)
    private LocalDateTime created;

    /**
     * The Membership who created/persisted this AbstractTimestampedText.
     */
    @Column(nullable = false)
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @XmlElement(required = true)
    private Membership createdBy;

    /**
     * An optional/nullable timestamp when this AbstractTimestampedText was last updated.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @XmlAttribute
    private LocalDateTime lastUpdated;

    /**
     * The optional/nullable Membership who last updated this AbstractTimestampedText.
     */
    @JoinColumn
    @ManyToOne(fetch = FetchType.EAGER)
    @XmlElement
    private Membership lastUpdatedBy;

    /**
     * JAXB-friendly constructor.
     */
    public AbstractTimestampedText() {
    }

    /**
     * Compound constructor, creating an AbstractTimestampedText wrapping the supplied data.
     *
     * @param created   The timestamp when this AbstractTimestampedText was (initially) created.
     * @param createdBy The Membership who (initially) created this AbstractTimestampedText.
     */
    protected AbstractTimestampedText(final LocalDateTime created, final Membership createdBy) {

        // Assign internal state
        this.created = created;
        this.createdBy = createdBy;
    }

    /**
     * Setter metadata method to indicate that this AbstractTimestampedText has been updated.
     *
     * @param lastUpdated   The timestamp when this AbstractTimestampedText was updated.
     *                      If {@code null}, the current timestamp is substituted.
     * @param lastUpdatedBy The non-null Membership updating this AbstractTimestampedText.
     */
    public void setUpdated(final LocalDateTime lastUpdated, final Membership lastUpdatedBy) {

        // Check sanity and assign internal state.
        this.lastUpdatedBy = Validate.notNull(lastUpdatedBy, "Cannot handle null 'lastUpdatedBy' argument.");
        this.lastUpdated = lastUpdated == null ? LocalDateTime.now() : lastUpdated;
    }

    /**
     * Retrieves the timestamp when this AbstractTimestampedText was initially created.
     *
     * @return the timestamp when this AbstractTimestampedText was initially created.
     */
    public LocalDateTime getCreated() {
        return created;
    }

    /**
     * Retrieves the Membership who created/persisted this AbstractTimestampedText.
     *
     * @return The Membership who created/persisted this AbstractTimestampedText.
     */
    public Membership getCreatedBy() {
        return createdBy;
    }

    /**
     * Retrieves an optional/nullable timestamp when this AbstractTimestampedText was last updated.
     *
     * @return an optional/nullable timestamp when this AbstractTimestampedText was last updated.
     */
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    /**
     * Retrieves the optional/nullable Membership who last updated this AbstractTimestampedText.
     *
     * @return the optional/nullable Membership who last updated this AbstractTimestampedText.
     */
    public Membership getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(created, "created")
                .notNull(createdBy, "createdBy")
                .endExpressionAndValidate();
    }
}
