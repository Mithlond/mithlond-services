/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-jaxb
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
package se.mithlond.services.shared.spi.jaxb;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PeopleTest extends AbstractPlainJaxbTest {

    // Shared state
    private People people;

    @Before
    public void setupSharedState() {

        people = new People();

        final List<DummyPersonTransportable> persons = people.getPersonList();
        persons.add(new DummyPersonTransportable(25L, "Allan", "Octamac"));
        persons.add(new DummyPersonTransportable(32L, "Ellen", "Ectamac"));

        jaxb.add(People.class);
    }

    @Test
    public void validateMarshallingToXML() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/people.xml");

        // Act
        final String marshalled = marshalToXML(people);
        // System.out.println("Got: " + marshalled);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, marshalled).identical());
    }

    @Test
    public void validateMarshallingToJSON() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/people.json");

        // Act
        final String marshalled = marshalToJSon(people);
        // System.out.println("Got: " + marshalled);

        // Assert
        Assert.assertEquals(expected.trim().replaceAll("\\p{Space}", ""),
                marshalled.trim().replaceAll("\\p{Space}", ""));
    }
}
