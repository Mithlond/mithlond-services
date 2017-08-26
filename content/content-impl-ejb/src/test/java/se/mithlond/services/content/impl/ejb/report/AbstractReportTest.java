package se.mithlond.services.content.impl.ejb.report;

import org.junit.Assert;
import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.address.Address;
import se.mithlond.services.organisation.model.finance.WellKnownCurrency;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.mithlond.services.shared.test.entity.JpaIdMutator;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.time.LocalDate;
import java.time.Month;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractReportTest {

    // Shared state
    protected Organisation organisation;
    protected User haxx, erion;
    protected Membership memHaxx, memErion;
    protected File targetDir;

    protected void createStandardSharedState(@NotNull final String targetDirName) {

        final Category addressCategory = new Category("Visiting address", "visiting_address",
                "Address for visiting the organisation");
        final Address location = new Address(null, null, "Foo Street",
                "5",
                "Göteborg",
                "253 54",
                "Sverige",
                "Visiting address");

        organisation = new Organisation("The Organisation",
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
        JpaIdMutator.setId(organisation, 1);

        // Create some users
        haxx = new User(
                "Mr",
                "Häxxmästaren",
                LocalDate.of(1968, Month.SEPTEMBER, 17),
                (short) 1235,
                new Address(null,
                        null,
                        "Testgatan",
                        "45 E",
                        "Grååååbo",
                        "234 54",
                        "Sverige",
                        "Hemma hos Mr Häxx"),
                null,
                null,
                "dasToken");
        JpaIdMutator.setId(haxx, 42);

        erion = new User(
                "Das",
                "Erion",
                LocalDate.of(1922, Month.FEBRUARY, 5),
                (short) 2345,
                new Address(null,
                        null,
                        "Yttertestgatan",
                        "25",
                        "Göteborg",
                        "411 11",
                        "Sverige",
                        "Hemma hos Das Erion"),
                null,
                null,
                "dasErionToken");
        JpaIdMutator.setId(erion, 43);

        memHaxx = new Membership("Häxx",
                "Das Filuro",
                "haxx",
                true,
                haxx,
                organisation);
        memErion = new Membership("Erion",
                null,
                "erion",
                true,
                erion,
                organisation);
        JpaIdMutator.setId(memHaxx, 101);
        JpaIdMutator.setId(memErion, 102);

        final String path = getClass().getClassLoader()
                .getResource("testdata")
                .getPath();
        File targetParentDir = new File(path);
        Assert.assertTrue(targetParentDir.exists());
        Assert.assertTrue(targetParentDir.isDirectory());

        this.targetDir = new File(targetParentDir, targetDirName);
        if(!targetDir.exists()) {
            targetDir.mkdirs();
        }

        Assert.assertTrue(targetDir.exists());
        Assert.assertTrue(targetDir.isDirectory());
    }

    protected File getTargetFile(final String fileName, final String fileSuffix) {

        File toReturn = null;

        for (int i = 0; true; i++) {
            toReturn = new File(targetDir, fileName + "_" + i + "." + fileSuffix);
            if (!toReturn.exists()) {
                return toReturn;
            }
        }
    }
}
