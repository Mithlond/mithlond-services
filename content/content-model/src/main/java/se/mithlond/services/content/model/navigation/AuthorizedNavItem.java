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

import se.mithlond.services.content.model.navigation.integration.StandardMenu;
import se.mithlond.services.shared.authorization.api.GlobAuthorizationPattern;

import java.util.List;
import java.util.SortedSet;

/**
 * <h2>Authorized "NavItem"?</h2>
 * <p>An Authorized NavItem constitutes the minimalistic specification for items which may
 * be presented within a bootstrap-style menu structure. Bootstrap provides a separator which
 * is considered the minimal component to stash within a menu:</p>
 * <pre>
 *     &lt;li id="someId" enabled="false" tabindex="3" role="separator" class="divider"&gt;&lt;/li&gt;
 * </pre>
 * <p>Most attributes are optional, as indicated by the JavaDoc of their respective getters.
 * If an attribute getter returns a {@code null} value, the attribute should not be rendered
 * in the AuthorizedNavItem client-side rendering.</p>
 * <h2>NavItem authorization</h2>
 * <p>All NavItems can be given required permissions for normal access. The navigation service
 * implementation will need to define what should be returned - if anything - if a user does not
 * possess the required authorization to view the data in a NavItem. The standard authorization is
 * expressed for each NavItem as a set of AuthorizationPatterns.
 * At least one of the SemanticAuthorizationPaths available to a User must match at least one of the
 * AuthorizationPatterns found for each NavItem in order to view the full data within this AuthorizedNavItem.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface AuthorizedNavItem {

    /**
     * <p>Retrieves the value of the {@code role} attribute for a Menu Item (typically a {@code li} element).
     * In a standard Twitter Bootstrap menu, this attribute has the following values:</p>
     * <dl>
     *     <dt>separator</dt>
     *     <dd>Used to define menu separators (empty, non-selectable menu acting as a
     *     separator between menu items.)</dd>
     *     <dt>search</dt>
     *     <dd>Typically used as an attribute for form elements, placed within menu structures
     *     and used for searching.</dd>
     *     <dt>button</dt>
     *     <dd>Attribute for anchor elements within menus, when the anchors should act like buttons.</dd>
     * </dl>
     *
     * @return The value of the {@code role} attribute.
     */
    String getRoleAttribute();

    /**
     * @return If non-null, this method indicates that an AuthorizedNavItem should use the provided DOM ID.
     */
    String getIdAttribute();

    /**
     * @return If non-null, this method retrieves the {@code tabindex} attribute of this AuthorizedNavItem.
     */
    Integer getTabIndexAttribute();

    /**
     * Retrieves a List of CSS classes which should be applied to this AuthorizedNavItem.
     *
     * @return A List of CSS classes which should be applied to this AuthorizedNavItem. Null results imply
     * that no CSS classes should be applied - and hence the {@code class} attribute should not be rendered
     * onto this AuthorizedNavItem.
     */
    List<String> getCssClasses();

    /**
     * Getter indicating if this AuthorizedNavItem should be rendered in an enabled (default) or disabled state.
     * The client-side rendering engine should only need to modify the standard rendering if
     * this getter returns the value {@code false}.
     *
     * @return {@code true} to indicate that this AuthorizedNavItem should be enabled.
     */
    boolean isEnabled();

    /**
     * <p>Retrieves an optional Set of AuthorizationPath instances. If the user possesses at least one of these
     * AuthorizationPaths, this PresentableLink should be shown to the user with all its detail.
     * If the user does <strong>not</strong> possess any of the AuthorizationPaths returned by this method,
     * only non-sensitive data from this PresentableLink should be made available to the user.</p>
     * <p>In this context, <strong>non-sensitive data</strong> might be the text and icon of the PresentableLink,
     * but not any HREFs.</p>
     *
     * @return A SortedSet containing AuthorizationPaths required to view the data of this PresentableLink.
     * A {@code null} return value from this method indicates that this PresentableLink does not have any protection,
     * and is hence viewable by all Users.
     */
    SortedSet<GlobAuthorizationPattern> getRequiredAuthorizationPatterns();

    /**
     * Retrieves the parent StandardMenu element of this AuthorizedNavItem.
     * Parent-Child relations create a navigable tree of AuthorizedNavItems.
     *
     * @return the parent AuthorizedNavItem element of this AuthorizedNavItem. {@code null} only for the root menu.
     */
    StandardMenu getParent();
}
