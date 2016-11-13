/*
 * #%L
 * Nazgul Project: mithlond-services-shared-authorization-model
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
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
 * Specification for how to check/verify a single SemanticAuthorizationPath against a supplied pattern.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@FunctionalInterface
public interface SemanticAuthorizationPathFilter {

    /**
     * The prefix of a pattern implying that it should be interpreted as a globber pattern.
     */
    String GLOB_PREFIX = "glob:";

    /**
     * Standard filter method to implement in order to filter/verify the supplied
     * SemanticAuthorizationPath with the supplied filter. If the pattern is {@code null}, the result is undefined
     * and if the path is {@code null} the result should normally be a failure.
     *
     * @param pattern The {@link SemanticAuthorizationPath} to filter.
     * @param path    The pattern to filter with.
     * @return true if the path matched the supplied pattern, and false otherwise.
     */
    boolean filter(String pattern, SemanticAuthorizationPath path);
}
