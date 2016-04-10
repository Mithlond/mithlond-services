/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
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
package se.mithlond.services.shared.spi.algorithms.exception;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to unwrap exception messages.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public final class ExceptionMessageManager {

    /*
     * Hide constructor for utility classes.
     */
    private ExceptionMessageManager(){}

    /**
     * Extracts a printable string stacktrace from the supplied Throwable.
     *
     * @param ex A non-null Throwable from which to extract a stacktrace.
     * @return a printable string stacktrace from the supplied Exception.
     */
    public static String getReadableStacktrace(final Throwable ex) {

        // Create a sort-of human-readable string containing the exception stack trace.
        final StringBuilder builder = new StringBuilder();
        final List<String> segmentList = new ArrayList<>();

        for (Throwable current = ex; current != null; current = current.getCause()) {

            final String message = current instanceof ConstraintViolationException
                    ? extractConstraintViolationExceptionStacktrace((ConstraintViolationException) current)
                    : current.getMessage();

            segmentList.add(" [" + current.getClass().getSimpleName() + "]: " + message);
        }

        for (int i = 0; i < segmentList.size(); i++) {
            final String current = " (" + (i + 1) + "/" + segmentList.size() + "): " + segmentList.get(i) + "\n";
            builder.append(current);
        }

        // All done.
        return builder.toString();
    }

    /**
     * Extracts a printable string stacktrace from the supplied ConstraintViolationException.
     *
     * @param ex A non-null ConstraintViolationException from which to extract a stacktrace.
     * @return a printable string stacktrace from the supplied ConstraintViolationException.
     */
    public static String extractConstraintViolationExceptionStacktrace(final ConstraintViolationException ex) {

        final StringBuilder toReturn = new StringBuilder();
        final int numViolations = ex.getConstraintViolations().size();
        int index = 0;

        for (ConstraintViolation<?> current : ex.getConstraintViolations()) {

            final StringBuilder pathBuilder = new StringBuilder();
            current.getPropertyPath().forEach(node -> {

                // Is the node part of a Collection or Map?
                final String nodeIndex = node.getIndex() != null ? ", at index (" + node.getIndex() + ")" : "";
                final String keyValue = node.getKey() != null ? ", at key (" + node.getKey() + ")" : "";

                final String nodeName = "    " + node.getName() + nodeIndex + keyValue + "\n";
                pathBuilder.append(nodeName);
            });

            final String msg = "  Constraint [" + index++ + "/" + numViolations + "]: " + current.getMessage() + "\n"
                    + "   with invalid value " + current.getInvalidValue() + "\n"
                    + "   " + extractBeanInformation("RootBean", current.getRootBean()) + "\n"
                    + "   " + extractBeanInformation("LeafBean", current.getLeafBean()) + "\n"
                    + "   with node path: " + pathBuilder.toString() + "\n";
            toReturn.append(msg);
        }

        // All Done
        return toReturn.toString();
    }

    /**
     * Extracts type and JPA information from the supplied Object.
     *
     * @param obj         The Object/Bean from which to extract information.
     * @param designation a {@link java.lang.String} object.
     * @return The Object/Bean from which to extract information.
     */
    public static String extractBeanInformation(final String designation, final Object obj) {

        final String prefix = designation + ": ";

        // Check sanity
        if (obj == null) {
            return prefix + "<null>";
        }

        // First, add the prefix and class name.
        final StringBuilder toReturn = new StringBuilder();
        toReturn.append(prefix).append(obj.getClass().getName());

        // Second, add JPA ID and version, if we can extract them from the supplied Object
        if (obj instanceof NazgulEntity) {

            final NazgulEntity nazgulEntity = (NazgulEntity) obj;
            final String jpaDetails = " with JpaID [" + nazgulEntity.getId() + "] and Version [" + nazgulEntity
                    .getVersion() + "]";
            toReturn.append(jpaDetails);
        }

        // All done.
        return toReturn.toString();
    }
}
