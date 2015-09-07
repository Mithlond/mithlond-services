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
                "Argument 'patternString' cannot contain '" + SemanticAuthorizationPath.PATTERN_SEPARATOR + "' " +
                        "characters. " + expectedFormat);

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
                                    List<Integer> separatorIndices) {

        final String[] toReturn = new String[]{ANY, ANY, ANY};
        final boolean[] assigned = new boolean[]{false, false, false};

        int startIndex = 0;
        int endIndex = -1;

        for (int i = 0; i < separatorIndices.size(); i++) {

            // Update the endIndex
            endIndex = separatorIndices.get(i);

            // Check sanity on the current segment, and assign the parsed substring.
            toReturn[i] = getOrReplaceWithAnyPattern(toSegment.substring(startIndex, endIndex));
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

            toReturn[index] = getOrReplaceWithAnyPattern(toSegment.substring(startIndex, toSegment.length()));
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

    private static String getOrReplaceWithAnyPattern(final String aString) {
        return "".equals(aString) ? ANY : aString;
    }
}
