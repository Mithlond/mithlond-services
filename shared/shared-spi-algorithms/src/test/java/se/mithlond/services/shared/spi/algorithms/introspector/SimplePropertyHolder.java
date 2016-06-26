/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
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
package se.mithlond.services.shared.spi.algorithms.introspector;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SimplePropertyHolder {

    private String stringProperty;
    private Long longProperty;
    private Integer intProperty;

    public SimplePropertyHolder() {
    }

    public SimplePropertyHolder(final String stringProperty, final Long longProperty, final Integer intProperty) {
        this.stringProperty = stringProperty;
        this.longProperty = longProperty;
        this.intProperty = intProperty;
    }

    public String getStringProperty() {
        return stringProperty;
    }

    public void setStringProperty(final String stringProperty) {
        this.stringProperty = stringProperty;
    }

    public Long getLongProperty() {
        return longProperty;
    }

    public void setLongProperty(final Long longProperty) {
        this.longProperty = longProperty;
    }

    public Integer getIntProperty() {
        return intProperty;
    }

    public void setIntProperty(final Integer intProperty) {
        this.intProperty = intProperty;
    }
}
