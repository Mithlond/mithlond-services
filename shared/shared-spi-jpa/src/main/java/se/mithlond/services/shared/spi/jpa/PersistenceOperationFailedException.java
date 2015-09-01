/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-jpa
 * %%
 * Copyright (C) 2010 - 2015 jGuru Europe AB
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
package se.mithlond.services.shared.spi.jpa;

/**
 * Runtime exception subclass thrown whenever an exception was
 * raised as a result of a failed EntityManager / Persistence operation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class PersistenceOperationFailedException extends RuntimeException {

    /**
     * <p>Constructs a new PersistenceOperationFailedException with the specified (mandatory)
     * detail message and cause.</p>
     * <p><strong>Note!</strong> The detail message associated with <code>cause</code> is <strong>not</strong>
     * automatically incorporated in this runtime exception's detail message.</p>
     *
     * @param message the detail message (which is saved for later retrieval by the {@link #getMessage()} method).
     * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method).
     *                (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
     * @since 1.4
     */
    public PersistenceOperationFailedException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
