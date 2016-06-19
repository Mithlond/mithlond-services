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
package se.mithlond.services.organisation.model.transport.activity;

import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.activity.Activity;
import se.mithlond.services.shared.spi.jaxb.AbstractSimpleTransporter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Transport model for {@link ActivityVO}s, which are relevant for shallow listings, without the full
 * detail of the corresponding {@link se.mithlond.services.organisation.model.activity.Activity} entity.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE)
@XmlType(namespace = OrganisationPatterns.TRANSPORT_NAMESPACE, propOrder = {"activities", "activityVOs"})
@XmlAccessorType(XmlAccessType.FIELD)
public class Activities extends AbstractSimpleTransporter {

    /**
     * A List of ActivityVO instances providing a shallow representation of the Activities.
     * Suitable for listings and similar.
     */
    @XmlElementWrapper
    @XmlElement(name = "activityVO")
    private List<ActivityVO> activityVOs;

    /**
     * A List of full Activity instances providing a full/deep representation of the Activities.
     * Suitable for detailed views or editing.
     */
    @XmlElementWrapper
    @XmlElement(name = "activity")
    private List<Activity> activities;

    /**
     * JAXB-friendly constructor.
     */
    public Activities() {
        activityVOs = new ArrayList<>();
        activities = new ArrayList<>();
    }

    /**
     * Convenience constructor creating an Activities instance wrapping the supplied data.
     *
     * @param activities The activities to wrap.
     */
    public Activities(final Activity... activities) {
        this();

        if (activities != null) {
            this.activities.addAll(Arrays.asList(activities));
        }
    }

    /**
     * Convenience constructor creating an Activities instance wrapping the supplied data.
     *
     * @param activityVOs The activities to wrap.
     */
    public Activities(final ActivityVO... activityVOs) {
        this();

        if (activityVOs != null) {
            this.activityVOs.addAll(Arrays.asList(activityVOs));
        }
    }

    /**
     * Retrieves all known activityVOs.
     *
     * @return The List of {@link ActivityVO} objects transported by this {@link Activities}.
     */
    public List<ActivityVO> getActivityVOs() {
        return activityVOs;
    }

    /**
     * Retrieves all carried {@link Activity} objects.
     *
     * @return all carried {@link Activity} objects.
     */
    public List<Activity> getActivities() {
        return activities;
    }
}
