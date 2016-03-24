/*
 * #%L
 * Nazgul Project: mithlond-services-organisation-model
 * %%
 * Copyright (C) 2010 - 2013 jGuru Europe AB
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
package se.mithlond.services.organisation.model.membership;

import org.apache.commons.lang3.Validate;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.organisation.model.OrganisationPatterns;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Map;
import java.util.TreeMap;

/**
 * Personal settings for Memberships.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = PersonalSettings.NAMEDQ_GET_BY_MEMBERSHIP_ID,
                query = "select a from PersonalSettings a where a.membership = :" + OrganisationPatterns.PARAM_MEMBERSHIP_ID),
        @NamedQuery(name = PersonalSettings.NAMEDQ_GET_BY_ORGNAME_AND_ALIAS,
                query = "select a from PersonalSettings a"
                        + " where a.membership.organisation.organisationName = :" + OrganisationPatterns.PARAM_ORGANISATION_NAME
                        + " and a.membership.alias = :" + OrganisationPatterns.PARAM_ALIAS)
})
@Entity
@XmlType(namespace = OrganisationPatterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class PersonalSettings extends NazgulEntity {

    /**
     * NamedQuery for getting PersonalSettings by Membership ID.
     */
    public static final String NAMEDQ_GET_BY_MEMBERSHIP_ID = "PersonalSettings.getByMembershipId";

    /**
     * NamedQuery for getting PersonalSettings by Organisation name and Alias.
     */
    public static final String NAMEDQ_GET_BY_ORGNAME_AND_ALIAS = "PersonalSettings.getByOrganisationNameAndAlias";

    // Internal state
    @ElementCollection
    @MapKeyJoinColumn(name = "settings_key")
    @Column(name = "setting_value")
    @JoinTable(name = "personalsettings_setting", joinColumns = @JoinColumn(name = "settings_id"))
    private Map<String, String> settings;

    @XmlElement(nillable = false, required = true)
    @OneToOne(optional = false)
    private Membership membership;

    /**
     * JPA & JAXB-friendly constructor;
     */
    public PersonalSettings() {
    }

    /**
     * Convenience constructor, creating a new PersonalSettings instance wrapping an Empty settings map.
     *
     * @param membership The membership to which the settings should be applied.
     */
    public PersonalSettings(final Membership membership) {
        this(new TreeMap<String, String>(), membership);
    }

    /**
     * Compound constructor, defining a PersonalSettings instance from the supplied data.
     *
     * @param membership The membership to which the settings should be applied.
     * @param settings   The settings to be assigned to this PersonalSettings instance.
     */
    public PersonalSettings(final Map<String, String> settings, final Membership membership) {

        // Check sanity
        Validate.notNull(settings, "Cannot handle null settings argument.");
        Validate.notNull(membership, "Cannot handle null membership argument.");

        // Assign internal state
        this.settings = settings;
        this.membership = membership;
    }

    /**
     * @return The settings data of this PersonalSettings.
     */
    public Map<String, String> getSettings() {
        return settings;
    }

    /**
     * @return the Membership of this PersonalSettings instance.
     */
    public Membership getMembership() {
        return membership;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        final StringBuilder builder = new StringBuilder("PersonalSettings for " + membership.toString() + "\n");
        for (Map.Entry<String, String> current : settings.entrySet()) {
            builder.append(" [" + current.getKey() + "]: " + current.getValue() + "\n");
        }

        // All done.
        return builder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        // Ensure that the ElementCollection is fully loaded.
        // Workaround for the https://hibernate.atlassian.net/browse/HHH-8839 bug.
        if (settings != null) {
            for (Map.Entry<String, String> current : settings.entrySet()) {
                current.getKey().length();
                current.getValue().length();
            }
        }

        InternalStateValidationException.create()
                .notNull(membership, "member")
                .notNull(settings, "settings")
                .endExpressionAndValidate();
    }
}
