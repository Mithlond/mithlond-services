/*
 * #%L
 * Nazgul Project: mithlond-services-shared-entity-test
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
package se.mithlond.services.shared.test.entity.helpers.jsonarrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = "mithlond:shared:test:jsonarrays")
@XmlType(namespace = "mithlond:shared:test:jsonarrays", propOrder = {"cars", "pets"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Owner extends AbstractNamed {

    @XmlElementWrapper
    @XmlElement(name = "pet")
    private List<Pet> pets;

    @XmlElementWrapper
    @XmlElement(name = "car")
    private Car[] cars;

    public Owner() {
        pets = new ArrayList<>();
        cars = new Car[0];
    }

    public Owner(final String name, final String ... carNames) {

        super(name);

        pets = new ArrayList<>();
        if(carNames != null) {
            final List<Car> carList = Arrays.stream(carNames).map(Car::new).collect(Collectors.toList());
            this.cars = carList.toArray(new Car[carNames.length]);
        } else {
            this.cars = new Car[0];
        }
    }

    public List<Pet> getPets() {
        return pets;
    }

    public Car[] getCars() {
        return cars;
    }
}
