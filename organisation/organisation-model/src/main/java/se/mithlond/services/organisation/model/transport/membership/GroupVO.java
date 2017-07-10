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
package se.mithlond.services.organisation.model.transport.membership;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.transport.AbstractOrganisationalVO;
import se.mithlond.services.organisation.model.transport.OrganisationVO;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * The SimpleTransportable version of a Group.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE,
        propOrder = {"parentGroupName", "description", "isGuild"})
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupVO extends AbstractOrganisationalVO {

    /**
     * An optional short description of this Group.
     */
    @XmlElement
    private String description;

    /**
     * The optional name for the parent GroupVO of this GroupVO.
     */
    @XmlElement
    private String parentGroupName;

    /**
     * Indicates if the Group of this GroupVO is a Guild (true), or a Group (false/null).
     */
    @XmlAttribute
    private Boolean isGuild;

    /**
     * JAXB-friendly constructor.
     */
    public GroupVO() {
    }

    /**
     * Compound constructor creating a GroupVO wrapping the supplied data.
     *
     * @param jpaID           The JPA ID of this {@link AbstractOrganisationalVO}. Null implies a new one.
     * @param organisation    The Organisation owning this {@link AbstractOrganisationalVO}.
     * @param name            The name of this {@link AbstractOrganisationalVO}; this should be decently
     *                        unique per instance.
     * @param parentGroupName The optional name for the parent GroupVO of this GroupVO.
     * @param description     An optional short description of this Group.
     */
    public GroupVO(final Long jpaID,
                   final OrganisationVO organisation,
                   final String name,
                   final String parentGroupName,
                   final String description,
                   final boolean isGuild) {

        super(jpaID, organisation, name);

        // Assign internal state
        this.parentGroupName = parentGroupName;
        this.description = description;

        if (isGuild) {
            this.isGuild = true;
        }
    }

    /**
     * Copy constructor creating a GroupVO from the data in the supplied Group.
     *
     * @param aGroup A non-null Group.
     */
    public GroupVO(final Group aGroup) {

        // Check sanity
        Validate.notNull(aGroup, "aGroup");

        // Assign internal state
        initialize(aGroup.getId(), new OrganisationVO(aGroup.getOrganisation()), aGroup.getGroupName());
        this.parentGroupName = aGroup.getParent() != null ? aGroup.getParent().getGroupName() : null;
        this.description = aGroup.getDescription();
        if (aGroup instanceof Guild) {
            this.isGuild = true;
        }
    }

    /**
     * @return An optional short description of this Group.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The optional name for the parent GroupVO of this GroupVO.
     */
    public String getParentGroupName() {
        return parentGroupName;
    }

    /**
     * @return {@code true} if this GroupVO represents a {@link Guild} (rather than "only" a {@link Group}).
     */
    public boolean isGuild() {
        return isGuild != null && isGuild;
    }
}
