package se.mithlond.services.content.model.articles.media;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class BitmapImageTest {

    // Shared state
    private Map<String, File> fileName2ImageFileMap;
    private Map<String, BitmapImage> fileName2BitmapImageMap;

    private File jarFileFile;
    private ClassLoader originalClassLoader;

    @Before
    public void setupSharedState() throws MalformedURLException {

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

        fileName2ImageFileMap = Stream.of("gif", "png", "jpg")
                .map(suffix -> "testdata/articles/example_" + suffix + "." + suffix)
                .map(path -> getClass().getClassLoader().getResource(path))
                .filter(imgURL -> imgURL != null)
                .map(imgURL -> new File(imgURL.getPath()))
                .collect(Collectors.toMap(File::getName, f -> f));
        Assert.assertEquals(3, fileName2ImageFileMap.size());

        fileName2BitmapImageMap = new TreeMap<>();
        fileName2ImageFileMap.entrySet().forEach(entry -> {
            fileName2BitmapImageMap.put(entry.getKey(), BitmapImage.createFromFile(entry.getValue()));
        });
        Assert.assertEquals(3, fileName2BitmapImageMap.size());
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
    public void validateMimeTypes() {

        // Assemble
        final SortedMap<String, String> expectedMimeTypeMap = new TreeMap<>();
        expectedMimeTypeMap.put("example_gif.gif", "image/gif");
        expectedMimeTypeMap.put("example_png.png", "image/png");
        expectedMimeTypeMap.put("example_jpg.jpg", "image/jpeg");

        // Act & Assert
        fileName2BitmapImageMap.entrySet().forEach(entry -> {

            final String expectedMimeType = expectedMimeTypeMap.get(entry.getKey());
            Assert.assertNotNull("Expected filename [" + entry.getKey() + "] not found.", expectedMimeType);

            final BitmapImage bitmapImage = entry.getValue();
            Assert.assertEquals(expectedMimeType, bitmapImage.getContentType());
        });
    }

    @Test
    public void validateImageDataSize() {

        // Assemble
        final SortedMap<String, Integer> expectedImageDataSize = new TreeMap<>();
        expectedImageDataSize.put("example_gif.gif", 16862);
        expectedImageDataSize.put("example_png.png", 54482);
        expectedImageDataSize.put("example_jpg.jpg", 83261);

        // Act & Assert
        fileName2BitmapImageMap.entrySet().forEach(entry -> {

            final BitmapImage bitmapImage = entry.getValue();
            final byte[] imageData = bitmapImage.getImageData();

            Assert.assertNotNull(imageData);
            Assert.assertEquals((long) expectedImageDataSize.get(entry.getKey()), (long) imageData.length);
        });
    }

    @Test
    public void validateDataSizesInJarFile() throws MalformedURLException {

        // Assemble
        final SortedMap<String, Integer> expectedImageDataSize = new TreeMap<>();
        expectedImageDataSize.put("images/example_gif.gif", 16862);
        expectedImageDataSize.put("images/example_png.png", 54482);
        expectedImageDataSize.put("images/example_jpg.jpg", 83261);

        /*
        [example_gif.gif] --> jar:file:/path/to/target/test-classes/testdata/articles/images.jar!/images/example_gif.gif
        [example_jpg.jpg] --> jar:file:/path/to/target/test-classes/testdata/articles/images.jar!/images/example_jpg.jpg
        [example_png.png] --> jar:file:/path/to/target/test-classes/testdata/articles/images.jar!/images/example_png.png
         */
        final Set<String> resourcePaths = fileName2BitmapImageMap.keySet()
                .stream()
                .map(fileName -> "images/" + fileName)
                .collect(Collectors.toSet());

        // Act
        final Map<String, BitmapImage> resourcePath2BitmapImage = resourcePaths
                .stream()
                .collect(Collectors.toMap(rp -> rp, BitmapImage::createFromResourcePath));

        // Assert
        Assert.assertEquals(3, resourcePath2BitmapImage.size());
        resourcePath2BitmapImage.entrySet().forEach(entry -> {

            final BitmapImage bitmapImage = entry.getValue();
            final byte[] imageData = bitmapImage.getImageData();

            Assert.assertNotNull(imageData);
            Assert.assertEquals((long) expectedImageDataSize.get(entry.getKey()), (long) imageData.length);
        });
    }

    @Test
    public void validateMimeTypesInJarFile() throws MalformedURLException {

        // Assemble
        final SortedMap<String, String> expectedMimeTypeMap = new TreeMap<>();
        expectedMimeTypeMap.put("images/example_gif.gif", "image/gif");
        expectedMimeTypeMap.put("images/example_png.png", "image/png");
        expectedMimeTypeMap.put("images/example_jpg.jpg", "image/jpeg");

        /*
        [example_gif.gif] --> jar:file:/path/to/target/test-classes/testdata/articles/images.jar!/images/example_gif.gif
        [example_jpg.jpg] --> jar:file:/path/to/target/test-classes/testdata/articles/images.jar!/images/example_jpg.jpg
        [example_png.png] --> jar:file:/path/to/target/test-classes/testdata/articles/images.jar!/images/example_png.png
         */
        final Set<String> resourcePaths = fileName2BitmapImageMap.keySet()
                .stream()
                .map(fileName -> "images/" + fileName)
                .collect(Collectors.toSet());

        // Act
        final Map<String, BitmapImage> resourcePath2BitmapImage = resourcePaths
                .stream()
                .collect(Collectors.toMap(rp -> rp, BitmapImage::createFromResourcePath));

        // Assert
        Assert.assertEquals(3, resourcePath2BitmapImage.size());
        resourcePath2BitmapImage.entrySet().forEach(entry -> {

            final String expectedMimeType = expectedMimeTypeMap.get(entry.getKey());
            Assert.assertNotNull("Expected filename [" + entry.getKey() + "] not found.", expectedMimeType);

            final BitmapImage bitmapImage = entry.getValue();
            Assert.assertEquals(expectedMimeType, bitmapImage.getContentType());
        });
    }
}
