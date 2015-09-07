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

/**
 * Specification of a semantically structured path defining authorization.
 * While technically a path can contain an arbitrary amount of segments, common practise restricts
 * SemanticAuthorizationPaths to 3 levels - Realm/Group/Qualifier.
 * Hence, all SemanticAuthorizationPaths must contain exactly 3 segments.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface SemanticAuthorizationPath extends Comparable<SemanticAuthorizationPath> {

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
}
