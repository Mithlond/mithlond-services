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
package se.mithlond.services.content.api.navigation.transport;

import org.apache.commons.lang3.Validate;
import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.content.model.navigation.AuthorizedNavItem;
import se.mithlond.services.content.model.navigation.integration.SeparatorMenuItem;
import se.mithlond.services.content.model.navigation.integration.StandardMenu;
import se.mithlond.services.content.model.navigation.integration.StandardMenuItem;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Transportable menu structure holder.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlRootElement(namespace = Patterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class MenuStructure {

    // Internal state
    @XmlAttribute(required = true)
    private String realm;

    @XmlElementWrapper(required = true, nillable = false)
    @XmlElements(value = {
            @XmlElement(name = "subMenu", type = StandardMenu.class),
            @XmlElement(name = "menuItem", type = StandardMenuItem.class),
            @XmlElement(name = "separator", type = SeparatorMenuItem.class)
    })
    private List<AuthorizedNavItem> rootMenu;

    /**
     * JAXB-friendly constructor.
     */
    public MenuStructure() {
        this.rootMenu = new ArrayList<>();
    }

    /**
     * Compound constructor, creating a MenuStructure for the supplied realm.
     *
     * @param realm The realm name for which this MenuStructure is created.
     */
    public MenuStructure(final String realm) {

        // Delegate, and check sanity
        this();
        Validate.notEmpty(realm, "realm");

        // Assign
        this.realm = realm;
    }

    /**
     * Adds the supplied AuthorizedNavItems to this MenuStructure.
     *
     * @param menuItems The menuItems to add to this MenuStructure.
     */
    public void add(final AuthorizedNavItem ... menuItems) {
        if(menuItems != null) {
            Collections.addAll(rootMenu, menuItems);
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
     * @return The list of AuthorizedNavItems within this MenuStructure.
     */
    public List<AuthorizedNavItem> getRootMenu() {
        return rootMenu;
    }
}
