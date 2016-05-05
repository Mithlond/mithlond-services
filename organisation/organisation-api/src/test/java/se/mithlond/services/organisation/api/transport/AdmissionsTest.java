/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-api
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
package se.mithlond.services.organisation.api.transport;

import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.persistence.jaxb.UnmarshallerProperties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.transport.activity.AdmissionVO;
import se.mithlond.services.organisation.model.transport.activity.Admissions;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.util.List;
import java.util.SortedMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class AdmissionsTest extends AbstractPlainJaxbTest {

    // Shared state
    private Admissions admissions;

    @Before
    public void setupSharedState() {

        admissions = new Admissions();

        final List<AdmissionVO> details = admissions.getDetails();
        for (int i = 0; i < 5; i++) {
            details.add(new AdmissionVO((long) (20 + i), "alias_" + i, "organisation_" + i, "note_" + i, i % 3 == 0));
        }
    }

    @Test
    public void validateMarshallingToXML() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/admissions.xml");

        // Act
        final String result = marshalToXML(admissions);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertTrue(XmlTestUtils.compareXmlIgnoringWhitespace(expected, result).identical());
    }

    @Test
    public void validateMarshallingToJSon() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/admissions.json");

        final SortedMap<String, Object> marshallerProperties = jaxb.getMarshallerProperties();
        marshallerProperties.put(MarshallerProperties.JSON_INCLUDE_ROOT, true);
        marshallerProperties.put(MarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);
        marshallerProperties.put(MarshallerProperties.JSON_REDUCE_ANY_ARRAYS, true);

        // Act
        final String result = marshalToJSon(admissions);
        // System.out.println("Got: " + result);
        // System.out.println("Got: " + jaxb.getMarshallerProperties());

        // Assert
        Assert.assertEquals(expected.replaceAll("\\s+", ""), result.replaceAll("\\s+", ""));
    }

    @Test
    public void validateUnmarshallingFromXML() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/admissions.xml");
        jaxb.add(Admissions.class);

        // Act
        final Admissions resurrected = unmarshalFromXML(Admissions.class, data);

        // Assert
        Assert.assertNotNull(resurrected);
        Assert.assertEquals(5, resurrected.getDetails().size());
    }

    @Ignore("Does not work correctly.")
    @Test
    public void validateUnmarshallingFromJSon() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/admissions.json");
        jaxb.add(Admissions.class);

        final SortedMap<String, Object> unMarshallerProperties = jaxb.getUnMarshallerProperties();
        unMarshallerProperties.put(UnmarshallerProperties.JSON_VALUE_WRAPPER, "false");
        unMarshallerProperties.put(UnmarshallerProperties.JSON_WRAPPER_AS_ARRAY_NAME, true);

        // Act
        final Admissions resurrected = unmarshalFromJSON(Admissions.class, data);

        // Assert
        Assert.assertNotNull(resurrected);
        Assert.assertEquals(5, resurrected.getDetails().size());
    }
}
