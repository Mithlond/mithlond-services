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
package se.mithlond.services.shared.spi.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Person dummy class for testing JAXB conversions.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = SharedJaxbPatterns.NAMESPACE, propOrder = {"firstName", "lastName"})
@XmlAccessorType(XmlAccessType.FIELD)
public class DummyPersonTransportable extends AbstractSimpleTransportable {

    @XmlAttribute
    private String firstName;

    @XmlElement
    private String lastName;

    /**
     * JAXB-friendly constructor.
     */
    public DummyPersonTransportable() {
    }

    /**
     * Compound constructor.
     *
     * @param jpaID     The JPA ID.
     * @param firstName The first name
     * @param lastName  The last name
     */
    public DummyPersonTransportable(final Long jpaID,
            final String firstName,
            final String lastName) {

        super(jpaID);
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
