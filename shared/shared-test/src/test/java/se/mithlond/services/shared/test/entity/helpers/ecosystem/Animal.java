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
package se.mithlond.services.shared.test.entity.helpers.ecosystem;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = "mithlond:shared:test:ecosystem",
        propOrder = {"name", "fishList"})
public class Animal implements Comparable<Animal> {

    // Internal state
    @XmlElement(nillable = false, required = true)
    private String name;

    @XmlIDREF
    @XmlElementWrapper(nillable = true, name = "allFish")
    @XmlElement(nillable = false, required = true, name = "fish")
    private List<Fish> fishList;

    public Animal() {
        fishList = new ArrayList<>();
    }

    public Animal(final String name, final Fish ... fish) {

        this();

        // Assign internal state
        this.name = name;
        Collections.addAll(this.fishList, fish);
    }

    public String getName() {
        return name;
    }

    public List<Fish> getFish() {
        return fishList;
    }

    @Override
    public int compareTo(final Animal that) {
        return this.name.compareTo(that.name);
    }
}
