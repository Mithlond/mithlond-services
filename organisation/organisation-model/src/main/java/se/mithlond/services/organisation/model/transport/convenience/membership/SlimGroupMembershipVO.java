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

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.membership.GroupMembership;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransportable;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Slimmed down version of the GroupMembership Entity.
 * 
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"parentXmlId"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SlimGroupMembershipVO extends AbstractSimpleTransportable implements Validatable {

    /**
     * The optional parent (Group) XML ID of this SlimGroupMembershipVO.
     */
    @XmlAttribute
    private String parentXmlId;

    /**
     * JAXB-friendly constructor.
     */
    public SlimGroupMembershipVO() {
    }

    /**
     * Compound constructor creating a SlimGroupMembershipVO wrapping the supplied Group.
     *
     * @param aGroupMembership A GroupMembership to convert into a SlimGroupMembershipVO.
     */
    public SlimGroupMembershipVO(@NotNull final GroupMembership aGroupMembership) {

        // Delegate
        super(aGroupMembership.getGroup().getId(), aGroupMembership.getGroup().getXmlId());

        // Assign internal state
        final String parentXmlID = aGroupMembership.getGroup().getParentXmlID();
        if(parentXmlID != null && !parentXmlID.isEmpty()) {
            this.parentXmlId = parentXmlID;
        }
    }

    /**
     * Retrieves the optional (i.e. nullable) Parent XML ID of this Group.
     *
     * @return the optional (i.e. nullable) Parent XML ID of this Group.
     */
    public String getParentXmlId() {
        return parentXmlId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString()
                + ", parentXmlId='" + parentXmlId + '\'';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(xmlId, "xmlId")
                .endExpressionAndValidate();
    }
}
