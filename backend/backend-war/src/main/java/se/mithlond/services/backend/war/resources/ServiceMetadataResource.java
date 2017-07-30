/*-
 * #%L
 * Nazgul Project: mithlond-services-backend-war
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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
package se.mithlond.services.backend.war.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.organisation.model.transport.metadata.ServiceMetadataInfo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * Resource emitting information about this service itself.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Path("/metadata")
public class ServiceMetadataResource extends AbstractResource {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(ServiceMetadataResource.class);

    private static final String MANIFEST_RESOURCE_PATH = "META-INF/MANIFEST.MF";

    /**
     * Retrieves the metadata information of this running Service.
     *
     * @return the metadata information of this running Service.
     */
    @GET
    public ServiceMetadataInfo getMetadata() {

        // #0) Create the mandatory properties.
        String serverVersion = "unknown";
        LocalDateTime buildTime = LocalDateTime.now();

        // #1) Find and read the Manifest file to retrieve its properties
        try {

            final Enumeration<URL> resources = getClass().getClassLoader().getResources(MANIFEST_RESOURCE_PATH);
            if (resources != null) {

                final List<URL> resourceURLs = Collections.list(resources)
                        .stream()
                        .filter(anURL -> !anURL.getPath().contains("/lib/"))
                        .filter(anURL -> !anURL.getPath().contains(".jar!"))
                        .filter(anURL -> !anURL.getPath().endsWith("WEB-INF/classes/META-INF/MANIFEST.MF"))
                        .collect(Collectors.toList());
                if (resourceURLs != null && resourceURLs.size() > 0) {

                    /*
                    Bnd-LastModified: 1501091138984
                    Build-Jdk: 1.8.0_131
                    Built-By: lj
                    Bundle-Description: Mithlond Service: Backend (WAR, version 1.0.0-SNAPSHOT)
                    Bundle-DocURL: http://www.mithlond.se
                    Bundle-License: http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
                    Bundle-ManifestVersion: 2
                    Bundle-Name: mithlond-services-backend-war
                    Bundle-SymbolicName: mithlond-services-backend-war
                    Bundle-Vendor: Mithlond
                    Bundle-Version: 1.0.0.SNAPSHOT
                    */

                    // Fetch the Maven GAV
                    final SortedMap<String, String> manifestProperties = parseManifestFile(resourceURLs.get(0));
                    serverVersion = manifestProperties.get("Bundle-Version");

                    // Fetch the build time
                    final String numSeconds = manifestProperties.get("Bnd-LastModified");
                    buildTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(Long.parseLong(numSeconds)),
                            ZoneId.systemDefault());
                }

            }

        } catch (IOException e) {
            log.error("Could not retrieve data from Manifest file", e);
        }

        // #2) Find the data to return
        final SortedMap<String, String> jvmProperties = new TreeMap<>();

        getSortedSystemProperties().entrySet()
                .stream()
                .filter(Objects::nonNull)
                .filter(e -> !e.getKey().toLowerCase().contains("path"))
                .filter(e -> !e.getKey().toLowerCase().contains(".dirs"))
                .filter(e -> !e.getKey().toLowerCase().contains(".security"))
                .filter(e -> !e.getKey().toLowerCase().contains("javax."))
                .filter(e -> !e.getKey().toLowerCase().contains("jboss.server"))
                .filter(e -> !e.getKey().toLowerCase().contains("sun.java.command"))
                .filter(e -> !e.getKey().toLowerCase().contains("user.dir"))
                .filter(e -> !e.getKey().toLowerCase().contains("user.home"))
                .filter(e -> !e.getKey().isEmpty() || !e.getValue().isEmpty())
                .filter(e -> !e.getKey().startsWith("file."))
                .forEach(e -> jvmProperties.put(e.getKey(), e.getValue()));

        // All Done.
        return new ServiceMetadataInfo(serverVersion, buildTime, jvmProperties);
    }

    //
    // Private helpers
    //

    private SortedMap<String, String> parseManifestFile(final URL manifestURL) {

        final SortedMap<String, String> toReturn = new TreeMap<>();

        try {
            final Manifest manifest = new Manifest(manifestURL.openStream());

            final Attributes mainAttributes = manifest.getMainAttributes();
            mainAttributes.forEach((k, v) -> {

                final String stringKey = "" + k;
                final String stringValue = "" + v;

                if (!stringKey.isEmpty() && !stringValue.isEmpty()) {
                    toReturn.put(stringKey, stringValue);
                }
            });
        } catch (IOException e) {
            log.error("Could not parse manifestFile [" + manifestURL + "]", e);
        }

        // All Done.
        return toReturn;
    }

    private SortedMap<String, String> getSortedSystemProperties() {

        final SortedMap<String, String> toReturn = new TreeMap<>();

        System.getProperties().forEach((k, v) -> {
            toReturn.put("" + k, "" + v);
        });

        // All Done.
        return toReturn;
    }
}
