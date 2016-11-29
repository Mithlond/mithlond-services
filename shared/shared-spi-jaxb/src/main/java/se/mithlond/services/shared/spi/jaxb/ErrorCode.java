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

import javax.xml.bind.annotation.XmlTransient;

/**
 * Enumeration of the possible error codes available for {@link AbstractSimpleTransporter}s.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public enum ErrorCode {

    UNAUTHORIZED(401),

    INTERNAL_SERVER_ERROR(500),

    VALUE_NOT_FOUND(1023);

    // Internal state
    private int code;

    /**
     * Compound constructor creating an ErrorCode instance wrapping the supplied data.
     *
     * @param code The integer value of this ErrorCode.
     */
    ErrorCode(final int code) {
        this.code = code;
    }

    /**
     * Retrieves the integer code value of this ErrorCode.
     *
     * @return the integer code value of this ErrorCode.
     */
    public int getCode() {
        return code;
    }
}
