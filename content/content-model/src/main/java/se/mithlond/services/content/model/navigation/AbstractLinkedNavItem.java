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
package se.mithlond.services.content.model.navigation;

import se.mithlond.services.content.model.Patterns;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Abstract implementation of a LinkedNavItem, forming the basis for Menus and MenuItems.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MappedSuperclass
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"iconIdentifier", "href"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractLinkedNavItem extends AbstractAuthorizedNavItem implements LinkedNavItem {

    /**
     * Standard font awesome class for fixed width icons.
     */
    public static final String ICON_FIXED_WITH_CSS = "icon-fixed-width";

    @XmlElement
    private String iconIdentifier;

    @XmlElement
    private String href;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public AbstractLinkedNavItem() {
        super();
    }

    /**
     * Compound constructor creating an AbstractLinkedNavItem wrapping the supplied data.
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
    public AbstractLinkedNavItem(final String role,
                                 final String domId,
                                 final Integer tabIndex,
                                 final String cssClasses,
                                 final String authorizationPatterns,
                                 final boolean enabled,
                                 final String iconIdentifier,
                                 final String href) {

        super(role,
                domId,
                tabIndex,
                cssClasses,
                authorizationPatterns,
                enabled);

        // Assign internal state
        this.iconIdentifier = iconIdentifier;
        this.href = href;

        // Handle non-null iconIdentifier
        if(iconIdentifier != null) {
            addCssClass(ICON_FIXED_WITH_CSS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getHrefAttribute() {
        return href;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconIdentifier() {
        return iconIdentifier;
    }
}
