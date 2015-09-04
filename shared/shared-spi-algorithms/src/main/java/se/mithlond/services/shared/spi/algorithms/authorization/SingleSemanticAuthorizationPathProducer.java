package se.mithlond.services.shared.spi.algorithms.authorization;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Convenience implementation for SemanticAuthorizationPathProducers which will ever
 * only produce a single SemanticAuthorizationPath.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface SingleSemanticAuthorizationPathProducer extends SemanticAuthorizationPathProducer {

    /**
     * Main factory method, creating the SemanticAuthorizationPath instance
     * from this SingleSemanticAuthorizationPathProducer.
     *
     * @return the SemanticAuthorizationPath instance of this SingleSemanticAuthorizationPathProducer.
     */
    SemanticAuthorizationPath createPath();

    /**
     * {@inheritDoc}
     */
    @Override
    default SortedSet<SemanticAuthorizationPath> getPaths() {
        final SortedSet<SemanticAuthorizationPath> toReturn = new TreeSet<>();
        toReturn.add(createPath());
        return toReturn;
    }
}
