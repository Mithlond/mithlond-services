package se.mithlond.services.organisation.model;

import se.mithlond.services.shared.spi.algorithms.authorization.AuthorizationPath;

import java.util.SortedSet;

/**
 * Specification for how to acquire an AuthorizationPath from the implementing class.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface AuthorizationPathProducer {

    /**
     * @return An AuthorizationPath created from this Producer.
     */
    SortedSet<AuthorizationPath> getAuthorizationPaths();
}
