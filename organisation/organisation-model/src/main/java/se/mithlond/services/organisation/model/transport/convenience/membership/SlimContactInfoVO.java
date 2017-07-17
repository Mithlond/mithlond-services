/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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
package se.mithlond.services.organisation.model.transport.convenience.membership;

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.Objects;

/**
 * JAXB-annotated transport entity for a contact information Map entry.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"medium", "addressOrNumber"})
@XmlAccessorType(XmlAccessType.FIELD)
public class SlimContactInfoVO implements Serializable, Validatable, Comparable<SlimContactInfoVO> {

    /**
     * The type of contact info, such as "EMAIL".
     */
    @XmlAttribute(required = true)
    private String medium;

    /**
     * The value of the addressOrNumber, such as "foobar@gmail.com".
     */
    @XmlAttribute(required = true)
    private String addressOrNumber;

    /**
     * JAXB-friendly constructor.
     */
    public SlimContactInfoVO() {
    }

    /**
     * Compound constructor creating a SlimContactInfoVO wrapping the supplied data.
     *
     * @param medium The type of contact info, such as "EMAIL".
     * @param addressOrNumber The value of the addressOrNumber, such as "foobar@gmail.com".
     */
    public SlimContactInfoVO(final String medium, final String addressOrNumber) {
        this.medium = medium;
        this.addressOrNumber = addressOrNumber;
    }

    /**
     * @return The type of contact info, such as "EMAIL".
     */
    public String getMedium() {
        return medium;
    }

    /**
     * @return The value of the addressOrNumber, such as "foobar@gmail.com".
     */
    public String getAddressOrNumber() {
        return addressOrNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {

        // Fail fast
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        // Delegate to internal state
        final SlimContactInfoVO that = (SlimContactInfoVO) o;
        return Objects.equals(getMedium(), that.getMedium())
                && Objects.equals(getAddressOrNumber(), that.getAddressOrNumber());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(getMedium(), getAddressOrNumber());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final SlimContactInfoVO that) {

        // Fail fast
        if (that == null) {
            return -1;
        } else if (that == this) {
            return 0;
        }

        // Delegate to internal state
        int toReturn = getMedium().compareTo(that.getMedium());

        if (toReturn == 0) {
            // This should really not happen...
            toReturn = getAddressOrNumber().compareTo(that.getAddressOrNumber());
        }
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(medium, "medium")
                .notNullOrEmpty(addressOrNumber, "addressOrNumber")
                .endExpressionAndValidate();
    }
}
