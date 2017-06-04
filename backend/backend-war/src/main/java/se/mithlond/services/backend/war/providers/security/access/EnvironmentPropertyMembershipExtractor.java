/*
 * #%L
 * Nazgul Project: mithlond-services-backend-war
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
package se.mithlond.services.backend.war.providers.security.access;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.backend.war.providers.security.MembershipData;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import java.util.Arrays;

/**
 * Mock-alike MembershipAndMethodFinder which harvests information from the
 * Environment, System Properties, and lastly HTTP request arguments.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EnvironmentPropertyMembershipExtractor implements MembershipFinder {

    // Our log
    private static final Logger log = LoggerFactory.getLogger(EnvironmentPropertyMembershipExtractor.class);

    enum PropertyName {

        ORGANISATION("org"),
        FIRST_NAME("first_name"),
        LAST_NAME("last_name"),
        USER_IDENTIFIER("user_identifier");

        private static final String PREFIX = "nazgul_";

        private String propertySuffix;

        PropertyName(final String propertySuffix) {
            this.propertySuffix = propertySuffix;
        }

        public String getPropertyName() {
            return PREFIX + propertySuffix;
        }

        public String getPropertyValue(final ServletPropertyAccessor accessor, final HttpServletRequest request) {
            return accessor.get(request, getPropertyName());
        }
    }

    // Internal state
    private final ServletPropertyAccessor propertyAccessor;

    /**
     * Compound constructor, injecting all required state into this EnvironmentPropertyMembershipExtractor.
     *
     * @param propertyAccessor The property accessor, which extracts information from Servlet Contexts.
     */
    @Inject
    public EnvironmentPropertyMembershipExtractor(final ServletPropertyAccessor propertyAccessor) {
        this.propertyAccessor = propertyAccessor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MembershipData getMembershipData(
            final ContainerRequestContext ctx,
            final HttpServletRequest request) {

        if(log.isDebugEnabled()) {

            final String propertyNames = Arrays.stream(PropertyName.values())
                    .map(PropertyName::getPropertyName)
                    .sorted()
                    .reduce((l, r) -> l + ", " + r)
                    .orElse("<none>");

            log.debug("Retrieving access data from HttpServletRequest using ["
                    + propertyAccessor.getClass().getName() + "]. Property names retrieved: "
                    + propertyNames);
        }

        // All Done.
        return new MembershipData(
                PropertyName.ORGANISATION.getPropertyValue(propertyAccessor, request),
                PropertyName.FIRST_NAME.getPropertyValue(propertyAccessor, request),
                PropertyName.LAST_NAME.getPropertyValue(propertyAccessor, request),
                PropertyName.USER_IDENTIFIER.getPropertyValue(propertyAccessor, request));
    }
}
