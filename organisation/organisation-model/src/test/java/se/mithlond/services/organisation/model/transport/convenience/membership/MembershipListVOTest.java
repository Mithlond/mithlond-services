package se.mithlond.services.organisation.model.transport.convenience.membership;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.model.AbstractEntityTest;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.GroupMembership;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.membership.guild.GuildMembership;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransportable;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class MembershipListVOTest extends AbstractEntityTest {

    // Shared state
    private Organisation mifflond, fjodjim;
    private User bilboUser, cimnolweUser;
    private Membership bilboMifflond, dildoFjodjim, cimnolweMifflond, cimnolweFjodjim;
    private Group mifflondMembers, mifflondAdmins, fjodjimMembers, fjodjimKnights;
    private Guild mifflondElves, mifflondMusicians, fjodjimDwarves, fjodjimSingers;

    @Before
    public void setupSharedState() {

        jaxb.add(MembershipListVO.class);

        // #1) Create Organisations
        mifflond = new Organisation("mifflond",
                "suffix",
                "phone",
                "bankAccountInfo",
                "postAccountInfo",
                createAddress(1),
                "emailSuffix",
                TimeFormat.SWEDISH_TIMEZONE.normalized(),
                TimeFormat.SWEDISH_LOCALE,
                WellKnownCurrency.SEK);
        AbstractEntityTest.setJpaIDFor(mifflond, 1L);

        fjodjim = new Organisation("fjodjim",
                "suffix",
                "phone",
                "bankAccountInfo",
                "postAccountInfo",
                createAddress(2),
                "emailSuffix",
                TimeFormat.SWEDISH_TIMEZONE.normalized(),
                TimeFormat.SWEDISH_LOCALE,
                WellKnownCurrency.SEK);
        AbstractEntityTest.setJpaIDFor(fjodjim, 2L);

        // #2) Create users
        bilboUser = new User("Mr", "Anderson",
                LocalDate.of(1975, Month.JUNE, 22),
                (short) 2346,
                createAddress(3),
                null,
                null,
                "bullboUser");
        AbstractEntityTest.setJpaIDFor(bilboUser, 10L);

        bilboUser.getContactDetails().put("EMAIL", "mr.anderson@matrix.org");
        bilboUser.getContactDetails().put("SKYPE", "whatisthematrix");
        bilboUser.getContactDetails().put("HOME_PHONE", "+46 284 192 203");

        cimnolweUser = new User("Mrs", "Arrrrveteg",
                LocalDate.of(1976, Month.OCTOBER, 1),
                (short) 4567,
                createAddress(4),
                null,
                null,
                "cimnollllwe");
        AbstractEntityTest.setJpaIDFor(cimnolweUser, 11L);

        cimnolweUser.getContactDetails().put("EMAIL", "skittrevlig@yahoo.com");
        cimnolweUser.getContactDetails().put("GITHUB", "clueless");
        cimnolweUser.getContactDetails().put("HOME_PHONE", "+46 345 678 901");

        // #3) Add Memberships
        bilboMifflond = new Membership("Bilbo",
                null,
                "bilbo",
                true,
                bilboUser,
                mifflond,
                null,
                null);
        AbstractEntityTest.setJpaIDFor(bilboMifflond, 101L);

        dildoFjodjim = new Membership("Dildo Dagger",
                "The slim one",
                "dildo",
                true,
                bilboUser,
                fjodjim,
                null,
                null);
        AbstractEntityTest.setJpaIDFor(dildoFjodjim, 102L);

        cimnolweMifflond = new Membership("Cimnolwe",
                "Morgoths Svarta Massssk",
                "cimnolwe",
                true,
                cimnolweUser,
                mifflond,
                null,
                null);
        AbstractEntityTest.setJpaIDFor(cimnolweMifflond, 103L);

        cimnolweFjodjim = new Membership("Cimnolwe",
                "Morgoths lilla svarrrta",
                "cimnolwe",
                false,
                cimnolweUser,
                fjodjim,
                null,
                null);
        AbstractEntityTest.setJpaIDFor(cimnolweFjodjim, 104L);

        // #3) Add some Groups and Group memberships
        mifflondMembers = new Group("Inbyggare",
                "Mifflonds Inbyggare",
                mifflond,
                null,
                "mifflond");
        AbstractEntityTest.setJpaIDFor(mifflondMembers, 201L);

        mifflondAdmins = new Group("Administratörer",
                "Mifflonds Administratörer",
                mifflond,
                mifflondMembers,
                "admins");
        AbstractEntityTest.setJpaIDFor(mifflondAdmins, 202L);

        fjodjimMembers = new Group("Inbuggare",
                "Fjodjims Inbuggare",
                fjodjim,
                null,
                "inbuggare");
        AbstractEntityTest.setJpaIDFor(fjodjimMembers, 203L);

        fjodjimKnights = new Group("Knäck",
                "Fjodjims knäck",
                fjodjim,
                fjodjimMembers,
                "knektar");
        AbstractEntityTest.setJpaIDFor(fjodjimKnights, 204L);

        bilboMifflond.getGroupMemberships().add(new GroupMembership(mifflondMembers, bilboMifflond));
        cimnolweMifflond.getGroupMemberships().add(new GroupMembership(mifflondMembers, cimnolweMifflond));
        cimnolweMifflond.getGroupMemberships().add(new GroupMembership(mifflondAdmins, cimnolweMifflond));
        dildoFjodjim.getGroupMemberships().add(new GroupMembership(fjodjimMembers, dildoFjodjim));
        dildoFjodjim.getGroupMemberships().add(new GroupMembership(fjodjimKnights, dildoFjodjim));
        cimnolweFjodjim.getGroupMemberships().add(new GroupMembership(fjodjimMembers, cimnolweFjodjim));

        // #4) Add some Guilds and Guild memberships
        mifflondElves = new Guild("Alvgillet",
                "Gillet samlar Mifflonds Alver.",
                mifflond,
                "eldar",
                "Eldar",
                "Mellonath");
        AbstractEntityTest.setJpaIDFor(mifflondElves, 301L);

        mifflondMusicians = new Guild("Muzikgillet",
                "Gillet samlar Mifflonds Musikanter.",
                mifflond,
                "musiker",
                null,
                null);
        AbstractEntityTest.setJpaIDFor(mifflondMusicians, 302L);

        fjodjimDwarves = new Guild("Dvärgagillet",
                "Gillet samlar Fjodjims Dvärgar.",
                fjodjim,
                "khuzdul",
                null,
                null);
        AbstractEntityTest.setJpaIDFor(fjodjimDwarves, 303L);

        fjodjimSingers = new Guild("Sånggillet",
                "Gillet samlar Fjodjims Sångare.",
                fjodjim,
                "gleowine",
                "Mellonath",
                "Gleowine");
        AbstractEntityTest.setJpaIDFor(fjodjimSingers, 304L);

        bilboMifflond.getGroupMemberships().add(
                new GuildMembership(mifflondElves,
                        bilboMifflond,
                        false,
                        false,
                        false));
        cimnolweMifflond.getGroupMemberships().add(new GuildMembership(mifflondElves,
                cimnolweMifflond,
                true,
                false,
                false));
        cimnolweMifflond.getGroupMemberships().add(new GuildMembership(mifflondMusicians,
                cimnolweMifflond,
                false,
                false,
                false));
        dildoFjodjim.getGroupMemberships().add(new GuildMembership(fjodjimDwarves,
                dildoFjodjim,
                false,
                false,
                false));
        dildoFjodjim.getGroupMemberships().add(new GuildMembership(fjodjimSingers,
                dildoFjodjim,
                false,
                true,
                false));
        cimnolweFjodjim.getGroupMemberships().add(new GuildMembership(fjodjimSingers,
                cimnolweFjodjim,
                false,
                false,
                false));
    }

    @Test
    public void validateAddingOnlyMembershipsWithinGivenOrganisation() throws Exception {

        // Assemble
        final String expected = XmlTestUtils.readFully(
                "testdata/transport/convenience/membership/membershipListVO.json");
        final MembershipListVO unitUnderTest = new MembershipListVO(mifflond);

        // Act
        unitUnderTest.add(bilboMifflond, dildoFjodjim, cimnolweMifflond, cimnolweFjodjim);
        final String result = marshalToJSon(unitUnderTest);
        // System.out.println("Got: " + result);

        // Assert
        final List<SlimMemberVO> memberInfoVOs = unitUnderTest.getMemberInformation();
        Assert.assertEquals(2, memberInfoVOs.size());
        final SlimMemberVO bilboVO = memberInfoVOs.stream()
                .filter(c -> c.getFullAlias().startsWith("Bilbo"))
                .findFirst()
                .orElse(null);
        final SlimMemberVO cimnolweVO = memberInfoVOs.stream()
                .filter(c -> c.getFullAlias().startsWith("Cimnolwe"))
                .findFirst()
                .orElse(null);
        Assert.assertNotNull(bilboVO);
        Assert.assertNotNull(cimnolweVO);

        // #1) Verify that only the correct memberships have been included.
        final List<String> aliasList = memberInfoVOs.stream()
                .map(SlimMemberVO::getFullAlias)
                .collect(Collectors.toList());
        // System.out.println("Got: " + aliasList.stream().reduce((l,r) -> l + "; " + r).orElse("<none>"));

        Assert.assertTrue(aliasList.contains("Bilbo"));
        Assert.assertTrue(aliasList.contains("Cimnolwe, Morgoths Svarta Massssk"));

        // #2) Validate the login permitted flags
        Assert.assertTrue(bilboVO.getLoginPermitted());
        Assert.assertTrue(cimnolweVO.getLoginPermitted());

        // #3) Validate basic data
        validateBasicData(cimnolweVO, cimnolweMifflond);
        validateBasicData(bilboVO, bilboMifflond);

        JSONAssert.assertEquals(expected, result, true);
    }

    //
    // Private helpers
    //

    private void validateBasicData(final SlimMemberVO theVO, final Membership theMembership) {

        final User theUser = theMembership.getUser();

        Assert.assertEquals(theVO.getBirthday(), theUser.getBirthday());
        Assert.assertEquals(theVO.getFirstName(), theUser.getFirstName());
        Assert.assertEquals(theVO.getLastName(), theUser.getLastName());
        Assert.assertEquals(theVO.getEmailAlias(), theMembership.getEmailAlias());

        final Set<GroupMembership> groupMemberships = theMembership.getGroupMemberships();

        // Groups
        final List<SlimGroupMembershipVO> groups = theVO.getGroups();
        final List<GroupMembership> grMemberships = groupMemberships
                .stream()
                .filter(gr -> !(gr instanceof GuildMembership))
                .collect(Collectors.toList());
        Assert.assertEquals(groups.size(), grMemberships.size());

        final List<String> slimGroupXmlIds = groups.stream()
                .map(AbstractSimpleTransportable::getXmlId)
                .sorted()
                .collect(Collectors.toList());
        final List<String> groupXmlIds = groups.stream()
                .map(AbstractSimpleTransportable::getXmlId)
                .sorted()
                .collect(Collectors.toList());
        Assert.assertEquals(slimGroupXmlIds.size(), groupXmlIds.size());
        slimGroupXmlIds.forEach(slim -> Assert.assertTrue(groupXmlIds.contains(slim)));

        // Guilds
        final List<SlimGuildMembershipVO> guilds = theVO.getGuilds();
        final List<GroupMembership> guildMemberships = groupMemberships
                .stream()
                .filter(gr -> (gr instanceof GuildMembership))
                .collect(Collectors.toList());
        Assert.assertEquals(guilds.size(), guildMemberships.size());

        final List<String> slimGuildXmlIds = guilds.stream()
                .map(AbstractSimpleTransportable::getXmlId)
                .sorted()
                .collect(Collectors.toList());
        final List<String> guildXmlIds = guilds.stream()
                .map(AbstractSimpleTransportable::getXmlId)
                .sorted()
                .collect(Collectors.toList());
        Assert.assertEquals(slimGuildXmlIds.size(), guildXmlIds.size());
        slimGuildXmlIds.forEach(slim -> Assert.assertTrue(guildXmlIds.contains(slim)));
    }

    private Address createAddress(int plusIndex) {
        return new Address(
                "careOfLine_" + plusIndex,
                "departmentName_" + plusIndex,
                "street_" + plusIndex,
                "number_" + plusIndex,
                "city_" + plusIndex,
                "zipCode_" + plusIndex,
                "country_" + plusIndex,
                "description_" + plusIndex);
    }
}
