/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
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
package se.mithlond.services.shared.spi.algorithms.diff;

import java.io.Serializable;
import java.util.Optional;

/**
 * Specification for a container holding objects which can be compared to one another.
 * The terminology is <strong>actual</strong> and <strong>comparison</strong> for the two objects.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface DiffHolder<A, C, S extends DiffHolder<A, C, S>> extends Comparable<S>, Serializable {

    /**
     * Enumeration of relevant types of modification.
     */
    enum Modification {

        /**
         * Created/New - implying that {@link #getActual()} is not present and {@link #getComparison()} is present.
         */
        CREATED,

        /**
         * Modified - implying that both {@link #getActual()} and {@link #getComparison()} are present.
         */
        MODIFIED,

        /**
         * Deleted - implying that {@link #getActual()} nor {@link #getComparison()} are present.
         */
        DELETED,

        /**
         * Unknown - implying that neither {@link #getActual()} nor {@link #getComparison()} are present.
         */
        UNKNOWN;
    }

    /**
     * @return The optional Left object.
     */
    Optional<A> getActual();

    /**
     * @return The optional Right object.
     */
    Optional<C> getComparison();

    /**
     * Assigns the object to compare with.
     *
     * @param comparison the non-null object to compare to the {@link #getActual()} object.
     */
    void setComparison(final C comparison);

    /**
     * Assigns the actual object to use as a baseline for comparing to the {@link #getComparison()} object.
     *
     * @param actual The non-null actual object used as a basis for comparing against.
     */
    void setActual(final A actual);

    /**
     * @return The Modification status of this DiffHolder, using a default existence-based algorithm.
     */
    default Modification getModification() {

        if (getActual().isPresent()) {
            return getComparison().isPresent()
                    ? Modification.MODIFIED
                    : Modification.DELETED;
        }

        return getComparison().isPresent()
                ? Modification.CREATED
                : Modification.UNKNOWN;
    }
}
