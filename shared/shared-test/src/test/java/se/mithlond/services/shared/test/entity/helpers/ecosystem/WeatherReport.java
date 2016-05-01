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
package se.mithlond.services.shared.test.entity.helpers.ecosystem;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement()
@XmlType(namespace = "mithlond:shared:test:ecosystem", propOrder = {"weathers"})
@XmlAccessorType(XmlAccessType.FIELD)
public class WeatherReport {

    @XmlElementWrapper
    @XmlElement(name = "weather")
    private List<Weather> weathers;

    public WeatherReport() {
        this.weathers = new ArrayList<>();
    }

    public WeatherReport(final Weather ... weathers) {
        this();

        if(weathers != null) {
            this.weathers.addAll(Arrays.asList(weathers));
        }
    }

    public List<Weather> getWeathers() {
        return weathers;
    }
}
