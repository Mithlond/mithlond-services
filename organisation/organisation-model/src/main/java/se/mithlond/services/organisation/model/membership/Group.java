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

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.Patterns;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

/**
 * Entity implementation for Groups of Memberships within an Organisation.
 * Groups are structured in a Tree, which means that each Group may have (a single) parent Group.
 * Effective settings for a Group is the aggregation of all settings inherited from all its parent Groups.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Table(name = "InternalGroups",
        uniqueConstraints = {
                @UniqueConstraint(name = "groupNameAndOrganisationIsUnique",
                        columnNames = {"groupname", "organisation_id"})})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "discriminator", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("group")
@XmlType(namespace = Patterns.NAMESPACE,
        propOrder = {"groupName", "organisation", "parent", "emailList"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Group extends NazgulEntity implements Comparable<Group> {

    /**
     * Name of this Group, which must be non-empty and unique within each Organisation.
     */
    @Basic(optional = false)
    @Column(nullable = false, length = 128)
    @XmlElement(required = true, nillable = false)
    private String groupName;

    /**
     * An optional electronic mail list which delivers elecronic mail to all members of this Group.
     * If the emailList property does not contain a full email address, the email suffix of the Organisation will be
     * appended to form the full mail address of [emailList]@[organisation email suffix].
     */
    @Basic
    @XmlElement(nillable = true, required = false)
    private String emailList;

    /**
     * Syntetic XML ID for this Group, generated immediately before Marshalling.
     * Since whitespace is not permitted in an XML ID, the beforeMarshal listener
     * method generates this field from the organisation and group name while replacing
     * all whitespace with underscore.
     */
    @XmlID
    @XmlAttribute(required = true)
    @Transient
    @SuppressWarnings("all")
    protected String xmlID;

    /**
     * XML ID reference to the Organisation in which this Group exists.
     */
    @ManyToOne(optional = false,
            fetch = FetchType.EAGER,
            cascade = {CascadeType.PERSIST, CascadeType.REFRESH, CascadeType.MERGE})
    @XmlIDREF
    @XmlAttribute(required = true)
    private Organisation organisation;

    /**
     * XML ID reference to the parent of this Group.
     * Groups are structured in a Tree, which means that each Group may have (a single) parent Group.
     * Effective settings for a Group is the aggregation of all settings inherited from all its parent Groups.
     */
    @ManyToOne(optional = true)
    @XmlIDREF
    @XmlAttribute(required = false)
    private Group parent;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public Group() {
    }

    /**
     * Compound constructor creating a Group, wrapping the supplied data.
     *
     * @param groupName    Name of this Group, which must be non-empty and unique within each Organisation.
     * @param organisation The non-null Organisation in which this Group exists.
     * @param parent       An optional parent to this Group. Groups are structured in a Tree, which means that each
     *                     Group may have (a single) parent Group. Effective settings for a Group is the aggregation
     *                     of all settings inherited from all its parent Groups.
     * @param emailList    An optional electronic mail list which delivers elecronic mail to all members of this Group.
     *                     If the emailList property does not contain a full email address, the email suffix of the
     *                     Organisation will be appended to form the full mail address of
     *                     <code>[emailList]@[organisation email suffix]</code>.
     */
    public Group(final String groupName,
                 final Organisation organisation,
                 final Group parent,
                 final String emailList) {

        // Assign internal state
        this.groupName = groupName;
        this.organisation = organisation;
        this.parent = parent;
        this.emailList = emailList;
    }

    /**
     * The name of this group, such as "Jarlar".
     *
     * @return The name of this Group entity
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * The Organisation to which this Group belongs. For instance, "Mth Morgoth" implies "Forodrim".
     *
     * @return The Organisation of this Group.
     */
    public Organisation getOrganisation() {
        return organisation;
    }

    /**
     * An optional parent to this Group. Groups are structured in a Tree, which means that each
     * Group may have (a single) parent Group. Effective settings for a Group is the aggregation
     * of all settings inherited from all its parent Groups.
     *
     * @return The optional parent to this Group.
     */
    public Group getParent() {
        return parent;
    }

    /**
     * An optional electronic mail list which delivers elecronic mail to all members of this Group.
     *
     * @return If the emailList property does not contain a full email address, the email suffix of the
     * Organisation will be appended to form the full mail address of
     * <code>[emailList]@[organisation email suffix]</code>.
     */
    public String getEmailList() {
        return emailList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(groupName, "groupName")
                .notNull(organisation, "organisation")
                .notTrue(parent == this, "parent == this")
                .endExpressionAndValidate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return groupName.hashCode() + organisation.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        // Check sanity
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Group)) {
            return false;
        }

        // Delegate
        final Group that = (Group) obj;
        return groupName.equals(that.groupName)
                && organisation.equals(that.organisation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final Group that) {

        // Check sanity
        if (that == null) {
            return -1;
        } else if (this == that) {
            return 0;
        }

        // Delegate
        int toReturn = groupName.compareTo(that.groupName);
        if (toReturn == 0) {
            toReturn = organisation.getOrganisationName().compareTo(that.getOrganisation().getOrganisationName());
        }

        // All Done
        return toReturn;
    }

    //
    // Private helpers
    //

    /**
     * Standard JAXB class-wide listener method, automagically invoked
     * immediately before this object is Marshalled.
     *
     * @param marshaller The active Marshaller.
     */
    @SuppressWarnings("all")
    private void beforeMarshal(final Marshaller marshaller) {
        this.xmlID = "group_" + organisation.getOrganisationName().replaceAll("\\s+", "_")
                + "_" + groupName.trim().replaceAll("\\s+", "_");
    }
}
