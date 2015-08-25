/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
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
package se.mithlond.services.content.model;

import java.util.List;

/**
 * Specification for how to indicate that content may be protected, and hence require authorization to permit access.
 * This really simplistic authorization model implies that callers must possess at least one of a
 * set of required roles which are in order to be granted access to the protected resource.
 * Each role is represented as a string path, which can be interpreted and understood by the underlying resource.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface ProtectedResource {

    /**
     * The separator char which may be used to separate semantic parts of Roles (i.e. role paths).
     */
    String SEPARATOR = "/";

    /**
     * @return The role paths required to access this ProtectedResource. {@code null} values indicate that
     *         the resource should be accessible for any access (i.e. without possessing any particular role).
     */
    default List<String> getRequiredRoles() {
        return null;
    }
}
