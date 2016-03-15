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

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class which segments path structures into pattern-padded arrays.
 * This Utility class is considerably more intelligent than a StringTokenizer, in that it
 * handles empty segments/tokens and the absence of segments/tokens in better ways than
 * the implemented behaviour of the StringTokenizer.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class Segmenter {

    /**
     * The pattern used to accept any segment value.
     */
    public static final String ANY = "(\\p{javaLetterOrDigit}|_)*";

    /**
     * Hide constructor for utility classes.
     */
    private Segmenter() {
        // Do nothing
    }

    /**
     * Copies the supplied segments to an output String[], replacing empty elements with the
     * {@code ANY} pattern (i.e. <code>{@value #ANY}</code>). If {@code trimSegments} is true, the
     * inbound segments will be {@code trim()}-med before comparing for empty-ness.
     *
     * @param segments     The non-null String[] segments to copy (and potentially modify) before returning.
     * @param trimSegments If {@code trimSegments} is true, the inbound segments will be {@code trim()}-med
     *                     before comparing for empty-ness.
     * @return A Copy of the supplied segments String[] with any empty element replaced with {@value #ANY}.
     */
    public static String[] replaceEmptySegmentsWithAnyPattern(final String[] segments,
                                                              final boolean trimSegments) {

        // Check sanity
        Validate.notNull(segments, "Cannot handle null 'segments' parameter.");

        final String[] toReturn = new String[segments.length];
        for (int i = 0; i < segments.length; i++) {

            final boolean replaceWithAnyPattern = segments[i] != null && trimSegments
                    ? "".equals(segments[i].trim())
                    : "".equals(segments[i]);
            toReturn[i] = replaceWithAnyPattern ? ANY : segments[i];
        }

        // All done.
        return toReturn;
    }

    /**
     * Parses the supplied pattern string into a string array of length 3.
     * The patternString cannot contain more than 3 path segments.
     * Empty path segments will be replaced by {@code #ANY}.
     *
     * @param patternString The pattern string to parse. Expected patternString format:
     *                      {@code [/]realm/group/qualifier}.
     * @return A string array of length 3 parsed from the supplied patternString.
     * @throws IllegalArgumentException if the patternString contained
     *                                  {@code SemanticAuthorizationPath.PATTERN_SEPARATOR} or more than 3
     *                                  {@code SemanticAuthorizationPath.SEGMENT_SEPARATOR} characters.
     * @see #ANY
     */
    public static String[] segment(final String patternString) {

        // Check sanity
        final String expectedFormat = "Expected patternString format: [/]realm/group/qualifier.";
        Validate.notNull(patternString, "patternString");
        Validate.isTrue(!patternString.contains(SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING),
                "Argument 'patternString' cannot contain '" + SemanticAuthorizationPath.PATTERN_SEPARATOR + "' "
                        + "characters. " + expectedFormat);

        // Launder the patternString, and find all indices within the effectivePattern containing segment separators.
        final String effectivePattern = launder(patternString);
        final List<Integer> segmentIndices = new ArrayList<>();
        for (int i = 0; i < effectivePattern.length(); i++) {
            if (effectivePattern.charAt(i) == SemanticAuthorizationPath.SEGMENT_SEPARATOR) {
                segmentIndices.add(i);
            }
        }
        Validate.isTrue(segmentIndices.size() < 3, "A patternString may contain a maximum of 3 segments. Got ["
                + segmentIndices.size() + "] for patternString [" + patternString + "]");

        // All done.
        return segment(effectivePattern, segmentIndices);
    }

    //
    // Private helpers
    //

    private static String[] segment(final String toSegment,
                                    final List<Integer> separatorIndices) {

        final String[] toReturn = new String[]{"", "", ""};
        final boolean[] assigned = new boolean[]{false, false, false};

        int startIndex = 0;
        int endIndex = -1;

        for (int i = 0; i < separatorIndices.size(); i++) {

            // Update the endIndex
            endIndex = separatorIndices.get(i);

            // Check sanity on the current segment, and assign the parsed substring.
            toReturn[i] = toSegment.substring(startIndex, endIndex);
            assigned[i] = true;

            // Update the startIndex
            startIndex = endIndex + SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING.length();
        }

        // Add the last part of the pattern
        if (endIndex != toSegment.length()) {
            int index = 0;
            for (int i = 0; i < assigned.length; i++) {
                if (!assigned[i]) {
                    index = i;
                    break;
                }
            }

            // Don't process the results.
            toReturn[index] = toSegment.substring(startIndex, toSegment.length());
        }

        // All done.
        return toReturn;
    }

    private static String launder(final String patternString) {

        final String trimmed = patternString.trim();
        if (trimmed.startsWith(SemanticAuthorizationPath.SEGMENT_SEPARATOR_STRING)) {
            return trimmed.substring(SemanticAuthorizationPath.SEGMENT_SEPARATOR_STRING.length());
        }

        // All done.
        return trimmed;
    }
}
