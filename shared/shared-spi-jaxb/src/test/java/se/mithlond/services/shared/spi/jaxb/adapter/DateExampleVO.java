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

import se.mithlond.services.shared.spi.jaxb.SharedJaxbPatterns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.TimeZone;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = SharedJaxbPatterns.NAMESPACE)
@XmlType(namespace = SharedJaxbPatterns.NAMESPACE,
        propOrder = {"lastAdmissionDate", "eventStartTime", "eventEndTime", "admissionTime", "eventTimeZone"})
@XmlAccessorType(XmlAccessType.FIELD)
public class DateExampleVO {

    @XmlAttribute
    private LocalDate lastAdmissionDate;

    @XmlElement
    private LocalDateTime eventStartTime;

    @XmlElement
    private LocalTime eventEndTime;

    @XmlAttribute
    private ZonedDateTime admissionTime;

    @XmlElement
    private TimeZone eventTimeZone;

    public DateExampleVO() {
    }

    public DateExampleVO(
            final LocalDate lastAdmissionDate,
            final LocalDateTime eventStartTime,
            final LocalTime eventEndTime,
            final ZonedDateTime admissionTime,
            final TimeZone eventTimeZone) {

        this.lastAdmissionDate = lastAdmissionDate;
        this.eventStartTime = eventStartTime;
        this.admissionTime = admissionTime;
        this.eventEndTime = eventEndTime;
        this.eventTimeZone = eventTimeZone;
    }

    public LocalDate getLastAdmissionDate() {
        return lastAdmissionDate;
    }

    public LocalDateTime getEventStartTime() {
        return eventStartTime;
    }

    public ZonedDateTime getAdmissionTime() {
        return admissionTime;
    }

    public LocalTime getEventEndTime() {
        return eventEndTime;
    }

    public TimeZone getEventTimeZone() {
        return eventTimeZone;
    }
}
