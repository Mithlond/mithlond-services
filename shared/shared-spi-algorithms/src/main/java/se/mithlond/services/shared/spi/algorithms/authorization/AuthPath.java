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
import se.mithlond.services.shared.spi.algorithms.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Simple SemanticAuthorizationPath implementation storing all its state within a single List.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AuthPath implements SemanticAuthorizationPath {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(PathPatterns.class);

    // Internal state
    private List<String> segments;

    /**
     * Default constructor, creating the internal state.
     */
    protected AuthPath() {
        this.segments = new ArrayList<>();
    }

    /**
     * Compound constructor creating am AuthPath from the supplied segments.
     *
     * @param segments the segments to
     */
    public AuthPath(final List<String> segments) {

        // Delegate
        this();

        // Check sanity
        Validate.notNull(segments, "segments");
        final int segmentsSize = segments.size();

        Validate.notNull(segmentsSize == 3, "segments.size() != 3 [Got: " + segmentsSize + "]");
        this.segments.addAll(segments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRealm() {
        return segments.size() <= PathSegment.REALM.ordinal()
                ? ""
                : segments.get(PathSegment.REALM.ordinal());
    }

    @Override
    public String getGroup() {
        return segments.size() <= PathSegment.GROUP.ordinal()
                ? ""
                : segments.get(PathSegment.GROUP.ordinal());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getQualifier() {
        return segments.size() <= PathSegment.QUALIFIER.ordinal()
                ? ""
                : segments.get(PathSegment.QUALIFIER.ordinal());
    }

    /**
     * @return An unmodifiable List containing the SemanticAuthorizationPath segments.
     */
    @Override
    public List<String> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        segments.forEach(current -> builder.append(Separators.SEGMENT.toString()).append(current));
        return builder.toString();
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
        return (obj instanceof AuthPath && toString().equals(obj.toString()));
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
        return toString().compareTo(that.toString());
    }

    /**
     * Parses a string consisting of concatenated AuthPaths to a SortedSet of SemanticAuthorizationPaths.
     *
     * @param toParse The String to parse.
     * @return A SortedSet containing the AuthPaths parsed.
     * @throws IllegalArgumentException if any of the paths within the toParse String did not contain 3 segments.
     */
    public static SortedSet<SemanticAuthorizationPath> parse(final String toParse) throws IllegalArgumentException {

        final SortedSet<SemanticAuthorizationPath> toReturn = new TreeSet<>();
        if (toParse != null) {

            final StringTokenizer tok = new StringTokenizer(toParse, Separators.PATTERN.toString(), false);
            while (tok.hasMoreTokens()) {

                final String current = tok.nextToken();
                final StringTokenizer pathTokenizer = new StringTokenizer(
                        current,
                        Separators.SEGMENT.toString(),
                        false);

                // Check sanity
                if (pathTokenizer.countTokens() < 3) {
                    throw new IllegalArgumentException("Expected a path token on the form [realm]/[group]/[qualifier]."
                            + " Got: [" + current + "]");
                }

                final AuthPath toAdd = AuthPathBuilder
                        .create()
                        .withRealm(pathTokenizer.nextToken())
                        .withGroup(pathTokenizer.nextToken())
                        .withQualifier(pathTokenizer.nextToken())
                        .build();

                if (log.isDebugEnabled()) {
                    log.debug("Parsed AuthPath [" + toAdd + "]");
                }

                // Add the AuthPath
                toReturn.add(toAdd);
            }
        }

        // All done.
        return toReturn;
    }
}
