/*
 * #%L
 * Nazgul Project: mithlond-services-shared-authorization-model
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
package se.mithlond.services.shared.authorization.model;

import javax.xml.bind.annotation.XmlTransient;

/**
 * Utility class holding a suite of reusable patterns, constants and algorithms.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public final class Patterns {

    /**
     * The XML namespace used by authorization model transport objects.
     */
    public static final String NAMESPACE = "http://xmlns.mithlond.se/xml/ns/authorization";

    /*
     * Hide constructor for utility classes.
     */
    private Patterns() {
        // Do nothing.
    }

    /**
     * Named parameter for the name of a realm.
     */
    public static final String PARAM_REALM_NAME = "realmName";

    /**
     * Named parameter for the name of a group.
     */
    public static final String PARAM_GROUP_NAME = "groupName";

    /**
     * Named parameter for the name of a qualifier.
     */
    public static final String PARAM_QUALIFIER_NAME = "qualifierName";

    /**
     * Named parameter for the JPA ID of a realm.
     */
    public static final String PARAM_REALM_ID = "realmJpaID";

    /**
     * Named parameter for the JPA ID of a group.
     */
    public static final String PARAM_GROUP_ID = "groupJpaID";
}
