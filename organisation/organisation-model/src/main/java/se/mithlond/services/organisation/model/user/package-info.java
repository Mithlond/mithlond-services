/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 Mithlond
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
/**
 * <p>Package containing User types, corresponding to basic information related to users.
 * Note that <strong>credentials</strong> (certificates, password hashes, etc) are not handled by the types within
 * this package. Instead, the assumption is that another system (typically and IDM) will manage users and credentials
 * to perform authentication. Authorization, however, is more closely related to the service model, and will hence be
 * managed by the Mithlond Services project.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlSchema(
        xmlns = {
                @XmlNs(prefix = "shared", namespaceURI = SharedJaxbPatterns.NAMESPACE),
                @XmlNs(prefix = "organisation", namespaceURI = OrganisationPatterns.NAMESPACE),
                @XmlNs(prefix = "xs", namespaceURI = "http://www.w3.org/2001/XMLSchema"),
                @XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance"),
                @XmlNs(prefix = "vc", namespaceURI = "http://www.w3.org/2007/XMLSchema-versioning")
        }
)
@XmlJavaTypeAdapters({
        @XmlJavaTypeAdapter(type = LocalDate.class, value = LocalDateAdapter.class),
        @XmlJavaTypeAdapter(type = LocalTime.class, value = LocalTimeAdapter.class),
        @XmlJavaTypeAdapter(type = LocalDateTime.class, value = LocalDateTimeAdapter.class),
        @XmlJavaTypeAdapter(type = ZonedDateTime.class, value = ZonedDateTimeAdapter.class),
        @XmlJavaTypeAdapter(type = TimeZone.class, value = TimeZoneAdapter.class),
        @XmlJavaTypeAdapter(type = Locale.class, value = LocaleAdapter.class)
})
@XmlAccessorType(XmlAccessType.FIELD)
package se.mithlond.services.organisation.model.user;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.spi.jaxb.SharedJaxbPatterns;
import se.mithlond.services.shared.spi.jaxb.adapter.LocalDateAdapter;
import se.mithlond.services.shared.spi.jaxb.adapter.LocalDateTimeAdapter;
import se.mithlond.services.shared.spi.jaxb.adapter.LocalTimeAdapter;
import se.mithlond.services.shared.spi.jaxb.adapter.LocaleAdapter;
import se.mithlond.services.shared.spi.jaxb.adapter.TimeZoneAdapter;
import se.mithlond.services.shared.spi.jaxb.adapter.ZonedDateTimeAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Locale;
import java.util.TimeZone;