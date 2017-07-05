package se.mithlond.services.backend.war.resources.user;

import se.mithlond.services.backend.war.resources.AbstractResource;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.transport.membership.Memberships;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.util.Set;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Path("/activeuser")
public class ActiveUserResource extends AbstractResource {

    /**
     * Retrieves a {@link Memberships} wrapper containing the full-detail membership information of
     * the currently logged in user.
     *
     * @return A {@link Memberships} wrapper containing the Membership, User, Organisation
     * and Groups of the active Membership.
     * @see Memberships
     */
    @Path("/info")
    @GET
    public Memberships getActiveMembershipInfo() {

        // First, find the given Membership
        final Set<Membership> membershipSet = new TreeSet<>();
        membershipSet.add(getActiveMembership());

        // All Done.
        return new Memberships(membershipSet);
    }

}
