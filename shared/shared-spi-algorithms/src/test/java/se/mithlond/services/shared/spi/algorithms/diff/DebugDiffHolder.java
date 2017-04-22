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

import se.jguru.nazgul.core.algorithms.api.Validate;

import java.util.Optional;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class DebugDiffHolder implements DiffHolder<String, StringBuffer, DebugDiffHolder> {

    // Internal state
    private String actual;
    private StringBuffer comparison;

    public DebugDiffHolder(final String actual, final StringBuffer comparison) {
        this.actual = actual;
        this.comparison = comparison;
    }

    @Override
    public Optional<String> getActual() {
        return actual == null ? Optional.empty() : Optional.of(actual);
    }

    @Override
    public Optional<StringBuffer> getComparison() {
        return comparison == null ? Optional.empty() : Optional.of(comparison);
    }

    @Override
    public void setComparison(final StringBuffer comparison) {
        this.comparison = Validate.notNull(comparison, "comparison");
    }

    @Override
    public void setActual(final String actual) {
        this.actual = Validate.notNull(actual, "actual");
    }

    @Override
    public int compareTo(final DebugDiffHolder o) {

        if(this == o) {
            return 0;
        } else if (o == null) {
            return -1;
        }


        int toReturn = this.getActual().orElse("").compareTo(o.getActual().orElse(""));
        if(toReturn == 0) {
            final String thisComparison = this.getComparison().isPresent() ? this.getComparison().toString() : "";
            final String thatComparison = o.getComparison().isPresent() ? o.getComparison().toString() : "";

            toReturn = thisComparison.compareTo(thatComparison);
        }

        return toReturn;
    }
}
