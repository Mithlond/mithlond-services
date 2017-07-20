/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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
package se.mithlond.services.organisation.model.transport.convenience.membership;

import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Compact Membership list VO, shipping aggregate Memberships
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"organisationName", "organisationJpaID", "memberInformation"})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({SlimMemberVO.class, Address.class})
public class MembershipListVO extends AbstractSimpleTransporter {

    /**
     * The name of the organisation holding these Memberships.
     */
    @XmlElement(required = true)
    private String organisationName;

    /**
     * The jpa ID of the organisation holding these Memberships.
     */
    @XmlAttribute(required = true)
    private Long organisationJpaID;

    /**
     * The SlimMemberVOs wrapped within this {@link MembershipListVO} transporter.
     */
    @XmlElementWrapper
    @XmlElement(name = "memberData")
    private List<SlimMemberVO> memberInformation;

    /**
     * JAXB-friendly constructor.
     */
    public MembershipListVO() {
        this.memberInformation = new ArrayList<>();
    }

    /**
     * Compound constructor creating a MembershiplistVO wrapping the supplied data.
     *
     * @param organisation the organisation for which Memberships should be transported.
     */
    public MembershipListVO(@NotNull Organisation organisation) {

        // Delegate
        this();

        // Assign internal state
        this.organisationName = organisation.getOrganisationName();
        this.organisationJpaID = organisation.getId();
    }

    /**
     * Converts and adds the supplied Memberships to this MembershipListVO.
     *
     * @param toAdd The array of Memberships to add.
     */
    public void add(final Membership... toAdd) {

        if (toAdd != null && toAdd.length > 0) {

            // Only add Memberships within the supplied Organisation.
            Arrays.stream(toAdd)
                    .filter(Objects::nonNull)
                    .filter(m -> m.getOrganisation().getOrganisationName().equals(getOrganisationName()))
                    .map(SlimMemberVO::new)
                    .filter(s -> !memberInformation.contains(s))
                    .forEach(memberInformation::add);
        }
    }

    /**
     * @return The name of the organisation holding the transported Memberships.
     */
    public String getOrganisationName() {
        return organisationName;
    }

    /**
     * @return The jpa ID of the organisation holding the transported Memberships.
     */
    public Long getOrganisationJpaID() {
        return organisationJpaID;
    }

    /**
     * @return The SlimMemberVOs wrapped within this {@link MembershipListVO} transporter.
     */
    public List<SlimMemberVO> getMemberInformation() {
        return memberInformation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return "MembershipListVO [org: " + organisationName + ", jpaID: " + organisationJpaID + "]: "
                + getMemberInformation().stream()
                .sorted()
                .map(SlimMemberVO::toString)
                .reduce((l,r) -> l + ", " + r)
                .orElse("<none>");
    }
}
