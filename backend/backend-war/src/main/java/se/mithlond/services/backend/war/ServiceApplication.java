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

import javax.servlet.ServletContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Context;
import java.util.HashSet;
import java.util.Set;

/**
 * The JAX-RS Application-derived class providing the s
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
}
