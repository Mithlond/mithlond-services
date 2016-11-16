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
package se.mithlond.services.content.impl.ejb;

import se.mithlond.services.content.model.articles.Article;
import se.mithlond.services.content.model.articles.Section;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.organisation.model.activity.Admission;
import se.mithlond.services.organisation.model.activity.EventCalendar;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.finance.Amount;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class PersistenceHelper {

    // Internal state
    private static EntityManager entityManager;

    /**
     * Assigns the EntityManager within this {@link PersistenceHelper}.
     *
     * @param entityManager The non-null EntityManager.
     */
    public static void setEntityManager(final EntityManager entityManager) {
        PersistenceHelper.entityManager = entityManager;
    }

    private static <T> T persist(final T toPersist) {

        if (toPersist != null && !entityManager.contains(toPersist)) {

            // Persist, and immediately flush to ensure that JPA IDs are properly created.
            entityManager.persist(toPersist);
            entityManager.flush();

            // All Done.
            return toPersist;
        }

        // Nah.
        return null;
    }

    /**
     * Creates and persists an Organisation with the supplied data.
     *
     * @param persist          if true, persists JPA persistence before returning.
     * @param organisationName The name of the organisation
     * @param suffix           The organisation's suffix.
     * @param emailSuffix      The organisation's email suffix.
     * @return The Persisted organisation.
     */
    public static Organisation createOrganisation(
            final boolean persist,
            final String organisationName,
            final String suffix,
            final String emailSuffix) {

        final Organisation organisation = new Organisation(organisationName,
                suffix,
                "031-123456",
                "5060-12340",
                "4090201-1",
                new Address(null,
                        null,
                        "Småstensgatan",
                        "34 B",
                        "Landvetter",
                        "34564",
                        "Sverige",
                        "Besöksadress till " + organisationName),
                emailSuffix,
                TimeFormat.SWEDISH_TIMEZONE.normalized(),
                TimeFormat.SWEDISH_LOCALE,
                WellKnownCurrency.SEK);

        return persist ? persist(organisation) : organisation;
    }


    /**
     * Creates - and optionally persists - a User.
     *
     * @param persist             if true, persists JPA persistence before returning.
     * @param userIdentifierToken The unique user Identifier token.
     * @param firstName           The first name of the User.
     * @param lastName            The last name of the User.
     * @return The User created (and, optionally, persisted).
     */
    public static User createUser(
            final boolean persist,
            final String userIdentifierToken,
            final String firstName,
            final String lastName) {

        final List<Membership> memberships = new ArrayList<>();
        final Map<String, String> contactDetails = new TreeMap<>();

        final String addressDescription = "Hemma hos " + firstName + " " + lastName;

        // All Done.
        final User user = new User(firstName, lastName,
                LocalDate.of(1974, Month.MAY, 23),
                (short) 2346,
                new Address(null, null, "Testgatan", "42", "Härryda", "45235", "Sverige", addressDescription),
                memberships,
                contactDetails,
                userIdentifierToken);

        // All Done.
        return persist ? persist(user) : user;
    }

    public static Activity createActivity(
            final boolean persist,
            final Membership responsible,
            final String shortDesc,
            final LocalDateTime startTime,
            final Amount cost,
            final CategorizedAddress catAddress) {

        final Activity activity = new Activity(shortDesc, "Full description for " + shortDesc,
                startTime,
                startTime.plusHours(2),
                cost,
                new Amount(cost.getValue().add(BigDecimal.valueOf(25L)), WellKnownCurrency.SEK),
                startTime.minusDays(2L).toLocalDate(),
                startTime.minusDays(2L).toLocalDate(),
                false,
                "Midgårda Dräkt",
                catAddress.getCategory(),
                catAddress.getAddress(),
                catAddress.getShortDesc(),
                catAddress.getOwningOrganisation(),
                null,
                true);

        final Admission initialAdmission = new Admission(activity,
                responsible,
                LocalDateTime.now(),
                LocalDateTime.now(),
                responsible.getAlias() + " skapade aktiviteten.",
                true,
                null);

        activity.getAdmissions().add(initialAdmission);

        return persist ? persist(activity) : activity;
    }

    /**
     * Performs a base population of the Database.
     */
    public static void doStandardSetup() {

        // Setup some Organisations
        final Organisation mifflond = createOrganisation(true, "Mifflond", "Göteborgs Toksällskap", "mifflond.se");
        final Organisation fjodjim = createOrganisation(true, "Fjodjim", "Ftockholmf Toksällskap", "fjodrim.se");

        // Create some Groups
        final Group mithlondMembers = persist(
                new Group("Inbyggare", "Mifflonds inbyggare", mifflond, null, "inbyggare"));
        final Group mithlondCouncil = persist(
                new Group("Grå Rådet", "Mifflonds Grå Råd", mifflond, mithlondMembers, "graradet"));
        final Group fjodjimMembers = persist(
                new Group("Inbyggare", "Fjodjims inbyggare", fjodjim, null, "inbyggare"));
        final Group fjodjimCouncil = persist(
                new Group("Stora Rådet", "Fjodjims Stora Råd", fjodjim, fjodjimMembers, "storaradet"));

        // Create some Guilds
        final Guild mellonathEldar = persist(new Guild("Alvgillet", "Gillet samlar Mifflonds alver", mifflond,
                "alvgillet", "Eldar", "Mellonath"));
        final Guild corMenelmacar = persist(
                new Guild("Kämpaleksgillet", "Gillet samlar Mifflonds slagskämpar",
                        mifflond, "kämpaleksgillet", "Menelmacar", "Cor"));

        final Guild mellonathBralda = persist(new Guild("Ölgillet", "Gillet samlar Fjodjims öldrickare", fjodjim,
                "bralda", "Bralda", "Mellonath"));

        // Create some Users.
        final User allan = createUser(true, "allan", "Allan", "Octamac");
        final User asa = createUser(true, "åsa", "Åsa", "Österstjärna");
        final User rackham = createUser(true, "rackham", "Rackham", "Den Röde");
        final User nobody = createUser(true, "nobody", "Mr", "Noone");

        // Create some Memberships
        final Membership mifflondBilbo = persist(
                new Membership("Bilbo Baggins", null, "bilbo", true, allan, mifflond));
        final Membership mifflondDildo = persist(
                new Membership("Dildo Baggins", "The nemesis", "dildo", true, asa, mifflond));
        final Membership mifflondGromp = persist(
                new Membership("Gromp", null, "gromp", true, rackham, mifflond));
        final Membership mifflondGrima = persist(
                new Membership("Grima", "the Wyrmtongue", "grima", false, nobody, mifflond));

        final Membership fjodjimAragorn = persist(
                new Membership("Aragorn", null, "aragorn", true, allan, fjodjim));
        final Membership fjodjimPledra = persist(
                new Membership("Pledra", "The sneak", "pledra", false, asa, fjodjim));
        final Membership fjodjimZap = persist(
                new Membership("Zap", "the wizard", "zap", true, rackham, fjodjim));

        // Create some guild members
        mifflondBilbo.addOrUpdateGuildMembership(mellonathEldar, true, false, false);
        mifflondBilbo.addOrUpdateGuildMembership(corMenelmacar, false, false, false);
        mifflondDildo.addOrUpdateGuildMembership(corMenelmacar, true, false, false);
        mifflondGromp.addOrUpdateGuildMembership(corMenelmacar, false, true, false);
        mifflondGromp.addOrUpdateGuildMembership(mellonathEldar, false, true, false);
        mifflondGrima.addOrUpdateGuildMembership(mellonathEldar, false, false, false);
        fjodjimAragorn.addOrUpdateGuildMembership(mellonathBralda, false, false, false);
        fjodjimPledra.addOrUpdateGuildMembership(mellonathBralda, true, false, false);
        fjodjimZap.addOrUpdateGuildMembership(mellonathBralda, false, false, true);

        // Create some CategorizedAddresses
        final Category restaurantAddress = persist(new Category(
                "Restaurang",
                CategorizedAddress.ACTIVITY_CLASSIFICATION,
                "Restaurang eller Café"));
        final Category shopAddress = persist(new Category(
                "Affär",
                CategorizedAddress.ACTIVITY_CLASSIFICATION,
                "Affär eller Bazaar"));

        final CategorizedAddress helloMonkeyAddress = persist(new CategorizedAddress("Hello Monkey",
                "Restaurang Hello Monkey",
                restaurantAddress,
                mifflond,
                new Address(null, null, "Linnégatan", "52", "Göteborg", "413 08", "Sverige",
                        "Besöksadress Hello Monkey Linnégatan")));
        final CategorizedAddress baristaKistaAddress = persist(new CategorizedAddress("Barista",
                "Barista Kista Galleria",
                restaurantAddress,
                fjodjim,
                new Address(null, null, "Danmarksgatan", "11", "Kista", "164 53", "Sverige",
                        "Besöksadress Barista Kista Galleria")));

        // Create some addresses
        final Activity swordFighting = createActivity(true,
                fjodjimAragorn,
                "Svärdsfäktning",
                LocalDateTime.of(2016, Month.OCTOBER, 5, 18, 0),
                new Amount(BigDecimal.valueOf(25L), WellKnownCurrency.SEK),
                new CategorizedAddress("Barista",
                        "Barista Stockholms Central",
                        restaurantAddress,
                        fjodjim,
                        new Address(null, null, "Centralplan", "15", "Stockholm", "111 20", "Sverige",
                                "Besöksadress Barista Stockholms Central")));

        final Activity shieldPainting = createActivity(true,
                fjodjimZap,
                "Sköldmålning",
                LocalDateTime.of(2016, Month.OCTOBER, 28, 19, 30),
                new Amount(BigDecimal.valueOf(5L), WellKnownCurrency.SEK),
                new CategorizedAddress("Barista",
                        "Barista Götgatan",
                        restaurantAddress,
                        fjodjim,
                        new Address(null, null, "Götgatan", "67", "Stockholm", "116 21", "Sverige",
                                "Besöksadress Barista Götgatan")));

        // Create a few EventCalendars
        final EventCalendar devCalendar = new EventCalendar(
                "Mifflond Development Calendar",
                "EventCalendar for Mifflond Application Development",
                mifflond,
                "mifflondDevCalendar",
                "Development");
        persist(devCalendar);
        entityManager.flush();

        final EventCalendar stagingCalendar = new EventCalendar(
                "Mifflond Staging Calendar",
                "EventCalendar for Mifflond Application Staging",
                mifflond,
                "mifflondStagingCalendar",
                "Development");
        persist(stagingCalendar);
        entityManager.flush();

        //
        // Content database population
        //

        // Create some articles
        final Article newsArticle = new Article(LocalDateTime.of(2016, Month.SEPTEMBER, 15, 3, 52),
                mifflondGromp,
                "Hot off the Mifflond presses",
                "/mifflond/news/hot",
                mifflond);
        persist(newsArticle);
        entityManager.flush();

        final Article humorousArticle = new Article(LocalDateTime.of(2016, Month.AUGUST, 25, 14, 11),
                mifflondBilbo,
                "Cough up the Mifflond presses",
                "/mifflond/humor/funny",
                mifflond);
        persist(humorousArticle);
        entityManager.flush();

        // Add some sections with dummy content.
        Stream.of(newsArticle, humorousArticle).forEach(article -> {

            for (int i = 1; i < 6; i++) {

                final Section section = new Section("This is section " + i,
                        true,
                        "This is the text in section " + i);
                persist(section);
                entityManager.flush();

                article.addSection(section, mifflondDildo);
            }

            entityManager.merge(article);
        });
    }
}
