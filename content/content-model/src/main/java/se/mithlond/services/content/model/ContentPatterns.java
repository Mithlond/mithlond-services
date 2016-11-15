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

import javax.xml.bind.annotation.XmlTransient;

/**
 * Utility class holding a suite of reusable patterns, constants and algorithms.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public final class ContentPatterns {

    /**
     * The XML namespace used by content model entity objects.
     */
    public static final String NAMESPACE = "http://xmlns.mithlond.se/xml/ns/content";

    /**
     * The XML namespace used by content model transport objects.
     */
    public static final String TRANSPORT_NAMESPACE = "http://xmlns.mithlond.se/xml/ns/content/transport";

    /**
     * NamedQuery parameter name for last modified timestamp.
     */
    public static final String PARAM_LAST_MODIFIED = "last_modified";

    /**
     * NamedQuery parameter name for an interval start.
     */
    public static final String PARAM_INTERVAL_START = "interval_start";

    /**
     * NamedQuery parameter name for an interval end.
     */
    public static final String PARAM_INTERVAL_END = "interval_end";

    /**
     * NamedQuery parameter name for content path.
     */
    public static final String PARAM_CONTENT_PATH = "content_path";

    /**
     * NamedQuery parameter name for content path.
     */
    public static final String PARAM_CONTENT_PATHS = "content_paths";

    /*
     * Hide utility-class constructors.
     */
    private ContentPatterns() {
        // Do nothing.
    }
}
