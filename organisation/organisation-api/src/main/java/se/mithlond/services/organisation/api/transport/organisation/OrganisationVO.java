package se.mithlond.services.organisation.api.transport.organisation;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransportable;

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlType;

/**
 * The SimpleTransportable version of an Organisation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = OrganisationPatterns.NAMESPACE, propOrder = {"organisationName"})
@XmlAccessorType(XmlAccessType.FIELD)
public class OrganisationVO extends AbstractSimpleTransportable {

    /**
     * The organisation name.
     */
    @NotNull
    @XmlID
    @XmlAttribute(required = true)
    private String organisationName;

    /**
     * A human-readable description of this OrganisationVO
     */
    @NotNull
    @XmlElement(required = true)
    private String description;

    /**
     * JAXB-friendly constructor.
     */
    public OrganisationVO() {
    }

    /**
     * Compound constructor creating an {@link OrganisationVO} wrapping the supplied data.
     *
     * @param jpaID            The JPA ID of the {@link se.mithlond.services.organisation.model.Organisation}
     *                         represented by this {@link OrganisationVO}.
     * @param organisationName The organisation name.
     * @param description      A human-readable description.
     */
    public OrganisationVO(final Long jpaID, final String organisationName, final String description) {
        super(jpaID);

        // Check sanity
        this.organisationName = Validate.notEmpty(organisationName, "organisationName");
        this.description = Validate.notEmpty(description, "description");
    }

    /**
     * @return The organisation name.
     */
    public String getOrganisationName() {
        return organisationName;
    }

    /**
     * @return The description of the Organisation being transported.
     */
    public String getDescription() {
        return description;
    }
}
