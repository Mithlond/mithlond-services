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
