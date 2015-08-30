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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement()
@XmlType(namespace = "mithlond:shared:test:ecosystem",
        propOrder = {"fish", "animals"})
public class EcoSystem {

    @XmlElementWrapper(nillable = true, name = "fish")
    @XmlElement(nillable = false, required = true, name = "aFish")
    private SortedSet<Fish> fish;

    @XmlElementWrapper(nillable = true)
    @XmlElement(nillable = false, required = true, name = "animal")
    private SortedSet<Animal> animals;

    public EcoSystem() {
        fish = new TreeSet<>();
        animals = new TreeSet<>();
    }

    public EcoSystem(final Fish... fish) {
        this();
        add(fish);
    }

    public SortedSet<Fish> getFish() {
        return fish;
    }

    public SortedSet<Animal> getAnimals() {
        return animals;
    }

    public void add(final Fish... fish) {
        Collections.addAll(this.fish, fish);
    }

    public void add(final Animal... animals) {
        Collections.addAll(this.animals, animals);
    }
}
