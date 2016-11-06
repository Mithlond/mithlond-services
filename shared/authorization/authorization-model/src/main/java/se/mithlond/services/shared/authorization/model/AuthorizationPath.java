/*
 * #%L
 * Nazgul Project: mithlond-services-shared-authorization-model
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
package se.mithlond.services.shared.authorization.model;

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
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

/**
 * Immutable Entity implementation of a Path consisting of 3 named/semantic segments, realm, group and qualifier.
 * Any of these segments may be empty. AuthorizationPaths constitute the definitions of which privileges
 * should be held in order to perform a particular operation or access certain content.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "pathIsUnique",
        columnNames = {"auth_realm", "auth_group", "auth_qualifier"})})
@Access(AccessType.FIELD)
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"xmlID"})
public class AuthorizationPath extends NazgulEntity implements SemanticAuthorizationPath {

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
        // Do nothing.
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
        final String group = segments.length > 1 ? validateSegment(segments[1]) : null;
        final String qualifier = segments.length > 2 ? validateSegment(segments[2]) : null;

        // All Done.
        return new AuthorizationPath(realm, group, qualifier);
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
