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

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import org.apache.commons.lang3.Validate;
import se.mithlond.services.backend.war.providers.exceptions.RuntimeExceptionHandler;
import se.mithlond.services.backend.war.providers.headers.HttpHeadersFilter;

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
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

        // Add standard providers
        addJaxRsSingleton(new HttpHeadersFilter());
        addJaxRsSingleton(new RuntimeExceptionHandler());

        // Add swagger introspectors
        addJaxRsClass(ApiListingResource.class);
        addJaxRsClass(SwaggerSerializers.class);

        // Configure Swagger
        final String packagePrefix = "se.mithlond.services.backend.war.resources";
        final String commaSeparatedPackageList = Arrays.asList("", "organisation", "content").stream()
                .map(c -> packagePrefix + (c.isEmpty() ? "" : "." + c))
                .reduce((l, r) -> l + "," + r)
                .orElse("");

        // Read the version from the dependencies.properties file.
        final String version = getDependenciesProperties().get("version");
        final BeanConfig beanConfig = new BeanConfig();
        beanConfig.setVersion(version);
        beanConfig.setSchemes(new String[]{"http"});
        beanConfig.setPrettyPrint(true);
        beanConfig.setHost("localhost:8080");
        beanConfig.setBasePath("/backend/resource");
        beanConfig.setResourcePackage(commaSeparatedPackageList);
        beanConfig.setScan(true);

        // Running in a RestEasy environment?
        // final String resteasyClassName = "org.jboss.resteasy.api.validation.Validation";
        // if(getClass().getClassLoader().loadClass(resteasyClassName))
        // addJaxRsSingleton(new SecurityFilter());
    }

    /**
     * Adds the supplied class to the resource types of this IntranetJaxRsApplication.
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
     * Adds the supplied singleton instance to this IntranetJaxRsApplication.
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

    private SortedMap<String, String> getDependenciesProperties() {

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
