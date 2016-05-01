package se.mithlond.services.organisation.api.transport.activity;

import se.mithlond.services.organisation.api.transport.organisation.OrganisationVO;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.Amount;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransportable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Set;

/**
 * Transport object containing data required for creating or modifying
 * Activity state.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"organisation", "shortDesc", "fullDesc", "startTime"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ActivityVO extends AbstractSimpleTransportable {

    /**
     * The Organisation owning this Activity.
     */
    @XmlIDREF
    @XmlElement(required = true)
    private OrganisationVO organisation;

    /**
     * The short description of this Activity.
     */
    @XmlElement
    private String shortDesc;

    /**
     * The full description of this Activity.
     */
    @XmlElement
    private String fullDesc;

    @XmlAttribute
    private LocalDateTime startTime;

    @XmlAttribute
    private LocalDateTime endTime;

    @XmlElement
    private Amount cost;

    @XmlElement
    private Amount lateAdmissionCost;

    @XmlElement
    private LocalDate lateAdmissionDate;

    @XmlElement
    private LocalDate lastAdmissionDate;

    @XmlAttribute
    private boolean cancelled;

    @XmlElement
    private String dressCode;

    @XmlAttribute
    private String addressCategory;

    @XmlElement
    private Address location;

    @XmlElement
    private String addressShortDescription;

    @XmlElement
    private String responsibleGroupName;

    @XmlElement
    private Set<AdmissionVO> admissions;

    @XmlAttribute
    private boolean isOpenToGeneralPublic;
}
