package se.mithlond.services.organisation.model.helpers;

import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.GroupMembership;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.user.User;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlType(propOrder = {"organisations", "categories", "categorizedAddresses", "groups", "users"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Users {

    // Internal state
    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = false, required = true, name = "organisation")
    private List<Organisation> organisations;

    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = false, required = true, name = "category")
    private List<Category> categories;

    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = true, required = false, name = "categorizedAddress")
    private List<CategorizedAddress> categorizedAddresses;

    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = true, required = false, name = "group")
    private List<Group> groups;

    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = true, required = false, name = "user")
    private List<User> users;

    public Users() {
        this((User) null);
    }

    public Users(final User... users) {

        organisations = new ArrayList<>();
        categorizedAddresses = new ArrayList<>();
        categories = new ArrayList<>();
        this.users = new ArrayList<>();

        // Harvest all unique organisations and categories, since they must be
        // written before the CategorizedAddresses that refer them.
        final SortedMap<String, Organisation> organisationMap = new TreeMap<>();
        final SortedMap<String, Category> categoryMap = new TreeMap<>();
        final SortedMap<String, Group> groupMap = new TreeMap<>();

        if (users != null) {
            for (User current : users) {
                if (current != null) {
                    this.users.add(current);

                    for(Membership currentMembership : current.getMemberships()) {

                        final Organisation currentOrg = currentMembership.getOrganisation();

                        organisationMap.put(currentOrg.getOrganisationName(), currentOrg);

                        for(GroupMembership currentGM : currentMembership.getGroupMemberships()) {
                            groupMap.put(currentGM.getGroup().getGroupName(), currentGM.getGroup());
                        }
                    }
                }
            }
        }

        organisations.addAll(organisationMap.values());

        if(this.users != null && this.users.size() > 0) {


            for(CategorizedAddress current : categorizedAddresses) {
                final Organisation currentOrg = current.getOwningOrganisation();
                final Category currentCategory = current.getCategory();
                organisationMap.put(currentOrg.getOrganisationName(), currentOrg);
                categoryMap.put(currentCategory.toString(), currentCategory);
            }

            for(Map.Entry<String, Organisation> current : organisationMap.entrySet()) {
                organisations.add(current.getValue());
            }

            for(Map.Entry<String, Category> current : categoryMap.entrySet()) {
                categories.add(current.getValue());
            }
        }
    }
}
