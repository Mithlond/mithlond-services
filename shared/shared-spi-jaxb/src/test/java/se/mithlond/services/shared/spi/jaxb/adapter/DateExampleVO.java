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
