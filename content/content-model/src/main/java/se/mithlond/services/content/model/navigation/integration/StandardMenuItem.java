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

import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.content.model.navigation.AbstractLinkedNavItem;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Default Entity implementation of a MenuItem, complying with the MenuItem specification of a
 * <a href="http://getbootstrap.com">Twitter Bootstrap</a>-style menu item. Refer to the
 * MenuItem for a markup model specification.</p>
 * <pre>
 *     &lt;li&gt;
 *      &lt;a role="[linkRole]" tabindex="[tabIndex]" href="[href]"&gt;
 *          &lt;i class="icon-fixed-width [iconIdentifier]"&gt;&lt;/i&gt; [text]&lt;/a&gt;
 *     &lt;/li&gt;
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@DiscriminatorValue("menu_item")
@Access(AccessType.FIELD)
@XmlType(namespace = Patterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class StandardMenuItem extends AbstractLinkedNavItem {

    /**
     * JAXB/JPA-friendly constructor.
     */
    public StandardMenuItem() {
        super();
    }

    /**
     * Compound constructor creating a StandardMenuItem wrapping the supplied data.
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
    public StandardMenuItem(final String role,
                            final String domId,
                            final Integer tabIndex,
                            final String cssClasses,
                            final String authorizationPatterns,
                            final boolean enabled,
                            final String iconIdentifier,
                            final String href) {

        // Delegate
        super(role, domId, tabIndex, cssClasses, authorizationPatterns, enabled, iconIdentifier, href);
    }
}
