package se.mithlond.services.backend.war.providers.security.access;

import se.mithlond.services.backend.war.providers.security.OrganisationAndAlias;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

/**
 * Mock-alike MembershipAndMethodFinder which harvests information from the
 * Environment, System Properties, and lastly HTTP request arguments.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class EnvironmentPropertyMembershipAndMethodExtractor implements MembershipAndMethodFinder {

    /**
     * Organisation id key
     */
    public static final String ORGANISATION_KEY = "org";

    /**
     * {@inheritDoc}
     */
    @Override
    public OrganisationAndAlias getOrganisationNameAndAlias(
            final ContainerRequestContext ctx,
            final HttpServletRequest request) {

        // Find the id from the Environment, System Properties and lastly request arguments.
        String asdsad = System.getenv("asdsad");

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Method getTargetMethod(final ContainerRequestContext ctx) {
        return null;
    }

    private String getPropertyIn(final Function<Object, String> getter) {
        getter.apply()
    }
}
