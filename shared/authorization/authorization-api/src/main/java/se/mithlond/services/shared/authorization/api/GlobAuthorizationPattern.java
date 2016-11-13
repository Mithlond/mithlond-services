/*
 * #%L
 * Nazgul Project: mithlond-services-shared-authorization-api
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
package se.mithlond.services.shared.authorization.api;

import se.mithlond.services.shared.authorization.model.AuthorizationPath;
import se.mithlond.services.shared.authorization.model.Patterns;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.function.Consumer;

import static se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath.SEGMENT_SEPARATOR_STRING;

/**
 * <p>Utility class to generate and manage Patterns used to match (or not) AuthorizationPath instances.</p>
 * <p>Each AuthorizationPattern has 3 segments used to match realm, group and qualifier respectively.
 * AuthorizationPatterns are synthesized into a regular expression pattern by joining on
 * {@code SemanticAuthorizationPath.SEGMENT_SEPARATOR} (i.e. "{@value SemanticAuthorizationPath#SEGMENT_SEPARATOR}").
 * Therefore, AuthorizationPatterns have the form {@code realm/group/qualifier}.
 * Should one of the segments in the AuthorizationPattern be empty, it can be replaced by a regular expression
 * which matches any string/any text. This replacement pattern is <code>{@value GlobAuthorizationPattern#ANY}</code>
 * .</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see SemanticAuthorizationPath#PATTERN_SEPARATOR_STRING
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"realmPattern", "groupPattern", "qualifierPattern"})
public class GlobAuthorizationPattern implements Comparable<GlobAuthorizationPattern> {

    /**
     * Glob pattern to match any text within the current segment.
     *
     * @see Path#forEach(Consumer)
     */
    public static final String ANY = "*";

    /**
     * The glob pattern for matching the name of a Realm, typically found within an AuthorizationPath.
     */
    @XmlElement
    private String realmPattern;

    /**
     * The glob pattern for matching the name of a Group, typically found within an AuthorizationPath.
     */
    @XmlElement
    private String groupPattern;

    /**
     * The glob pattern for matching the name of a Qualifier, typically found within an AuthorizationPath.
     */
    @XmlElement
    private String qualifierPattern;

    @XmlTransient
    private Path path;

    /**
     * Default constructor creating an AuthorizationPattern permitting any AuthorizationPath.
     */
    public GlobAuthorizationPattern() {
        this(ANY, ANY, ANY);
    }

    /**
     * Compound constructor creating an AuthorizationPattern wrapping the supplied
     * patterns, to match AuthorizationPaths using the supplied regexp patterns.
     * Uses the {@code ANY} pattern for qualifier.
     *
     * @param realmPattern A regexp pattern defining realms to permit.
     * @param groupPattern A regexp pattern defining group names to permit.
     */
    public GlobAuthorizationPattern(
            final String realmPattern,
            final String groupPattern) {
        this(realmPattern, groupPattern, ANY);
    }

    /**
     * Compound constructor creating an AuthorizationPattern wrapping the supplied patterns, to permit
     * (or not) an AuthorizationPath with matching realm, group and qualifier.
     *
     * @param realmPattern     A regexp pattern defining realms to permit.
     * @param groupPattern     A regexp pattern defining group names to permit.
     * @param qualifierPattern A regexp pattern defining qualifiers to permit.
     */
    @SuppressWarnings("PMD")
    public GlobAuthorizationPattern(final String realmPattern,
            final String groupPattern,
            final String qualifierPattern) {

        // Check sanity
        Validate.notNull(realmPattern, "realmPattern");
        Validate.notNull(groupPattern, "groupPattern");
        Validate.notNull(qualifierPattern, "qualifierPattern");

        // Assign internal state
        this.realmPattern = realmPattern;
        this.groupPattern = groupPattern;
        this.qualifierPattern = qualifierPattern;

        this.path = Paths.get(realmPattern, groupPattern, qualifierPattern);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return SemanticAuthorizationPath.SEGMENT_SEPARATOR + realmPattern
                + SemanticAuthorizationPath.SEGMENT_SEPARATOR + groupPattern
                + SemanticAuthorizationPath.SEGMENT_SEPARATOR + qualifierPattern;
    }

    /**
     * {@code}
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
        return (obj instanceof GlobAuthorizationPattern && toString().equals(obj.toString()));
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
    public int compareTo(final GlobAuthorizationPattern that) {
        return that == null ? -1 : toString().compareTo(that.toString());
    }

    /**
     * Checks if the supplied CharSequence matches this AuthorizationPattern.
     *
     * @param sequence The sequence to check.
     * @return {@code false} if {@code sequence} is null. Otherwise matches the regexp
     * pattern of this AuthorizationPattern.
     */
    public boolean matches(final CharSequence sequence) {

        // Check sanity
        if (sequence == null) {
            return false;
        }

        // Handle initial "/"'s
        String normalized = AuthorizationPath.parse(sequence.toString())
                .toString()
                .replace(SemanticAuthorizationPath.NO_VALUE, GlobAuthorizationPattern.ANY);
        final String globPatternToMatch = "glob:"
                + (normalized.startsWith("/") ? normalized.substring(1) : normalized);

        // Delegate.
        return this.path.getFileSystem().getPathMatcher(globPatternToMatch).matches(this.path);
    }

    /**
     * Parses the supplied concatenatedPatterns into several AuthorizationPattern instances.
     * Empty path segments (after trimming) will be replaced by
     * {@link GlobAuthorizationPattern#ANY}.
     *
     * @param concatenatedPatterns A string containing concatenated AuthorizationPatterns.
     * @return a SortedSet containing AuthorizationPattern instances, extracted from the concatenatedPatterns string.
     */
    public static SortedSet<GlobAuthorizationPattern> parse(final String concatenatedPatterns) {

        final SortedSet<GlobAuthorizationPattern> toReturn = new TreeSet<>();
        if (concatenatedPatterns != null) {

            final StringTokenizer tok = new StringTokenizer(
                    concatenatedPatterns,
                    Character.toString(SemanticAuthorizationPath.PATTERN_SEPARATOR),
                    false);

            while (tok.hasMoreTokens()) {
                toReturn.add(createSinglePattern(tok.nextToken()));
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * Parses the supplied pattern string into a single AuthorizationPattern instance. The patternString cannot
     * contain more than 3 path segments. Empty path segments (after trimming) will be replaced by
     * {@link GlobAuthorizationPattern#ANY}.
     *
     * @param patternString The pattern string to parse. Expected patternString format:
     *                      {@code [/]realm/group/qualifier}.
     * @return The single AuthorizationPattern instance parsed from the supplied patternString.
     * @throws IllegalArgumentException if the patternString contained
     *                                  {@code SemanticAuthorizationPath.PATTERN_SEPARATOR} or more than 3
     *                                  {@code SemanticAuthorizationPath.SEGMENT_SEPARATOR} characters.
     */
    public static GlobAuthorizationPattern createSinglePattern(final String patternString)
            throws IllegalArgumentException {

        // Check sanity
        Validate.notEmpty(patternString, "patternString");

        // Expected pattern: /realm[/group[/qualifier]]
        // Peel off the initial "/" if present.
        final String effectivePath = patternString.trim().startsWith(SEGMENT_SEPARATOR_STRING)
                ? patternString.substring(1)
                : patternString.trim();
        final String[] segments = effectivePath.split(SEGMENT_SEPARATOR_STRING, -1);
        if(segments.length > 3) {
            throw new IllegalArgumentException("Expected pattern: /realm[/group[/qualifier]], but got: "
                    + patternString);
        }

        final String realmPattern = segments.length > 0 && !segments[0].isEmpty()
                ? segments[0]
                : GlobAuthorizationPattern.ANY;
        final String groupPattern = segments.length > 1 && !segments[1].isEmpty()
                ? segments[1]
                : GlobAuthorizationPattern.ANY;
        final String qualifierPattern = segments.length > 2 && !segments[2].isEmpty()
                ? segments[2]
                : GlobAuthorizationPattern.ANY;

        // All Done.
        return new GlobAuthorizationPattern(realmPattern, groupPattern, qualifierPattern);
    }
}
