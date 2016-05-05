package se.mithlond.services.organisation.model.transport.address;

import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * XML transporter for Categories, Addresses and CategorizedAddresses
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE,
        propOrder = {"categories", "organisations", "categorizedAddresses"})
@XmlAccessorType(XmlAccessType.FIELD)
public class CategoriesAndAddresses extends AbstractSimpleTransporter {

    /**
     * The Organisations owning the CategorizedAddresses
     */
    @XmlElementWrapper
    @XmlElement(name = "organisation")
    private List<Organisation> organisations;

    /**
     * A List of {@link Category} objects.
     */
    @XmlElementWrapper
    @XmlElement(name = "category")
    private SortedSet<Category> categories;

    /**
     * A List of {@link CategorizedAddress} objects.
     */
    @XmlElementWrapper
    @XmlElement(name = "categorizedAddress")
    private List<CategorizedAddress> categorizedAddresses;

    /**
     * JAXB-friendly constructor.
     */
    public CategoriesAndAddresses() {
        this.organisations = new ArrayList<>();
        this.categories = new TreeSet<>();
        this.categorizedAddresses = new ArrayList<>();
    }

    /**
     * Creates a {@link CategoriesAndAddresses} object stashed with the supplied categories.
     *
     * @param categories categores to transport.
     */
    public CategoriesAndAddresses(final Category... categories) {

        this();

        if (categories != null) {
            this.categories.addAll(Arrays.asList(categories));
        }
    }

    /**
     * Creates a {@link CategoriesAndAddresses} object stashed with the supplied {@link CategorizedAddress}es.
     *
     * @param addresses {@link CategorizedAddress} objects to transport.
     */
    public CategoriesAndAddresses(final CategorizedAddress... addresses) {

        this();

        if (addresses != null) {
            Arrays.asList(addresses).stream()
                    .filter(c -> c != null)
                    .forEach(this::addCategorizedAddress);
        }
    }

    /**
     * @return A List of {@link Category} objects.
     */
    public SortedSet<Category> getCategories() {
        return categories;
    }

    /**
     * @return An unmodifiable List of {@link CategorizedAddress} objects.
     * @see #addCategorizedAddress(CategorizedAddress)
     */
    public List<CategorizedAddress> getCategorizedAddresses() {
        return Collections.unmodifiableList(categorizedAddresses);
    }

    /**
     * Utility method to add the supplied {@link CategorizedAddress} to this {@link CategoriesAndAddresses} transport.
     *
     * @param toAdd A {@link CategorizedAddress} to add.
     */
    public void addCategorizedAddress(final CategorizedAddress toAdd) {

        if (toAdd != null) {

            final Category category = toAdd.getCategory();
            if (!categories.contains(category)) {
                categories.add(category);
            }

            if (!categorizedAddresses.contains(toAdd)) {

                final Organisation organisation = toAdd.getOwningOrganisation();
                if (!organisations.contains(organisation)) {
                    organisations.add(organisation);
                }

                categorizedAddresses.add(toAdd);
            }
        }
    }

    /**
     * @return The Organisations owning the respective {@link CategorizedAddress} instances transported.
     */
    public List<Organisation> getOrganisations() {
        return organisations;
    }
}
