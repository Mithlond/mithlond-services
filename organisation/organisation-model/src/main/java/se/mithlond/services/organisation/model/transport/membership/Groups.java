/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
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

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.organisation.model.membership.guild.Guild;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Arrays;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * XML transporter for Groups and GroupVOs.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE, propOrder = {"groups", "groupVOs"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Groups extends AbstractSimpleTransporter {

    /**
     * A Set of full/detailed {@link Group} objects.
     */
    @XmlElementWrapper
    @XmlElements(value = {
            @XmlElement(name = "group", type = Group.class),
            @XmlElement(name = "guild", type = Guild.class)
    })
    private SortedSet<Group> groups;

    /**
     * A Set of shallow/VO {@link GroupVO} objects.
     */
    @XmlElementWrapper
    @XmlElement(name = "groupVO")
    private SortedSet<GroupVO> groupVOs;

    /**
     * JAXB-friendly constructor.
     */
    public Groups() {
        this.groups = new TreeSet<>();
        this.groupVOs = new TreeSet<>();
    }

    /**
     * Convenience constructor creating a Groups instance wrapping the supplied data.
     *
     * @param groups The groups to wrap.
     */
    public Groups(final Group... groups) {

        this();

        if (groups != null) {
            this.groups.addAll(Arrays.asList(groups));
        }
    }

    /**
     * Convenience constructor creating a Groups instance wrapping the supplied data.
     *
     * @param groupVOs The groupVOs to wrap.
     */
    public Groups(final GroupVO... groupVOs) {

        this();

        if (groupVOs != null) {
            this.groupVOs.addAll(Arrays.asList(groupVOs));
        }
    }

    /**
     * @return A Set of full/detailed {@link Group} objects.
     */
    public SortedSet<Group> getGroups() {
        return groups;
    }

    /**
     * @return A Set of shallow/VO {@link GroupVO} objects.
     */
    public SortedSet<GroupVO> getGroupVOs() {
        return groupVOs;
    }
}
