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
