package se.mithlond.services.shared.spi.algorithms.authorization;

import se.mithlond.services.shared.spi.algorithms.Validate;

import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Simple AuthorizationPath implementation with a pair of Strings for internal state.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SimpleAuthorizationPath implements AuthorizationPath, Comparable<SimpleAuthorizationPath> {

    // Internal state
    private String realm;
    private String group;

    /**
     * Compound constructor, creating a SimpleAuthorizationPath wrapping the supplied data.
     *
     * @param realm A non-null realm.
     * @param group A non-null group.
     */
    public SimpleAuthorizationPath(final String realm, final String group) {

        // Check sanity
        Validate.notEmpty(realm, "realm");
        Validate.notEmpty(group, "group");

        // Assign internal state
        this.realm = realm;
        this.group = group;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRealm() {
        return realm;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getGroup() {
        return group;
    }

    /**
     * @return A SortedSet of AuthorizationPath instances parsed from the supplied String.
     * @see AuthorizationPath#INSTANCE_SEPARATOR
     * @see AuthorizationPath#PATH_SEPARATOR
     */
    public static SortedSet<AuthorizationPath> parse(final String toParse) throws IllegalArgumentException {

        final SortedSet<AuthorizationPath> toReturn = new TreeSet<>();
        if (toParse != null) {

            final StringTokenizer tok = new StringTokenizer(toParse, INSTANCE_SEPARATOR, false);
            while (tok.hasMoreTokens()) {

                final String current = tok.nextToken();
                final StringTokenizer pathTokenizer = new StringTokenizer(current, PATH_SEPARATOR, false);

                // Check sanity
                if (pathTokenizer.countTokens() < 2) {
                    throw new IllegalArgumentException("Expected a path token on the form [realm]/[group]."
                            + " Got: [" + current + "]");
                }

                toReturn.add(new SimpleAuthorizationPath(pathTokenizer.nextToken(), pathTokenizer.nextToken()));
            }
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(final SimpleAuthorizationPath that) {

        // Check sanity
        if (that == null) {
            return -1;
        }
        if (this == that) {
            return 0;
        }

        // Delegate
        int toReturn = this.realm.compareTo(that.realm);
        if (toReturn == 0) {
            toReturn = this.group.compareTo(that.group);
        }

        // All done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return realm.hashCode() + group.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object obj) {

        // Check sanity
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AuthorizationPath)) {
            return false;
        }

        // Delegate.
        final AuthorizationPath that = (AuthorizationPath) obj;
        return realm.equals(that.getRealm()) && group.equals(that.getGroup());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return AuthorizationPath.PATH_SEPARATOR + realm + AuthorizationPath.PATH_SEPARATOR + group;
    }
}
