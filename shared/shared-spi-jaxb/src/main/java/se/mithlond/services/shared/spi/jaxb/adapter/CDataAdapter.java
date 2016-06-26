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

/**
 * XML Adapter class to surround String values with CDATA blocks during marshalling,
 * and remove those blocks during unmarshalling. This is usable when wanting to send
 * markup embedded in XML.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public class CDataAdapter extends XmlAdapter<String, String> {

    /**
     * The start block of a CDATA structure.
     */
    public static final String CDATA_START = "<![CDATA[";

    /**
     * The end block of a CDATA structure.
     */
    public static final String CDATA_END = "]]>";

    /**
     * {@inheritDoc}
     */
    @Override
    public String unmarshal(final String toUnmarshal) throws Exception {

        if(toUnmarshal == null) {
            return null;
        }

        // Should we peel off the CDATA blocks from the toUnmarshal String?
        final boolean peelOffCDataStart = toUnmarshal.startsWith(CDATA_START);
        final boolean peelOffCDataEnd = toUnmarshal.endsWith(CDATA_END);

        final int startIndex = peelOffCDataStart ? CDATA_START.length() : 0;
        final int endIndex = peelOffCDataEnd ? toUnmarshal.length() - CDATA_END.length() : toUnmarshal.length();

        // All Done.
        return toUnmarshal.substring(startIndex, endIndex);
    }

    /**
     * {@inheritDoc}
     */
    public String marshal(final String toMarshal) throws Exception {
        return toMarshal == null ? null : CDATA_START + toMarshal + CDATA_END;
    }
}
