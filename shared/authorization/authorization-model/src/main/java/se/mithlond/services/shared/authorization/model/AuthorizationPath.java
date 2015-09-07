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
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;
import java.util.StringTokenizer;

/**
 * Immutable entity implementation of a semantic Path consisting of 3 segments.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(name = "pathIsUnique",
        columnNames = {"auth_realm", "auth_group", "auth_qualifier"})})
@Access(AccessType.FIELD)
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"xmlID", "realm", "group", "qualifier"})
public class AuthorizationPath extends NazgulEntity implements SemanticAuthorizationPath {

    /**
     * XML ID representation of this AuthorizationPath.
     */
    @Transient
    @XmlID
    @XmlAttribute(required = true)
    @SuppressWarnings("all")
    private String xmlID;

    /**
     * The Realm of this AuthorizationPath, which could be equal to the organisation's name.
     * Never {@code null}.
     */
    @Basic(optional = false)
    @Column(nullable = false, name = "auth_realm")
    @XmlAttribute(required = true, name = "auth_realm")
    private String realm;

    /**
     * The Group of this AuthorizationPath, which could be equal to a Group name.
     * Never {@code null}.
     */
    @Basic(optional = false)
    @Column(nullable = false, name = "auth_group")
    @XmlAttribute(required = true, name = "auth_group")
    private String group;

    /**
     * The Qualifier of this AuthorizationPath, which could be equal to the name of a Sub-Group.
     * ("Guildmaster", "Auditor" or "Spouse" for example). Never {@code null}.
     */
    @Basic(optional = false)
    @Column(nullable = false, name = "auth_qualifier")
    @XmlAttribute(required = true, name = "auth_qualifier")
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
        this.realm = realm;
        this.group = group;
        this.qualifier = qualifier;

        // Prime the XML ID.
        // (Not really necessary here, true, but used in testing).
        beforeMarshal(null);
    }

    /**
     * @return The Realm of this AuthorizationPath. Never {@code null}.
     */
    public String getRealm() {
        return realm;
    }

    /**
     * @return The Group of this AuthorizationPath. Never {@code null}.
     */
    public String getGroup() {
        return group;
    }

    /**
     * @return The qualifier of this AuthorizationPath. Never {@code null}.
     */
    public String getQualifier() {
        return qualifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return SEGMENT_SEPARATOR + realm + SEGMENT_SEPARATOR + group + SEGMENT_SEPARATOR + qualifier;
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
        int toReturn = realm.compareTo(that.getRealm());
        if (toReturn == 0) {
            toReturn = group.compareTo(that.getGroup());
        }
        if (toReturn == 0) {
            toReturn = qualifier.compareTo(that.getQualifier());
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
        this.xmlID = "authorizationPath" + toString().replace(SEGMENT_SEPARATOR, '_');
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
     * returning an AuthorizationPath created from the supplied path. None of the AuthorizationPath segments can be
     * null or empty.
     *
     * @param path A full AuthorizationPath specification on the form {@code [/]realm/group/qualifier}.
     *             None of the path segments can be {@code null} or empty.
     * @return An AuthorizationPath created from the supplied path.
     */
    public static AuthorizationPath create(final String path) {

        final String expectedFormat = " Expected argument format: [/]realm/group/qualifier.";

        // Check sanity
        Validate.notEmpty(path, "path");
        if (path.contains(PATTERN_SEPARATOR_STRING)) {
            throw new IllegalArgumentException("Argument 'path' cannot contain '"
                    + PATTERN_SEPARATOR + "' characters." + expectedFormat);
        }

        // Expected pattern: [/]realm[/group[/qualifier]]
        final String effectivePath = path.trim().startsWith(SEGMENT_SEPARATOR_STRING)
                ? path.trim()
                : SEGMENT_SEPARATOR + path.trim();

        final StringTokenizer tok = new StringTokenizer(effectivePath, SEGMENT_SEPARATOR_STRING, false);
        if (tok.countTokens() != 3) {
            throw new IllegalArgumentException("Incorrect 'path' argument. Got [" + path + "]." + expectedFormat);
        }

        // All done.
        return new AuthorizationPath(
                tok.nextToken(),
                tok.nextToken(),
                tok.nextToken());
    }
}
