package se.mithlond.services.organisation.api;

import se.mithlond.services.organisation.model.membership.Membership;

import javax.ejb.Local;

/**
 * Service specification for Memberships and corresponding things.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Local
public interface MembershipService {

    /**
     * Retrieves the Membership corresponding to the supplied organisation name and alias.
     *
     * @param organisationName The non-empty organisation name in which the retrieved Membership exists.
     * @param alias            The alias of the user for which a Membership should be retrieved.
     * @return The Membership corresponding to the supplied data, or {@code null} if none was found.
     */
    Membership getMembership(final String organisationName, final String alias);

    /**
     * Retrieves the Membership within the named Organisation for the User with the
     * supplied first and last names.
     *
     * @param organisationName The non-empty name of the Organisation in which the retrieved Membership should exist.
     * @param firstName        The non-empty first name of the User with the retrieved Membership.
     * @param lastName         The non-empty last name of the User with the retrieved Membership.
     * @return The Membership corresponding to the supplied data, or {@code null} if none was found.
     */
    Membership getMembership(final String organisationName, final String firstName, final String lastName);
}
