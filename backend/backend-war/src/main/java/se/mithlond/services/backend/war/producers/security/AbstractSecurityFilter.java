package se.mithlond.services.backend.war.producers.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.collections.predicate.Tuple;
import se.mithlond.services.organisation.api.MembershipService;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.PersonalSettings;
import se.mithlond.services.shared.authorization.api.AuthorizationPattern;
import se.mithlond.services.shared.authorization.api.RequireAuthorization;
import se.mithlond.services.shared.authorization.api.Segmenter;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ejb.EJB;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>Abstract ContainerRequestFilter implementation used to ensure that an active user is authenticated
 * and has required authorization to access a JAX-RS resource method. This abstract implementation should
 * not sport any JAX-RS implementation-specific classes at all. The ContainerRequestFilter must be a
 * post-matching filter, to actually be able to retrieve the {@link Method method} invoked.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public abstract class AbstractSecurityFilter implements ContainerRequestFilter {

    /**
     * Default authentication required to invoke any method, unless annotated with
     */
    public static final AuthorizationPattern DEFAULT_AUTH_PATTERN = new AuthorizationPattern(Segmenter.ANY, "member");

    // Our log
    private static final Logger log = LoggerFactory.getLogger(AbstractSecurityFilter.class);

    @EJB
    private MembershipService membershipService;

    /**
     * Trivial holder for an organisation name and an alias.
     */
    class OrganisationAndAlias {
        private String organisationName;
        private String alias;

        /**
         * Creates a new OrganisationAndAlias instance wrapping the supplied data.
         *
         * @param organisationName The name of the organisation.
         * @param alias            The alias of the caller.
         */
        public OrganisationAndAlias(final String organisationName, final String alias) {
            this.organisationName = organisationName;
            this.alias = alias;
        }

        /**
         * @return The organisation name.
         */
        public String getOrganisationName() {
            return organisationName;
        }

        /**
         * @return The alias.
         */
        public String getAlias() {
            return alias;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void filter(final ContainerRequestContext ctx) {

        // Find the authorization requirements of the currently invoked method.
        final Method targetMethod = getTargetMethod(ctx);
        SortedSet<AuthorizationPattern> requiredAuthPatterns = new TreeSet<>();

        // Adhere to the standard WRT the @PermitAll annotation.
        if (!targetMethod.isAnnotationPresent(PermitAll.class)) {

            // Access denied, as per the spec?
            // If so, abort further processing and return a 'forbidden' (HTTP 403) status.
            if (targetMethod.isAnnotationPresent(DenyAll.class)) {
                ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity("Access to requested resource explicitly denied.")
                        .build());
                return;
            }

            // We need to validate the active User's authorization.
            final RequireAuthorization authAnnotation = targetMethod.getAnnotation(RequireAuthorization.class);
            if(authAnnotation != null && authAnnotation.authorizationPatterns() != null) {
                final String suppliedAuthPatterns = authAnnotation.authorizationPatterns();
                try {
                    requiredAuthPatterns.addAll(AuthorizationPattern.parse(suppliedAuthPatterns));
                } catch (Exception e) {
                    log.error("Could not parse AuthPatterns from [" + suppliedAuthPatterns + "] in method ["
                            + targetMethod + "]", e);
                    requiredAuthPatterns.add(DEFAULT_AUTH_PATTERN);
                }
            } else {
                requiredAuthPatterns.add(DEFAULT_AUTH_PATTERN);
            }

            // Find the Membership of the active caller.
            final OrganisationAndAlias holder = getOrganisationNameAndAlias(ctx);
            final Membership activeMembership = membershipService.getMembership(
                    holder.getOrganisationName(), holder.getAlias());

            if (activeMembership == null) {

                // The user is not logged in. Abort.
                ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Insufficient credentials supplied.")
                        .build());
                return;
            }
        }

        // We really need a Membership to continue.

        // Get the headers. We will use them to acquire authentication data.
        final MultivaluedMap<String, String> headers = ctx.getHeaders();

        // Find the
        // Just to show how to user info from access token in REST endpoint

        /*
        final KeycloakSecurityContext securityContext = (KeycloakSecurityContext)
                httpRequest.getAttribute(KeycloakSecurityContext.class.getName());
        final AccessToken accessToken = securityContext.getToken();
        securityContext.getRealm()
        accessToken.getPreferredUsername();
        */

        // The organisation header must be present
        final String organisation = getRequestParameter(ctx, HEADER_ORGANISATION);
        if (null == organisation) {
            ctx.abortWith(Response.status(Response.Status.BAD_REQUEST)
                    .entity("No [" + HEADER_ORGANISATION + "] found.")
                    .build());
            return;
        }

        // Find the method invoked.
        final Method targetMethod = getTargetMethod(ctx);

        // Is authorization (and therefore authentication) required?
        if (!targetMethod.isAnnotationPresent(PermitAll.class)) {

            // Access denied?
            // If so, abort further processing and return a 'forbidden' (HTTP 403) status.
            if (targetMethod.isAnnotationPresent(DenyAll.class)) {
                ctx.abortWith(Response.status(Response.Status.FORBIDDEN)
                        .entity("User cannot access requested resource.")
                        .build());
                return;
            }

            // Printout the HTTP headers, if available.
            if (log.isDebugEnabled()) {

                final StringBuilder builder = new StringBuilder();
                builder.append("\n\n====== [Inbound HTTP Headers] ======\n");
                for (String currentKey : new TreeSet<>(headers.keySet())) {

                    final List<String> values = headers.get(currentKey);
                    if (values.size() <= 1) {
                        builder.append("  [" + currentKey + "]: " + values.get(0) + "\n");
                    } else {
                        for (int i = 0; i < values.size(); i++) {
                            builder.append("  [" + currentKey + " (" + i + "/" + (values.size() - 1)
                                    + ")]: " + values.get(i) + "\n");
                        }
                    }
                }
                builder.append("====== [End Inbound HTTP Headers] ======\n\n");
                log.debug(builder.toString());
            }

            //
            // Authorization is required.
            //

            final IdentityWrapper identityWrapper = getWrapper(organisation, headers);
            if (identityWrapper == null) {

                // The user is not logged in. Abort.
                ctx.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                        .entity("Insufficient credentials supplied.")
                        .build());
                return;
            }

            final Tuple<Membership, PersonalSettings> tuple = authorizationService.login(
                    identityWrapper.getOrganisation(),
                    identityWrapper.getUserId(),
                    identityWrapper.getCredential());
            if (tuple == null) {

                // The IdentityWrapper did not hold sufficient authentication data. Abort.
                ctx.abortWith(
                        Response.status(Response.Status.UNAUTHORIZED)
                                .entity("Incorrect credentials supplied.")
                                .build());
                return;
            }

            // If the user does not possess the required roles, simply abort.
            if (isMembershipAuthorized(getAllowedRoles(targetMethod), tuple.getKey())) {
                ctx.setSecurityContext(new NazgulMembershipSecurityContext(tuple.getKey(), tuple.getValue()));
            }
        }
    }

    /**
     * Retrieves the OrganisationAndAlias from the data within the supplied ContainerRequestContext.
     *
     * @param ctx The non-null ContainerRequestContext.
     * @return The populated OrganisationAndAlias instance.
     */
    protected abstract OrganisationAndAlias getOrganisationNameAndAlias(final ContainerRequestContext ctx);

    protected Membership getMembership(final String organisationName, final String alias) {
        return membershipService.getMembership(organisationName, alias);
    }

    protected SortedSet<AuthorizationPattern> getRequiredAuthorization() {

    }

    /**
     * Retrieves the target Method being invoked within the current JAX-RS resource.
     *
     * @param ctx The active ContainerRequestContext.
     * @return the target Method being invoked within the current JAX-RS resource.
     */
    protected abstract Method getTargetMethod(final ContainerRequestContext ctx);

    /**
     * Retrieves a SortedSet holding the roles allowed to access the supplied Method.
     * Override this method to provide a custom implementation. The default implementation
     * will ensure that the returned SortedSet always contains at least one role ("Inbyggare")
     * even if the {@code RolesAllowed} annotation does not sport any value at all.
     *
     * @param method The currently invoked JAX-RS resource method.
     * @return a SortedSet holding the roles allowed to access the supplied Method.
     */
    protected SortedSet<String> getAllowedRoles(final Method method) {

        final SortedSet<String> toReturn = new TreeSet<>();
        if (method.isAnnotationPresent(RolesAllowed.class)) {

            // Handle the case where no value has been supplied.
            final String[] roles = method.getAnnotation(RolesAllowed.class).value();
            if (roles != null && roles.length > 0) {
                toReturn.addAll(Arrays.asList(roles));
            }
        }

        if (toReturn.isEmpty()) {
            toReturn.add("Inbyggare");
        }

        // All done.
        return toReturn;
    }

    /**
     * Creates an IdentityWrapper from the supplied HTTP request headers.
     * The default implementation assumes HTTP Basic standard authentication.
     * Override to use another algorithm.
     *
     * @param organisation   The non-empty name of the organisation.
     * @param requestHeaders The active HTTP request headers.
     * @return an IdentityWrapper for the supplied headers, or {@code null} if no IdentityWrapper
     * could be created from the supplied headers.
     */
    protected IdentityWrapper getWrapper(final String organisation,
                                         final MultivaluedMap<String, String> requestHeaders) {

        // Check sanity
        final String authHeader = requestHeaders.getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || authHeader.isEmpty()) {
            return null;
        }

        // Delegate and return
        final String[] userIdAndCredentials = getUserIdAndCredentials(authHeader);
        return new IdentityWrapper(userIdAndCredentials[0], userIdAndCredentials[1], organisation);
    }

    /**
     * Acquires the userId and credentials from the supplied nonEmpty Http Authentication header.
     *
     * @param nonEmptyAuthHeader The non-empty authentication HTTP header, {@code HttpHeaders.AUTHORIZATION}.
     * @return A String[] with the first index containing the userID and the second index containing the credentials.
     */
    protected abstract String[] getUserIdAndCredentials(final String nonEmptyAuthHeader);

    /**
     * Checks if the supplied Membership was authorized, implying that it is a member of at least one Group
     * whose name is identical to one of the requiredRoles.
     *
     * @param requiredRoles The required roles for being authorized to invoke a resource method.
     * @param membership    The active membership.
     * @return {@code true} if the supplied membership was a member of at least one Group whose name is identical to
     * one of the requiredRoles.
     */
    protected boolean isMembershipAuthorized(final SortedSet<String> requiredRoles, final Membership membership) {

        // Find all Groups to which the membership belongs
        final SortedSet<Group> allGroups = new TreeSet<>(Comparators.GROUP_COMPARATOR);
        for (Group current : membership.getGroups()) {
            populate(allGroups, current);
        }

        // Extract a group name SortedSet
        final SortedSet<String> allGroupNames = new TreeSet<>();
        for (Group current : allGroups) {
            allGroupNames.add(current.getGroupName());
        }

        for (String current : requiredRoles) {
            if (allGroupNames.contains(current)) {

                // The membership was a member of at least one authorized group.
                return true;
            }
        }

        // Nopes.
        return false;
    }

    //
    // Private helpers
    //

    private String getRequestParameter(final ContainerRequestContext ctx, final String parameter) {

        // 1) Search the HTTP Headers for the parameter
        String toReturn = ctx.getHeaderString(parameter);

        // 2) Search the HTTP query parameters for the parameter
        if (toReturn == null) {
            toReturn = ctx.getUriInfo().getQueryParameters().getFirst(parameter);
        }

        // All done.
        return toReturn;
    }
}
