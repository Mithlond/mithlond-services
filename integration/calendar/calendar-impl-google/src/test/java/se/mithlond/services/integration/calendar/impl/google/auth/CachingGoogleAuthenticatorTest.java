package se.mithlond.services.integration.calendar.impl.google.auth;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.mithlond.services.shared.spi.algorithms.Deployment;

import java.io.File;
import java.net.URL;


/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class CachingGoogleAuthenticatorTest {

    // Shared state
    private CachingGoogleAuthenticator unitUnderTest;
    private String deploymentType;
    private File localRootDir;

    @Before
    public void setupSharedState() {

        this.unitUnderTest = new CachingGoogleAuthenticator();

        // Assign the deployment type
        deploymentType = "unittest";
        System.setProperty(Deployment.DEPLOYMENT_TYPE_KEY, deploymentType);

        // Assign the local configuration file root directory
        final URL configRootDirURL = getClass().getResource("/testdata/configRootDir");
        localRootDir = new File(configRootDirURL.getPath());
        System.setProperty(Deployment.DEPLOYMENT_STORAGE_ROOTDIR_KEY, localRootDir.getAbsolutePath());
    }

    @After
    public void teardownSharedState() {

        System.clearProperty(Deployment.DEPLOYMENT_STORAGE_ROOTDIR_KEY);
        System.clearProperty(Deployment.DEPLOYMENT_TYPE_KEY);
    }

    @Test
    public void validateHttpTransportIsNotNull() throws Exception {

        // Assemble

        // Act
        final HttpTransport transport1 = unitUnderTest.getTransport();
        final int hash1 = transport1.hashCode();

        final HttpTransport transport2 = unitUnderTest.getTransport();
        final int hash2 = transport2.hashCode();

        // Assert
        Assert.assertNotNull(transport1);
        Assert.assertEquals(NetHttpTransport.class, transport1.getClass());
        Assert.assertEquals(hash1, hash2);
        Assert.assertSame(transport1, transport2);
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnNullOrganisationNameForClient() throws Exception {

        // Assemble

        // Act & Assert
        unitUnderTest.getCalendarClient(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnEmptyOrganisationNameForClient() throws Exception {

        // Assemble

        // Act & Assert
        unitUnderTest.getCalendarClient("");
    }

    @Test
    public void validateGettingServiceAccountEmail() {

        // Assemble
        final String expected = "exampleServiceAccountMail@google.com";
        final String organisationName = "Foobar";

        // Act
        final String result = unitUnderTest.getServiceAccountEmail(organisationName);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertNotNull(result);
        Assert.assertEquals(expected, result);
    }

    @Test
    public void validateGettingP12File() {

        // Assemble
        final String organisationName = "Foobar";
        final File expected = new File(localRootDir, organisationName + "/unittest/google/calendar/"
                + GoogleAuthenticator.GOOGLE_SERVICE_ACCOUNT_P12_FILE);

        // Act
        final File serviceP12File = unitUnderTest.getServiceP12File(organisationName);
        // System.out.println("Got: " + result);

        // Assert
        Assert.assertNotNull(serviceP12File);
        Assert.assertTrue(serviceP12File.getAbsolutePath().endsWith(expected.getAbsolutePath()));
    }
}
