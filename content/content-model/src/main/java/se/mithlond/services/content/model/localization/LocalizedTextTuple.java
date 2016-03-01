/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
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
package se.mithlond.services.content.model.localization;

import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;
import java.util.Objects;

/**
 * Transport model for a localization and a text.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class LocalizedTextTuple implements Comparable<LocalizedTextTuple> {

    /**
     * The compact transport form for a Localization.
     */
    @XmlAttribute(required = true)
    private String locale;

    /**
     * The localized text.
     */
    @XmlValue
    private String text;

    /**
     * JAXB-friendly constructor.
     */
    public LocalizedTextTuple() {
    }

    /**
     * Compound constructor, creating a LocalizedText wrapping the supplied values.
     *
     * @param localization a non-null Localization.
     * @param text         a non-null text.
     */
    public LocalizedTextTuple(final Localization localization, final String text) {

        // Check sanity
        Validate.notNull(localization, "localization");
        Validate.notEmpty(text, "text");

        // Assign internal state
        this.text = text;
        this.locale = localization.toString();
    }

    /**
     * @return The wrapped Localization.
     */
    public Localization getLocalization() {
        return Localization.parse(locale);
    }

    /**
     * @return The localized text.
     */
    public String getText() {
        return text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final LocalizedTextTuple that) {

        // Return fast
        if (that == this) {
            return 0;
        } else if (that == null) {
            return -1;
        }

        // Delegate to internals.
        int toReturn = locale.compareTo(that.locale);
        if (toReturn == 0) {
            toReturn = text.compareTo(that.text);
        }
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final LocalizedTextTuple that = (LocalizedTextTuple) o;
        return Objects.equals(locale, that.locale) &&
                Objects.equals(text, that.text);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(locale, text);
    }
}
