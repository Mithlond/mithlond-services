package se.mithlond.services.backend.war.producers.security;

import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.util.BasicAuthHelper;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;

/**
 * RestEasy-flavoured AbstractSecurityFilter implementation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Provider
public class RestEasySecurityFilter extends AbstractSecurityFilter {

    // Internal state
    private static final String METHOD_INVOKER_KEY = "org.jboss.resteasy.core.ResourceMethodInvoker";

    /**
     * {@inheritDoc}
     */
    @Override
    protected Method getTargetMethod(final ContainerRequestContext ctx) {

        // Dig out the invoked resource method.
        final ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) ctx.getProperty(METHOD_INVOKER_KEY);
        return methodInvoker.getMethod();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getUserIdAndCredentials(final String nonEmptyAuthHeader) {
        return BasicAuthHelper.parseHeader(nonEmptyAuthHeader);
    }
}
