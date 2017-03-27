/*
 * #%L
 * Nazgul Project: mithlond-services-integration-war
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
package se.mithlond.services.integration.war;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

import javax.annotation.PostConstruct;
import javax.naming.InitialContext;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Application class for the integration WAR. This JavaEE deployment adapts the internal model
 * to external ones, such as Calendar services.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@ApplicationPath("/app")
public class NazgulIntegrationApplication extends Application {

    // Singleton instances
    private CamelContext camelContext;

    /**
     * Default constructor.
     */
    public NazgulIntegrationApplication() {
    }

    /**
     * Lifecycle callback method used to create the CamelContext.
     */
    @PostConstruct
    public void initializeCamelContext() {

        try {

            // Create the CamelContext.
            this.camelContext = new DefaultCamelContext(new InitialContext());

        } catch (Exception e) {
            throw new IllegalStateException("Could not create DefaultCamelContext from the JNDI registry", e);
        }
    }

    /**
     * Retrieves the CamelContext created within this JaxRS Application.
     *
     * @return the CamelContext created within this JaxRS Application.
     */
    public CamelContext getCamelContext() {
        return camelContext;
    }
}
