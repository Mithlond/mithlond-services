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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SimpleMapAdapter extends XmlAdapter<SimpleMapAdapter.ListMap, Map<Beverage, Integer>> {

    public static class ListMap {
        @XmlElementWrapper(required = true, nillable = false)
        @XmlElement(nillable = true, required = false, name = "record")
        public List<ConsumptionEntry> consumptionRecords = new ArrayList<>();

        public void addNameValuePair(Beverage beverage, int numConsumed) {
            ConsumptionEntry toAdd = new ConsumptionEntry();
            toAdd.beverage = beverage;
            toAdd.numConsumed = numConsumed;
            consumptionRecords.add(toAdd);
        }
    }

    public static class ConsumptionEntry {

        @XmlAttribute(required = true)
        public Integer numConsumed;

        @XmlElement(required = true)
        public Beverage beverage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Beverage, Integer> unmarshal(final ListMap toUnmarshal) throws Exception {
        final Map<Beverage, Integer> toReturn = new HashMap<>();

        if(toUnmarshal != null) {
            for(ConsumptionEntry current : toUnmarshal.consumptionRecords) {
                toReturn.put(current.beverage, current.numConsumed);
            }
        }
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListMap marshal(final Map<Beverage, Integer> toMarshal) throws Exception {

        final ListMap toReturn = new ListMap();

        if(toMarshal != null) {
            for(Map.Entry<Beverage, Integer> current : toMarshal.entrySet()) {
                toReturn.addNameValuePair(current.getKey(), current.getValue());
            }
        }
        return toReturn;
    }
}
