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

import javax.validation.constraints.NotNull;

/**
 * Specification of a semantically structured path defining authorization.
 * While technically a path can contain an arbitrary amount of segments, this specification provides semantic
 * meaning to 3 levels - Realm/Group/Qualifier. Any of these properties may be empty, in which case it indicates
 * that any value (including nulls) match that particular segment within the SemanticAuthorizationPath.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface SemanticAuthorizationPath extends Comparable<SemanticAuthorizationPath> {

    /**
     * Enum specification of the segments used by a SemanticAuthorizationPath.
     */
    enum Segment {

        /**
         * The Realm segment, normally indicating the segment with the highest priority (i.e. topmost group)
         * within a SemanticAuthorizationPath.
         */
        REALM,

        /**
         * The Group segment, normally indicating the segment between the realm and qualifier
         * within a SemanticAuthorizationPath.
         */
        GROUP,

        /**
         * The Qualifier segment, normally indicating the segment with the lowest priority (i.e. bottom-most group)
         * within a SemanticAuthorizationPath.
         */
        QUALIFIER
    }

    /**
     * Separator used to split concatenated SemanticAuthorizationPaths from one another.
     */
    char PATTERN_SEPARATOR = ',';

    /**
     * Separator used to split concatenated SemanticAuthorizationPaths from one another. (Cast to a String).
     */
    String PATTERN_SEPARATOR_STRING = Character.toString(PATTERN_SEPARATOR);

    /**
     * Separator used to split concatenated segments within a SemanticAuthorizationPath from one another.
     */
    char SEGMENT_SEPARATOR = '/';

    /**
     * Separator used to split concatenated segments within a SemanticAuthorizationPath from one another.
     * (Cast to a String).
     */
    String SEGMENT_SEPARATOR_STRING = Character.toString(SEGMENT_SEPARATOR);

    /**
     * @return The Realm of this AuthorizationPath. Should never return a {@code null} value.
     */
    default String getRealm() {
        return getSegment(Segment.REALM);
    }

    /**
     * @return The Group of this AuthorizationPath. Should never return a {@code null} value.
     */
    default String getGroup() {
        return getSegment(Segment.GROUP);
    }

    /**
     * @return The qualifier of this AuthorizationPath. Should never return a {@code null} value.
     */
    default String getQualifier() {
        return getSegment(Segment.QUALIFIER);
    }

    /**
     * Retrieves the given segment within this SemanticAuthorizationPath.
     *
     * @param segment the given segment within this SemanticAuthorizationPath.
     * @return the given segment within this SemanticAuthorizationPath.
     */
    String getSegment(@NotNull final Segment segment);

    /**
     * @return A String/path representation of this SemanticAuthorizationPath.
     */
    default String getPath() {
        return SEGMENT_SEPARATOR + getRealm().trim()
                + SEGMENT_SEPARATOR + getGroup().trim()
                + SEGMENT_SEPARATOR + getQualifier().trim();
    }
}
