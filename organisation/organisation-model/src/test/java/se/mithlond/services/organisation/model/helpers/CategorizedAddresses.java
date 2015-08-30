package se.mithlond.services.organisation.model.helpers;

import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.organisation.model.address.CategorizedAddress;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlType(propOrder = {"organisations", "categorizedAddresses"})
@XmlAccessorType(XmlAccessType.FIELD)
public class CategorizedAddresses {

    // Internal state
    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = false, required = true, name = "organisation")
    private List<Organisation> organisations;

    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = true, required = false, name = "categorizedAddress")
    private List<CategorizedAddress> categorizedAddresses;

    public CategorizedAddresses() {
        this((CategorizedAddress) null);
    }

    public CategorizedAddresses(final CategorizedAddress... cats) {

        organisations = new ArrayList<>();
        categorizedAddresses = new ArrayList<>();

        if (cats != null) {
            for (CategorizedAddress current : cats) {
                if (current != null) {
                    categorizedAddresses.add(current);
                }
            }
        }

        if(categorizedAddresses != null && categorizedAddresses.size() > 0) {
            // Harvest all unique organisations, since they must be
            // written before the CategorizedAddresses that refer them.
            final SortedMap<String, Organisation> tmp = new TreeMap<>();
            for(CategorizedAddress current : categorizedAddresses) {
                final Organisation currentOrg = current.getOwningOrganisation();
                tmp.put(currentOrg.getOrganisationName(), currentOrg);
            }

            for(Map.Entry<String, Organisation> current : tmp.entrySet()) {
                organisations.add(current.getValue());
            }
        }
    }

    public List<CategorizedAddress> getCategorizedAddresses() {
        return categorizedAddresses;
    }

    /**
     * JAXB callback method invoked after this instance is Unmarshalled.
     * This is the gracious JAXB instantiation sledge hammer...
     *
     * @param unmarshaller The unmarshaller used to perform the unmarshalling.
     */
    @SuppressWarnings("PMD")
    private void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {

        /*
        // Re-assign the XmlTransient collections.
        for (GuildMembership current : guildMemberships) {
            current.setMembership(this);
        }
        for (OrderLevelGrant current : orderLevelGrants) {
            current.setMembership(this);
        }

        if (log.isDebugEnabled()) {
            final String parentObjectType = parent == null ? "<null>" : parent.getClass().getName();
            log.debug("Got parent object of type: " + parentObjectType);
        }
        */
    }
}
