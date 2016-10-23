/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-impl-ejb
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
package se.mithlond.services.organisation.impl.ejb;

import org.junit.Assert;
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

    @Test
    public void validateCategorizedAddressSearch() throws Exception {

        // Assemble
        //
        //   <CATEGORIZEDADDRESS ID="2" FULLDESC="Barista Kista Galleria" SHORTDESC="Barista"
        //     VERSION="1" CITY="Kista" COUNTRY="Sverige" DESCRIPTION="Bes&#246;ksadress Barista Kista Galleria"
        //     NUMBER="11" STREET="Danmarksgatan" ZIPCODE="164 53" CATEGORY_ID="1" OWNINGORGANISATION_ID="2"/>
        //
        final CategorizedAddressSearchParameters shallowSearchParams = CategorizedAddressSearchParameters
                .builder()
                .withOrganisationID(FJODJIM_JPA_ID)
                .withCityPattern("ist") // Should match "Kista"
                .build();

        // Act
        final CategoriesAndAddresses caddrs = unitUnderTest.getCategorizedAddresses(shallowSearchParams);
        final List<CategorizedAddress> categorizedAddresses = caddrs.getCategorizedAddresses();

        // Assert
        Assert.assertNotNull(categorizedAddresses);
        Assert.assertEquals(1, categorizedAddresses.size());

        CategorizedAddress caddress = categorizedAddresses.get(0);
        Assert.assertEquals("Kista", caddress.getAddress().getCity());
        Assert.assertEquals("Barista", caddress.getShortDesc());
        Assert.assertEquals("Restaurang", caddress.getCategory().getCategoryID());
        Assert.assertEquals(CategorizedAddress.ACTIVITY_CLASSIFICATION, caddress.getCategory().getClassification());
    }
}
