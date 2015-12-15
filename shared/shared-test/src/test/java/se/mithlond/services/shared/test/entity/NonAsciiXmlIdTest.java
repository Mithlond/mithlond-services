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
package se.mithlond.services.shared.test.entity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.shared.test.entity.helpers.ecosystem.Animal;
import se.mithlond.services.shared.test.entity.helpers.ecosystem.EcoSystem;
import se.mithlond.services.shared.test.entity.helpers.ecosystem.Fish;

import java.util.List;
import java.util.SortedSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NonAsciiXmlIdTest extends AbstractPlainJaxbTest {

    // Shared state
    private EcoSystem ecosystem;
    private Animal fluffy;
    private Animal chubby;

    private Fish cod;
    private Fish jack;
    private Fish eel;

    @Before
    public void setupSharedState() {

        // Create the various Fish
        cod = new Fish("Torsk", "Saltvattensfisk");
        jack = new Fish("Gädda", "Insjöfisk");
        eel = new Fish("Ål", "Insjö- och saltvattensfisk");

        // ... and the Kittens
        fluffy = new Animal("Fluffy the cat", cod, jack);
        chubby = new Animal("Chubby the cat", jack, eel);

        // Now pull it all together
        ecosystem = new EcoSystem();
        ecosystem.add(cod, jack, eel);
        ecosystem.add(fluffy, chubby);
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/ecosystem.xml");

        // Act
        final String result = marshalToXML(ecosystem);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/ecosystem.xml");
        jaxb.add(EcoSystem.class);

        // Act
        final EcoSystem resurrected = unmarshalFromXML(EcoSystem.class, data);

        // Assert
        Assert.assertNotNull(resurrected);

        final SortedSet<Fish> expectedFish = ecosystem.getFish();
        final SortedSet<Animal> expectedAnimals = ecosystem.getAnimals();

        final SortedSet<Fish> resurrectedFish = resurrected.getFish();
        final SortedSet<Animal> resurrectedAnimals = resurrected.getAnimals();

        Assert.assertEquals(expectedFish.size(), resurrectedFish.size());
        Assert.assertEquals(expectedAnimals.size(), resurrectedAnimals.size());

        for(Fish current : expectedFish) {
            Assert.assertNotNull(findFish(current));
        }

        for(Animal current : expectedAnimals) {

            Animal resurrectedAnimal = findAnimal(current);
            Assert.assertNotNull(resurrectedAnimal);

            final List<Fish> expectedFishList = current.getFish();
            final List<Fish> resurrectedFishList = resurrectedAnimal.getFish();

            Assert.assertEquals(expectedFishList.size(), resurrectedFishList.size());
            for(int i = 0; i < expectedFishList.size(); i++) {
                Assert.assertEquals(0, expectedFishList.get(i).compareTo(resurrectedFishList.get(i)));
            }
        }
    }

    private Animal findAnimal(final Animal toMatch) {

        for(Animal current : ecosystem.getAnimals()) {
            if(current.compareTo(toMatch) == 0) {
                return current;
            }
        }

        return null;
    }

    private Fish findFish(final Fish toMatch) {

        for(Fish current : ecosystem.getFish()) {
            if(current.compareTo(toMatch) == 0) {
                return current;
            }
        }

        return null;
    }
}
