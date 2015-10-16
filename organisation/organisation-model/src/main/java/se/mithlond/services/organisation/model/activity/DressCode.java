/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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

import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.Listable;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.Patterns;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Entity class defining an DressCode.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
		@NamedQuery(name = DressCode.NAMEDQ_GET_BY_ORGANISATION,
				query = "select d from DressCode d "
						+ " where d.owningOrganisation.organisationName like :" + Patterns.PARAM_ORGANISATION_NAME
						+ " order by d.dressCode")
})
@Entity
@Table(uniqueConstraints = {
		@UniqueConstraint(
				name = "dresscodeIsUniquePerOrganisation",
				columnNames = {"dressCode", "owningorganisation_id"})})
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"dressCode"})
@XmlAccessorType(XmlAccessType.FIELD)
public class DressCode extends Listable {

	/**
	 * NamedQuery for getting Dresscodes by organisation name.
	 */
	public static final String NAMEDQ_GET_BY_ORGANISATION = "DressCode.getByOrganisation";

	/**
	 * The Dresscode name, such as "Midgårda dräkt".
	 * A single word (or two...) unique to this DressCode. Mostly used in all GUI-type of selections.
	 * Refer to shortDesc and fullDesc for richer descriptions on this DressCode.
	 */
	@NotNull
	@Basic(optional = false)
	@Column(length = 64, nullable = false)
	@XmlElement(nillable = false, required = true)
	private String dressCode;

	/**
	 * JAXB/JPA-friendly constructor.
	 */
	public DressCode() {
	}

	/**
	 * Compound constructor, creating a DressCode object wrapping the supplied data.
	 *
	 * @param shortDesc    The mandatory and non-empty short description of this Listable entity.
	 * @param fullDesc     The full description of this entity (up to 2048 chars), visible in detailed listings.
	 *                     May not be null or empty.
	 * @param organisation The organisation within which this Listable exists.
	 * @param dressCode    A single word (or two ...) unique to this DressCode. Mostly used in all selections.
	 */
	public DressCode(final String shortDesc,
					 final String fullDesc,
					 final Organisation organisation,
					 final String dressCode) {

		// Delegate
		super(shortDesc, fullDesc, organisation);

		// Assign internal state
		this.dressCode = dressCode;
	}

	/**
	 * @return A single word (or two...) unique to this DressCode. Mostly used in all selections.
	 */
	public String getDressCode() {
		return dressCode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void validateListableEntityState() throws InternalStateValidationException {

		InternalStateValidationException.create()
				.notNullOrEmpty(dressCode, "dressCode")
				.endExpressionAndValidate();
	}
}
