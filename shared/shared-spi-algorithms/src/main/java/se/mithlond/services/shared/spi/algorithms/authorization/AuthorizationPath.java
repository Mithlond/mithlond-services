package se.mithlond.services.shared.spi.algorithms.authorization;

import java.util.regex.Pattern;

/**
 * Implementation-neutral specification of paths containing
 * Authorization information, concatenated into a path-like String.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface AuthorizationPath {

    /**
     * The separator char which may be used to separate different instances (i.e.
     * different GroupPaths.
     */
    String INSTANCE_SEPARATOR = ",";

    /**
     * The character used to separate semantic parts of GroupPaths.
     */
    String PATH_SEPARATOR = "/";

    /**
     * A constant to indicate that the value in question is unimportant (i.e. that we don't care about it).
     */
    String DONT_CARE = "dont_care";

    /**
     * @return The Realm of this AuthorizationPath. Should never be {@code null}; use {@code #DONT_CARE} in that case.
     */
    String getRealm();

    /**
     * @return The Group of this AuthorizationPath. Should never be {@code null}; use {@code #DONT_CARE} in that case.
     */
    String getGroup();

    /**
     * Retrieves a Pattern which could be used to match this AuthorizationPath in a regexp search.
     *
     * @return a Pattern which could be used to match this AuthorizationPath in a regexp search.
     */
    default Pattern getPattern() {

        final String realmPattern = getRealm() != null && !getRealm().equalsIgnoreCase(DONT_CARE)
                ? getRealm()
                : ".*";
        final String groupPattern = getGroup() != null && !getGroup().equalsIgnoreCase(DONT_CARE)
                ? getGroup()
                : ".*";

        // All done.
        return Pattern.compile(INSTANCE_SEPARATOR + realmPattern + INSTANCE_SEPARATOR + groupPattern,
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
    }
}
