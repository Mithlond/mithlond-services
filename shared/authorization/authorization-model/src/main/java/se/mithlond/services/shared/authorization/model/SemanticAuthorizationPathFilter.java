package se.mithlond.services.shared.authorization.model;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@FunctionalInterface
public interface SemanticAuthorizationPathFilter {

    boolean filter(String pattern, SemanticAuthorizationPath path);
}
