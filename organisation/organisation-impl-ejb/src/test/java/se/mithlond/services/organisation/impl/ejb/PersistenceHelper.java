package se.mithlond.services.organisation.impl.ejb;

import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
                TimeFormat.SWEDISH_LOCALE);

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
    }
}
