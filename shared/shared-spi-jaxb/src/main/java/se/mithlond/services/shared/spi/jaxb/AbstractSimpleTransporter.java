/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-jaxb
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
package se.mithlond.services.shared.spi.jaxb;

import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.stream.Stream;

/**
 * Abstract skeleton for a transporter which has an annotated XmlRootElement and implements Serializable.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = SharedJaxbPatterns.NAMESPACE)
@XmlType(namespace = SharedJaxbPatterns.NAMESPACE, propOrder = {"errorCode", "errorDescription"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractSimpleTransporter implements Serializable {

    /**
     * Optional/nullable error code corresponding to executing
     */
    @XmlAttribute
    private Integer errorCode;

    /**
     * Optional/nullable description of a server-side error from this call.
     */
    @XmlAttribute
    private String errorDescription;

    /**
     * Assigns the errorDescription element.
     *
     * @param errorDescription An optional/nullable description of the error for this AbstractSimpleTransporter.
     */
    public void setError(final ErrorCode errorCode, final String errorDescription) {

        // Check sanity
        Validate.notNull(errorCode, "errorCode");

        // Assign internal state
        this.errorCode = errorCode.getCode();
        this.errorDescription = errorDescription;
    }

    /**
     * @return retrieves the optional/nullable error description for this AbstractSimpleTransporter.
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * Retrieves the ErrorCode for this AbstractSimpleTransporter.
     *
     * @return the ErrorCode for this AbstractSimpleTransporter. May be null.
     */
    public ErrorCode getErrorCode() {

        // Fail fast.
        if (errorCode == null) {
            return null;
        }

        // Filter and return
        return Stream.of(ErrorCode.values())
                .filter(ec -> ec.getCode() == errorCode)
                .findFirst().orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "]"
                + (getErrorDescription() != null ? " ERROR: " + getErrorDescription() : "");
    }
}
