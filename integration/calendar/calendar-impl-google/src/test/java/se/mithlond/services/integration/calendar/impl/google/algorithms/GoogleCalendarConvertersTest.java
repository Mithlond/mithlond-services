/*
 * #%L
 * Nazgul Project: mithlond-services-integration-calendar-impl-google
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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
package se.mithlond.services.integration.calendar.impl.google.algorithms;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.Event;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.activity.Admission;
import se.mithlond.services.organisation.model.activity.EventCalendar;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.finance.Amount;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class GoogleCalendarConvertersTest extends AbstractPlainJaxbTest {

    // Shared state
    private Address address;
    private Organisation organisation;

    @Before
    public void setupSharedState() {

        address = new Address("careOfLine", "departmentName", "street", "number",
                "city", "zipCode", "country", "description");

        organisation = setJpaID(new Organisation("name", "suffix", "phone", "bankAccountInfo",
                "postAccountInfo", address, "emailSuffix",
                TimeFormat.SWEDISH_TIMEZONE.normalized(),
                TimeFormat.SWEDISH_LOCALE,
                WellKnownCurrency.SEK), 2L);
    }

    @Test
    public void validateEventCalendarConversion() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/algorithms/localSideCalendar.json");
        final EventCalendar eventCalendar = setJpaID(
                new EventCalendar("shortDesc",
                        "fullDesc",
                        organisation,
                        "calendarIdentifier",
                        "RUNTIME_ENVIRONMENT"),
                4L);


        // Act
        final Calendar result = GoogleCalendarConverters.convert(eventCalendar);
        // new TreeMap<>(result).forEach((key, value) -> System.out.println("[" + key + "]: " + value));

        result.setFactory(GoogleCalendarConverters.JSON_FACTORY);
        final String prettyString = result.toPrettyString();


        // Assert
        Assert.assertNotNull(result);
        Assert.assertNotNull(prettyString);

        JSONAssert.assertEquals(expected, prettyString, true);
    }

    @Test
    public void validateLocalDateTimeConversion() throws Exception {

        // Assemble
        final String expectedRfcStringForm = "1968-09-16T15:32:00.000+01:00";
        final LocalDateTime sixteenthSeptember1968 = LocalDateTime.of(
                1968,
                Month.SEPTEMBER,
                16,
                15,
                32);

        // Act
        final DateTime result = GoogleCalendarConverters.convert(sixteenthSeptember1968);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(expectedRfcStringForm, result.toString());
        Assert.assertEquals(expectedRfcStringForm, result.toStringRfc3339());
        Assert.assertEquals(60, result.getTimeZoneShift()); // Minute shift
        Assert.assertEquals(-40728480000L, result.getValue());
    }

    @Test
    public void validateActivityConversion() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/algorithms/localSideEvent.json");

        final LocalDateTime startTime = LocalDateTime.of(
                2017,
                Month.JANUARY,
                12,
                18,
                30);
        final LocalDate lateAdmissionDate = LocalDate.of(2017, Month.JANUARY, 5);
        final LocalDate lastAdmissionDate = LocalDate.of(2017, Month.JANUARY, 10);

        final Amount cost = new Amount(BigDecimal.valueOf(40L), WellKnownCurrency.SEK);
        final Amount lateAdmissionCost = new Amount(BigDecimal.valueOf(50L), WellKnownCurrency.SEK);

        final Category addressCategory = setJpaID(
                new Category("HomeAddress",
                        CategorizedAddress.ACTIVITY_CLASSIFICATION,
                        "Hemma hos Mr Octamac."),
                256L);

        final Address octamacHomeAddress = new Address(null,
                null,
                "Ringvägen",
                "53 B",
                "Tokholm",
                "164 56",
                "Sverige",
                "Hemma hos Mr Octamac.");

        final List<User> users = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            users.add(setJpaID(
                    new User("firstName_" + i,
                            "lastName_" + i,
                            LocalDate.of(1976 + i,
                                    Month.APRIL,
                                    8 + i),
                            (short) (2345 + i),
                            new Address(null,
                                    null,
                                    "Tjovägen",
                                    "" + (10 + i),
                                    "Tokholm",
                                    "143 5" + (3 + i),
                                    "Sverige",
                                    "Hemma hos firstName_" + i),
                            new ArrayList<>(),
                            new TreeMap<>(),
                            "userIdToken_" + i),
                    90 + i));
        }

        final AtomicInteger index = new AtomicInteger(25);
        final List<Membership> memberships = users.stream()
                .map(u -> {

                    final Membership membership = setJpaID(new Membership("alias_" + index.get(),
                            "subAlias_" + index.get(),
                            "emailAlias_" + index.get(),
                            true,
                            u,
                            organisation), index.getAndIncrement());
                    u.getMemberships().add(membership);

                    return membership;
                })
                .collect(Collectors.toList());

        final Activity activity = setJpaID(
                new Activity("shortDesc", "fullDesc", startTime, startTime.plusHours(2), cost,
                        lateAdmissionCost,
                        lateAdmissionDate,
                        lastAdmissionDate,
                        false,
                        "dresscode",
                        addressCategory,
                        octamacHomeAddress,
                        "addressShortDesc",
                        organisation,
                        null,
                        true),
                4L);

        final Set<Admission> admissions = memberships.stream()
                .map(m -> new Admission(
                            activity,
                            m,
                            LocalDateTime.of(lateAdmissionDate.minusDays(1), LocalTime.of(15, 35)),
                            LocalDateTime.of(lateAdmissionDate.minusDays(1), LocalTime.of(15, 35)),
                            "Admitted note " + index.incrementAndGet(),
                            false, 
                            null))
                .collect(Collectors.toSet());
        activity.getAdmissions().addAll(admissions);

        final Field lastModifiedAtField = Admission.class.getDeclaredField("lastModifiedAt");
        lastModifiedAtField.setAccessible(true);

        activity.getAdmissions().forEach(a -> {
            try {

                // This field must have a stable value to enable testing.
                // Not relevant for normal processing.
                lastModifiedAtField.set(a,
                        LocalDateTime.of(lateAdmissionDate.minusDays(1),
                                LocalTime.of(15, 47)));

            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException("Could not modify lastUpdatedField", e);
            }
        });

        // Act
        final Event result = GoogleCalendarConverters.convert(activity);
        result.setUpdated(GoogleCalendarConverters.convert(
                LocalDateTime.of(lateAdmissionDate.minusDays(1),
                LocalTime.of(15, 47))));

        // new TreeMap<>(result).forEach((key, value) -> System.out.println("[" + key + "]: " + value));

        result.setFactory(GoogleCalendarConverters.JSON_FACTORY);
        final String prettyString = result.toPrettyString();


        // Assert
        Assert.assertNotNull(result);
        Assert.assertNotNull(prettyString);

        JSONAssert.assertEquals(expected, prettyString, true);
    }

    //
    // Helpers
    //

    public static <T extends NazgulEntity> T setJpaID(final T entity, final long value) {

        if (entity != null) {

            try {
                final Field idField = NazgulEntity.class.getDeclaredField("id");
                idField.setAccessible(true);

                idField.set(entity, value);
            } catch (Exception e) {
                throw new IllegalArgumentException("Could not set Jpa ID within ["
                        + entity.getClass().getCanonicalName() + "]", e);
            }
        }

        // All Done.
        return entity;
    }
}
