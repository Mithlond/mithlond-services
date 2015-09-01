package se.mithlond.services.organisation.model.helpers;

import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.Patterns;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.guild.Guild;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlType(propOrder = {"organisations", "groupsAndGuilds"})
@XmlAccessorType(XmlAccessType.FIELD)
public class GroupsAndGuilds {

    // Internal state
    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = false, required = true, name = "organisation")
    private List<Organisation> organisations;

    @XmlElementWrapper(nillable = true, required = false)
    @XmlElements(value = {
            @XmlElement(name = "group", type = Group.class),
            @XmlElement(name = "guild", type = Guild.class)
    })
    private List<Group> groupsAndGuilds;

    public GroupsAndGuilds() {
        organisations = new ArrayList<>();
        groupsAndGuilds = new ArrayList<>();
    }

    public GroupsAndGuilds(final Group... groups) {

        this();
        final SortedMap<String, Organisation> organisationMap = new TreeMap<>();
        final SortedMap<String, Group> groupMap = new TreeMap<>();

        if (groups != null) {
            Arrays.asList(groups).forEach(current -> {

                final Organisation currentOrg = current.getOrganisation();
                organisationMap.put(currentOrg.getOrganisationName(), currentOrg);

                groupMap.put(current.getGroupName(), current);
            });
        }

        this.organisations.addAll(organisationMap.values());
        this.groupsAndGuilds.addAll(groupMap.values());
    }

    public List<Organisation> getOrganisations() {
        return organisations;
    }

    public List<Group> getGroupsAndGuilds() {
        return groupsAndGuilds;
    }
}
