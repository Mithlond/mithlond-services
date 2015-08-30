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
