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
package se.mithlond.services.shared.test.entity;

import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.shared.test.entity.helpers.ecosystem.Weather;
import se.mithlond.services.shared.test.entity.helpers.ecosystem.WeatherReport;
import se.mithlond.services.shared.test.entity.helpers.jsonarrays.AbstractNamed;
import se.mithlond.services.shared.test.entity.helpers.jsonarrays.Car;
import se.mithlond.services.shared.test.entity.helpers.jsonarrays.Owner;
import se.mithlond.services.shared.test.entity.helpers.jsonarrays.Pet;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MoxyJsonAttributeTest extends AbstractPlainJaxbTest {

    // Shared state
    private WeatherReport report;
    private Owner owner;

    @Before
    public void setupSharedState() {

        // Use the setting to render JAXB wrappers as JSON array names.
        jaxb.setUseEclipseLinkMOXyIfAvailable(true);
        // jaxb.getMarshallerProperties().put(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
        // jaxb.getUnMarshallerProperties().put(UnmarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);

        report = new WeatherReport(
                new Weather(false, "Clear"),
                new Weather(null, "Undefined"),
                new Weather(true, "Overcast"));

        report.getWeathers().forEach(c -> System.out.println("Hash for " + c + ": " + c.hashCode()));

        jaxb.getMarshallerProperties().entrySet().forEach(c -> {
            System.out.println("Marshaller Property [" + c.getKey() + "]: " + c.getValue());
        });

        owner = new Owner("Pelle", "Ford", "Volvo");
        owner.getPets().addAll(
                Stream.of("Fluffy", "Bunny", "Tabby", "Tokah")
                        .map(Pet::new)
                        .collect(Collectors.toList()));
    }

    @After
    public void teardownSharedState() {
        jaxb.setUseEclipseLinkMOXyIfAvailable(false);

        // Use the setting to render JAXB wrappers as JSON array names.
        // jaxb.getMarshallerProperties().remove(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME);
        // jaxb.getUnMarshallerProperties().remove(UnmarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME);
    }

    @Test
    public void validateMarshallingToJSON() throws JSONException {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/weather_report.json");

        // Act
        final String marshalled = marshalToJSon(report);
        // System.out.println("Got: " + marshalled);

        // Assert
        JSONAssert.assertEquals(expected, marshalled, false);
    }

    @Test
    public void validateJSonArraysRendering() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/jsonArrayRendering.json");
        jaxb.mapXmlNamespacePrefix("mithlond:shared:test:jsonarrays", "json_array_test");
        jaxb.add(AbstractNamed.class, Owner.class, Pet.class, Car.class);
        jaxb.setUseEclipseLinkMOXyIfAvailable(true);

        // Act
        final String marshalled = marshalToJSon(owner);
        // System.out.println("Got: " + marshalled);

        // Assert
        JSONAssert.assertEquals(expected, marshalled, true);
    }

    @Test
    public void validateUnmarshallingFromJSON() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/weather_report.json");
        jaxb.add(WeatherReport.class, Weather.class);
        jaxb.mapXmlNamespacePrefix("mithlond:shared:test:ecosystem", "ecosystem");

        // Act
        final WeatherReport unmarshalled = unmarshalFromJSON(WeatherReport.class, data);

        // Assert
        Assert.assertEquals(report.getWeathers().size(), unmarshalled.getWeathers().size());
        for (int i = 0; i < unmarshalled.getWeathers().size(); i++) {

            final Weather unmarshalledWeather = unmarshalled.getWeathers().get(i);
            final Weather expectedWeather = report.getWeathers().get(i);

            Assert.assertEquals(unmarshalledWeather, expectedWeather);
        }
    }
}
