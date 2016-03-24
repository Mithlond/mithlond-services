/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model.helpers;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.address.WellKnownAddressType;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.NAMESPACE)
public class WellKnownAddressTypes {

    // Internal state
    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = true, required = false, name = "addressType")
    private List<WellKnownAddressType> addressTypes;

    public WellKnownAddressTypes() {
        addressTypes = new ArrayList<>();
    }

    public void add(WellKnownAddressType ... addressTypes) {
        Collections.addAll(this.addressTypes, addressTypes);
    }

    public List<WellKnownAddressType> getAddressTypes() {
        return addressTypes;
    }
}
