/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
package se.mithlond.services.organisation.model;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

/**
 * Abstract superclass for entities which should be able to be listed, holding standard descriptions.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MappedSuperclass
@Access(value = AccessType.FIELD)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "oneShortDescPerOrganisation", columnNames = {"shortDesc", "owningorganisation_id"})
})
@XmlType(propOrder = {"shortDesc", "fullDesc", "owningOrganisation"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Listable extends NazgulEntity {

    // Internal state
    @Basic(optional = false)
    @Column(nullable = false, length = 255)
    private String shortDesc;

    @Basic(optional = false)
    @Column(nullable = false, length = 2048)
    private String fullDesc;

    @XmlAttribute(required = true, name = "organisationReference")
    @XmlIDREF
    @ManyToOne(optional = false)
    private Organisation owningOrganisation;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public Listable() {
    }

    /**
     * Compound constructor creating a Listable entity that wraps the provided data.
     *
     * @param shortDesc    The mandatory and non-empty short description of this Listable entity.
     * @param fullDesc     The full description of this entity (up to 2048 chars), visible in detailed listings.
     *                     May not be null or empty.
     * @param organisation The organisation within which this Listable exists.
     */
    public Listable(final String shortDesc,
                    final String fullDesc,
                    final Organisation organisation) {

        // Assign internal state
        this.shortDesc = shortDesc;
        this.fullDesc = fullDesc;
        this.owningOrganisation = organisation;
    }

    /**
     * @return The mandatory and non-empty short description of this entity.
     */
    public String getShortDesc() {
        return shortDesc;
    }

    /**
     * @return The full description of this entity, visible in detailed listings.
     */
    public String getFullDesc() {
        return fullDesc;
    }

    /**
     * Assigns the fullDesc of this Listable.
     *
     * @param fullDesc the non-null fullDesc of this Listable.
     */
    public void setFullDesc(final String fullDesc) {

        // Check sanity
        Validate.notEmpty(fullDesc, "fullDesc");

        // Assign internal state
        this.fullDesc = fullDesc;
    }

    /**
     * Assigns the shortDesc of this Listable.
     *
     * @param shortDesc the shortDesc of this Listable.
     */
    public void setShortDesc(final String shortDesc) {

        // Check sanity
        Validate.notEmpty(shortDesc, "Cannot handle null or empty shortDesc argument.");

        // Assign internal state
        this.shortDesc = shortDesc;
    }

    /**
     * @return The organisation within which this Activity takes place.
     */
    public Organisation getOwningOrganisation() {
        return owningOrganisation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected final void validateEntityState() throws InternalStateValidationException {

        // Check sanity
        InternalStateValidationException.create()
                .notNullOrEmpty(shortDesc, "shortDesc")
                .notNullOrEmpty(fullDesc, "fullDesc")
                .notNull(owningOrganisation, "owningOrganisation")
                .endExpressionAndValidate();

        // Delegate
        validateListableEntityState();
    }

    /**
     * Override this method to perform validation of the entity internal state of this Listable.
     *
     * @throws InternalStateValidationException if the state of this Listable was in
     *                                          an incorrect state (i.e. invalid).
     */
    protected abstract void validateListableEntityState() throws InternalStateValidationException;
}
