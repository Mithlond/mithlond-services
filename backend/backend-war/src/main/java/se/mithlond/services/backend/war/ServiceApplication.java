/*
 * #%L
 * Nazgul Project: mithlond-services-backend-war
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
package se.mithlond.services.backend.war;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The JAX-RS Application-derived class providing the integration point to backend services.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@ApplicationPath("/resource")
public class ServiceApplication extends Application {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(ServiceApplication.class);

    // Internal state
    @Context private ServletContext context;

    private Set<Class<?>> jaxRsClasses;
    private Set<Object> jaxRsSingletons;

    /**
     * Default constructor.
     */
    public ServiceApplication() {

        // Create internal state.
        this.jaxRsClasses = new HashSet<>();
        this.jaxRsSingletons = new HashSet<>();
    }

    @PostConstruct
    public void logState() {

        log.info("ServiceApplication launched.");

        final Set<Class<?>> classes = getClasses();
        log.info("Got [" + classes.size() + "] Classes");
        classes.forEach(current -> log.info("Class [" + current.getCanonicalName() + "]"));

        final Set<Object> singletons = getSingletons();
        log.info("Got [" + singletons.size() + "] Singletons");
        singletons.forEach(current -> log.info("Singleton [" + current.getClass().getCanonicalName() + "]"));

        final SortedMap<String, Object> props = new TreeMap<>(getProperties());
        log.info("Got [" + props.size() + "] properties");
        props.entrySet().forEach(pr -> log.info("Property [" + pr.getKey() + "]: " + pr.getValue()));
    }

    /**
     * <p>Adds the supplied class to the resource types of this IntranetJaxRsApplication.</p>
     * <p><strong>Note!</strong> Adding classes this way disables the classpath scanning when running
     * RESTeasy (and this is somewhat dimly expressed
     * <a href="https://jsr311.java.net/nonav/releases/1.1/spec/spec.html"in the JAXRS 1.1 specification, section
     * 2.3.2</a>:</p>
     * <blockquote>... if both Application.getClasses and Application.getSingletons
     * return an empty list then all root resource classes and providers packaged in the web
     * application MUST be included in the published JAX-RS application. If either getClasses
     * or getSingletons return a non-empty list then only those classes or singletons returned
     * MUST be included in the published JAX-RS application.</blockquote>
     * <p>Hence - do not add any Classes using this method, unless you are prepare to add
     * <strong>all</strong> classes (Resource/Provider/...) for your application explicitly using this method.</p>
     *
     * @param toAdd A non-null class to add as a resource type to this IntranetJaxRsApplication.
     */
    public void addJaxRsClass(final Class<?> toAdd) {

        // Check sanity
        Validate.notNull(toAdd, "Cannot handle null 'toAdd' argument.");

        // All done.
        jaxRsClasses.add(toAdd);
    }

    /**
     * <p>Adds the supplied singleton instance to this IntranetJaxRsApplication.</p>
     * <p><strong>Note!</strong> Adding classes this way disables the classpath scanning when running
     * RESTeasy (and this is somewhat dimly expressed
     * <a href="https://jsr311.java.net/nonav/releases/1.1/spec/spec.html"in the JAXRS 1.1 specification, section
     * 2.3.2</a>:</p>
     * <blockquote>... if both Application.getClasses and Application.getSingletons
     * return an empty list then all root resource classes and providers packaged in the web
     * application MUST be included in the published JAX-RS application. If either getClasses
     * or getSingletons return a non-empty list then only those classes or singletons returned
     * MUST be included in the published JAX-RS application.</blockquote>
     * <p>Hence - do not add any Classes using this method, unless you are prepare to add
     * <strong>all</strong> classes (Resource/Provider/...) for your application explicitly using this method.</p>
     *
     * @param singleton A non-null singleton instance to add as a resource to this IntranetJaxRsApplication.
     */
    public void addJaxRsSingleton(final Object singleton) {

        // Check sanity
        Validate.notNull(singleton, "Cannot handle null 'singleton' argument.");

        // All done.
        jaxRsSingletons.add(singleton);
    }

    /**
     * @return The injected ServletContext
     */
    public ServletContext getContext() {
        return context;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Object> getSingletons() {
        return jaxRsSingletons;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<?>> getClasses() {
        return jaxRsClasses;
    }

    //
    // Private helpers
    //

    /**
     * Retrieves a sorted map containing the properties within the dependencies.properties file.
     *
     * @return a Map containing the properties within the {@code META-INF/maven/dependencies.properties} file.
     */
    public static SortedMap<String, String> getDependenciesProperties() {

        final String depPropFile = "META-INF/maven/dependencies.properties";
        final InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(depPropFile);
        final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        final SortedMap<String, String> toReturn = new TreeMap<>();

        try {
            String aLine = null;
            while ((aLine = reader.readLine()) != null) {

                final String currentLine = aLine.trim();
                if (currentLine.isEmpty() || currentLine.startsWith("#")) {
                    continue;
                }

                final int equalsIndex = currentLine.indexOf("=");
                if (equalsIndex != -1) {

                    final String key = currentLine.substring(0, equalsIndex).trim();
                    final String value = currentLine.substring(equalsIndex + 1).trim();

                    toReturn.put(key, value);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read [" + depPropFile + "]", e);
        }

        // All Done.
        return toReturn;
    }
}
