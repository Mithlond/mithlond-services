package se.mithlond.services.backend.war.providers.security.access;

import se.mithlond.services.backend.war.providers.security.OrganisationAndAlias;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import java.lang.reflect.Method;

/**
 * Specification for finding an active Membership and invoked JaxRS method.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface MembershipAndMethodFinder {

    /**
     * Retrieves the OrganisationAndAlias from the data within the supplied ContainerRequestContext.
     *
     * @param ctx     The non-null ContainerRequestContext.
     * @param request The active and non-null HttpServletRequest.
     * @return The populated OrganisationAndAlias instance.
     */
    OrganisationAndAlias getOrganisationNameAndAlias(
            final ContainerRequestContext ctx,
            final HttpServletRequest request);

    /**
     * Retrieves the target Method being invoked within the current JAX-RS resource.
     *
     * @param ctx The active ContainerRequestContext.
     * @return the target Method being invoked within the current JAX-RS resource.
     */
    Method getTargetMethod(final ContainerRequestContext ctx);
}
