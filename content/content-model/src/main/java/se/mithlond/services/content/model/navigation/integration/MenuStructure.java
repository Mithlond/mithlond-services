/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
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
package se.mithlond.services.content.model.navigation.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.core.algorithms.api.Validate;
import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.content.model.ContentPatterns;
import se.mithlond.services.organisation.model.localization.LocaleDefinition;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;

import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class relating a (Root) StandardMenu to an Organisation.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@NamedQueries({
        @NamedQuery(name = MenuStructure.NAMEDQ_GET_BY_ORGANISATION_NAME,
                query = "select ms from MenuStructure ms "
                        + "where ms.owningOrganisation.organisationName like :"
                        + OrganisationPatterns.PARAM_ORGANISATION_NAME)
})
@Entity
@XmlRootElement(namespace = ContentPatterns.NAMESPACE)
@XmlType(namespace = ContentPatterns.NAMESPACE, propOrder = {"localeDefinitions", "organisationName", "rootMenu"})
@XmlAccessorType(XmlAccessType.FIELD)
public class MenuStructure extends NazgulEntity {

    /**
     * NamedQuery for getting a MenuStructure by an Organisation's name.
     */
    public static final String NAMEDQ_GET_BY_ORGANISATION_NAME =
            "MenuStructure.getByOrganisationName";

    // Our Logger
    @XmlTransient
    private static final Logger log = LoggerFactory.getLogger(MenuStructure.class);

    // Internal state

    @XmlTransient
    @Transient
    private final Object[] lock = new Object[0];

    /**
     * The Localizations encountered within this MenuStructure.
     * There is no guarantee that all elements within this MenuStructure sports all collected Localizations.
     */
    @XmlElementWrapper
    @XmlElement(name = "localization")
    @Transient
    private List<LocaleDefinition> localeDefinitions;

    /**
     * The name of the Organisation for which this MenuStructure is defined.
     */
    @Transient
    @XmlElement(required = true)
    private String organisationName;

    @OneToOne
    @XmlTransient
    private Organisation owningOrganisation;

    /**
     * The single root menu of this MenuStructure.
     */
    @XmlElement(required = true)
    @OneToOne
    private StandardMenu rootMenu;

    /**
     * JAXB-friendly constructor.
     */
    public MenuStructure() {
        localeDefinitions = new ArrayList<>();
    }

    /**
     * Compound constructor, creating a MenuStructure for transport purposes only (and not for persistence).
     *
     * @param organisationName The non-empty organisationName of the Organisation owning this MenuStructure.
     * @param rootMenu         The non-null Root menu of this MenuStructure.
     */
    public MenuStructure(final String organisationName, final StandardMenu rootMenu) {

        // Delegate
        this();

        // Assign internal state
        this.organisationName = organisationName;
        this.rootMenu = rootMenu;
    }

    /**
     * Compound constructor, creating a MenuStructure for the supplied realm. This is the only complete constructor,
     * permitting both transport and storage.
     *
     * @param owningOrganisation The non-null Organisation owning this MenuStructure.
     * @param rootMenu           The non-null Root menu of this MenuStructure.
     */
    public MenuStructure(@NotNull final StandardMenu rootMenu,
                         @NotNull final Organisation owningOrganisation) {

        // Delegate
        this();

        // Check sanity
        Validate.notNull(owningOrganisation, "owningOrganisation");
        Validate.notNull(rootMenu, "rootMenu");

        // Assign internal state
        this.rootMenu = rootMenu;
        this.owningOrganisation = owningOrganisation;
        this.organisationName = owningOrganisation.getOrganisationName();
    }

    /**
     * @return The non-empty name of the organisation owning this MenuStructure.
     */
    public String getOrganisationName() {
        return organisationName;
    }

    /**
     * @return Retrieves the Organisation owning this MenuStructure, if invoked on the Server side. This instance is
     * not marshalled for transport, and will hence not be available on the client side of a communications channel.
     * @see #owningOrganisation
     * @see javax.xml.bind.annotation.XmlTransient
     */
    public Organisation getOwningOrganisation() {
        return owningOrganisation;
    }

    /**
     * Assigns the Organisation of this MenuStructure, and also assigns the {@link #organisationName} of this
     * MenuStructure to the name of the supplied owningOrganisation. This method should only be used on the Server
     * side, to create/update MenuStructures.
     *
     * @param owningOrganisation A non-null Organisation using this MenuStructure.
     * @see #owningOrganisation
     */
    public void setOwningOrganisation(final Organisation owningOrganisation) {

        // Check sanity
        Validate.notNull(owningOrganisation, "Cannot handle null 'owningOrganisation' argument.");

        // Assign internal state
        synchronized (lock) {
            this.owningOrganisation = owningOrganisation;
            this.organisationName = owningOrganisation.getOrganisationName();
        }
    }

    /**
     * @return The non-null Root menu for this MenuStructure.
     */
    public StandardMenu getRootMenu() {
        return rootMenu;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        // Check sanity
        if (organisationName == null) {
            if (owningOrganisation != null) {
                this.organisationName = owningOrganisation.getOrganisationName();
            } else {
                log.warn("Could not assign 'organisationName' property, as the 'owningOrganisation' is not set. "
                        + "Proceed with caution.");
            }
        }

        InternalStateValidationException.create()
                .notNull(rootMenu, "rootMenu")
                .endExpressionAndValidate();
    }

    //
    // Private helpers
    //

    /**
     * Standard JAXB class-wide listener method, automagically invoked
     * immediately before this object is Marshalled.
     *
     * @param marshaller The active Marshaller.
     */
    @SuppressWarnings("all")
    private void beforeMarshal(final Marshaller marshaller) {
        populateLocalizations(this.localeDefinitions, this.rootMenu);
    }

    private void populateLocalizations(final List<LocaleDefinition> toPopulate, final StandardMenu menu) {

        // Start with the locally known Localizations
        menu.getLocalizedTexts().getContainedLocalizations()
                .stream()
                .filter(current -> !toPopulate.contains(current))
                .forEach(toPopulate::add);

        // Now find all StandardMenu children.
        final List<StandardMenu> menus = menu.getChildren().stream()
                .filter(current -> current instanceof StandardMenu)
                .map(current -> (StandardMenu) current)
                .collect(Collectors.toList());

        // Descend into child level
        for (StandardMenu current : menus) {
            populateLocalizations(toPopulate, current);
        }
    }
}