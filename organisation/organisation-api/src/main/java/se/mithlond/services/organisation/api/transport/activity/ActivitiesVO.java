package se.mithlond.services.organisation.api.transport.activity;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * Transport model for Activities.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.NAMESPACE)
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"activities"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ActivitiesVO extends AbstractSimpleTransporter {

    // Internal state
    @XmlElementWrapper(required = true)
    @XmlElement(name = "admissionDetails")
    private List<ActivityVO> activities;

    /**
     * JAXB-friendly constructor.
     */
    public ActivitiesVO() {
        activities = new ArrayList<>();
    }

    /**
     * Retrieves all known activities.
     *
     * @return The List of Acti wrapped by this Admissions.
     */
    public List<ActivityVO> getActivities() {
        return activities;
    }
}
