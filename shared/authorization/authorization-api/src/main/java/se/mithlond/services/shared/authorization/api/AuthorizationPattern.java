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

import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;
import se.mithlond.services.shared.spi.algorithms.Validate;

import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Regular expression path wrapper used to match AuthorizationPaths.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AuthorizationPattern implements Comparable<AuthorizationPattern> {

    // Internal state
    private String realmPattern;
    private String groupPattern;
    private String qualifierPattern;
    private Pattern pattern;

    /**
     * Default constructor creating an AuthorizationPattern permitting any AuthorizationPath.
     *
     * @see Segmenter#ANY
     */
    public AuthorizationPattern() {
        this(Segmenter.ANY, Segmenter.ANY, Segmenter.ANY);
    }

    /**
     * Compound constructor creating an AuthorizationPattern wrapping the supplied
     * patterns, to match AuthorizationPaths using the supplied regexp patterns.
     * Uses the {@code ANY} pattern for qualifier.
     *
     * @param realmPattern A regexp pattern defining realms to permit.
     * @param groupPattern A regexp pattern defining group names to permit.
     * @see Segmenter#ANY
     */
    public AuthorizationPattern(final String realmPattern,
                                final String groupPattern) {
        this(realmPattern, groupPattern, Segmenter.ANY);
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
    public AuthorizationPattern(final String realmPattern,
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

        // Should the initial '/' be optional when matching?
        this.pattern = Pattern.compile(toString());
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
        return (obj instanceof AuthorizationPattern && toString().equals(obj.toString()));
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
    public int compareTo(final AuthorizationPattern that) {
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
        if (sequence == null) {
            return false;
        }

        // Delegate.
        return pattern.matcher(sequence).matches();
    }

    /**
     * Parses the supplied concatenatedPatterns into several AuthorizationPattern instances.
     *
     * @param concatenatedPatterns A string containing concatenated AuthorizationPatterns.
     * @return a SortedSet containing AuthorizationPattern instances, extracted from the concatenatedPatterns string.
     */
    public static SortedSet<AuthorizationPattern> parse(final String concatenatedPatterns) {

        final SortedSet<AuthorizationPattern> toReturn = new TreeSet<>();
        if (concatenatedPatterns != null) {

            final StringTokenizer tok = new StringTokenizer(
                    concatenatedPatterns,
                    Character.toString(SemanticAuthorizationPath.PATTERN_SEPARATOR),
                    false);

            while (tok.hasMoreTokens()) {
                toReturn.add(parseSingle(tok.nextToken()));
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * Parses the supplied pattern string into a single AuthorizationPattern instance. The patternString cannot
     * contain more than 3 path segments. Empty path segments will be replaced by {@code #ANY}.
     *
     * @param patternString The pattern string to parse. Expected patternString format:
     *                      {@code [/]realm/group/qualifier}.
     * @return The single AuthorizationPattern instance parsed from the supplied patternString.
     * @throws IllegalArgumentException if the patternString contained
     *                                  {@code SemanticAuthorizationPath.PATTERN_SEPARATOR} or more than 3
     *                                  {@code SemanticAuthorizationPath.SEGMENT_SEPARATOR} characters.
     * @see Segmenter#ANY
     */
    public static AuthorizationPattern parseSingle(final String patternString) throws IllegalArgumentException {

        final String[] segments = Segmenter.segment(patternString);
        return new AuthorizationPattern(segments[0], segments[1], segments[2]);
    }
}
