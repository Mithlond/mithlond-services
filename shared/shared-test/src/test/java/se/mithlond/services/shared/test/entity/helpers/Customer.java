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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = "mithlond:shared:test:people",
        propOrder = {"name", "consumption"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Customer implements Comparable<Customer> {

    // Internal state
    private String name;

    @XmlJavaTypeAdapter(SimpleMapAdapter.class)
    private Map<Beverage, Integer> consumption;

    public Customer() {
        this.consumption = new TreeMap<>();
    }

    public Customer(final String name, final Map<Beverage, Integer> consumption) {
        this();
        this.name = name;
        this.consumption = consumption;
    }

    public String getName() {
        return name;
    }

    public Map<Beverage, Integer> getConsumption() {
        return consumption;
    }

    @Override
    public int compareTo(final Customer that) {
        int result = this.getName().compareTo(that.getName());

        if (result == 0) {
            result = getConsumption().size() - that.getConsumption().size();
        }

        return result;
    }
}
