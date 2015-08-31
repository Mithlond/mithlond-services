/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 Mithlond
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
package se.mithlond.services.organisation.model.membership;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;

/**
 * JPA compound key class for Membership-to-Group relationship mappings.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Embeddable
@Access(value = AccessType.FIELD)
@XmlTransient
public class GroupMembershipId implements Serializable {

    private static final long serialVersionUID = 8829995L;

    // Shared state
    @Column(name = "group_id")
    public long groupId;

    @Column(name = "membership_id")
    public long membershipId;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public GroupMembershipId() {
    }

    /**
     * Compound key creating a GroupMembershipId object from the supplied keys.
     *
     * @param groupId      The id of the Group to which a Membership should be added.
     * @param membershipId The id of the Membership to add to the Guild given.
     */
    public GroupMembershipId(final long groupId, final long membershipId) {
        this.groupId = groupId;
        this.membershipId = membershipId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        if (obj instanceof GroupMembershipId) {

            final GroupMembershipId that = (GroupMembershipId) obj;
            return groupId == that.groupId && membershipId == that.membershipId;
        }

        // All done.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return (int) (groupId + membershipId) % Integer.MAX_VALUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "GroupMembershipId [Group: " + groupId + ", Membership: " + membershipId + "]";
    }
}
