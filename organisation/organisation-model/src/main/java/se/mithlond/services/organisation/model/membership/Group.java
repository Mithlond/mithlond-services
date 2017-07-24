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
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.XmlIdHolder;
import se.mithlond.services.shared.authorization.model.AuthorizationPath;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPathProducer;

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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

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
@NamedQueries({
        @NamedQuery(name = Group.NAMEDQ_GET_BY_NAME_ORGANISATION,
                query = "select a from Group a "
                        + " where a.groupName like :" + OrganisationPatterns.PARAM_GROUP_NAME
                        + " and a.organisation.organisationName like :" + OrganisationPatterns.PARAM_ORGANISATION_NAME
                        + " order by a.groupName"),
        @NamedQuery(name = Group.NAMEDQ_GET_BY_NAME_ORGANISATION,
                query = "select a from Group a "
                        + " where a.groupName like :" + OrganisationPatterns.PARAM_GROUP_NAME
                        + " and a.organisation.organisationName like :" + OrganisationPatterns.PARAM_ORGANISATION_NAME
                        + " order by a.groupName"),
        @NamedQuery(name = Group.NAMEDQ_GET_BY_PARENTGROUPNAME_ORGANISATION,
                query = "select a from Group a "
                        + " where a.parent.groupName like :" + OrganisationPatterns.PARAM_GROUP_NAME
                        + " and a.organisation.organisationName like :" + OrganisationPatterns.PARAM_ORGANISATION_NAME
                        + " order by a.groupName"),
        @NamedQuery(name = Group.NAMEDQ_GET_BY_ORGANISATION,
                query = "select a from Group a "
                        + " where a.organisation.organisationName like :" + OrganisationPatterns.PARAM_ORGANISATION_NAME
                        + " order by a.groupName"),
        @NamedQuery(name = Group.NAMEDQ_GET_BY_SEARCHPARAMETERS,
                query = "select g from Group g "
                        + " where "
                        + "( 0 = :" + OrganisationPatterns.PARAM_NUM_GROUPIDS + " or g.id in :"
                        + OrganisationPatterns.PARAM_GROUP_IDS + " ) "
                        + " and ( 0 = :"
                        + OrganisationPatterns.PARAM_NUM_ORGANISATIONIDS + " or g.organisation.id in :"
                        + OrganisationPatterns.PARAM_ORGANISATION_IDS + " ) "
                        + " order by g.groupName")
})
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"groupName", "description", "organisation", "emailList", "parentXmlID"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Group extends NazgulEntity implements Comparable<Group>, SemanticAuthorizationPathProducer, XmlIdHolder {

    /**
     * NamedQuery for getting Group by its Parent Group's groupName and organisation name.
     */
    public static final String NAMEDQ_GET_BY_PARENTGROUPNAME_ORGANISATION =
            "Group.getByParentNameAndOrganisationName";

    /**
     * NamedQuery for getting Group by its organisation name.
     */
    public static final String NAMEDQ_GET_BY_ORGANISATION =
            "Group.getByOrganisationName";

    /**
     * NamedQuery for getting Group by groupName and organisation name.
     */
    public static final String NAMEDQ_GET_BY_NAME_ORGANISATION =
            "Group.getByNameAndOrganisationName";

    /**
     * NamedQuery for getting Group by the values within a GroupSearchParameters object.
     */
    public static final String NAMEDQ_GET_BY_SEARCHPARAMETERS =
            "Group.getBySearchParameters";

    /**
     * Name of this Group, which must be non-empty and unique within each Organisation.
     */
    @Basic(optional = false)
    @Column(nullable = false, length = 128)
    @XmlElement(required = true)
    private String groupName;

    /**
     * A short description of this Group, which must be non-empty.
     */
    @Basic(optional = false)
    @Column(nullable = false, length = 1024)
    @XmlElement(required = true)
    private String description;

    /**
     * An optional electronic mail list which delivers elecronic mail to all members of this Group.
     * If the emailList property does not contain a full email address, the email suffix of the Organisation will be
     * appended to form the full mail address of [emailList]@[organisation email suffix].
     */
    @Basic
    @XmlElement
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
    @JoinColumn(nullable = false)
    @XmlIDREF
    @XmlAttribute(required = true, name = "organisationReference")
    private Organisation organisation;

    /**
     * Groups are structured in a Tree, which means that each Group may have (a single) parent Group.
     * Effective settings for a Group is the aggregation of all settings inherited from all its parent Groups.
     */
    @ManyToOne
    @XmlTransient
    private Group parent;

    /**
     * The xmlID of the parent for this Group.
     */
    @XmlAttribute
    @Transient
    private String parentXmlID;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public Group() {
    }

    /**
     * Compound constructor creating a Group, wrapping the supplied data.
     *
     * @param groupName    Name of this Group, which must be non-empty and unique within each Organisation.
     * @param description  Description of this Group, which must be non-empty.
     * @param organisation The non-null Organisation in which this Group exists.
     * @param parent       An optional parent to this Group. Groups are structured in a Tree, which means that each
     *                     Group may have (a single) parent Group. Effective settings for a Group is the aggregation
     *                     of all settings inherited from all its parent Groups.
     * @param emailList    An optional electronic mail list which delivers elecronic mail to all members of this Group.
     *                     If the emailList property does not contain a full email address, the email suffix of the
     *                     Organisation will be appended to form the full mail address of
     *                     <code>[emailList]@[organisation email suffix]</code>.
     */
    public Group(
            final String groupName,
            final String description,
            final Organisation organisation,
            final Group parent,
            final String emailList) {

        // Assign internal state
        this.groupName = groupName;
        this.organisation = organisation;
        this.parent = parent;
        this.emailList = emailList;
        this.description = description;
        setXmlID();
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
     * The description of this Group, such as "I musikgillet träffas vi i instrumentbeprydda hem, där vi spelar
     * och sjunger glada visor i Midgårda anda. När gillet tidigare var mer aktivt, brukade vi börja varje gille
     * med att baka en kaka som fick stå i ugnen medan vi musicerade."
     *
     * @return A non-empty description of this Group.
     */
    public String getDescription() {
        return description;
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
     * Retrieves the XML ID of the parent for this Group, or {@code null} if this Group has no parent group.
     *
     * @return the optional (i.e. nullable) XML ID of the parent for this Group.
     */
    public String getParentXmlID() {
        return parentXmlID;
    }

    /**
     * An optional electronic mail list which delivers electronic mail to all members of this Group.
     *
     * @return If the emailList property does not contain a full email address, the email suffix of the
     * Organisation will be appended to form the full mail address of
     * <code>[emailList]@[organisation email suffix]</code>.
     */
    public String getEmailList() {

        if (emailList.contains("@")) {
            return emailList;
        }

        // Append the organisation's mailing suffix.
        return emailList + "@" + getOrganisation().getEmailSuffix();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(groupName, "groupName")
                .notNullOrEmpty(description, "description")
                .notNull(organisation, "organisation")
                .notTrue(parent == this, "parent == this")
                .endExpressionAndValidate();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {

        // Fail fast.
        if (this == o) {
            return true;
        }
        if (!(o instanceof Group)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        // Delegate to internal state validation. Ignore the parent Group.
        final Group that = (Group) o;
        return Objects.equals(groupName, that.groupName)
                && Objects.equals(description, that.description)
                && Objects.equals(emailList, that.emailList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        // Ignore the NazgulEntity properties.
        return Objects.hash(groupName, description, emailList);
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("all")
    @Override
    public int compareTo(final Group that) {

        // Check sanity
        if (that == null) {
            return -1;
        } else if (this == that) {
            return 0;
        }

        // Do we have a parent?
        final boolean thisHasParent = this.getParent() != null;
        final boolean thatHasParent = that.getParent() != null;

        if(thisHasParent && !thatHasParent) {
            return 1;
        } else if(!thisHasParent && thatHasParent) {
            return -1;
        }

        // Delegate
        int toReturn = groupName.compareTo(that.groupName);
        if (toReturn == 0) {
            toReturn = organisation.getOrganisationName().compareTo(that.getOrganisation().getOrganisationName());
        }

        // All Done
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<SemanticAuthorizationPath> getPaths() {

        // Create a non-null SortedSet to return.
        final SortedSet<SemanticAuthorizationPath> toReturn = new TreeSet<>();

        // Create an AuthorizationPath for this Group, using an empty qualifier.
        toReturn.add(new AuthorizationPath(
                organisation.getOrganisationName(),
                groupName,
                SemanticAuthorizationPath.NO_VALUE));

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final String parentGroupName = parent != null
                ? " with Parent Group [" + parent.groupName + "]"
                : " without Parent Group.";
        return "Group [" + groupName + "]" + parentGroupName;
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
        setXmlID();
    }

    private void setXmlID() {
        this.xmlID = "group_"
                + organisation.getOrganisationName().replaceAll("\\s+", "_")
                + "_" + groupName.trim().replaceAll("\\s+", "_");

        if(getParent() != null) {

            // Ensure that the parent's XML ID is set.
            getParent().setXmlID();

            // ... and fetch the Parent's XML ID.
            this.parentXmlID = getParent().xmlID;
        }
    }

    /**
     * Retrieves the XML ID of this Group.
     *
     * @return the XML ID of this Group.
     */
    @Override
    public String getXmlId() {
        setXmlID();
        return this.xmlID;
    }
}
