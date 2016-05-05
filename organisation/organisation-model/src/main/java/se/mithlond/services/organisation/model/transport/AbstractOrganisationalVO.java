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

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransportable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

/**
 * Abstract VO implementation for something owned by an Organisation and having a reasonably unique name.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE, propOrder = {"organisation", "name"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractOrganisationalVO extends AbstractSimpleTransportable {

    /**
     * The Organisation owning this {@link AbstractOrganisationalVO}.
     */
    @XmlIDREF
    @XmlElement(required = true)
    private OrganisationVO organisation;

    /**
     * The name of this {@link AbstractOrganisationalVO}.
     */
    @XmlElement(required = true)
    private String name;

    /**
     * JAXB-friendly constructor.
     */
    public AbstractOrganisationalVO() {
    }

    /**
     * Compound constructor creating an {@link AbstractOrganisationalVO} wrapping the supplied data.
     *
     * @param jpaID        The JPA ID of this {@link AbstractOrganisationalVO}. Null implies a new one.
     * @param organisation The Organisation owning this {@link AbstractOrganisationalVO}.
     * @param name         The name of this {@link AbstractOrganisationalVO}; this should be decently
     *                     unique per instance.
     */
    public AbstractOrganisationalVO(final Long jpaID, final OrganisationVO organisation, final String name) {
        this();
        initialize(jpaID, organisation, name);
    }

    /**
     * Initializes this {@link AbstractOrganisationalVO} with the supplied values.
     *
     * @param jpaID        The JPA ID of this {@link AbstractOrganisationalVO}.
     * @param organisation The Organisation owning this {@link AbstractOrganisationalVO}.
     * @param name         The name of this {@link AbstractOrganisationalVO}; this should be decently
     *                     unique per instance.
     */
    protected final void initialize(final Long jpaID, final OrganisationVO organisation, final String name) {

        // Delegate
        super.initialize(jpaID);

        // Assign internal state
        this.organisation = organisation;
        this.name = name;
    }

    /**
     * @return The Organisation owning this {@link AbstractOrganisationalVO}.
     */
    public OrganisationVO getOrganisation() {
        return organisation;
    }

    /**
     * @return The name of this {@link AbstractOrganisationalVO}; this should be decently unique per instance.
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {

        // Fail fast
        if (this == o) {
            return true;
        }
        if (!(o instanceof AbstractOrganisationalVO)) {
            return false;
        }

        // #1) Compare class and JPA ID.
        final AbstractOrganisationalVO that = (AbstractOrganisationalVO) o;
        if (!super.equals(that)) {
            return false;
        }

        // #2) Compare internal state.
        return Objects.equals(getOrganisation(), that.getOrganisation())
                && Objects.equals(getName(), that.getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getOrganisation(), getName());
    }
}
