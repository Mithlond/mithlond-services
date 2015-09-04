/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
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
package se.mithlond.services.shared.spi.algorithms.authorization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlTransient;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Utility class to help in parsing, synthesizing and matching paths.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class PathPatterns {

    // Our Logger
    @XmlTransient
    private static final Logger log = LoggerFactory.getLogger(PathPatterns.class);

    /**
     * Enum defining the separators used to parse and synthesize PathPatterns.
     */
    public enum Separators {

        /**
         * Separator used to split concatenated Patterns from one another.
         */
        PATTERN(","),

        /**
         * Separator used to split concatenated (string) parts within a Patterns from one another.
         */
        PART("/");

        // Internal state
        private String separatorChar;

        Separators(final String separatorChar) {
            this.separatorChar = separatorChar;
        }

        /**
         * @return The Separator char of this Separator.
         */
        @Override
        public String toString() {
            return separatorChar;
        }
    }

    private static final Comparator<Pattern> PATTERN_COMPARATOR = (lhs, rhs) -> {

        if (lhs == null) {
            return -1;
        }
        if (rhs == null) {
            return 1;
        }

        // Delegate
        return lhs.pattern().compareTo(rhs.pattern());
    };

    /**
     * Determines if the supplied paths match at least one of the requiredPathPatterns.
     *
     * @param requiredPathPatterns        A concatenated string containing path specifications on the form
     *                                    <code>[path pattern 1],[path pattern 2],...</code>
     * @param possessedAuthorizationPaths A set of paths to match against each of the Patterns parsed
     *                                    from the requiredPathPatterns.
     * @return {@code true} if any path matched at least one of the requiredPathPatterns.
     */
    public static boolean isMatched(final String requiredPathPatterns,
                                    final Set<String> possessedAuthorizationPaths) {

        // No authorization required?
        if (isAuthorizationNotRequired(requiredPathPatterns)) {
            return true;
        }

        final SortedSet<String> givenPaths = new TreeSet<>();
        possessedAuthorizationPaths.forEach(current -> {
            final Pattern currentPattern = Pattern.compile(current.toString());
        });

        // Delegate.
        return isMatched(requiredPathPatterns, givenPaths);
    }

    /**
     * Determines if the supplied paths match at least one of the requiredPathPatterns.
     *
     * @param requiredPathPatterns A concatenated string containing path specifications on the form
     *                             <code>[path pattern 1],[path pattern 2],...</code>
     * @param suppliedPaths        A set of paths to match against each of the Patterns parsed
     *                             from the requiredPathPatterns.
     * @return {@code true} if any path matched at least one of the requiredPathPatterns.
     */
    public static boolean isMatched(final String requiredPathPatterns,
                                    final SortedSet<String> suppliedPaths) {

        // No authorization required?
        if (isAuthorizationNotRequired(requiredPathPatterns)) {
            return true;
        }

        // Match supplied paths to the Patterns
        for (Pattern current : parse(requiredPathPatterns)) {
            for (String toMatch : suppliedPaths) {
                if (current.matcher(toMatch).matches()) {

                    if (log.isDebugEnabled()) {
                        log.debug("Match for [" + toMatch + "] to pattern [" + current.pattern() + "]");
                    }

                    return true;
                }
            }
        }

        // Nopes.
        return false;
    }

    //
    // Private helpers
    //

    private static boolean isAuthorizationNotRequired(final String requiredPathPatterns) {
        return requiredPathPatterns == null || requiredPathPatterns.length() == 0;
    }

    private static SortedSet<Pattern> parse(final String toParse) {

        final SortedSet<Pattern> toReturn = new TreeSet<>(PATTERN_COMPARATOR);
        final int matchSpecification = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS;

        if (toParse != null && toParse.length() > 0) {
            final StringTokenizer tok = new StringTokenizer(toParse, Separators.PATTERN.toString(), false);
            while (tok.hasMoreTokens()) {
                toReturn.add(Pattern.compile(tok.nextToken(), matchSpecification));
            }
        }

        // All done.
        return toReturn;
    }
}
