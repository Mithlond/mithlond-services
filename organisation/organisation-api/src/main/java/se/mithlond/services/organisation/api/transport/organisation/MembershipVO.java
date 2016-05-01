package se.mithlond.services.organisation.api.transport.organisation;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransportable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;

/**
 * The SimpleTransportable version of a Membership.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"alias", "organisation", "subAlias", "emailAlias", "loginPermitted"})
@XmlAccessorType(XmlAccessType.FIELD)
public class MembershipVO extends AbstractSimpleTransportable {

    // Internal state
    @XmlElement(required = true)
    private String alias;

    @XmlElement
    private String subAlias;

    @XmlElement
    private String emailAlias;

    @XmlAttribute(required = true)
    private boolean loginPermitted;

    @XmlIDREF
    @XmlElement
    private OrganisationVO organisation;

    /**
     * JAXB-friendly constructor.
     */
    public MembershipVO() {
    }

    /**
     * Compound constructor creating a {@link MembershipVO} wrapping the supplied data.
     *
     * @param jpaID          The JPA ID of the {@link se.mithlond.services.organisation.model.membership.Membership}
     *                       represented by this {@link MembershipVO}.
     * @param alias          The non-empty alias of the Membership.
     * @param subAlias       The optional sub-alias of the Membership.
     * @param emailAlias     The optional email-alias of the Membership.
     * @param loginPermitted If {@code true}, indicates that the Membership is permitted login (i.e. not locked out).
     * @param organisation   The non-null {@link OrganisationVO} in which this {@link MembershipVO} is part.
     */
    public MembershipVO(final Long jpaID,
            final String alias,
            final String subAlias,
            final String emailAlias,
            final boolean loginPermitted,
            final OrganisationVO organisation) {

        super(jpaID);

        // Assign internal state
        this.alias = Validate.notEmpty(alias, "alias");
        this.organisation = Validate.notNull(organisation, "organisation");
        this.subAlias = subAlias;
        this.emailAlias = emailAlias;
        this.loginPermitted = loginPermitted;
    }

    /**
     * @return The non-empty alias of the {@link MembershipVO}.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @return The optional Sub-Alias of the {@link MembershipVO}.
     */
    public String getSubAlias() {
        return subAlias;
    }

    /**
     * @return The optional email alias of this {@link MembershipVO}.
     */
    public String getEmailAlias() {
        return emailAlias;
    }

    /**
     * @return if {@code true}, indicates that the Membership is permitted login (i.e. not locked out).
     */
    public boolean isLoginPermitted() {
        return loginPermitted;
    }

    /**
     * @return The {@link OrganisationVO} of this {@link MembershipVO}.
     */
    public OrganisationVO getOrganisation() {
        return organisation;
    }
}
