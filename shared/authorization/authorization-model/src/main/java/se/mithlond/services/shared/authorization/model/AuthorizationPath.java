/*
 * #%L
 * Nazgul Project: mithlond-services-shared-authorization-model
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
package se.mithlond.services.shared.authorization.model;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Immutable Entity implementation of a Path consisting of 3 named/semantic segments, realm, group and qualifier.
 * Any of these segments may be empty. AuthorizationPaths constitute the definitions of which privileges
 * should be held in order to perform a particular operation or access certain content.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@NamedQueries({
        @NamedQuery(name = AuthorizationPath.NAMEDQ_GET_BY_PARAMS,
                query = "select ap from AuthorizationPath ap where "
                        + " ap.realm like :" + Patterns.PARAM_REALM_NAME
                        + " and ap.group like :" + Patterns.PARAM_GROUP_NAME
                        + " and ap.qualifier like :" + Patterns.PARAM_QUALIFIER_NAME
                        + " order by ap.realm, ap.group")
})
@Table(uniqueConstraints = {@UniqueConstraint(name = "pathIsUnique",
        columnNames = {"auth_realm", "auth_group", "auth_qualifier"})})
@Access(AccessType.FIELD)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"xmlID"})
public class AuthorizationPath extends NazgulEntity implements SemanticAuthorizationPath {

    /**
     * Named query retrieving an {@link AuthorizationPath} by its Realm, Group and Qualifier properties
     * using 'like' comparisons on all 3 properties.
     */
    public static final String NAMEDQ_GET_BY_PARAMS = "AuthorizationPath.getByParams";

    /**
     * The prefix placed before the XMLID to comply with the fact that all XMLIDs must start with a (proper) String.
     */
    public static final String AUTHORIZATION_PATH_XMLID_PREFIX = "authPath:";

    /**
     * XML ID representation of this AuthorizationPath.
     */
    @Transient
    @XmlID
    @XmlAttribute(required = true)
    @SuppressWarnings("all")
    private String xmlID;

    /**
     * The Realm of this AuthorizationPath, which could be equal to an Organisation's name.
     */
    @Basic(optional = false)
    @Column(nullable = false, name = "auth_realm")
    @XmlTransient
    private String realm;

    /**
     * The Group of this AuthorizationPath, which could be equal to a Group name.
     */
    @Basic(optional = false)
    @Column(nullable = false, name = "auth_group")
    @XmlTransient
    private String group;

    /**
     * The Qualifier of this AuthorizationPath, which could be equal to the name of a Sub-Group.
     * ("Guildmaster", "Auditor" or "Spouse" for example).
     */
    @Basic(optional = false)
    @Column(nullable = false, name = "auth_qualifier")
    @XmlTransient
    private String qualifier;

    /**
     * JPA/JAXB-friendly constructor.
     */
    public AuthorizationPath() {
    }

    /**
     * Compound constructor, creating an AuthorizationPath wrapping the supplied data.
     *
     * @param realm     The Realm of this AuthorizationPath, which could be equal to the organisation's name.
     * @param group     The Group of this AuthorizationPath, which could be equal to a group name.
     * @param qualifier The Qualifier of this AuthorizationPath, which could be equal to the name of a Sub-Group.
     */
    public AuthorizationPath(final String realm,
            final String group,
            final String qualifier) {

        this.realm = validateSegment(realm);
        this.group = validateSegment(group);
        this.qualifier = validateSegment(qualifier);

        // Prime the XML ID.
        // (Not really necessary here, true, but used in testing).
        beforeMarshal(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSegment(@NotNull final Segment segment) {

        // Check sanity
        Validate.notNull(segment, "segment");

        String toReturn = realm;
        switch (segment) {

            case QUALIFIER:
                toReturn = qualifier;
                break;

            case GROUP:
                toReturn = group;
                break;

            default:
                // Do nothing; toReturn is already assigned the realm value.
                break;
        }

        // Don't return nulls. Ever.
        return toReturn == null ? "" : toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return toString().hashCode();
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
        return (obj instanceof AuthorizationPath && toString().equals(obj.toString()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final SemanticAuthorizationPath that) {

        // Check sanity
        if (that == null) {
            return -1;
        }
        if (this == that) {
            return 0;
        }

        // Delegate
        int toReturn = this.getRealm().compareTo(that.getRealm());
        if (toReturn == 0) {
            toReturn = this.getGroup().compareTo(that.getGroup());
        }
        if (toReturn == 0) {
            toReturn = this.getQualifier().compareTo(that.getQualifier());
        }
        return toReturn;
    }

    /**
     * JAXB event method invoked before a Marshaller marshals this AuthorizationPath to XML.
     *
     * @param marshaller The active Marshaller.
     */
    @SuppressWarnings("PMD")
    private void beforeMarshal(final Marshaller marshaller) {
        this.xmlID = AUTHORIZATION_PATH_XMLID_PREFIX + toString();
    }

    /**
     * This method is called after all the properties (except IDREF) are unmarshalled for
     * this object, but before this object is set to the parent object.
     */
    @SuppressWarnings("PMD")
    private void afterUnmarshal(final Unmarshaller unmarshaller, final Object parentObject) {

        // The XMLID must start with the #AUTHORIZATION_PATH_XMLID_PREFIX
        if (!this.xmlID.startsWith(AUTHORIZATION_PATH_XMLID_PREFIX)) {
            throw new IllegalStateException("Illegal XMLID value found while unmarshalling. "
                    + "Must start with '" + AUTHORIZATION_PATH_XMLID_PREFIX + "'");
        }

        final String toParse = this.xmlID.substring(AUTHORIZATION_PATH_XMLID_PREFIX.length());
        final AuthorizationPath tmp = parse(toParse);

        // Assign the internal state
        this.realm = tmp.getRealm();
        this.group = tmp.getGroup();
        this.qualifier = tmp.getQualifier();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(realm, "realm")
                .notNullOrEmpty(group, "group")
                .notNullOrEmpty(qualifier, "qualifier")
                .endExpressionAndValidate();
    }

    /**
     * Parses a string containing a full AuthorizationPath specification, implying realm, group and qualifier -
     * returning an AuthorizationPath created from the supplied path.
     * None of the segments can be null.
     *
     * @param path A full AuthorizationPath specification on the form {@code [/]realm/group/qualifier}.
     *             None of the path segments can be {@code null} or empty.
     * @return An AuthorizationPath created from the supplied path.
     */
    public static AuthorizationPath parse(final String path) {

        final String expectedFormat = " Expected argument format: [/]realm/group/qualifier.";

        // Check sanity
        Validate.notEmpty(path, "path");
        if (path.contains(PATTERN_SEPARATOR_STRING)) {
            throw new IllegalArgumentException("Argument 'path' cannot contain '"
                    + PATTERN_SEPARATOR + "' characters." + expectedFormat);
        }

        // Expected pattern: /realm[/group[/qualifier]]
        // Peel off the initial "/" if present.
        final String effectivePath = path.trim().startsWith(SEGMENT_SEPARATOR_STRING)
                ? path.substring(1)
                : path.trim();
        final String[] segments = effectivePath.split(SemanticAuthorizationPath.SEGMENT_SEPARATOR_STRING, -1);

        // Extract and validate the tokens to use.
        final String realm = validateSegment(segments[0]);
        final String group = segments.length > 1 ? validateSegment(segments[1]) : SemanticAuthorizationPath.NO_VALUE;
        final String qualifier = segments.length > 2 ? validateSegment(segments[2]) : SemanticAuthorizationPath.NO_VALUE;

        // All Done.
        return new AuthorizationPath(realm, group, qualifier);
    }

    /**
     * Splices the supplied toParse string into a SortedSet of AuthorizationPaths.
     *
     * @param toParse A non-empty string to splice and parse into a set of AuthorizationPaths.
     * @return A non-null SortedSet containing AuthorizationPaths.
     */
    public static SortedSet<SemanticAuthorizationPath> spliceAndParse(final String toParse) {

        // Check sanity
        Validate.notEmpty(toParse, "toParse");

        final SortedSet<SemanticAuthorizationPath> toReturn = new TreeSet<>();

        // Splice on separators, then delegate to the parse method.
        Stream.of(toParse.split(SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING, -1))
                .forEach(splitPattern -> toReturn.add(AuthorizationPath.parse(splitPattern)));

        // All Done.
        return toReturn;
    }

    /**
     * Convenience method creating and returning a new {@link Builder} instance.
     *
     * @return A new, pristine Builder.
     */
    public static Builder getBuilder() {
        return new Builder();
    }

    /**
     * Simple Builder/Factory implementation to supp
     */
    @XmlTransient
    public static class Builder implements Serializable {

        // Internal state
        private String realm = SemanticAuthorizationPath.NO_VALUE;
        private String group = SemanticAuthorizationPath.NO_VALUE;
        private String qualifier = SemanticAuthorizationPath.NO_VALUE;

        /**
         * Sets the value for this builder.
         *
         * @param realm A non-empty string for the assigned value.
         * @return this Builder, for chaining.
         * @see AuthorizationPath#getRealm()
         */
        public Builder withRealm(final String realm) {

            // Check sanity and assign internal state
            this.realm = Validate.notEmpty(realm, "realm");

            // All Done.
            return this;
        }

        /**
         * Sets the value for this builder.
         *
         * @param group A non-empty string for the assigned value.
         * @return this Builder, for chaining.
         * @see AuthorizationPath#getGroup()
         */
        public Builder withGroup(final String group) {

            // Check sanity and assign internal state
            this.group = Validate.notEmpty(group, "group");

            // All Done.
            return this;
        }

        /**
         * Sets the value for this builder.
         *
         * @param qualifier A non-empty string for the assigned value.
         * @return this Builder, for chaining.
         * @see AuthorizationPath#getQualifier()
         */
        public Builder withQualifier(final String qualifier) {

            // Check sanity and assign internal state
            this.qualifier = Validate.notEmpty(qualifier, "qualifier");

            // All Done.
            return this;
        }

        /**
         * @return Retrieves the fully created AuthorizationPath instance, made up from the state of this Builder.
         */
        public AuthorizationPath build() {
            return new AuthorizationPath(realm, group, qualifier);
        }
    }

    //
    // Private helpers
    //

    private static String validateSegment(final String candidate) {

        if (candidate == null) {
            throw new IllegalArgumentException("Segments cannot be null.");
        } else {
            if (candidate.isEmpty()) {
                throw new IllegalArgumentException("Segments cannot be empty.");
            }
            if (candidate.contains(SEGMENT_SEPARATOR_STRING)) {
                throw new IllegalArgumentException("Segments cannot contain [" + SEGMENT_SEPARATOR_STRING
                        + "]. Got: " + candidate);
            }
            if (candidate.contains(PATTERN_SEPARATOR_STRING)) {
                throw new IllegalArgumentException("Segments cannot contain [" + PATTERN_SEPARATOR_STRING
                        + "]. Got: " + candidate);
            }
        }

        // All Done.
        return candidate.trim().isEmpty() ? NO_VALUE : candidate.trim();
    }
}
