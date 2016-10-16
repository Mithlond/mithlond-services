/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
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
package se.mithlond.services.content.model.articles.media;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.content.model.ContentPatterns;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.imageio.ImageIO;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

/**
 * Encapsulation of a bitmapped image. The image types permitted within this BitmapImage are:
 * <ol>
 * <li>PNG images (which must have the suffix ".png")</li>
 * <li>JPG images (which must have the suffix ".jpg" or ".jpeg")</li>
 * <li>GIF images (which must have the suffix ".gif")</li>
 * </ol>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@XmlType(namespace = ContentPatterns.NAMESPACE,
        propOrder = {"name", "contentType", "widthInPixels", "heightInPixels"})
@XmlAccessorType(XmlAccessType.FIELD)
public class BitmapImage extends NazgulEntity {

    /**
     * The only permitted
     */
    public static final SortedSet<String> PERMITTED_IMAGE_FILE_SUFFIXES;

    /**
     * The protocol for a file.
     */
    public static final String FILE_PROTOCOL = "file";

    /**
     * The protocol for a JAR.
     */
    public static final String JAR_PROTOCOL = "jar";

    static {

        // Create the permitted image file suffixes Map.
        final SortedSet<String> tmp = new TreeSet<>();
        Stream.of("jpg", "png", "gif").map(c -> "." + c).forEach(tmp::add);
        PERMITTED_IMAGE_FILE_SUFFIXES = Collections.unmodifiableSortedSet(tmp);
    }

    /**
     * The width of this BitmapImage, in pixels.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlAttribute(required = true)
    private int widthInPixels;

    /**
     * The height of this BitmapImage, in pixels.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlAttribute(required = true)
    private int heightInPixels;

    /**
     * The name of this image.
     */
    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(required = true)
    private String name;

    /**
     * The content type (i.e. mime type) of this BitmapImage. Typically something like "image/jpeg".
     */
    @Basic(optional = false)
    @Column(nullable = false, length = 120)
    @XmlElement(required = true)
    private String contentType;

    /**
     * The binary image data (i.e. byte[]) of the image.
     * This is not transmitted within the JAXB representation of this BitmapImage.
     */
    @Lob
    @Column(length = 100000)
    @XmlTransient
    private byte[] imageData;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public BitmapImage() {
    }

    /**
     * Compound constructor creating a {@link BitmapImage} wrapping the supplied data.
     *
     * @param name           The optional image name.
     * @param widthInPixels  The width of the image, in pixels.
     * @param heightInPixels The height of the image, in pixels.
     * @param contentType    The content type (such as "image/jpeg") of this BitmapImage.
     * @param imageData      The binary image data content.
     */
    private BitmapImage(final String name,
            final int widthInPixels,
            final int heightInPixels,
            final String contentType,
            final byte[] imageData) {

        // Assign internal state
        this.widthInPixels = widthInPixels;
        this.heightInPixels = heightInPixels;
        this.name = name;
        this.contentType = contentType;
        this.imageData = imageData;
    }

    /**
     * @return The width of this BitmapImage, in pixels.
     */
    public int getWidthInPixels() {
        return widthInPixels;
    }

    /**
     * @return The height of this BitmapImage, in pixels.
     */
    public int getHeightInPixels() {
        return heightInPixels;
    }

    /**
     * Retrieves the name of this BitmapImage.
     *
     * @return the name of this BitmapImage.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The content type (such as "image/jpeg") of this BitmapImage.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @return The binary image data content.
     */
    public byte[] getImageData() {
        return imageData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(widthInPixels, "widthInPixels")
                .notNull(heightInPixels, "heightInPixels")
                .notNullOrEmpty(contentType, "contentType")
                .notNull(imageData, "imageData")
                .endExpressionAndValidate();
    }

    /**
     * Creates a BitmapImage originating from a File.
     *
     * @param resourcePath The resource path to an image file.
     * @return The converted and created BitmapImage.
     */
    public static BitmapImage createFromResourcePath(final String resourcePath) {

        // Check sanity
        Validate.notEmpty(resourcePath, "resourcePath");
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(resourcePath);
        if (resource == null) {
            throw new IllegalArgumentException("No resource found for resourcePath " + resourcePath);
        }

        // We will only handle "JAR" or "FILE" protocols.
        if (FILE_PROTOCOL.equalsIgnoreCase(resource.getProtocol())) {

            // Delegate
            return createFromFile(new File(resource.getPath()));
        }
        if (!JAR_PROTOCOL.equalsIgnoreCase(resource.getProtocol())) {
            throw new IllegalArgumentException("Will only handle 'JAR' protocol URLs.");
        }

        //
        // Ensure that the URI targets a single image file.
        // The typical URI should look like the following:
        //
        // jar:file:/some/path/to/images.jar!/images/example_png.png
        //
        final String jarProtocolStart = JAR_PROTOCOL + ":";
        final String innerProtocolStringForm = resource.toString().substring(
                resource.toString().indexOf(jarProtocolStart) + jarProtocolStart.length());

        if (!innerProtocolStringForm.trim().toLowerCase().startsWith(FILE_PROTOCOL + ":")) {
            throw new IllegalArgumentException("Expected sub-protocol 'file:' within the JAR file., but got: "
                    + innerProtocolStringForm);
        }

        final String internalJarPath = innerProtocolStringForm.substring(innerProtocolStringForm.indexOf("!") + 1);

        // Harmonize and validate the image file name.
        final String fileName = internalJarPath.substring(internalJarPath.lastIndexOf("/") + 1);
        final String harmonizedFileName = validateAndHarmonizeName(fileName);

        // Find the Mime type of the image.
        final FileNameMap fileNameMap = URLConnection.getFileNameMap();
        final String mimeType = fileNameMap.getContentTypeFor(harmonizedFileName);

        // Read the image data from the JarEntry
        final byte[] data;
        final BufferedImage bufferedImage;
        try {
            data = IOUtils.toByteArray(resource.openStream());
            bufferedImage = ImageIO.read(new ByteArrayInputStream(data));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read image data from [" + resource.toString() + "]", e);
        }

        // All Done.
        return new BitmapImage(
                harmonizedFileName,
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                mimeType,
                data);
    }

    /**
     * Creates a BitmapImage originating from a File.
     *
     * @param imageFile The File pointing to an Image.
     * @return The converted and created BitmapImage.
     */
    public static BitmapImage createFromFile(final File imageFile) {

        // Check sanity
        final File file = Validate.notNull(imageFile, "imageFile");

        // Replace whitespace with '_' and make the name lowercase.
        final String harmonizedFileName = validateAndHarmonizeName(file.getName());

        // Read the data, and convert it to a BufferedImage.
        final byte[] bytes;
        final BufferedImage bufferedImage;
        try {
            bytes = Files.readAllBytes(file.toPath());
            bufferedImage = ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (IOException e) {

            // Complain.
            throw new IllegalArgumentException("Could not create a BufferedImage from file ["
                    + imageFile.getName() + "] with the harmonized name [" + harmonizedFileName + "]", e);
        }

        // Find the Mime type of the image.
        final FileNameMap fileNameMap = URLConnection.getFileNameMap();
        final String mimeType = fileNameMap.getContentTypeFor(harmonizedFileName);

        // All Done.
        return new BitmapImage(harmonizedFileName,
                bufferedImage.getWidth(),
                bufferedImage.getHeight(),
                mimeType,
                bytes);
    }

    private static String validateAndHarmonizeName(final String pathOrFileName) {

        // Replace whitespace with '_' and make the name lowercase.
        final String harmonizedFileName = StringUtils.join(
                StringUtils.split(
                        pathOrFileName.trim().toLowerCase()), '_');
        final boolean isPermittedFileName = PERMITTED_IMAGE_FILE_SUFFIXES.stream()
                .filter(harmonizedFileName::endsWith)
                .findFirst()
                .isPresent();

        if (!isPermittedFileName) {
            throw new IllegalArgumentException("Cannot handle file [" + pathOrFileName + "] with the harmonized "
                    + "name [" + harmonizedFileName + "], since its name does not end with one of the required "
                    + "suffixes: "
                    + PERMITTED_IMAGE_FILE_SUFFIXES.stream().reduce((l, r) -> l + ", " + r).orElse("<Nah>"));
        }

        // All Done.
        return harmonizedFileName;
    }
}
