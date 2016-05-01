package se.mithlond.services.shared.spi.jaxb.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.TimeZone;

/**
 * XML Adapter class to handle Java 8 {@link TimeZone} - which will convert to
 * and from Strings using the {@link TimeZone#getTimeZone(String)}.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class TimeZoneAdapter extends XmlAdapter<String, TimeZone> {

    /**
     * {@inheritDoc}
     */
    @Override
    public TimeZone unmarshal(final String transportForm) throws Exception {

        // Handle nulls
        if(transportForm == null) {
            return null;
        }

        return TimeZone.getTimeZone(transportForm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String marshal(final TimeZone objectForm) throws Exception {

        // Handle nulls
        if(objectForm == null) {
            return null;
        }

        return objectForm.getID();
    }
}
