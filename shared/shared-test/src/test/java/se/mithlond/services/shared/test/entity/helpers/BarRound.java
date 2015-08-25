/*
 * #%L
 * Nazgul Project: mithlond-services-shared-entity-test
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
package se.mithlond.services.shared.test.entity.helpers;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = "mithlond:shared:test:event")
@XmlType(propOrder = {"identifier", "customers"})
public class BarRound {

    // Internal state
    private String identifier;

    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = true, required = false, name = "customer")
    private List<Customer> customers;

    public BarRound() {
    }

    public BarRound(final String identifier, final List<Customer> customers) {
        this.identifier = identifier;
        this.customers = customers;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<Customer> getCustomers() {
        return customers;
    }
}
