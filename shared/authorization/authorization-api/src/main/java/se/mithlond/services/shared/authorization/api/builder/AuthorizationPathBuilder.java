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
package se.mithlond.services.shared.authorization.api.builder;

import se.mithlond.services.shared.authorization.api.Segmenter;
import se.mithlond.services.shared.authorization.model.AuthorizationPath;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;
import se.mithlond.services.shared.spi.algorithms.Validate;

import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * <p>Builder class to simplify creating AuthorizationPath instances. Typical usage:</p>
 * <pre>
 *     <code>
 *         // Create an AuthorizationPath using the builder.
 *         final AuthorizationPath mithlondPath = AuthorizationPathBuilder
 *               .create()
 *               .withGroup("mithlond")
 *               .build();
 *     </code>
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class AuthorizationPathBuilder {

    // Internal state
    private String group = "";
    private String realm = "";
    private String qualifier = "";

    /**
     * Hide the constructor of the builder; use static factory methods instead.
     */
    private AuthorizationPathBuilder() {
    }

    /**
     * Factory method to retrieve an AuthPathBuilder.
     *
     * @return a newly created AuthPathBuilder.
     */
    public static AuthorizationPathBuilder create() {
        return new AuthorizationPathBuilder();
    }

    /**
     * Assigns the realm to the AuthPath being built.
     *
     * @param realm The realm of the AuthPath being built.
     * @return This AuthPathBuilder, for chaining.
     */
    public AuthorizationPathBuilder withRealm(final String realm) {

        // Check sanity
        Validate.notNull(realm, "realm");

        // Assign and return
        this.realm = realm;
        return this;
    }

    /**
     * Assigns the group to the AuthPath being built.
     *
     * @param group The group of the AuthPath being built.
     * @return This AuthPathBuilder, for chaining.
     */
    public AuthorizationPathBuilder withGroup(final String group) {

        // Check sanity
        Validate.notNull(group, "group");

        // Assign and return
        this.group = group;
        return this;
    }

    /**
     * Assigns the qualifier to the AuthPath being built.
     *
     * @param qualifier The qualifier of the AuthPath being built.
     * @return This AuthPathBuilder, for chaining.
     */
    public AuthorizationPathBuilder withQualifier(final String qualifier) {

        // Check sanity
        Validate.notNull(qualifier, "qualifier");

        // Assign and return
        this.qualifier = qualifier;
        return this;
    }

    /**
     * @return The resulting AuthorizationPath.
     */
    public AuthorizationPath build() {
        return new AuthorizationPath(realm, group, qualifier);
    }

    /**
     * Parses the supplied concatenatedPatterns into several AuthorizationPath instances.
     * Since the AuthorizationPattern class could contain regular expressions in its respective
     *
     * @param concatenatedPaths A string containing concatenated AuthorizationPaths.
     * @return a SortedSet containing AuthorizationPath instances, extracted from the concatenatedPatterns string.
     */
    public static SortedSet<SemanticAuthorizationPath> parse(final String concatenatedPaths) {

        final SortedSet<SemanticAuthorizationPath> toReturn = new TreeSet<>();
        if (concatenatedPaths != null && !concatenatedPaths.isEmpty()) {

            final StringTokenizer tok = new StringTokenizer(
                    concatenatedPaths,
                    SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING,
                    false);

            while (tok.hasMoreTokens()) {
                final String[] segments = Segmenter.segment(tok.nextToken());
                toReturn.add(new AuthorizationPath(segments[0], segments[1] , segments[2]));
            }
        }

        // All done.
        return toReturn;
    }
}
