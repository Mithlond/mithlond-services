package se.mithlond.services.shared.spi.jaxb.adapter;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * XML Adapter class to handle Java 8 {@link LocalDateTime} - which will convert to
 * and from Strings using the {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME}.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public class LocalDateTimeAdapter extends XmlAdapter<String, LocalDateTime> {

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDateTime unmarshal(final String transportForm) throws Exception {

        // Handle nulls
        if (transportForm == null) {
            return null;
        }

        return LocalDateTime.parse(transportForm, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String marshal(final LocalDateTime objectForm) throws Exception {

        // Handle nulls
        if (objectForm == null) {
            return null;
        }

        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(objectForm);
    }
}
