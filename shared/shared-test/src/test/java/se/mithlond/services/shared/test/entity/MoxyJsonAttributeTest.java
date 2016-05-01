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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.shared.test.entity.helpers.ecosystem.Weather;
import se.mithlond.services.shared.test.entity.helpers.ecosystem.WeatherReport;

import static se.mithlond.services.shared.test.entity.PlainJaxbContextRule.ECLIPSELINK_JAXB_CONTEXT_FACTORY;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MoxyJsonAttributeTest extends AbstractPlainJaxbTest {

    // Shared state
    private WeatherReport report;

    @Before
    public void setupSharedState() {

        System.setProperty("javax.xml.bind.context.factory", ECLIPSELINK_JAXB_CONTEXT_FACTORY);

        report = new WeatherReport(
                new Weather(false, "Clear"),
                new Weather(null, "Undefined"),
                new Weather(true, "Overcast"));

        report.getWeathers().stream().forEach(c -> System.out.println("Hash for " + c + ": " + c.hashCode()));
    }

    @After
    public void teardownSharedState() {
        System.clearProperty("javax.xml.bind.context.factory");
    }

    @Test
    public void validateMarshallingToJSON() {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/weather_report.json");

        // Act
        final String marshalled = marshalToJSon(report);
        // System.out.println("Got: " + marshalled);

        // Assert
        Assert.assertEquals(
                expected.trim().replaceAll("\\p{Space}", ""),
                marshalled.trim().replaceAll("\\p{Space}", ""));
    }

    @Ignore("Not quite operational yet.")
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
