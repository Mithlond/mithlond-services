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

import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.user.User;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ActivityTest extends AbstractEntityTest {

	// Shared state
	private Organisation mifflond;
	private User member1;
	private User member2;
	private Group subGroup;
	private Group superGroup;
	private Guild mmm;
	private Guild zzz;
	private Membership membership1;
	private Membership membership2;
	private Category restaurant;

	/**
	 * Performs custom setup logic.

	@Override
	protected void doCustomSetup() {

		// Unmarshal the Guild.
		final String data = XmlTestUtils.readFully("testdata/aMembership.xml");
		final List<Object> result = jaxb.unmarshal(new StringReader(data));

		// Check sanity, and assign shared state
		Assert.assertEquals(9, result.size());
		mifflond = (Organisation) result.get(0);
		subGroup = (Group) result.get(1);
		superGroup = (Group) result.get(2);
		mmm = (Guild) result.get(3);
		zzz = (Guild) result.get(4);
		member1 = (User) result.get(5);
		member2 = (User) result.get(6);
		membership1 = (Membership) result.get(7);
		membership2 = (Membership) result.get(8);

		// Create shared state
		restaurant = new Category("Restaurang eller Krog", "restaurant", "Restaurang eller Krog");
	}
	 */

    /*
	@Test
    public void validateMarshalling() throws Exception {

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

        // final SortedMap<Membership, Boolean> participants = new TreeMap<Membership, Boolean>();

        for(Membership current : mmm.getDeputyGuildMasters()) {
            participants.put(current, false);
        }

	final Activity unitUnderTest = new Activity("activityShortDesc", "activityFullDesc", startTime, endTime,
			lowCost, higherCost, lateAdmissionDate, lastAdmissionDate, false, dressCode, restaurant,
			activityLocation, activityLocationShortDescription, mifflond, mmm, true);

	final Set<Admission> admissions = unitUnderTest.getAdmissions();
	admissions.add(new

	Admission(unitUnderTest, membership1,
			  lateAdmissionDate.minusDays(1),

	"The GuildMaster doesn't like Sprouts.",true));
	admissions.add(new

	Admission(unitUnderTest, membership2,
			  lateAdmissionDate, null,false)

	);

	final String expected = XmlTestUtils.readFully("testdata/activity.xml");

	// Act - ensure that we have forward references WRT @XmlIDREFs here.
	final String result = binder.marshal(mifflond, subGroup, superGroup, mmm, zzz, member1, member2, unitUnderTest);
	System.out.println("Got: "+result);

	// Assert
	validateIdenticalContent(expected, result);
}

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
