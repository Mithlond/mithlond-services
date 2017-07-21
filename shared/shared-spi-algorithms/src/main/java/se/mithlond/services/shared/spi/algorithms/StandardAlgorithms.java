/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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
package se.mithlond.services.shared.spi.algorithms;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Simple, reusable algorithms.
 * 
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public final class StandardAlgorithms {

    private StandardAlgorithms() {
        // Do nothing
    }

    /**
     * Trims whitespace and replaces all whitespace within the toTrim string with a single space.
     *
     * @param toTrim The string to trim.
     * @return {@code null} if the inbound toTrim string is null and otherwise a trimmed version of the inbound string.
     */
    public static String trimAndHarmonizeWhitespace(final String toTrim) {

        if (toTrim == null) {
            return null;
        }

        // All Done.
        return toTrim.trim().replaceAll("\\s+", " ").trim();
    }
}
