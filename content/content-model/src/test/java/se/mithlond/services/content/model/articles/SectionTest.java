package se.mithlond.services.content.model.articles;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.mithlond.services.content.model.articles.media.BitmapImage;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SectionTest {

    // Shared state
    private Section unitUnderTest;
    private BitmapImage pngImage;

    private File jarFileFile;
    private ClassLoader originalClassLoader;

    @Before
    public void setupSharedState() throws Exception {

        // First, identify the path to the JAR containing images.
        final URL imageJarURL = getClass().getClassLoader().getResource("testdata/articles/images.jar");
        Assert.assertNotNull(imageJarURL);

        jarFileFile = new File(imageJarURL.getPath());
        Assert.assertTrue(jarFileFile.exists() && jarFileFile.isFile());

        // Setup the classloader to include the image JAR.
        originalClassLoader = Thread.currentThread().getContextClassLoader();
        final URL[] imageJarFileURLs = new URL[]{jarFileFile.toURI().toURL()};
        final URLClassLoader imageJarClassLoader = new URLClassLoader(imageJarFileURLs, originalClassLoader);
        Thread.currentThread().setContextClassLoader(imageJarClassLoader);

        // Read the BitmapImage and create its Section.
        this.pngImage = BitmapImage.createFromResourcePath("images/example_png.png");
        unitUnderTest = new Section("heading", true, "This is some text", pngImage);
    }

    @After
    public void restoreClassLoader() {

        try {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        } catch (Exception e) {
            // Do nothing.
        }
    }

    @Test
    public void validateEmtyImageListOnNoImages() {

        // Assemble
        final Section unitUnderTest = new Section("someHeading", true, "TextData");

        // Act
        final List<BitmapImage> images = unitUnderTest.getImages();

        // Assert
        Assert.assertNotNull(images);
        Assert.assertEquals(0, images.size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void validateExceptionOnSettingEmptyHeading() {

        // Act & Assert
        unitUnderTest.setHeading("");
    }

    @Test(expected = NullPointerException.class)
    public void validateExceptionOnSettingNullHeading() {

        // Act & Assert
        unitUnderTest.setHeading(null);
    }

    @Test
    public void validateUpdatingInternalState() {

        // Assemble
        final String heading = "updatedHeading";

        // Act
        unitUnderTest.setHeading(heading);
        unitUnderTest.setShowHeading(false);

        // Assert
        Assert.assertEquals(heading, unitUnderTest.getHeading());
        Assert.assertFalse(unitUnderTest.isShowHeading());
    }
}
