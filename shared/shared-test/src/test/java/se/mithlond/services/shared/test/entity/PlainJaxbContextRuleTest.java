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

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
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
import java.util.SortedMap;

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

        lennart.getConsumption().put(rootBeer, 2);
        lennart.getConsumption().put(dipa, 1);
        malin.getConsumption().put(rootBeer, 1);
        malin.getConsumption().put(dipa, 2);

        barRound = new BarRound("fridayNightOut", new ArrayList<>());
        customerList = barRound.getCustomers();
        customerList.add(lennart);
        customerList.add(malin);
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/anotherBarRound.xml");

        // Act
        unitUnderTest.add(BarRound.class, Beverage.class, Customer.class);
        unitUnderTest.mapXmlNamespacePrefix("mithlond:shared:test:event", "barRound");
        final String result = unitUnderTest.marshal(getClass().getClassLoader(), barRound);

        // Assert
        /*
        System.out.println("Got: " + result);
        final Diff diff = XmlTestUtils.compareXmlIgnoringWhitespace(expected, result);
        final SortedMap<String, List<Difference>> diffMap = XmlTestUtils.getXPathLocationToDifferenceMap(diff);
        System.out.println("Got: " + diffMap);
        */
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/anotherBarRound.xml");

        // Act
        unitUnderTest.add(BarRound.class, Beverage.class, Customer.class);
        unitUnderTest.mapXmlNamespacePrefix("mithlond:shared:test:event", "barRound");
        final BarRound result = unitUnderTest.unmarshal(getClass().getClassLoader(), BarRound.class, data);

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
