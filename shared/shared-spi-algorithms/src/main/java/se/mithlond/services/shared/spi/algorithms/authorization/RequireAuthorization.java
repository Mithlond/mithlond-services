package se.mithlond.services.shared.spi.algorithms.authorization;

/**
 * Specification for how to indicate that content may be protected, and hence require
 * authorization to permit access. This really simplistic authorization model implies
 * that callers must possess at least one of a set of required GroupMemberships to be
 * granted access to a protected resource.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public @interface RequireAuthorization {

    /**
     * Value indicating that no AuthorizationPath is required
     */
    String NONE = "##none";

    /**
     * @return The AuthorizationPaths required to have the required authorization.
     * {@code null} values indicate that no particular AuthorizationPath is required.
     *
     * @see AuthorizationPath#parse(String)
     */
    String authorizationPaths() default NONE;
}
