/*-
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-jpa
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
package se.mithlond.services.shared.spi.jpa.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Locale;

/**
 * JPA AttributeConverter class to handle {@link java.util.Locale}s - which will convert to
 * and from {@link String}s using the {@link Locale#toLanguageTag()} and {@link Locale#forLanguageTag(String)}
 * methods.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Converter(autoApply = true)
public class LocaleConverter implements AttributeConverter<Locale, String>  {

    /**
     * {@inheritDoc}
     */
    @Override
    public String convertToDatabaseColumn(final Locale attribute) {

        // Handle nulls
        if(attribute == null) {
            return null;
        }

        // All Done.
        return attribute.toLanguageTag();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale convertToEntityAttribute(final String dbData) {

        // Handle nulls
        if(dbData == null) {
            return null;
        }

        // All Done.
        return Locale.forLanguageTag(dbData);
    }
}
