/*
 * #%L
 * Nazgul Project: mithlond-services-content-model
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
package se.mithlond.services.content.model.navigation.integration;

import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.content.model.navigation.AbstractLinkedNavItem;
import se.mithlond.services.content.model.navigation.AuthorizedNavItem;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Simple JPA implementation of a Bootstrap-type Menu (button) which provides a dropdown containing MenuItems.</p>
 * <pre>
 *     &lt;li class="dropdown"&gt;
 *      &lt;a href="[link]"
 *          class="dropdown-toggle"
 *          data-toggle="dropdown"
 *          role="button"
 *          aria-haspopup="true"
 *          aria-expanded="false"&gt;[Text] &lt;span class="caret"&gt;&lt;/span&gt;&lt;/a&gt;
 *      &lt;ul class="dropdown-menu"&gt;
 *              [child list of PresentableLinks]
 *      &lt;/ul&gt;
 *     &lt;/li&gt;
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@DiscriminatorValue("menu")
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"children"})
@XmlAccessorType(XmlAccessType.FIELD)
public class StandardMenu extends AbstractLinkedNavItem {

    // Internal state
    @XmlElementWrapper(required = true, nillable = false)
    @XmlElements(value = {
            @XmlElement(name = "subMenu", type = StandardMenu.class),
            @XmlElement(name = "menuItem", type = StandardMenuItem.class),
            @XmlElement(name = "separator", type = SeparatorMenuItem.class)
    })
    private List<AuthorizedNavItem> children;

    /**
     * JPA/JAXB-friendly constructor.
     */
    public StandardMenu() {
        this.children = new ArrayList<>();
    }

    /**
     * Compound constructor creating a StandardMenu wrapping the supplied data.
     *
     * @param role                  The value of the {@code role} attribute. Typically something like
     *                              "separator", "search" or "button".
     * @param domId                 The DOM ID of this AbstractAuthorizedNavItem.
     * @param tabIndex              The tabindex to be rendered on this AbstractAuthorizedNavItem.
     * @param cssClasses            A concatenated string of CSS classes to be used within the "classes" attribute
     *                              on the markup element rendered by this AbstractAuthorizedNavItem.
     * @param authorizationPatterns A concatenated string of AuthorizationPatterns to be used on this
     *                              AbstractAuthorizedNavItem.
     * @param enabled               {@code false} to indicate that this AbstractAuthorizedNavItem should be disabled.
     * @param iconIdentifier        An Icon identifier string. If {@code null} is returned, the client-side
     *                              rendering engine is instructed not to render an icon for this LinkedNavItem.
     * @param href                  The hypertext link of this AbstractLinkedNavItem.
     */
    public StandardMenu(final String role,
                        final String domId,
                        final Integer tabIndex,
                        final String cssClasses,
                        final String authorizationPatterns,
                        final boolean enabled,
                        final String iconIdentifier,
                        final String href) {
        super(role, domId, tabIndex, cssClasses, authorizationPatterns, enabled, iconIdentifier, href);

        // Assign internal state
        this.children = new ArrayList<>();
    }

    /**
     * Retrieves the (ordered) list of LinkedNavItems which should be rendered as children of this StandardMenu.
     *
     * @return the (ordered) list of LinkedNavItems which should be rendered as children of this StandardMenu.
     * Can be empty, but never {@code null}.
     */
    public List<AuthorizedNavItem> getChildren() {
        return children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {
    }
}
