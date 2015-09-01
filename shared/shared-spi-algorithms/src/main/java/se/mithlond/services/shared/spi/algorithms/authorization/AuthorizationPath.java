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

import java.util.regex.Pattern;

/**
 * Implementation-neutral specification of paths containing
 * Authorization information, concatenated into a path-like String.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface AuthorizationPath {

    /**
     * The separator char which may be used to separate different instances
     * (i.e. different AuthorizationPaths).
     */
    String INSTANCE_SEPARATOR = ",";

    /**
     * The character used to separate semantic parts of AuthorizationPaths.
     */
    String PATH_SEPARATOR = "/";

    /**
     * A constant to indicate that the value in question is unimportant (i.e. that we don't care about it).
     */
    String DONT_CARE = "dont_care";

    /**
     * @return The Realm of this AuthorizationPath. Should never be {@code null}; use {@code #DONT_CARE} in that case.
     */
    String getRealm();

    /**
     * @return The Group of this AuthorizationPath. Should never be {@code null}; use {@code #DONT_CARE} in that case.
     */
    String getGroup();

    /**
     * @return The qualifier of this AuthorizationPath. Should never be {@code null};
     * use {@code #DONT_CARE} in that case.
     */
    String getQualifier();

    /**
     * Retrieves a Pattern which could be used to match this AuthorizationPath in a regexp search.
     *
     * @return a Pattern which could be used to match this AuthorizationPath in a regexp search.
     */
    default Pattern getPattern() {

        final String realmPattern = getRealm() != null && !getRealm().equalsIgnoreCase(DONT_CARE)
                ? getRealm()
                : ".*";
        final String groupPattern = getGroup() != null && !getGroup().equalsIgnoreCase(DONT_CARE)
                ? getGroup()
                : ".*";
        final String qualifierPattern = getQualifier() != null && !getQualifier().equalsIgnoreCase(DONT_CARE)
                ? getQualifier()
                : ".*";

        // All done.
        return Pattern.compile(INSTANCE_SEPARATOR + realmPattern
                        + INSTANCE_SEPARATOR + groupPattern
                        + INSTANCE_SEPARATOR + qualifierPattern,
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
    }
}
