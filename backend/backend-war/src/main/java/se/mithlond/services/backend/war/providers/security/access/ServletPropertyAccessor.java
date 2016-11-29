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

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;

/**
 * Specification for how to read configuration properties from the Web
 * parts of a JavaEE container.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@FunctionalInterface
public interface ServletPropertyAccessor {

    /**
     * The List containing the property sources, in the order they should be
     * investigated for property values.
     */
    List<String> ORDERED_PROPERTY_SOURCES = Arrays.asList(
            "Environment",
            "System Property",
            "Request Header",
            "Request Parameter",
            "Session Attribute",
            "ServletContext Attribute",
            "ServletContext InitParameter");

    /**
     * Finds a property with the supplied propertyName from one of the sources, as given within
     * the {@link #ORDERED_PROPERTY_SOURCES}.
     *
     * @param request      A non-null HttpServletRequest, as received from the Container as
     *                     the result of an inbound request.
     * @param propertyName The name of the property to read.
     * @return The value of the property as requested - or {@code null} if none was foundl
     */
    String get(final HttpServletRequest request, final String propertyName);
}
