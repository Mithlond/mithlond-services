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
import se.mithlond.services.organisation.api.parameters.ActivitySearchParameters;
import se.mithlond.services.organisation.model.Listable;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.finance.Amount;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.transport.OrganisationVO;
import se.mithlond.services.organisation.model.transport.activity.Activities;
import se.mithlond.services.organisation.model.transport.activity.ActivityVO;
import se.mithlond.services.organisation.model.transport.activity.AdmissionVO;
import se.mithlond.services.organisation.model.transport.address.CategoriesAndAddresses;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class ActivityServiceBeanTest extends AbstractOrganisationIntegrationTest {

    // Shared state
    private ActivityServiceBean unitUnderTest;
    private MembershipServiceBean membershipServiceBean;
    private OrganisationServiceBean organisationServiceBean;
    private LocalDateTime aTimestamp;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doCustomSetup() {

        // First, handle the standard setup.
        super.doCustomSetup();

        // Create the test unit
        organisationServiceBean = new OrganisationServiceBean();
        membershipServiceBean = new MembershipServiceBean();
        unitUnderTest = new ActivityServiceBean(organisationServiceBean);

        // Inject the EntityManager connected to the in-memory DB.
        injectEntityManager(unitUnderTest);
        injectEntityManager(membershipServiceBean);
        injectEntityManager(organisationServiceBean);

        aTimestamp = LocalDateTime.of(2016, Month.JUNE, 20, 17, 0);
    }

    @Test
    public void validateAddingAdmissions() throws Exception {

        // Assemble
        // System.out.println("Got: \n" + extractFlatXmlDataSet(iDatabaseConnection.createDataSet()));
        final Membership zap = membershipServiceBean.getMembership(ORG_FJODJIM, "Zap");

        final ActivitySearchParameters shallowSearchParams = ActivitySearchParameters.builder()
                .withOrganisationIDs(FJODJIM_JPA_ID)
                .withStartPeriod(LocalDateTime.of(2016, Month.OCTOBER, 16, 0, 0)) // Should match "Shieldpainting" only.
                .build();
        final ActivitySearchParameters detailSearchParams = ActivitySearchParameters.builder()
                .withOrganisationIDs(FJODJIM_JPA_ID)
                .withStartPeriod(LocalDateTime.of(2016, Month.SEPTEMBER, 16, 0, 0))
                // Should match "Shieldpainting" and "Svärdsfäktning".
                .withDetailedResponsePreferred(true)
                .build();

        // Act
        final Activities shallowActivities = unitUnderTest.getActivities(shallowSearchParams, zap);
        final Activities detailActivities = unitUnderTest.getActivities(detailSearchParams, zap);

        // Assert
        Assert.assertNotNull(shallowActivities);
        Assert.assertEquals(0, shallowActivities.getActivities().size());
        Assert.assertEquals(1, shallowActivities.getActivityVOs().size());

        Assert.assertNotNull(detailActivities);
        Assert.assertEquals(2, detailActivities.getActivities().size());
        Assert.assertEquals(0, detailActivities.getActivityVOs().size());

        final ActivityVO activityVO = shallowActivities.getActivityVOs().get(0);
        final String shallowActivityShortDesc = activityVO.getShortDesc();
        Assert.assertEquals("Sköldmålning", shallowActivityShortDesc);

        final Map<String, Activity> detailedMap = detailActivities.getActivities()
                .stream()
                .collect(Collectors.toMap(Listable::getShortDesc, a -> a));

        Assert.assertTrue(detailedMap.containsKey("Svärdsfäktning"));
        Assert.assertTrue(detailedMap.containsKey("Sköldmålning"));
    }

    @Test
    public void validateAddingActivities() throws Exception {

        // Assemble
        final Membership zap = membershipServiceBean.getMembership(ORG_FJODJIM, "Zap");
        final Membership pledra = membershipServiceBean.getMembership(ORG_FJODJIM, "Pledra");
        final Membership bilbo = membershipServiceBean.getMembership(ORG_MIFFLOND, "Bilbo Baggins");
        final Membership dildo = membershipServiceBean.getMembership(ORG_MIFFLOND, "Dildo Baggins");

        final Organisation fjodjim = zap.getOrganisation();
        final Organisation mifflond = bilbo.getOrganisation();

        Assert.assertEquals(fjodjim.getOrganisationName(), "Fjodjim");
        Assert.assertEquals(mifflond.getOrganisationName(), "Mifflond");

        final OrganisationVO fjodjimVO = new OrganisationVO(fjodjim);
        final OrganisationVO mifflondVO = new OrganisationVO(mifflond);

        final CategoriesAndAddresses fjodjimActivityAddresses = unitUnderTest.getActivityLocationAddresses(
                fjodjim.getId());
        final CategoriesAndAddresses mifflondActivityAddresses = unitUnderTest.getActivityLocationAddresses(
                mifflond.getId());

        final CategorizedAddress mifflondActivityAddress = mifflondActivityAddresses.getCategorizedAddresses().get(0);
        final CategorizedAddress fjodjimActivityAddress = fjodjimActivityAddresses.getCategorizedAddresses().get(0);

        Assert.assertEquals(2, fjodjimActivityAddresses.getCategories().size());
        Assert.assertEquals(2, mifflondActivityAddresses.getCategories().size());

        final ActivityVO mifflondActivityVO = new ActivityVO(0L,
                mifflondVO,
                "Teprovning",
                "Välkommen till teprovning hos Alvgillet",
                LocalDateTime.of(2016, Month.SEPTEMBER, 14, 18, 30),
                LocalDateTime.of(2016, Month.SEPTEMBER, 14, 22, 30),
                new Amount(BigDecimal.ZERO, WellKnownCurrency.SEK),
                new Amount(BigDecimal.ZERO, WellKnownCurrency.SEK),
                LocalDate.of(2016, Month.SEPTEMBER, 14),
                LocalDate.of(2016, Month.SEPTEMBER, 14),
                false,
                "Utgårda klädsel",
                mifflondActivityAddress.getCategory().getCategoryID(),
                mifflondActivityAddress.getAddress(),
                mifflondActivityAddress.getShortDesc(),
                null,
                true);

        final ActivityVO fjodjimActivityVO = new ActivityVO(0L,
                fjodjimVO,
                "Biskvi-krig",
                "Välkommen till chokladprovning hos Morgothgillet",
                LocalDateTime.of(2016, Month.SEPTEMBER, 20, 17, 45),
                LocalDateTime.of(2016, Month.SEPTEMBER, 20, 19, 0),
                new Amount(BigDecimal.valueOf(50L), WellKnownCurrency.SEK),
                new Amount(BigDecimal.valueOf(60L), WellKnownCurrency.SEK),
                LocalDate.of(2016, Month.SEPTEMBER, 10),
                LocalDate.of(2016, Month.SEPTEMBER, 19),
                false,
                "Oömma klädsel, som tål choklad",
                fjodjimActivityAddress.getCategory().getCategoryID(),
                fjodjimActivityAddress.getAddress(),
                fjodjimActivityAddress.getShortDesc(),
                null,
                true);

        mifflondActivityVO.getAdmissions().add(new AdmissionVO(
                AdmissionVO.UNINITIALIZED,
                bilbo.getId(),
                bilbo.getAlias(),
                bilbo.getOrganisation().getOrganisationName(),
                aTimestamp.plusDays(1),
                null,
                "Skapade Aktiviteten",
                true));
        mifflondActivityVO.getAdmissions().add(new AdmissionVO(
                AdmissionVO.UNINITIALIZED,
                dildo.getId(),
                dildo.getAlias(),
                dildo.getOrganisation().getOrganisationName(),
                aTimestamp.plusDays(1),
                aTimestamp.plusDays(1),
                null,
                false));

        fjodjimActivityVO.getAdmissions().add(new AdmissionVO(
                AdmissionVO.UNINITIALIZED,
                zap.getId(),
                zap.getAlias(),
                zap.getOrganisation().getOrganisationName(),
                aTimestamp.plusDays(2),
                aTimestamp.plusDays(3),
                "Skapade Aktiviteten",
                true));
        fjodjimActivityVO.getAdmissions().add(new AdmissionVO(
                AdmissionVO.UNINITIALIZED,
                dildo.getId(),
                dildo.getAlias(),
                dildo.getOrganisation().getOrganisationName(),
                aTimestamp.plusDays(2),
                aTimestamp.plusDays(4),
                null,
                false));

        // Act
        final Activities mifflondActivities = unitUnderTest.createActivities(new Activities(mifflondActivityVO), bilbo);
        final Activities fjodjimActivities = unitUnderTest.createActivities(new Activities(fjodjimActivityVO), zap);

        final Activities shallowFjodjimActivities = unitUnderTest.getActivities(
                ActivitySearchParameters.builder()
                        .withOrganisationIDs(FJODJIM_JPA_ID)
                        .withStartPeriod(LocalDateTime.of(2016, Month.SEPTEMBER, 10, 0, 0))
                        .withEndPeriod(LocalDateTime.of(2016, Month.SEPTEMBER, 25, 0, 0))
                        .build(),
                zap);
        final Activities shallowMifflondActivities = unitUnderTest.getActivities(
                ActivitySearchParameters.builder()
                        .withOrganisationIDs(MIFFLOND_JPA_ID)
                        .withStartPeriod(LocalDateTime.of(2016, Month.SEPTEMBER, 10, 0, 0))
                        .withEndPeriod(LocalDateTime.of(2016, Month.SEPTEMBER, 25, 0, 0))
                        .build(),
                zap);

        // Assert
        Assert.assertNotNull(shallowFjodjimActivities);
        Assert.assertNotNull(shallowMifflondActivities);

        Assert.assertEquals(1, shallowFjodjimActivities.getActivityVOs().size());
        Assert.assertEquals(1, shallowMifflondActivities.getActivityVOs().size());
    }
}
