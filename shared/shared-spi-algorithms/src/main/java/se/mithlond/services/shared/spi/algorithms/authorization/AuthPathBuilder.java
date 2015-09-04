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

import se.mithlond.services.shared.spi.algorithms.Validate;

import java.util.Arrays;

/**
 * Builder class to simplify creating AuthPath instances.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class AuthPathBuilder {

    /**
     * The pattern used to accept any segment value.
     */
    public static final String ANY = "\\p{javaLetterOrDigit}*";

    // Internal state
    private String[] segments;

    /**
     * Hide the constructor of the builder, and use a factory instead.
     */
    private AuthPathBuilder() {
        segments = new String[3];
        for(int i = 0; i < segments.length; i++) {
            segments[i] = ANY;
        }
    }

    /**
     * Factory method to retrieve an AuthPathBuilder.
     *
     * @return a newly created AuthPathBuilder.
     */
    public static AuthPathBuilder create() {
        return new AuthPathBuilder();
    }

    /**
     * Assigns the realm to the AuthPath being built.
     *
     * @param realm The realm of the AuthPath being built.
     * @return This AuthPathBuilder, for chaining.
     */
    public AuthPathBuilder withRealm(final String realm) {
        return with(SemanticAuthorizationPath.PathSegment.REALM, realm);
    }

    /**
     * Assigns the group to the AuthPath being built.
     *
     * @param group The group of the AuthPath being built.
     * @return This AuthPathBuilder, for chaining.
     */
    public AuthPathBuilder withGroup(final String group) {
        return with(SemanticAuthorizationPath.PathSegment.GROUP, group);
    }

    /**
     * Assigns the qualifier to the AuthPath being built.
     *
     * @param qualifier The qualifier of the AuthPath being built.
     * @return This AuthPathBuilder, for chaining.
     */
    public AuthPathBuilder withQualifier(final String qualifier) {
        return with(SemanticAuthorizationPath.PathSegment.QUALIFIER, qualifier);
    }

    /**
     * @return The resulting AuthPath.
     */
    public AuthPath build() {
        return new AuthPath(Arrays.asList(segments));
    }

    //
    // Internal state
    //

    private AuthPathBuilder with(final SemanticAuthorizationPath.PathSegment segment, final String value) {

        // Check sanity
        Validate.notEmpty(value, segment.name());

        // Assign internal state
        segments[segment.ordinal()] = value;

        // All done.
        return this;
    }
}
