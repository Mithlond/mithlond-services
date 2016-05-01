/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-jpa
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
package se.mithlond.services.shared.spi.jpa.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.Date;
import java.time.LocalDate;

/**
 * JPA AttributeConverter class to handle Java 8 {@link LocalDate} - which will convert to
 * and from {@link java.sql.Date}s.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Converter(autoApply = true)
public class LocalDateAttributeConverter implements AttributeConverter<LocalDate, Date> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Date convertToDatabaseColumn(final LocalDate attribute) {

        // Handle nulls
        if(attribute == null) {
            return null;
        }

        return Date.valueOf(attribute);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDate convertToEntityAttribute(final Date dbData) {

        // Handle nulls
        if(dbData == null) {
            return null;
        }

        return dbData.toLocalDate();
    }
}
