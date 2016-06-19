/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 Mithlond
 * %%
 * Licensed under the jGuru Europe AB license (the "License"), based
 * on Apache License, Version 2.0; you may not use this file except
 * in compliance with the License.
 * 
 * You may obtain a copy of the License at
 * 
 *       http://www.jguru.se/licenses/jguruCorporateSourceLicense-2.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package se.mithlond.services.organisation.model.transport.membership;

import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.organisation.model.user.User;
import se.mithlond.services.shared.spi.algorithms.Validate;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Transport class for memberships, users and organisations. This is the transport object for detailed entities,
 * and not the shallow version.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.NAMESPACE,
        propOrder = {"organisations", "groups", "users", "memberships"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Memberships extends AbstractSimpleTransporter {

    /**
     * All {@link User}s correlating to transported {@link Membership}s.
     */
    @XmlElementWrapper
    @XmlElement(name = "user")
    private List<User> users;

    /**
     * All {@link User}s correlating to transported {@link Membership}s.
     */
    @XmlElementWrapper
    @XmlElement(name = "organisation")
    private List<Organisation> organisations;

    /**
     * All {@link Group}s correlating to transported {@link Membership}s.
     */
    @XmlElementWrapper(required = false)
    @XmlElements(value = {
            @XmlElement(name = "group", type = Group.class),
            @XmlElement(name = "guild", type = Guild.class)
    })
    private List<Group> groups;

    /**
     * All {@link Membership}s transported in this wrapper.
     */
    @XmlElementWrapper
    @XmlElement(name = "membership")
    private List<Membership> memberships;

    /**
     * JAXB-friendly constructor.
     */
    public Memberships() {
        users = new ArrayList<>();
        organisations = new ArrayList<>();
        groups = new ArrayList<>();
        memberships = new ArrayList<>();
    }

    /**
     * Compound constructor, adding all the supplied Memberships, including referenced Users
     * and Organisations, to this Memberships transport.
     *
     * @param memberships The Memberships to transport.
     */
    public Memberships(final Set<Membership> memberships) {

        this();

        // Assign internal state
        memberships.forEach(this::addMembership);
    }

    /**
     * Adds the supplied Organisation(s) to this Memberships transport.
     *
     * @param toAdd The non-null organisation objects to add to this Memberships transport structure.
     */
    public void addOrganisations(final Organisation... toAdd) {

        // Check sanity
        Validate.notNull(toAdd, "Cannot handle null 'toAdd' argument.");

        // Add the current Organisation, unless already added.
        Arrays.asList(toAdd).forEach(c -> {
            if (c != null && !this.organisations.contains(c)) {
                this.organisations.add(c);
            }
        });
    }

    /**
     * Adds the supplied Group(s) to this Memberships transport.
     * Also adds any Organisations to which the toAdd Groups belong.
     *
     * @param toAdd The non-null Group objects to add to this Memberships transport structure.
     */
    public void addGroups(final Group... toAdd) {

        // Check sanity
        Validate.notNull(toAdd, "Cannot handle null 'toAdd' argument.");

        // Add the current Group, unless already added.
        Arrays.asList(toAdd).stream().forEach(c -> {

            if (c != null) {

                // Add the Organisation, if not already added
                addOrganisations(c.getOrganisation());

                if (!this.groups.contains(c)) {
                    this.groups.add(c);
                }
            }
        });
    }

    /**
     * Adds the supplied Membership, including its internal state.
     *
     * @param toAdd The Membership to add.
     */
    public void addMembership(final Membership toAdd) {

        // Check sanity
        final Membership nonNull = Objects.requireNonNull(toAdd, "Cannot handle null 'toAdd' argument.");

        // Add the Organisation, unless already added.
        addOrganisations(nonNull.getOrganisation());

        // Add the User, unless already added.
        final User currentUser = nonNull.getUser();
        if (!users.contains(currentUser)) {
            users.add(currentUser);
        }

        // Add all Groups and Guilds.
        nonNull.getGroupMemberships().forEach(c -> {
            addGroups(c.getGroup());
        });

        // Add the membership itself, unless already added.
        if (!this.memberships.contains(nonNull)) {
            this.memberships.add(nonNull);
        }
    }

    /**
     * @return The users within this Memberships transport.
     */
    public List<User> getUsers() {
        return Collections.unmodifiableList(users);
    }

    /**
     * @return The organisations within this Memberships transport.
     */
    public List<Organisation> getOrganisations() {
        return Collections.unmodifiableList(organisations);
    }

    /**
     * @return The groups within this Memberships transport.
     */
    public List<Group> getGroups() {
        return Collections.unmodifiableList(groups);
    }

    /**
     * @return The memberships within this Memberships transport.
     */
    public List<Membership> getMemberships() {
        return Collections.unmodifiableList(memberships);
    }
}
