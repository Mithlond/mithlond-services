package se.mithlond.services.organisation.model.membership;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.transport.membership.GroupVO;
import se.mithlond.services.organisation.model.transport.membership.Groups;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.AbstractPlainJaxbTest;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class GroupsTest extends AbstractPlainJaxbTest {

    // Shared state
    private Organisation[] organisations;
    private Category[] categories;
    private Group[] groups;
    private Guild[] guilds;
    private Group[] groupsAndGuilds;

    @Before
    public void setupSharedState() {

        final AtomicLong idSequence = new AtomicLong();

        // Create the Organisations
        organisations = new Organisation[3];
        for (int i = 0; i < organisations.length; i++) {
            final Address currentAddress = new Address(
                    null,
                    "departmentName_" + i,
                    "street_" + i,
                    "number_" + i,
                    "city_" + i,
                    "zipCode_" + i,
                    "country_" + i,
                    "description_" + i);

            organisations[i] = new Organisation(
                    "name_" + i,
                    "suffix_" + i,
                    "phone_" + i,
                    "bankAccountInfo_" + i,
                    "postAccountInfo_" + i,
                    currentAddress,
                    "emailSuffix_" + i,
                    TimeFormat.SWEDISH_TIMEZONE.normalized(),
                    TimeFormat.SWEDISH_LOCALE,
                    WellKnownCurrency.SEK);
            AbstractEntityTest.setJpaIDFor(organisations[i], idSequence.incrementAndGet());
        }


        // Create some categories
        categories = new Category[10];
        for (int i = 0; i < categories.length; i++) {
            categories[i] = new Category(
                    "categoryID_" + i,
                    "classification_" + i,
                    "description_" + i);
        }

        // Create some Groups
        groups = new Group[5];
        guilds = new Guild[groups.length];
        for (int i = 0; i < groups.length; i++) {

            groups[i] = new Group(
                    "groupName_" + i,
                    "groupDescription_" + i,
                    organisations[i % organisations.length],
                    (i > 1 ? groups[i - 1] : null),
                    "emailList_" + i);
            AbstractEntityTest.setJpaIDFor(groups[i], idSequence.incrementAndGet());

            guilds[i] = new Guild(
                    "guildName_" + i,
                    "guildDescription_" + i,
                    organisations[i % organisations.length],
                    "emailList_" + i,
                    "quenyaName_" + i,
                    "quenyaPrefix_" + i);
            AbstractEntityTest.setJpaIDFor(guilds[i], idSequence.incrementAndGet());
        }

        groupsAndGuilds = new Group[groups.length + guilds.length];
        for (int i = 0; i < groups.length; i++) {
            groupsAndGuilds[2 * i] = groups[i];
            groupsAndGuilds[(2 * i) + 1] = guilds[i];
        }
    }

    @Test
    public void validateMarshallingToDetailedJSon() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/membership/groupsAndGuilds.json");
        final Groups unitUnderTest = new Groups(groupsAndGuilds);

        // Act
        final String result = marshalToJSon(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }

    @Test
    public void validateMarshallingToShallowJSon() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully("testdata/transport/membership/groupVOs.json");
        final List<GroupVO> groupVOs = Arrays
                .stream(groupsAndGuilds)
                .map(GroupVO::new)
                .collect(Collectors.toList());
        final Groups unitUnderTest = new Groups(groupVOs.toArray(new GroupVO[groupVOs.size()]));

        // Act
        final String result = marshalToJSon(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        JSONAssert.assertEquals(expected, result, true);
    }
}
