/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-jaxb
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
     * {@inheritDoc}
     */
    @Override
    public LocalDate unmarshal(final String transportForm) throws Exception {
        return transportForm == null ? null : LocalDate.parse(transportForm, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String marshal(final LocalDate instant) throws Exception {
        return instant == null ? null : DateTimeFormatter.ISO_LOCAL_DATE.format(instant);
    }
}
