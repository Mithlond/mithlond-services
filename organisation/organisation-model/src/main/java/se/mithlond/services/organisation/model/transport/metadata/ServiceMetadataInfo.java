/*-
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
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
package se.mithlond.services.organisation.model.transport.metadata;

import se.jguru.nazgul.tools.validation.api.Validatable;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Metadata information of the backend service.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"serverVersion", "buildTime", "jvmProperties"})
@XmlAccessorType(XmlAccessType.FIELD)
public class ServiceMetadataInfo implements Serializable, Validatable {

    /**
     * The executing version of the backend service.
     */
    @XmlElement(required = true)
    private String serverVersion;

    /**
     * The build time data.
     */
    @XmlElement(required = true)
    private LocalDateTime buildTime;

    /**
     * Parts of the properties for the running JVM. For security reasons, not all standard JVM properties
     * may be supplied.
     */
    @XmlElementWrapper
    @XmlElement(name = "jvmProperty")
    private SortedMap<String, String> jvmProperties;

    /**
     * JAXB-friendly constructor.
     */
    public ServiceMetadataInfo() {
        this.jvmProperties = new TreeMap<>();
    }

    /**
     * Compound constructor creating a ServiceMetadataInfo object wrapping the supplied data.
     *
     * @param serverVersion The Maven GAV version of this Backend Service.
     * @param buildTime     The build time stamp of this Backend service.
     * @param jvmProperties (Parts of) all available JVM properties.
     */
    public ServiceMetadataInfo(@NotNull final String serverVersion,
                               @NotNull final LocalDateTime buildTime,
                               final SortedMap<String, String> jvmProperties) {

        // First, delegate
        this();

        // Assign internal state
        this.serverVersion = serverVersion;
        this.buildTime = buildTime;

        if (jvmProperties != null) {
            this.jvmProperties.putAll(jvmProperties);
        }
    }

    /**
     * The Maven GAV version of the executing backend service.
     *
     * @return the Maven GAV version of the executing backend service.
     */
    public String getServerVersion() {
        return serverVersion;
    }

    /**
     * Retrieves the timestamp of the build of this backend service.
     *
     * @return the timestamp of the build of this backend service.
     */
    public LocalDateTime getBuildTime() {
        return buildTime;
    }

    /**
     * Parts of the properties for the running JVM.
     * For security reasons, not all standard JVM properties may be supplied.
     *
     * @return Parts of the properties for the running JVM. 
     */
    public SortedMap<String, String> getJvmProperties() {
        return jvmProperties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateInternalState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNullOrEmpty(serverVersion, "serverVersion")
                .notNull(buildTime, "buildTime")
                .endExpressionAndValidate();
    }
}
