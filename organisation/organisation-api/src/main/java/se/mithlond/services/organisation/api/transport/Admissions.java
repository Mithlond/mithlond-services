package se.mithlond.services.organisation.api.transport;

import se.mithlond.services.organisation.model.Patterns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A transport wrapper for multiple Region objects.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"details"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Admissions {

	// Internal state
	@XmlElementWrapper(required = true, nillable = false)
	@XmlElement(nillable = false, required = false, name = "admissionDetails")
	private SortedSet<AdmissionDetails> details;

	/**
	 * JAXB-friendly constructor.
	 */
	public Admissions() {
		details = new TreeSet<>();
	}

	/**
	 * Retrieves all known AdmissionDetails.
	 *
	 * @return The SortedSet of AdmissionDetails wrapped by this Admissions.
	 */
	public SortedSet<AdmissionDetails> getDetails() {
		return details;
	}
}
