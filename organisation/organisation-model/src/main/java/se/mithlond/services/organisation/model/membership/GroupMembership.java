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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.authorization.model.AuthorizationPath;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPathProducer;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Version;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Relates a Membership to a Group.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Access(value = AccessType.FIELD)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "membership_type")
@DiscriminatorValue("group")
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"group"})
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupMembership implements Serializable, Comparable<GroupMembership>,
        Validatable, SemanticAuthorizationPathProducer {

    // Our Logger
    @XmlTransient
    private static final Logger log = LoggerFactory.getLogger(Membership.class);

    private static final long serialVersionUID = 88299927L;

    // Internal state
    @Version
    @XmlTransient
    private long version;

    @EmbeddedId
    @XmlTransient
    private GroupMembershipId groupMembershipId;

    @ManyToOne(optional = false)
    @MapsId("groupId")
    @XmlIDREF
    @XmlElement(required = true)
    private Group group;

    // This must be XmlTransient to avoid a cyclic graph in the XSD.
    // Handled by a callback method from the Membership side.
    @ManyToOne(optional = false)
    @MapsId("membershipId")
    @XmlTransient
    private Membership membership;

    /**
     * JPA/JAXB-friendly constructor.
     */
    public GroupMembership() {
    }

    /**
     * Compound constructor creating a GroupMembership relating the supplied, non-null, objects.
     *
     * @param group      The Group to which this GroupMembership indicates inclusion.
     * @param membership The Membership included in the supplied Group.
     */
    public GroupMembership(final Group group,
                           final Membership membership) {
        this.group = group;
        this.membership = membership;

        // Synthesize the GroupMembershipId
        this.groupMembershipId = new GroupMembershipId(group.getId(), membership.getId());
    }

    /**
     * @return the Database-generated version/revision of this GroupMembership.
     */
    public long getVersion() {
        return version;
    }

    /**
     * @return The embedded id of this GroupMembership.
     */
    public GroupMembershipId getGroupMembershipId() {
        return groupMembershipId;
    }

    /**
     * @return The Group of this GroupMembership.
     */
    public Group getGroup() {
        return group;
    }

    /**
     * Assigns the Group of this GroupMembership.
     *
     * @param group a non-null Group.
     */
    public void setGroup(final Group group) {

        // Check sanity
        Validate.notNull(group, "group");

        // Assign internal state
        this.group = group;
        if (this.groupMembershipId == null) {
            groupMembershipId = new GroupMembershipId(group.getId(), membership.getId());
        } else {
            groupMembershipId.groupId = group.getId();
        }
    }

    /**
     * @return The Membership included in the Group by this GroupMembership.
     */
    public Membership getMembership() {
        return membership;
    }

    /**
     * Assigns the Membership of this GroupMembership. This should only be called during XML unmarshalling,
     * to uphold referential integrity between Membership and GroupMembership. It is not considered part of the
     * public API of the GroupMembership class.
     *
     * @param membership The Membership which should be attached to a Group.
     */
    public void setMembership(final Membership membership) {

        // Check sanity
        Validate.notNull(membership, "membership");

        // Assign internal state
        this.membership = membership;

        if (this.groupMembershipId == null) {
            if (group == null) {
                log.warn("Cannot assign Membership with null group and membership parameters.");
            } else {
                groupMembershipId = new GroupMembershipId(group.getId(), membership.getId());
            }
        } else {
            groupMembershipId.membershipId = membership.getId();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return group.hashCode()
                + (membership == null ? 0 : (int) membership.getId() % Integer.MAX_VALUE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        // Check sanity
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }

        // All done.
        return obj instanceof GroupMembership
                && this.compareTo((GroupMembership) obj) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final GroupMembership that) {

        // Check sanity
        if (that == null) {
            return -1;
        } else if (that == this) {
            return 0;
        }

        // Delegate
        final Group thisGroup = this.getGroup();
        final Group thatGroup = that.getGroup();

        int toReturn = 0;
        if (thisGroup != null) {

            // Check internals
            if (thisGroup.getGroupName() != null) {
                toReturn = thisGroup.getGroupName().compareTo(thatGroup.getGroupName());
            }

            if (toReturn == 0
                    && thisGroup.getOrganisation() != null
                    && thisGroup.getOrganisation().getOrganisationName() != null) {
                toReturn = thisGroup.getOrganisation().getOrganisationName().compareTo(
                        thatGroup.getOrganisation().getOrganisationName());
            }
        }
        if (toReturn == 0 && getMembership() != null) {

            if (getMembership().getAlias() != null) {
                toReturn = getMembership().getAlias().compareTo(that.getMembership().getAlias());
            }

            if (getMembership().getOrganisation() != null
                    && getMembership().getOrganisation().getOrganisationName() != null) {
                toReturn = getMembership().getOrganisation().getOrganisationName().compareTo(
                        that.getMembership().getOrganisation().getOrganisationName());
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final String groupName = group == null ? "<group not yet set>" : group.getGroupName();
        final String alias = membership == null ? "<membership not yet set>" : membership.getAlias();
        return "GroupMembership [" + alias + " -> " + groupName + "]";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<SemanticAuthorizationPath> getPaths() {

        final SortedSet<SemanticAuthorizationPath> toReturn = new TreeSet<>();
        toReturn.add(new AuthorizationPath(
                getGroup().getOrganisation().getOrganisationName(),
                getGroup().getGroupName(),
                ""));
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(group, "group")
                .notNull(membership, "membership")
                .endExpressionAndValidate();
    }
}
