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
/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.mithlond.services.organisation.model.finance;

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.membership.Membership;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Objects;

/**
 * Single-instance audit data structure embeddable.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Embeddable
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"comment", "actor", "timestamp"})
@XmlAccessorType(XmlAccessType.FIELD)
public class AuditData implements Validatable, Serializable, Comparable<AuditData> {

	// Internal state
	@Basic(optional = false)
	@Column(nullable = false)
	@XmlElement(nillable = false, required = true)
	private String comment;

	@ManyToOne(fetch = FetchType.EAGER)
	@XmlElement(required = true, nillable = false)
	private Membership actor;

	@Basic(optional = false)
	@Column(nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@XmlAttribute(required = true)
	private Calendar timestamp;

	/**
	 * JAXB/JPA-friendly constructor.
	 */
	public AuditData() {
	}

	/**
	 * Convenience constructor, using the current timestamp.
	 *
	 * @param comment The comment of this AuditData.
	 * @param actor   The Membership which created this AuditData.
	 */
	public AuditData(final String comment, final Membership actor) {
		this(comment, actor, LocalDateTime.now());
	}

	/**
	 * Compound constructor creating an AuditData instance from the supplied data.
	 *
	 * @param comment   The comment of this AuditData.
	 * @param actor     The Membership which created this AuditData.
	 * @param timestamp The timestamp when this AuditData was created.
	 */
	public AuditData(final String comment,
					 final Membership actor,
					 final LocalDateTime timestamp) {

		this.comment = comment;
		this.actor = actor;

		// Extract the Calendar from the timestamp.
		if (timestamp != null) {
			final ZonedDateTime zdt = ZonedDateTime.of(timestamp, ZoneId.systemDefault());
			this.timestamp = GregorianCalendar.from(zdt);
		}
	}

	/**
	 * @return The comment of this AuditData.
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @return The Membership which created this AuditData instance.
	 */
	public Membership getActor() {
		return actor;
	}

	/**
	 * @return The timestamp of this AuditData's creation.
	 */
	public LocalDateTime getTimestamp() {
		return LocalDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault());
	}

	/**
	 * <p>Compares this AuditData with the given one for order in time.</p>
	 * {@inheritDoc}
	 */
	@Override
	public int compareTo(final AuditData that) {
		return that == null ? Integer.MIN_VALUE : this.timestamp.compareTo(that.timestamp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return Objects.hash(comment, actor, timestamp);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@SuppressWarnings("all")
	public boolean equals(final Object that) {

		// Check sanity
		if (this == that) {
			return true;
		}
		if (!(that instanceof AuditData)) {
			return false;
		}

		// Delegate
		final AuditData thatAuditData = (AuditData) that;
		return getComment().equals(thatAuditData.getComment())
				&& getTimestamp().equals(thatAuditData.getTimestamp())
				&& getActor().equals(thatAuditData.getActor());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void validateInternalState() throws InternalStateValidationException {

		InternalStateValidationException.create()
				.notNullOrEmpty(comment, "comment")
				.notNull(actor, "actor")
				.notNull(timestamp, "timestamp")
				.endExpressionAndValidate();
	}
}
