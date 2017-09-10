package se.mithlond.services.organisation.model.transport.activity;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.Amount;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ActivitiesTest extends AbstractEntityTest {

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

        organisation = new Organisation("AnOrganisation",
                "The Tolkien Society of Kinnekulle",
                null,
                "0123-234211",
                "02515-2325232-2323",
                new Address(null, null, "Kinnekullegatan", "54 C", "Kinnekulle", "142 41", "Sverige",
                        "Visiting address"),
                "kinnekulle.tolkien.se",
                TimeFormat.SWEDISH_TIMEZONE,
                TimeFormat.SWEDISH_LOCALE,
                WellKnownCurrency.SEK);

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
    public void validateMarshallingToJSon() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/activity/activities.json");
        activities.addActivityVOs(activityVO);

        // Act
        final String result = marshalToJSon(activities);
        // System.out.println("Got: " + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }
}