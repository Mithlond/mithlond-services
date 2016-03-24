/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2014 jGuru Europe AB
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


import se.mithlond.services.organisation.model.OrganisationPatterns;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;

/**
 * Many-to-many relation Admission key between Activity and Membership.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Embeddable
@Access(value = AccessType.FIELD)
@XmlTransient
@XmlType(namespace = OrganisationPatterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class AdmissionId implements Serializable {

	private static final long serialVersionUID = 8829991L;

	// Shared state
	@Column(name = "activity_id")
	public long activityId;

	@Column(name = "admitted_id")
	public long membershipId;

	/**
	 * JAXB/JPA-friendly constructor.
	 */
	public AdmissionId() {
	}

	/**
	 * Compound constructor, wrapping the supplied objects.
	 *
	 * @param activityId   The id of the Activity to admit a Membership to.
	 * @param membershipId The id of the Membership to admit to the Activity given.
	 */
	public AdmissionId(final long activityId,
					   final long membershipId) {

		// Assign internal state
		this.activityId = activityId;
		this.membershipId = membershipId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {

		if (obj instanceof AdmissionId) {

			final AdmissionId that = (AdmissionId) obj;
			return activityId == that.activityId && membershipId == that.membershipId;
		}

		// All done.
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return (int) (activityId + membershipId) % Integer.MAX_VALUE;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "AdmissionId [Activity: " + activityId + ", Membership: " + membershipId + "]";
	}
}
