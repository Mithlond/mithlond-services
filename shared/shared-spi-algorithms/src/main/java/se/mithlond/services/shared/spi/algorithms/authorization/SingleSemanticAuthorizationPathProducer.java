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

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Convenience implementation for SemanticAuthorizationPathProducers which will ever
 * only produce a single SemanticAuthorizationPath.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface SingleSemanticAuthorizationPathProducer extends SemanticAuthorizationPathProducer {

    /**
     * Main factory method, creating the SemanticAuthorizationPath instance
     * from this SingleSemanticAuthorizationPathProducer.
     *
     * @return the SemanticAuthorizationPath instance of this SingleSemanticAuthorizationPathProducer.
     */
    SemanticAuthorizationPath createPath();

    /**
     * {@inheritDoc}
     */
    @Override
    default SortedSet<SemanticAuthorizationPath> getPaths() {
        final SortedSet<SemanticAuthorizationPath> toReturn = new TreeSet<>();
        toReturn.add(createPath());
        return toReturn;
    }
}
