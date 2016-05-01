package se.mithlond.services.shared.spi.jaxb.adapter;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * XML Adapter class to handle Java 8 {@link LocalDate} - which will convert to
 * and from Strings using the {@link DateTimeFormatter#ISO_LOCAL_DATE}.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

    /**
     * Use the {@link DateTimeFormatter#ISO_LOCAL_DATE}.
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalDate unmarshal(final String transportForm) throws Exception {

        // Handle nulls
        if(transportForm == null)  {
            return null;
        }

        return LocalDate.parse(transportForm, FORMATTER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String marshal(final LocalDate instant) throws Exception {

        // Handle nulls
        if(instant == null) {
            return null;
        }

        return FORMATTER.format(instant);
    }
}
