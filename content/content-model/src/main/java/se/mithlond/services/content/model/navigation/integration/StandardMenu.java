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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.content.model.ContentPatterns;
import se.mithlond.services.content.model.navigation.AbstractAuthorizedNavItem;
import se.mithlond.services.content.model.navigation.AbstractLinkedNavItem;
import se.mithlond.services.organisation.model.localization.LocaleDefinition;
import se.mithlond.services.organisation.model.localization.LocalizedTexts;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Transient;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
@XmlType(namespace = ContentPatterns.NAMESPACE, propOrder = {"children"})
@XmlAccessorType(XmlAccessType.FIELD)
public class StandardMenu extends AbstractLinkedNavItem {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(StandardMenu.class);

    @XmlTransient
    @Transient
    private final Object[] lock = new Object[0];

    /**
     * The AbstractAuthorizedNavItem children of this StandardMenu.
     * Note that none of the children are JPA-Cascaded to persist/merge/delete operations, and hence
     * have to be managed explicitly within service implementations.
     */
    @XmlElementWrapper(required = true, nillable = false)
    @XmlElements(value = {
            @XmlElement(name = "menu", type = StandardMenu.class),
            @XmlElement(name = "menuItem", type = StandardMenuItem.class),
            @XmlElement(name = "separator", type = SeparatorMenuItem.class)
    })
    @OneToMany(mappedBy = "parent")
    private List<AbstractAuthorizedNavItem> children;

    /**
     * JPA/JAXB-friendly constructor.
     */
    public StandardMenu() {
        this.children = new ArrayList<>();
    }

    /**
     * Convenience constructor creating a StandardMenu, with a single localized text for display and a null
     * StandardMenu parent.
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
     * @param localeDefinition      The {@link LocaleDefinition} of the text for this StandardMenu.
     * @param text                  The non-empty text for this AbstractLinkedNavItem, supplied in the
     *                              languageCode given.
     */
    public StandardMenu(final String role,
            final String domId,
            final Integer tabIndex,
            final String cssClasses,
            final String authorizationPatterns,
            final boolean enabled,
            final String iconIdentifier,
            final String href,
            final LocaleDefinition localeDefinition,
            final String text) {

        // Delegate
        super(role,
                domId,
                tabIndex,
                cssClasses,
                authorizationPatterns,
                enabled,
                iconIdentifier,
                href,
                localeDefinition,
                text);
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
            final LocalizedTexts localizedTexts,
            final String href,
            final StandardMenu parent) {

        // First, delegate
        super(role,
                domId,
                tabIndex,
                cssClasses,
                authorizationPatterns,
                enabled,
                iconIdentifier,
                href,
                localizedTexts,
                parent);

        // Assign internal state
        this.children = new ArrayList<>();
    }

    /**
     * Retrieves an unmodifiable List of AbstractAuthorizedNavItem which should be rendered as
     * children of this StandardMenu.
     *
     * @return the unmodifiable List of LinkedNavItems which should be rendered as children
     * of this StandardMenu. Can be empty, but never {@code null}.
     */
    public List<AbstractAuthorizedNavItem> getChildren() {
        return Collections.unmodifiableList(children);
    }

    /**
     * Adds the supplied AbstractAuthorizedNavItem to the list of Children within this StandardMenu.
     *
     * @param child the non-null AbstractAuthorizedNavItem to be added to the list of Children within this StandardMenu.
     */
    public void addChild(final AbstractAuthorizedNavItem child) {

        // Check sanity
        Validate.notNull(child, "child");
        Validate.isTrue(child != this, "Cannot add a StandardMenu as a child to itself.");
        if (children.contains(child)) {

            // Don't add a child twice.
            return;
        }

        synchronized (lock) {

            // Add the child; update its parent and index properties.
            child.setParent(this);
            children.add(child);

            // Ensure that we have a sane state of all Children.
            updateIndexWithinChildren();
        }
    }

    /**
     * Removes the supplied AbstractAuthorizedNavItem as a child from this StandardMenu.
     *
     * @param child a non-null AbstractAuthorizedNavItem which should be removed as a child from this StandardMenu.
     */
    public void removeChild(final AbstractAuthorizedNavItem child) {

        // Check sanity
        Validate.notNull(child, "child");
        Validate.isTrue(child != this, "Cannot remove a StandardMenu as a child from itself.");
        if (!children.contains(child)) {

            if (log.isWarnEnabled()) {
                log.warn("Object [" + child + "] was not a child to this StandardMenu. Ignoring remove.");
            }

            // Never mind.
            return;
        }

        // Remove the child; update its parent and the index properties of all children.
        children.remove(child);

        child.setIndex(0);
        child.setParent(null);
    }

    /**
     * Copies all Children to the returned value, and clears the internal 'children' List.
     *
     * @return A non-null List containing all {@link AbstractAuthorizedNavItem} childen of this {@link StandardMenu}
     */
    public List<AbstractAuthorizedNavItem> removeAllChildren() {

        List<AbstractAuthorizedNavItem> toReturn;
        synchronized (lock) {

            // Copy all children
            toReturn = new ArrayList<>();
            toReturn.addAll(children);

            // Clear internal state
            this.children.clear();
        }

        // All Done.
        return toReturn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {

        // Fail fast
        if (this == o) {
            return true;
        }
        if (!(o instanceof StandardMenu)) {
            return false;
        }

        final StandardMenu that = (StandardMenu) o;
        return Objects.equals(children, that.children);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), children);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        // First, delegate
        super.validateEntityState();

        // Then, check own internal state.
        InternalStateValidationException.create()
                .notNull(children, "children")
                .endExpressionAndValidate();
    }

    /**
     * Retrieves a Builder to simplify creating a StandardMenu.
     *
     * @return a Builder used to create a StandardMenu.
     */
    public static StandardMenuBuilder getBuilder() {
        return new StandardMenuBuilder();
    }

    //
    // Private helpers
    //

    @PrePersist
    private void updateIndexWithinChildren() {

        // Check sanity
        if (children != null && !children.isEmpty()) {

            synchronized (lock) {

                final int numChildren = children.size();

                for (int i = 0; i < numChildren; i++) {
                    final AbstractAuthorizedNavItem currentChild = children.get(i);
                    currentChild.setIndex(i);
                }
            }
        }
    }

    /**
     * Simple builder class for StandardMenus.
     */
    @XmlTransient
    public static class StandardMenuBuilder extends AbstractLinkedNavItemBuilder<StandardMenuBuilder> {

        /**
         * Builds the StandardMenu from a constructor call using the "with"-ed parameters.
         *
         * @return a fully constructed StandardMenu.
         */
        public StandardMenu build() {
            return new StandardMenu(role,
                    domId,
                    tabIndex,
                    cssClasses,
                    authorizationPatterns,
                    enabled,
                    iconIdentifier,
                    localizedTexts,
                    href,
                    parent);
        }
    }

    /**
     * This method is called after all the properties (except IDREF) are unmarshalled for this object,
     * but before this object is set to the parent object.
     */
    @SuppressWarnings("PMD")
    private void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {

        // Re-connect all children
        for (AbstractAuthorizedNavItem current : children) {
            current.setParent(this);
        }
    }
}
