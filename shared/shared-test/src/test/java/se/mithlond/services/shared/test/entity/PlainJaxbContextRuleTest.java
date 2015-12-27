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
import se.mithlond.services.shared.test.entity.helpers.BarRound;
import se.mithlond.services.shared.test.entity.helpers.Beverage;
import se.mithlond.services.shared.test.entity.helpers.Customer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PlainJaxbContextRuleTest {

    // Shared state
    private PlainJaxbContextRule unitUnderTest;
    private BarRound barRound;
    private List<Customer> customerList;
    private Customer lennart;
    private Customer malin;
    private Beverage rootBeer;
    private Beverage dipa;

    @Before
    public void setupSharedState() {
        unitUnderTest = new PlainJaxbContextRule();

        lennart = new Customer("lennart", new HashMap<>());
        malin = new Customer("malin", new HashMap<>());

        rootBeer = new Beverage("Root Beer", "Ödeshögs Bryggeri");
        dipa = new Beverage("DIPA", "Poppels");

        lennart.getConsumption().put(dipa, 1);
        lennart.getConsumption().put(rootBeer, 2);
        malin.getConsumption().put(rootBeer, 1);
        malin.getConsumption().put(dipa, 2);

        barRound = new BarRound("fridayNightOut", new ArrayList<>());
        customerList = barRound.getCustomers();
        customerList.add(lennart);
        customerList.add(malin);
    }

    @Test
    public void validateMarshallingToXML() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/anotherBarRound.xml");

        // Act
        unitUnderTest.add(BarRound.class, Beverage.class, Customer.class);
        unitUnderTest.mapXmlNamespacePrefix("mithlond:shared:test:event", "barRound");
        final String result = unitUnderTest.marshal(getClass().getClassLoader(), false, barRound);

        // Assert
        /*
        final Diff diff = XmlTestUtils.compareXmlIgnoringWhitespace(expected, result);
        final SortedMap<String, List<Difference>> diffMap = XmlTestUtils.getXPathLocationToDifferenceMap(diff);

        int i = 0;
        for(Map.Entry<String, List<Difference>> current : diffMap.entrySet()) {
            System.out.println(" [" + i++ + "]: " + current.getKey());
            for(Difference currentDiff : current.getValue()) {
                System.out.println("     Test Node : " + currentDiff.getTestNodeDetail().getXpathLocation());
                System.out.println("     Test Value: " + currentDiff.getTestNodeDetail().getValue());
                System.out.println("     Ctrl Node : " + currentDiff.getControlNodeDetail().getXpathLocation());
                System.out.println("     Ctrl Value: " + currentDiff.getControlNodeDetail().getValue());
            }
        }
        */

        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateMarshallingToJSON() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/anotherBarRound.json");

        // Act
        unitUnderTest.add(BarRound.class, Beverage.class, Customer.class);
        unitUnderTest.mapXmlNamespacePrefix("mithlond:shared:test:event", "barRound");
        final String result = unitUnderTest.marshal(getClass().getClassLoader(), true, barRound);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertEquals(expected.replaceAll("\\s+", ""), result.replaceAll("\\s+", ""));
    }

    @Test
    public void validateUnmarshallingFromXML() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/anotherBarRound.xml");

        // Act
        unitUnderTest.add(BarRound.class, Beverage.class, Customer.class);
        unitUnderTest.mapXmlNamespacePrefix("mithlond:shared:test:event", "barRound");
        final BarRound result = unitUnderTest.unmarshal(getClass().getClassLoader(), false, BarRound.class, data);

        // Assert
        // System.out.println("Got: " + result);
        Assert.assertNotNull(result);
        Assert.assertEquals(barRound.getIdentifier(), result.getIdentifier());

        final List<Customer> customers = result.getCustomers();
        Assert.assertEquals(customerList.size(), customers.size());
        outer: for(Customer current : customers) {
            for(Customer comparison : customerList) {
                if(current.compareTo(comparison) == 0) {
                    continue outer;
                }
            }

            Assert.fail("Unexpected Customer [" + current.getName() + "] found.");
        }
    }

    @Test
    public void validateUnmarshallingFromJSON() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/anotherBarRound.json");

        // Act
        unitUnderTest.add(BarRound.class, Beverage.class, Customer.class);
        unitUnderTest.mapXmlNamespacePrefix("mithlond:shared:test:event", "barRound");
        final BarRound result = unitUnderTest.unmarshal(getClass().getClassLoader(), true, BarRound.class, data);

        // Assert
        // System.out.println("Got: " + result);
        Assert.assertNotNull(result);
        Assert.assertEquals(barRound.getIdentifier(), result.getIdentifier());

        final List<Customer> customers = result.getCustomers();
        Assert.assertEquals(customerList.size(), customers.size());
        outer: for(Customer current : customers) {
            for(Customer comparison : customerList) {
                if(current.compareTo(comparison) == 0) {
                    continue outer;
                }
            }

            Assert.fail("Unexpected Customer [" + current.getName() + "] found.");
        }
    }
}
