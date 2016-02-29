/*
 * #%L
 * Nazgul Project: mithlond-services-content-api
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
package se.mithlond.services.content.api.transport;

import org.apache.commons.lang3.Validate;
import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.content.model.navigation.AbstractAuthorizedNavItem;
import se.mithlond.services.content.model.navigation.integration.StandardMenu;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Transportable menu structure holder.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class MenuStructure implements Serializable {

    // Internal state
    @XmlAttribute(required = true)
    private String realm;

    /**
     * The single root menu of this MenuStructure.
     */
    @XmlElement(required = true)
    private StandardMenu rootMenu;

    /**
     * JAXB-friendly constructor.
     */
    public MenuStructure() {
        // Do nothing.
    }

    /**
     * Compound constructor, creating a MenuStructure for the supplied realm.
     *
     * @param realm    The realm name for which this MenuStructure is created.
     * @param rootMenu The non-null Root menu of this MenuStructure.
     */
    public MenuStructure(final String realm, final StandardMenu rootMenu) {

        // Delegate, and check sanity
        this();
        Validate.notEmpty(realm, "realm");
        Validate.notNull(rootMenu, "rootMenu");

        // Assign
        this.realm = realm;
        this.rootMenu = rootMenu;
    }

    /**
     * Convenience method to add all the supplied AbstractAuthorizedNavItem - in order - to this MenuStructure.
     *
     * @param menuItems The menuItems to add to this MenuStructure.
     */
    public void add(final AbstractAuthorizedNavItem... menuItems) {
        if (menuItems != null) {
            Arrays.asList(menuItems).stream().forEach(item -> rootMenu.addChild(item));
        }
    }

    /**
     * @return The realm of this menu.
     */
    public String getRealm() {
        return realm;
    }

    /**
     * Retrieves the root menu containing AuthorizedNavItems.
     *
     * @return The root StandardMenu within this MenuStructure.
     */
    public StandardMenu getRootMenu() {
        return rootMenu;
    }
}
