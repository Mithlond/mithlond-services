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
package se.mithlond.services.organisation.model.transport;

import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransportable;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

/**
 * The SimpleTransportable version of an Organisation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE, propOrder = {"organisationName", "description"})
@XmlAccessorType(XmlAccessType.FIELD)
public class OrganisationVO extends AbstractSimpleTransportable {

    /**
     * The organisation name.
     */
    @NotNull
    @XmlID
    @XmlAttribute(required = true)
    private String organisationName;

    /**
     * A human-readable description of this OrganisationVO
     */
    @NotNull
    @XmlElement(required = true)
    private String description;

    /**
     * JAXB-friendly constructor.
     */
    public OrganisationVO() {
    }

    /**
     * Compound constructor creating an {@link OrganisationVO} wrapping the supplied data.
     *
     * @param jpaID            The JPA ID of the {@link se.mithlond.services.organisation.model.Organisation}
     *                         represented by this {@link OrganisationVO}.
     * @param organisationName The organisation name.
     * @param description      A human-readable description.
     */
    public OrganisationVO(final Long jpaID, final String organisationName, final String description) {
        super(jpaID);

        // Check sanity
        this.organisationName = Validate.notEmpty(organisationName, "organisationName");
        this.description = Validate.notEmpty(description, "description");
    }

    /**
     * Copy constructor transforming an entity {@link Organisation} to a shallow transport {@link OrganisationVO}.
     *
     * @param organisation A non-null Organisation.
     */
    public OrganisationVO(final Organisation organisation) {

        // Check sanity
        Validate.notNull(organisation, "organisation");

        // Assign internal state
        initialize(organisation.getId());
        this.organisationName = organisation.getOrganisationName();
        this.description = organisation.getSuffix();
    }

    /**
     * @return The organisation name.
     */
    public String getOrganisationName() {
        return organisationName;
    }

    /**
     * @return The description of the Organisation being transported.
     */
    public String getDescription() {
        return description;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final AbstractSimpleTransportable cmp) {

        if (cmp instanceof OrganisationVO) {

            final OrganisationVO that = (OrganisationVO) cmp;

            // Check sanity
            if (that == this) {
                return 0;
            }

            // Delegate to normal value
            final String thisOrganisationName = this.organisationName == null ? "" : this.organisationName;
            final String thatOrganisationName = that.organisationName == null ? "" : that.organisationName;

            int toReturn = thisOrganisationName.compareTo(thatOrganisationName);
            if (toReturn == 0) {

                final String thisDescription = this.description == null ? "" : this.description;
                final String thatDescription = that.description == null ? "" : that.description;
                toReturn = thisDescription.compareTo(thatDescription);
            }

            // All done.
            return toReturn;
        }

        // Delegate.
        return super.compareTo(cmp);
    }
}
