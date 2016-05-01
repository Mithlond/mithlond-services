package se.mithlond.services.shared.spi.jaxb.adapter;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * XML Adapter class to handle Java 8 {@link LocalTime} - which will convert to
 * and from Strings using the {@link DateTimeFormatter#ISO_LOCAL_TIME}.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public class LocalTimeAdapter extends XmlAdapter<String, LocalTime> {

    /**
     * {@inheritDoc}
     */
    @Override
    public LocalTime unmarshal(final String transportForm) throws Exception {

        // Handle nulls
        if(transportForm == null) {
            return null;
        }

        return LocalTime.parse(transportForm, DateTimeFormatter.ISO_LOCAL_TIME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String marshal(final LocalTime objectForm) throws Exception {

        if(objectForm == null) {
            return null;
        }

        return DateTimeFormatter.ISO_LOCAL_TIME.format(objectForm);
    }
}
