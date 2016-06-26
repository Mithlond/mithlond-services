/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
/*
 * Copyright (c) jGuru Europe AB
 * All rights reserved.
 */
package se.mithlond.services.organisation.model.activity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.Amount;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.organisation.model.transport.activity.Activities;
import se.mithlond.services.organisation.model.transport.activity.ActivityVO;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ActivityTest extends AbstractEntityTest {

    // Shared state
    private Activities activities;
    private Activity activity;
    private ActivityVO activityVO;
    private LocalDateTime activityStartTime, activityEndTime;
    private LocalDate lateAdmissionDate, lastAdmissionDate;
    private Amount cost, lateAdmissionCost;
    private Category addressCategory;
    private Address location;
    private Organisation organisation;

    @Before
    public void setupSharedState() {

        activities = new Activities();

        // Create the activity data
        activityStartTime = LocalDateTime.of(2016, Month.JUNE, 20, 17, 0);
        activityEndTime = LocalDateTime.of(2016, Month.JUNE, 20, 22, 15);

        lateAdmissionDate = LocalDate.of(2016, Month.JUNE, 15);
        lastAdmissionDate = LocalDate.of(2016, Month.JUNE, 19);

        cost = new Amount(BigDecimal.valueOf(50), WellKnownCurrency.SEK);
        lateAdmissionCost = new Amount(BigDecimal.valueOf(75), WellKnownCurrency.SEK);

        addressCategory = new Category("Visiting address", "visiting_address", "Address for visiting the organisation");
        location = new Address(null, null, "Foo Street",
                "5",
                "Göteborg",
                "253 54",
                "Sverige",
                "Visiting address");

        organisation = new Organisation("The Organisation",
                "The Tolkien Society of Kinnekulle",
                null,
                "0123-234211",
                "02515-2325232-2323",
                new Address(null, null, "Kinnekullegatan", "54 C", "Kinnekulle", "142 41", "Sverige",
                        "Visiting address"),
                "kinnekulle.tolkien.se",
                TimeFormat.SWEDISH_TIMEZONE,
                TimeFormat.SWEDISH_LOCALE);

        activity = new Activity(
                "activityShortDesc",
                "activityFullDesc",
                activityStartTime,
                activityEndTime,
                cost,
                lateAdmissionCost,
                lateAdmissionDate,
                lastAdmissionDate,
                false,
                "activityDressCode",
                addressCategory,
                location,
                "addressShortDescription",
                organisation,
                null,
                true);

        activityVO = new ActivityVO(activity);

        jaxb.add(Activities.class);
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/activities.xml");
        activities.getActivities().add(activity);
        activities.getActivityVOs().add(activityVO);

        // Act
        final String result = marshalToXML(activities);
        // System.out.println("Got: " + result);

        // Assert
        validateIdenticalContent(expected, result);
    }

    @Test
    public void validateUnmarshalling() throws Exception {

        // Assemble
        final String data = XmlTestUtils.readFully("testdata/activities.xml");

        // Act
        final Activities unmarshalled = unmarshalFromXML(Activities.class, data);

        // Assert
        Assert.assertNotNull(unmarshalled);
        Assert.assertEquals(1, unmarshalled.getActivities().size());
        Assert.assertEquals(1, unmarshalled.getActivityVOs().size());

        final Activity fullActivity = unmarshalled.getActivities().get(0);
        final ActivityVO activityVO = unmarshalled.getActivityVOs().get(0);

        Assert.assertEquals(activity, fullActivity);
        Assert.assertEquals(this.activityVO, activityVO);
    }
}
