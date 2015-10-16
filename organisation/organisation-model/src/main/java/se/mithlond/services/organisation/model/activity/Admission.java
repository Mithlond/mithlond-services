/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
package se.mithlond.services.organisation.model.activity;

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

/**
 * Holds all data for an Admission to an Activity.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Access(value = AccessType.FIELD)
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {
		"admitted", "admissionTimestamp", "admissionNote", "responsible"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Admission implements Serializable, Comparable<Admission>, Validatable {

	// Internal state
	@EmbeddedId
	@XmlTransient
	private AdmissionId admissionId;

	// This must be XmlTransient to avoid a cyclic graph in the XSD.
	// Handled by a callback method from the Activity side.
	@ManyToOne
	@MapsId("activityId")
	@XmlTransient
	private Activity activity;

	/**
	 * The Membership admitted to the corresponding Activity.
	 */
	@NotNull
	@ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH})
	@MapsId("membershipId")
	@XmlElement(required = true, nillable = false)
	private Membership admitted;

	/**
	 * The timestamp when the Membership was admitted.
	 */
	@Basic(optional = false)
	@Column(nullable = false)
	@Temporal(value = TemporalType.TIMESTAMP)
	@XmlElement(required = true, nillable = false)
	private Calendar admissionTimestamp;

	/**
	 * An optional note of this Admission.
	 */
	@Basic(optional = true)
	@Column(nullable = true)
	@XmlElement(required = false, nillable = true)
	private String admissionNote;

	/**
	 * If "true" the admitted Membership is considered responsible for the activity to which this Admission is linked.
	 */
	@Basic(optional = false)
	@Column(nullable = false)
	@XmlAttribute(required = true)
	private boolean responsible;

	/**
	 * JAXB/JPA-friendly constructor.
	 */
	public Admission() {
	}

	/**
	 * <p>Convenience constructor creating an Admission with the current timestamp
	 * and otherwise the supplied data. Note that the Activity field is not set
	 * after calling this constructor, implying the following required call pattern:</p>
	 * <pre>
	 *     <code>
	 *         final Admission incomplete = new Admission(someMembership, null);
	 *         final Activity someActivity = new Activity(....);
	 *
	 *         // Now make the Admission complete.
	 *         incomplete.setActivity(someActivity);
	 *     </code>
	 * </pre>
	 *
	 * @param admitted      The membership admitted.
	 * @param admissionNote An optional admission note, holding
	 *                      information about the admission.
	 */
	public Admission(final Membership admitted,
					 final String admissionNote) {
		this(null, admitted, null, admissionNote, false);
	}

	/**
	 * Creates a new Admission instance wrapping the supplied data.
	 *
	 * @param activity           The Activity to which the supplied Membership is admitted.
	 * @param admitted           The membership admitted.
	 * @param admissionTimestamp The timestamp of admission.
	 * @param admissionNote      An optional admission note, holding
	 *                           information about the admission.
	 * @param responsible        If {@code true}, the admitted Membership is considered responsible for the
	 *                           activity to which this Admission is linked.
	 */
	public Admission(final Activity activity,
					 final Membership admitted,
					 final LocalDateTime admissionTimestamp,
					 final String admissionNote,
					 final boolean responsible) {

		// Assign internal state
		this.admissionId = new AdmissionId(activity.getId(), admitted.getId());

		this.activity = activity;
		this.admitted = admitted;
		this.admissionTimestamp = admissionTimestamp == null
				? Calendar.getInstance()
				: GregorianCalendar.from(admissionTimestamp.atZone(activity.getStartTime().getZone()));
		this.admissionNote = admissionNote;
		this.responsible = responsible;
	}

	/**
	 * @return The Id (primary key) of this Admission.
	 */
	public AdmissionId getAdmissionId() {

		// Handle JAXB unmarshalling
		recreateAdmissionIdIfRequired();

		// All done
		return admissionId;
	}

	/**
	 * @return The Membership admitted.
	 */
	public Membership getAdmitted() {

		// Handle JAXB unmarshalling
		recreateAdmissionIdIfRequired();

		// All done.
		return admitted;
	}

	/**
	 * @return The timestamp of this Admission.
	 */
	public ZonedDateTime getAdmissionTimestamp() {

		return ZonedDateTime.ofInstant(
				admissionTimestamp.toInstant(),
				admissionTimestamp.getTimeZone().toZoneId());
	}

	/**
	 * @return The optional note of this Admission. Can be {@code null}.
	 */
	public String getAdmissionNote() {
		return admissionNote;
	}

	/**
	 * @return {@code true} if the Admitted is considered responsible for the activity to which this Admission relates.
	 */
	public boolean isResponsible() {
		return responsible;
	}

	/**
	 * Assigns the responsible flag to the Admitted of this Admission, implying that the
	 * Membership of this Admission is considered responsible for the Activity to which this Admission refers.
	 *
	 * @param responsible {@code true} if the Admitted is considered responsible for the activity to which this
	 *                    Admission relates.
	 */
	public void setResponsible(final boolean responsible) {
		this.responsible = responsible;
	}

	/**
	 * Assigns the note of this Admission.
	 *
	 * @param admissionNote The new admissionNote. Cannot be {@code null}.
	 */
	public void setAdmissionNote(final String admissionNote) {
		this.admissionNote = Objects.requireNonNull(admissionNote, "Cannot handle null 'admissionNote' argument.");
	}

	/**
	 * @return The Activity to which this Admission is tied.
	 */
	public Activity getActivity() {

		// Handle JAXB unmarshalling
		recreateAdmissionIdIfRequired();

		// All done.
		return activity;
	}

	/**
	 * <p><strong>Note!</strong> This method is not part of the public API of the Admission.
	 * Instead, it is intended to be used after unmarshalling an Activity from XML, since
	 * the double-linked nature of the relationship between Activity and Admissions cannot
	 * be mapped by normal JAXB annotations.</p>
	 * <p>Therefore - this method should only be called by frameworks; never by users.</p>
	 *
	 * @param activity The Activity to which this Admission points.
	 */
	public void setActivity(final Activity activity) {
		this.activity = Objects.requireNonNull(activity, "Cannot handle null 'activity' argument.");
		if (admissionId != null) {
			admissionId.activityId = activity.getId();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(final Admission that) {

		// Check sanity
		if (that == null) {
			return -1;
		} else if (that == this) {
			return 0;
		}

		// Start by comparing the dates.
		int toReturn = getAdmissionTimestamp().compareTo(that.getAdmissionTimestamp());
		if (toReturn == 0) {
			toReturn = getAdmitted().getEmailAlias().compareTo(that.getAdmitted().getEmailAlias());
		}
		if (toReturn == 0) {
			final String thisAdmissionNote = admissionNote == null ? "" : admissionNote;
			final String thatAdmissionNote = that.getAdmissionNote() == null ? "" : that.getAdmissionNote();
			toReturn = thisAdmissionNote.compareTo(thatAdmissionNote);
		}

		// All done.
		return toReturn;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validateInternalState() throws InternalStateValidationException {

		// Check sanity
		InternalStateValidationException.create()
				.notNull(admitted, "admitted")
				.notNull(admissionTimestamp, "admissionTimestamp")
				.endExpressionAndValidate();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {

		final String responsible = "\nResponsible: " + (isResponsible() ? "true" : "false");

		// All done.
		String alias = getAdmitted() == null ? "<null admitted, so no alias>" : getAdmitted().getAlias();
		return "Admission [" + activity.getId() + "->" + admitted.getId() + "]: "
				+ alias + ", " + TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(getAdmissionTimestamp())
				+ responsible
				+ "\nNote: " + getAdmissionNote();
	}

	//
	// Private helpers
	//

	private void recreateAdmissionIdIfRequired() {
		if (admissionId == null && (admitted != null && activity != null)) {
			admissionId = new AdmissionId(activity.getId(), admitted.getId());
		}
	}
}
