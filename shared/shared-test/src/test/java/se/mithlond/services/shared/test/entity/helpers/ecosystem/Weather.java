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
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = "mithlond:shared:test:ecosystem", propOrder = {"name", "isDangerous"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Weather {

    @XmlAttribute
    private Boolean isDangerous;

    @XmlAttribute(required = true)
    private String name;

    public Weather() {
    }

    public Weather(final Boolean isDangerous, final String name) {
        this.isDangerous = isDangerous;
        this.name = name;
    }

    public Boolean getDangerous() {
        return isDangerous;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Weather weather = (Weather) o;
        return Objects.equals(isDangerous, weather.isDangerous) &&
                Objects.equals(name, weather.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isDangerous, name);
    }

    @Override
    public String toString() {
        return "Weather [Name: " + name + ", Dangerous: " + isDangerous + "]";
    }
}
