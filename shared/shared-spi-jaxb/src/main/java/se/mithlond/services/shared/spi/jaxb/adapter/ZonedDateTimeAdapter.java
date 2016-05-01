package se.mithlond.services.shared.spi.jaxb.adapter;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * XML Adapter class to handle Java 8 {@link ZonedDateTime} - which will convert to
 * and from Strings using the {@link DateTimeFormatter#ISO_ZONED_DATE_TIME}.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see DateTimeFormatter#ISO_ZONED_DATE_TIME
 */
@XmlTransient
public class ZonedDateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {

    /**
     * {@inheritDoc}
     */
    @Override
    public ZonedDateTime unmarshal(final String transportForm) throws Exception {

        // Handle nulls
        if(transportForm == null) {
            return null;
        }

        return ZonedDateTime.parse(transportForm, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String marshal(final ZonedDateTime dateTime) throws Exception {

        // Handle nulls
        if(dateTime == null) {
            return null;
        }

        return DateTimeFormatter.ISO_ZONED_DATE_TIME.format(dateTime);
    }
}
