package se.mithlond.services.organisation.impl.ejb;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.organisation.api.parameters.CategorizedAddressSearchParameters;
import se.mithlond.services.organisation.api.parameters.GroupIdSearchParameters;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.transport.Organisations;
import se.mithlond.services.organisation.model.transport.address.CategoriesAndAddresses;
import se.mithlond.services.organisation.model.transport.membership.GroupVO;
import se.mithlond.services.organisation.model.transport.membership.Groups;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class OrganisationServiceBeanTest extends AbstractOrganisationIntegrationTest {

    // Shared state
    private static final String ORG_MIFFLOND = "Mifflond";
    private static final String ORG_FJODJIM = "Fjodjim";
    private static final Long MIFFLOND_JPA_ID = 1L;
    private static final Long FJODJIM_JPA_ID = 2L;

    private OrganisationServiceBean unitUnderTest;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCustomSetup() {

        // First, handle the standard setup.
        super.doCustomSetup();

        // Create the test unit
        unitUnderTest = new OrganisationServiceBean();

        // Inject the EntityManager connected to the in-memory DB.
        injectEntityManager(unitUnderTest);
    }

    @Test
    public void validateFindingAndMarshallingOrganisations() {

        // Assemble
        final String expectedShallowRepresentation = XmlTestUtils
                .readFully("testdata/shallowOrganisationRepresentation.xml");
        final String expectedFullRepresentation = XmlTestUtils
                .readFully("testdata/fullOrganisationRepresentation.xml");

        // Act
        final Organisations shallowOrganisations = unitUnderTest.getOrganisations(false);
        final Organisations fullOrganisations = unitUnderTest.getOrganisations(true);

        final Organisations shallowMifflond = unitUnderTest.getOrganisation(MIFFLOND_JPA_ID, false);
        final Organisations fullMifflond = unitUnderTest.getOrganisation(MIFFLOND_JPA_ID, true);

        final String shallowResult = marshalToXML(shallowOrganisations);
        final String fullResult = marshalToXML(fullOrganisations);

        // System.out.println("Shallow: " + shallowResult);
        // System.out.println("Full: " + fullResult);

        // Assert
        validateIdenticalContent(expectedShallowRepresentation, shallowResult);
        validateIdenticalContent(expectedFullRepresentation, fullResult);

        Assert.assertNotNull(shallowMifflond);
        Assert.assertEquals(ORG_MIFFLOND, shallowMifflond.getOrganisationVOs().first().getOrganisationName());
        Assert.assertEquals(ORG_MIFFLOND, fullMifflond.getOrganisations().first().getOrganisationName());
    }

    @Test
    public void validateGroups() throws Exception {

        // Assemble
        final Set<String> expectedGroupNames = Stream.of("Inbyggare", "Grå Rådet").collect(Collectors.toSet());
        final Set<String> expectedGuildNames = Stream.of("Alvgillet", "Kämpaleksgillet").collect(Collectors.toSet());

        final GroupIdSearchParameters shallowSearchParams = GroupIdSearchParameters
                .builder()
                .withOrganisationIDs(MIFFLOND_JPA_ID)
                .withDetailedResponsePreferred(false)
                .build();
        final GroupIdSearchParameters detailedSearchParams = GroupIdSearchParameters
                .builder()
                .withOrganisationIDs(MIFFLOND_JPA_ID)
                .withDetailedResponsePreferred(true)
                .build();

        // Act
        final Groups shallowGroups = unitUnderTest.getGroups(shallowSearchParams);
        final Groups detailedGroups = unitUnderTest.getGroups(detailedSearchParams);

        final SortedSet<GroupVO> groupVOs = shallowGroups.getGroupVOs();
        final SortedSet<Group> groups = detailedGroups.getGroups();

        /*
        1: Mifflonds inbyggare
        2: Mifflonds Grå Råd
        3: Gillet samlar Mifflonds alver
        4: Gillet samlar Mifflonds slagskämpar
         */

        // Assert
        Assert.assertNotNull(shallowGroups);
        Assert.assertEquals(4, groupVOs.size());
        Assert.assertEquals(4, groups.size());
        Assert.assertEquals(0, shallowGroups.getGroups().size());
        Assert.assertEquals(0, detailedGroups.getGroupVOs().size());

        final Set<Guild> mithlondGuilds = groups.stream().filter(g -> g instanceof Guild)
                .map(g -> (Guild) g)
                .collect(Collectors.toSet());
        final Set<Group> pureGroups = groups.stream().filter(g -> !(g instanceof Guild))
                .collect(Collectors.toSet());

        final Set<String> guildVONames = groupVOs.stream().map(GroupVO::getName)
                .collect(Collectors.toSet());

        Assert.assertEquals(2, mithlondGuilds.size());
        Assert.assertEquals(2, pureGroups.size());

        pureGroups.stream()
                .map(Group::getGroupName)
                .forEach(name -> Assert.assertTrue(expectedGroupNames.contains(name)));
        mithlondGuilds.stream()
                .map(Group::getGroupName)
                .forEach(name -> Assert.assertTrue(expectedGuildNames.contains(name)));

        final Set<String> voNames = Stream.concat(expectedGroupNames.stream(), expectedGuildNames.stream())
                .collect(Collectors.toSet());
        guildVONames.forEach(voName -> Assert.assertTrue("VO Name [" + voName + "] was not found in voNames: "
                        + voNames.stream().reduce((l, r) -> l + ", " + r).orElse("<Nopes>"),
                voNames.contains(voName)));

    }

    @Ignore("Fix NamedQuery first.")
    @Test
    public void validateCategorizedAddressSearch() {

        // Assemble
        final CategorizedAddressSearchParameters shallowSearchParams = CategorizedAddressSearchParameters
                .builder()
                .withOrganisationID(FJODJIM_JPA_ID)
                .withCityPattern("%andvet%")
                .build();

        // Act
        final CategoriesAndAddresses caddrs = unitUnderTest.getCategorizedAddresses(shallowSearchParams);
        final List<CategorizedAddress> categorizedAddresses = caddrs.getCategorizedAddresses();

        // Assert
        Assert.assertNotNull(categorizedAddresses);
        Assert.assertEquals(1, categorizedAddresses.size());
    }
}
