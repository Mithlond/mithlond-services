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

import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.organisation.model.address.Address;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class Addresses {

    // Internal state
    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = true, required = false, name = "address")
    private List<Address> addresses;

    public Addresses() {
        this((Address[]) null);
    }

    public Addresses(final Address... addresses) {

        // Check sanity for EclipseLink's unmarshal process.
        if (this.addresses == null) {
            this.addresses = new ArrayList<>();
        }

        if(addresses != null) {
            for (int i = 0; i < addresses.length; i++) {
                final Address current = addresses[i];
                if (current != null) {
                    this.addresses.add(current);
                } else {
                    new IllegalStateException("Got null address for element [" + i + "]").printStackTrace();
                }
            }
        }
    }

    public List<Address> getAddresses() {
        return addresses;
    }
}
