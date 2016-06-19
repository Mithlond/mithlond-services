/*
 * #%L
 * Nazgul Project: mithlond-services-content-impl-ejb
 * %%
 * Copyright (C) 2015 Mithlond
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
package se.mithlond.services.content.impl.ejb;

import org.dbunit.dataset.IDataSet;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.test.xmlbinding.XmlTestUtils;
import se.mithlond.services.content.api.NavigationService;
import se.mithlond.services.organisation.model.localization.Localization;
import se.mithlond.services.content.model.navigation.AbstractAuthorizedNavItem;
import se.mithlond.services.content.model.navigation.integration.MenuStructure;
import se.mithlond.services.content.model.navigation.integration.SeparatorMenuItem;
import se.mithlond.services.content.model.navigation.integration.StandardMenu;
import se.mithlond.services.content.model.navigation.integration.StandardMenuItem;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.GroupMembership;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.authorization.api.SemanticAuthorizationPathProducer;
import se.mithlond.services.shared.authorization.api.UnauthorizedException;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.spi.jpa.AbstractJpaService;
import se.mithlond.services.shared.test.entity.AbstractIntegrationTest;

import javax.persistence.EntityManager;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class NavigationServiceBeanIntegrationTest extends AbstractIntegrationTest {

    // Our Log
    private static final Logger log = LoggerFactory.getLogger(NavigationServiceBeanIntegrationTest.class);

    // Shared state
    private User silSalad, witchking;
    private Membership silSaladMifflondMembership, whichKingMifflondMembership;
    private Group mithlondMembers, mifflondEditors;
    private List<GroupMembership> groupMemberships;
    private NavigationService unitUnderTest;
    private MenuStructure templateMenuStructure;
    private StandardMenu rootMenu;
    private Organisation mifflond;

    /**
     * {@inheritDoc}
     */
    @Override
    public void doCustomSetup() {

        log.debug("Starting custom setup.");

        // #1) Create the Mifflond organisation.
        final Address visitingAddress = new Address("C/O Sven Svensson", null, "Svengatan", "24 B", "Göteborg",
                "435 55", "Sverige", "Besöksadress till Toksällskapet Mifflond");

        mifflond = new Organisation("Mifflond",
                "Göteborgs Toksällskap",
                "031-123456",
                "5010-0123456",
                "123456789-0",
                visitingAddress,
                "mifflond.se",
                TimeFormat.SWEDISH_TIMEZONE,
                TimeFormat.SWEDISH_LOCALE);

        // #2) Create the Groups.
        mithlondMembers = new Group("members", "Medlemmar i sällskapet Mithlond.", mifflond, null, "mithlondMembers");
        mifflondEditors = new Group("editors", "Redaktörer i sällskapet Mithlond.", mifflond, mithlondMembers,
                "mifflondEditors");

        // #3) Create the Users.
        final ZonedDateTime silSaladBirthday = ZonedDateTime.of(1978, 5, 1, 13, 15, 0, 0, TimeFormat.SWEDISH_TIMEZONE);
        final ZonedDateTime witchKingBirthday = ZonedDateTime.of(1977, 4, 2, 10, 12, 0, 0, TimeFormat.SWEDISH_TIMEZONE);
        int i = 1;
        final Address silSaladAddress = new Address(
                "careOfLine_" + i,
                "departmentName_" + i,
                "street_" + i,
                "number_" + i,
                "city_" + i,
                "zipCode_" + i,
                "country_" + i,
                "description_" + i);
        i++;
        final Address witchKingAddress = new Address(
                "careOfLine_" + i,
                "departmentName_" + i,
                "street_" + i,
                "number_" + i,
                "city_" + i,
                "zipCode_" + i,
                "country_" + i,
                "description_" + i);

        silSalad = new User("Sil",
                "Salad",
                silSaladBirthday.toLocalDate(),
                (short) 1234,
                silSaladAddress,
                new ArrayList<>(),
                new TreeMap<>(),
                "someToken1");
        witchking = new User("Which",
                "King",
                witchKingBirthday.toLocalDate(),
                (short) 5678,
                witchKingAddress,
                new ArrayList<>(),
                new TreeMap<>(),
                "someOtherToken");

        // #3) Create the Memberships
        silSaladMifflondMembership = new Membership("Sil-Salad",
                "Mifflonds Dront",
                "sil-salad",
                true,
                silSalad,
                mifflond);
        whichKingMifflondMembership = new Membership("Withking",
                "of Angmar",
                "whichking",
                true,
                witchking,
                mifflond);

        // #4) Add the Group memberships
        groupMemberships = new ArrayList<>();
        final GroupMembership silSaladUser = new GroupMembership(mithlondMembers, silSaladMifflondMembership);
        final GroupMembership whichKingUser = new GroupMembership(mithlondMembers, whichKingMifflondMembership);
        final GroupMembership whichKingEditor = new GroupMembership(mifflondEditors, whichKingMifflondMembership);
        groupMemberships.addAll(Arrays.asList(silSaladUser, whichKingUser, whichKingEditor));

        silSaladMifflondMembership.getGroupMemberships().add(silSaladUser);
        whichKingMifflondMembership.getGroupMemberships().add(whichKingUser);
        whichKingMifflondMembership.getGroupMemberships().add(whichKingEditor);

        // #5) Create the service to test.
        final NavigationServiceBean bean = new NavigationServiceBean();
        injectEntityManager(bean);
        this.unitUnderTest = bean;

        rootMenu = StandardMenu.getBuilder()
                .withDomId("rootMenu")
                .withLocalizedText("sv", "Roooot")
                .build();

        StandardMenu firstMenu = StandardMenu.getBuilder()
                .withDomId("firstMenu")
                .withLocalizedText("sv", "Första Menyn")
                .withLocalizedText("dk", "Først Menu")
                .withHref("/firstMenu")
                .withIconIdentifier("cog")
                .withTabIndex(1)
                .build();
        rootMenu.addChild(firstMenu);

        firstMenu.addChild(StandardMenuItem.getBuilder()
                .withAuthorizationPatterns("/mithlond/members")
                .withDomId("membersPage")
                .withHref("/members/list")
                .withIconIdentifier("man")
                .withLocalizedText("sv", "Medlemssida")
                .build());

        firstMenu.addChild(new SeparatorMenuItem());

        firstMenu.addChild(StandardMenuItem.getBuilder()
                .withDomId("membersIdeaPage")
                .withHref("/mithlond/members/ideas")
                .withLocalizedText("sv", "Idésida")
                .withIconIdentifier("lightbulb")
                .build());

        rootMenu.addChild(StandardMenuItem.getBuilder()
                .withAuthorizationPatterns("/mithlond/members,/forodrim/members")
                .withLocalizedText("sv", "Aktivitetskalender")
                .withEnabledStatus(true)
                .withHref("/calendar")
                .withDomId("plainItemPage3")
                .build());

        templateMenuStructure = new MenuStructure(rootMenu, mifflond);

        // Debug some.
        printCurrentDatabaseState();
    }

    /**
     * Override to ignore Foreign Key violation in dropping all database objects.
     * Also reset the sequences for all tables within the HSQLDB database, or
     * expected values for further tests will be incorrect.
     */
    @Override
    protected void cleanupTestSchema(final boolean shutdownDatabase) {
        try {
            super.cleanupTestSchema(shutdownDatabase);
        } catch (Exception e) {
            // Ignore this
        }

        final String[] tablesToReset = {"LOCALIZED_DEFINITIONS","LOCALIZED_TEXTS", "ORGANISATION"};
        final List<String> resetIndexStatements = Arrays.asList(tablesToReset).stream()
                .map(current -> "ALTER TABLE \"" + current + "\" ALTER COLUMN \"ID\" RESTART WITH 0")
                .collect(Collectors.toList());

        /*
        final List<String> resetIndexSqlStatements = new ArrayList<>();
        try {
            dbConnection = getJpaUnitTestConnection(true);
            final String[] tableType = {"TABLE"};
            final ResultSet rs = dbConnection.getMetaData().getTables(null, null, null, tableType);

            while(rs.next()) {
                final String tableName = rs.getString(3);
                resetIndexSqlStatements.add("ALTER TABLE \"" + tableName + "\" ALTER COLUMN \"ID\" RESTART WITH 0");
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create index reset statements.", e);
        }

        // Debug somewhat
        resetIndexStatements.stream().forEach(current -> System.out.println("  " + current));
        */

        try {
            final Connection dbConnection = getJpaUnitTestConnection(true);
            final Statement statement = dbConnection.createStatement();
            for (String current : resetIndexStatements) {
                statement.addBatch(current);
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new IllegalStateException("Could not execute index reset batch statements.", e);
        }
    }

    @Ignore("For now ...")
    @Test(expected = UnauthorizedException.class)
    public void validateExceptionOnUnauthorizedPersistingMenuStructure() throws Exception {

        // Assemble
        performStandardTestDbSetup();

        // Act & Assert
        unitUnderTest.createOrUpdate(templateMenuStructure, null);
    }

    @Test
    public void validatePersistingMenuStructure() throws Exception {

        // Assemble
        final IDataSet expected = performStandardTestDbSetup();
        final List<SemanticAuthorizationPathProducer> producers = new ArrayList<>();
        producers.add(whichKingMifflondMembership);

        entityManager.persist(mifflond);
        entityManager.flush();

        // Persist all contained Localizations
        final List<Localization> containedLocalizations = rootMenu.getLocalizedTexts().getContainedLocalizations();
        for (Localization current : containedLocalizations) {
            entityManager.persist(current);
            entityManager.flush();
        }
        rootMenu.getLocalizedTexts().assignManagedLocalizations(containedLocalizations);

        // Act
        final MenuStructure result = unitUnderTest.createOrUpdate(templateMenuStructure, producers);

        // Assert
        final List<MenuStructure> menuStructures = entityManager.createNamedQuery(
                MenuStructure.NAMEDQ_GET_BY_ORGANISATION_NAME, MenuStructure.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, "Mifflond")
                .getResultList();

        Assert.assertNotNull(result);
        Assert.assertEquals(1, menuStructures.size());

        final MenuStructure resurrected = menuStructures.get(0);
        Assert.assertEquals(templateMenuStructure.getOrganisationName(), resurrected.getOrganisationName());
        Assert.assertEquals(templateMenuStructure.getRootMenu(), resurrected.getRootMenu());

        final List<AbstractAuthorizedNavItem> templateChildren = templateMenuStructure.getRootMenu().getChildren();
        final List<AbstractAuthorizedNavItem> resurrectedChildren = resurrected.getRootMenu().getChildren();
        Assert.assertEquals(templateChildren.size(), resurrectedChildren.size());
        for (int i = 0; i < templateChildren.size(); i++) {
            Assert.assertEquals(templateChildren.get(i), resurrectedChildren.get(i));
        }

        System.out.println(" ================== ");
        System.out.println(extractFlatXmlDataSet(expected));
        System.out.println(" ================== ");

        System.out.println(" ================== ");
        System.out.println(extractFlatXmlDataSet(iDatabaseConnection.createDataSet()));
        System.out.println(" ================== ");

        final String xmlMarshalled = marshalToXML(result);
        XmlTestUtils.compareXmlIgnoringWhitespace(XmlTestUtils.readFully("testdata/menuStructure.xml"), xmlMarshalled);
    }

    //
    // Private helpers
    //

    private void injectEntityManager(final NavigationServiceBean toBeInjected) {

        try {
            final Field entityManagerField = AbstractJpaService.class.getDeclaredField("entityManager");
            Assert.assertNotNull(entityManagerField);
            Assert.assertEquals(EntityManager.class, entityManagerField.getType());

            entityManagerField.setAccessible(true);
            entityManagerField.set(toBeInjected, entityManager);
        } catch (Exception e) {
            throw new IllegalStateException("Could not inject EntityManager into ["
                    + toBeInjected.getClass().getName() + "]", e);
        }
    }
}
