package se.mithlond.services.backend.war.producers.security;

/**
 * Holder for an organisation name and an alias.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class OrganisationAndAlias {

    // Internal state
    private String organisationName;
    private String alias;

    /**
     * Creates a new OrganisationAndAlias instance wrapping the supplied data.
     *
     * @param organisationName The name of the organisation.
     * @param alias            The alias of the caller.
     */
    public OrganisationAndAlias(final String organisationName,
                                final String alias) {
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
