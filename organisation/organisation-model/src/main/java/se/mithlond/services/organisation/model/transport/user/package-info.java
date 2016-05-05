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
 * Package containing transport classes for entities.
 * These transport classes are only JAXB-annotated and resides within a separate Namespace.
 * Transport classes are either annotated with {@link javax.xml.bind.annotation.XmlRootElement} or have the suffix
 * <strong>VO</strong> to indicate that they are ValueObjects simply used to transmit data.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 * @see javax.xml.bind.annotation.XmlRootElement
 */
@XmlSchema(
        xmlns = {
                @XmlNs(prefix = "shared", namespaceURI = SharedJaxbPatterns.NAMESPACE),
                @XmlNs(prefix = "organisation", namespaceURI = OrganisationPatterns.NAMESPACE),
                @XmlNs(prefix = "organisation_transport", namespaceURI = OrganisationPatterns.TRANSPORT_NAMESPACE),
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
        @XmlJavaTypeAdapter(type = TimeZone.class, value = TimeZoneAdapter.class)
})
@XmlAccessorType(XmlAccessType.FIELD)
package se.mithlond.services.organisation.model.transport.user;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.spi.jaxb.SharedJaxbPatterns;
import se.mithlond.services.shared.spi.jaxb.adapter.LocalDateAdapter;
import se.mithlond.services.shared.spi.jaxb.adapter.LocalDateTimeAdapter;
import se.mithlond.services.shared.spi.jaxb.adapter.LocalTimeAdapter;
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
import java.util.TimeZone;