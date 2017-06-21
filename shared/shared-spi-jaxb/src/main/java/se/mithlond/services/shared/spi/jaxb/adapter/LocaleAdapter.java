/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-jaxb
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
package se.mithlond.services.shared.spi.jaxb.adapter;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Locale;

/**
 * XML Adapter class to handle {@link Locale} objects.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public class LocaleAdapter extends XmlAdapter<String, Locale> {

    /**
     * {@inheritDoc}
     */
    @Override
    public Locale unmarshal(final String transportForm) throws Exception {

        // Handle nulls
        if (transportForm == null) {
            return null;
        }

        // All Done.
        return Locale.forLanguageTag(transportForm);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String marshal(final Locale objectForm) throws Exception {

        // Handle nulls
        if (objectForm == null) {
            return null;
        }

        // All Done.
        return objectForm.toLanguageTag();
    }
}
