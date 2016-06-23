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

import org.junit.Before;
import org.junit.Test;
import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.finance.Amount;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.transport.activity.Activities;
import se.mithlond.services.organisation.model.transport.activity.ActivityVO;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import javax.ejb.Local;
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

        jaxb.add(Activities.class);
    }

    @Test
    public void validateMarshalling() throws Exception {

        // Assemble
        activities.getActivities().add(activity);
        activities.getActivityVOs().add(new ActivityVO(activity));

        // Act
        final String result = marshalToXML(activities);
        // System.out.println("Got: " + result);

        // Assert
        validateIdenticalContent();
    }

	/*
    @Test
	public void validateUnmarshalling() throws Exception {

		// Assemble
		final String dressCode = "Midgårda dräkt";
		final Amount lowCost = new Amount(42.5, WellKnownCurrency.SEK);
		final Amount higherCost = new Amount(124, WellKnownCurrency.SEK);
		final DateTime startTime = new DateTime(2013, 5, 6, 18, 0, 0, DateTimeZone.UTC);
		final DateTime endTime = new DateTime(2013, 5, 6, 20, 0, 0, DateTimeZone.UTC);
		final DateTime lateAdmissionDate = new DateTime(2013, 5, 2, 0, 0, DateTimeZone.UTC);
		final DateTime lastAdmissionDate = new DateTime(2013, 5, 3, 0, 0, 0, DateTimeZone.UTC);
		final Address activityLocation = new Address(null, null, "TestGatan", "3", "TestStan",
				"12345", "Sverige", "TestAddress");
		final String activityLocationShortDescription = "Stadsbiblioteket";

		final Activity unitUnderTest = new Activity("activityShortDesc", "activityFullDesc", startTime, endTime,
				lowCost, higherCost, lateAdmissionDate, lastAdmissionDate, false, dressCode, restaurant,
				activityLocation, activityLocationShortDescription, mifflond, mmm, true);

		final String data = XmlTestUtils.readFully("testdata/activity.xml");

		// Act
		final List<Object> result = binder.unmarshal(new StringReader(data));

		// Assert
		Assert.assertEquals(8, result.size());
		final Activity resurrected = (Activity) result.get(7);
		final Set<Admission> admissions = resurrected.getAdmissions();

		Assert.assertNotSame(unitUnderTest, resurrected);
		Assert.assertNotNull(resurrected);
		Assert.assertEquals(unitUnderTest.getLocation(), resurrected.getLocation());
		Assert.assertNotNull(admissions);
		Assert.assertEquals(2, admissions.size());

		Admission guildMasterAdmission = null;
		for (Admission current : admissions) {
			if (current.getAdmitted().equals(membership1)) {
				guildMasterAdmission = current;
			}
		}

		Assert.assertNotNull(guildMasterAdmission);
		Assert.assertEquals("The GuildMaster doesn't like Sprouts.", guildMasterAdmission.getAdmissionNote());
		Assert.assertEquals(dressCode, resurrected.getDressCode());

		Assert.assertSame(resurrected, guildMasterAdmission.getActivity());
		Assert.assertNotNull(guildMasterAdmission.getAdmissionId());
		Assert.assertEquals(resurrected, guildMasterAdmission.getActivity());
	}
	*/
}
