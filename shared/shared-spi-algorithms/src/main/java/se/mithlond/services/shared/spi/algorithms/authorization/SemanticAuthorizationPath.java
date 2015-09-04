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

import java.util.List;

/**
 * Specification of a semantically structured path defining authorization.
 * While technically a path can contain an arbitrary amount of segments, common practise restricts
 * SemanticAuthorizationPaths to 3 levels - Realm/Group/Qualifier. Hence, all SemanticAuthorizationPaths
 * must contain exactly 3 segments.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface SemanticAuthorizationPath extends Comparable<SemanticAuthorizationPath> {

    /**
     * Enum defining the separators used to parse and synthesize PathPatterns.
     */
    enum Separators {

        /**
         * Separator used to split concatenated Patterns from one another.
         */
        PATTERN(","),

        /**
         * Separator used to split concatenated (string) segments within a Patterns from one another.
         */
        SEGMENT("/");

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

    /**
     * Enumeration specifying standard authorization path semantics
     * for each path segment, assumed to have the same index in the path
     * as the ordinal of this enum.
     */
    enum PathSegment {

        REALM,
        GROUP,
        QUALIFIER;
    }

    /**
     * @return The Realm of this AuthorizationPath. Should never be {@code null}.
     */
    String getRealm();

    /**
     * @return The Group of this AuthorizationPath. Should never be {@code null}.
     */
    String getGroup();

    /**
     * @return The qualifier of this AuthorizationPath. Should never be {@code null}.
     */
    String getQualifier();

    /**
     * @return All segments of this SemanticAuthorizationPath.
     */
    List<String> getSegments();
}
