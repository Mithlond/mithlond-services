package se.mithlond.services.organisation.model.membership.authorization;

import java.util.List;

/**
 * Specification for how to indicate that content may be protected, and hence require
 * authorization to permit access. This really simplistic authorization model implies
 * that callers must possess at least one of a set of required GroupMemberships to be
 * granted access to a protected resource.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public @interface RequireGroupMembership {

    /**
     * The separator char which may be used to separate semantic parts of GroupMemberships.
     */
    String SEPARATOR = "/";

    /**
     * @return The role paths required to access this ProtectedResource. {@code null} values indicate that
     * the resource should be accessible for any caller (i.e. without the caller possessing any particular
     * GroupMembership).
     * @see se.mithlond.services.organisation.model.membership.GroupMembership
     */
    String getRequiredGroupMemberships();
}
