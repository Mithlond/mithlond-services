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

import java.util.SortedSet;
import java.util.stream.Stream;

/**
 * Standard specification for how to produce SemanticAuthorizationPaths.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@FunctionalInterface
public interface SemanticAuthorizationPathProducer {

    /**
     * @return The SortedSet of SemanticAuthorizationPath instances produced by this SemanticAuthorizationPathProducer.
     */
    SortedSet<SemanticAuthorizationPath> getPaths();

    /**
     * Convenience method to create a Stream of {@link SemanticAuthorizationPath}s from this producer.
     *
     * @return A Stream containing the SemanticAuthorizationPath objects retrieved by the {@link #getPaths()} method,
     * and therefore ordered in a sorted manner.
     */
    default Stream<SemanticAuthorizationPath> stream() {
        return getPaths().stream();
    }
}
