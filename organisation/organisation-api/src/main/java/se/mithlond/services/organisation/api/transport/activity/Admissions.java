/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-api
 * %%
 * Copyright (C) 2015 Mithlond
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
package se.mithlond.services.organisation.api.transport.activity;

import se.mithlond.services.organisation.model.OrganisationPatterns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * A transport wrapper for multiple AdmissionDetails objects.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.NAMESPACE)
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"details"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Admissions {

    // Internal state
    @XmlElementWrapper(required = true)
    @XmlElement(name = "admissionDetails")
    private List<AdmissionVO> details;

    /**
     * JAXB-friendly constructor.
     */
    public Admissions() {
        details = new ArrayList<>();
    }

    /**
     * Retrieves all known AdmissionDetails.
     *
     * @return The List of AdmissionDetails wrapped by this Admissions.
     */
    public List<AdmissionVO> getDetails() {
        return details;
    }
}
