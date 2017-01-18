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
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * JAXB transporter for Organisations and OrganisationVOs.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE, propOrder = {"organisations", "organisationVOs"})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({Organisation.class, OrganisationVO.class})
public class Organisations extends AbstractSimpleTransporter {

    /**
     * A List of full/detailed {@link Organisation} objects.
     */
    @XmlElementWrapper
    @XmlElement(name = "organisation")
    private SortedSet<Organisation> organisations;

    /**
     * A List of shallow/VO {@link OrganisationVO} objects.
     */
    @XmlElementWrapper
    @XmlElement(name = "organisationVO")
    private SortedSet<OrganisationVO> organisationVOs;

    /**
     * JAXB-friendly constructor.
     */
    public Organisations() {
        this.organisations = new TreeSet<>();
        this.organisationVOs = new TreeSet<>();
    }

    /**
     * Convenience constructor creating an {@link Organisations} transport wrapping the supplied detailed
     * Organisation entities.
     *
     * @param organisations The Organisation instances to transport.
     */
    public Organisations(final Organisation... organisations) {

        this();

        if (organisations != null) {
            this.organisations.addAll(Arrays.asList(organisations));
        }
    }

    /**
     * Convenience constructor creating an {@link Organisations} transport wrapping the supplied shallow
     * OrganisationVO entities.
     *
     * @param organisationVOs The OrganisationVOs instances to transport.
     */
    public Organisations(final OrganisationVO... organisationVOs) {

        this();

        if (organisationVOs != null) {
            this.organisationVOs.addAll(Arrays.asList(organisationVOs));
        }
    }

    /**
     * @return The detailed {@link Organisation}s transported.
     */
    public SortedSet<Organisation> getOrganisations() {
        return organisations;
    }

    /**
     * @return The shallow {@link OrganisationVO}s transported.
     */
    public SortedSet<OrganisationVO> getOrganisationVOs() {
        return organisationVOs;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString() + " containing " + organisations.size()
                + " detailed, and " + organisationVOs.size()
                + " shallow representations.";
    }
}
