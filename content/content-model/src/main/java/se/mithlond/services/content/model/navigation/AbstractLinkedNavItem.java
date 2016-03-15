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

import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.content.model.localization.Localization;
import se.mithlond.services.content.model.localization.LocalizedTexts;
import se.mithlond.services.content.model.navigation.integration.StandardMenu;
import se.mithlond.services.shared.spi.algorithms.Validate;

import javax.persistence.CascadeType;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

/**
 * Abstract implementation of a LinkedNavItem, forming the basis for Menus and MenuItems.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MappedSuperclass
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"iconIdentifier", "href", "localizedTexts"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractLinkedNavItem extends AbstractAuthorizedNavItem implements LinkedNavItem {

    /**
     * Standard font awesome class for fixed width icons.
     */
    public static final String ICON_FIXED_WITH_CSS = "icon-fixed-width";

    /**
     * The icon identifier attribute of this AbstractLinkedNavItem.
     */
    @XmlElement
    private String iconIdentifier;

    /**
     * The HREF attribute of this AbstractLinkedNavItem. Should contain an URL (or equivalent).
     */
    @XmlElement
    private String href;

    /**
     * A Localized texts structure containing all texts for this AbstractLinkedNavItem.
     */
    @XmlElement
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private LocalizedTexts localizedTexts;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public AbstractLinkedNavItem() {
        super();
    }

    /**
     * Convenience constructor for creating an AbstractLinkedNavItem without a
     * StandardParent and otherwise wrapping the supplied data.
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
     * @param languageCode          The language code (as defined in {@link java.util.Locale}) of the text for
     *                              this AbstractLinkedNavItem.
     * @param text                  The non-empty text for this AbstractLinkedNavItem, supplied in the
     *                              languageCode given.
     */
    public AbstractLinkedNavItem(final String role,
            final String domId,
            final Integer tabIndex,
            final String cssClasses,
            final String authorizationPatterns,
            final boolean enabled,
            final String iconIdentifier,
            final String href,
            final String languageCode,
            final String text) {

        this(role,
                domId,
                tabIndex,
                cssClasses,
                authorizationPatterns,
                enabled,
                iconIdentifier,
                href,
                LocalizedTexts.build(languageCode, text),
                null);
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
     * @param localizedTexts        The LocalizedTexts used to provide texts for this AbstractLinkedNavItem in
     *                              various Locales.
     * @param parent                An optional (i.e. nullable) StandardMenu of this AbstractLinkedNavItem. Should be
     *                              null only for the root menu or until set by a call to
     *                              {@link AbstractLinkedNavItem#setParent(StandardMenu)}
     */
    public AbstractLinkedNavItem(final String role,
            final String domId,
            final Integer tabIndex,
            final String cssClasses,
            final String authorizationPatterns,
            final boolean enabled,
            final String iconIdentifier,
            final String href,
            final LocalizedTexts localizedTexts,
            final StandardMenu parent) {

        super(role,
                domId,
                tabIndex,
                cssClasses,
                authorizationPatterns,
                enabled,
                parent);

        // Assign internal state
        this.iconIdentifier = iconIdentifier;
        this.href = href;

        // Handle non-null iconIdentifier
        if (iconIdentifier != null) {
            addCssClass(ICON_FIXED_WITH_CSS);
        }

        // Assign the LocalizedTexts
        this.localizedTexts = localizedTexts;
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

    /**
     * Retrieves the LocalizedTexts for this AbstractLinkedNavItem.
     *
     * @return the non-null LocalizedTexts for this AbstractLinkedNavItem.
     */
    public LocalizedTexts getLocalizedTexts() {
        return localizedTexts;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText(final Localization localization) {
        return localizedTexts.getText(localization);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {

        InternalStateValidationException.create()
                .notNull(localizedTexts, "localizedTexts")
                .endExpressionAndValidate();
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
        if (!(o instanceof AbstractLinkedNavItem)) {
            return false;
        }

        // Delegate to internal state.
        final AbstractLinkedNavItem that = (AbstractLinkedNavItem) o;
        return Objects.equals(iconIdentifier, that.iconIdentifier)
                && Objects.equals(href, that.href)
                && Objects.equals(localizedTexts, that.localizedTexts);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(iconIdentifier, href, localizedTexts);
    }

    /**
     * Abstract builder implementation to be overridden/used by concrete sub-class builders.
     *
     * @param <T> the concrete subtype of AbstractLinkedNavItemBuilder.
     */
    public abstract static class AbstractLinkedNavItemBuilder<T extends AbstractLinkedNavItemBuilder> {

        // Internal state
        protected String role;
        protected String domId;
        protected Integer tabIndex;
        protected String cssClasses;
        protected String authorizationPatterns;
        protected boolean enabled = true;
        protected String iconIdentifier;
        protected LocalizedTexts localizedTexts;
        protected String href;
        protected StandardMenu parent;

        /**
         * @param role the non-empty role to assign.
         * @return this builder, for chaining.
         */
        public T withRole(final String role) {
            this.role = Validate.notEmpty(role, "role");
            return (T) this;
        }

        /**
         * @param domId the non-empty domID to assign.
         * @return this builder, for chaining.
         */
        public T withDomId(final String domId) {
            this.domId = Validate.notEmpty(domId, "domId");
            return (T) this;
        }

        /**
         * @param tabIndex the non-negative tab index to assign.
         * @return this builder, for chaining.
         */
        public T withTabIndex(final Integer tabIndex) {
            final Integer tmp = Validate.notNull(tabIndex, "tabindex");
            Validate.isTrue(tmp >= 0, "Cannot handle negative 'tabindex' argument. (Got: " + tabIndex + ")");
            this.tabIndex = tmp;

            return (T) this;
        }

        /**
         * @param cssClasses The whitespace-separated cssClasses to assign.
         * @return this builder, for chaining.
         */
        public T withCssClasses(final String cssClasses) {
            this.cssClasses = Validate.notEmpty(cssClasses, "cssClasses");

            return (T) this;
        }

        /**
         * @param authorizationPatterns The non-empty authorizationPatterns to assign.
         * @return this builder, for chaining.
         */
        public T withAuthorizationPatterns(final String authorizationPatterns) {
            this.authorizationPatterns = Validate.notEmpty(authorizationPatterns, "authorizationPatterns");
            return (T) this;
        }

        /**
         * @param enabled {@code false} to indicate that the created StandardMenu should be disabled, and
         *                {@code true} otherwise.
         * @return this builder, for chaining.
         */
        public T withEnabledStatus(final boolean enabled) {
            this.enabled = enabled;
            return (T) this;
        }

        /**
         * @param iconIdentifier The non-empty iconIdentifier of this StandardMenu.
         * @return this builder, for chaining.
         */
        public T withIconIdentifier(final String iconIdentifier) {
            this.iconIdentifier = Validate.notEmpty(iconIdentifier, "iconIdentifier");
            return (T) this;
        }

        /**
         * @param languageCode The language code for which the supplied text should be used.
         * @param text         The display text within the supplied language code for this StandardMenu.
         * @return this builder, for chaining.
         */
        public T withLocalizedText(final String languageCode, final String text) {

            // Check sanity
            final String nonNullLangCode = Validate.notEmpty(languageCode, "languageCode");
            final String nonNullText = Validate.notEmpty(text, "text");

            // Create or add the LocalizedTexts
            if (localizedTexts == null) {
                localizedTexts = LocalizedTexts.build(nonNullLangCode, nonNullText);
            } else {
                localizedTexts.setText(new Localization(nonNullLangCode), nonNullText);
            }

            return (T) this;
        }

        /**
         * @param href the non-empty href of this StandardMenu.
         * @return this builder, for chaining.
         */
        public T withHref(final String href) {
            this.href = Validate.notEmpty(href, "href");
            return (T) this;
        }

        /**
         * @param parent the non-null StandardMenu parent of this StandardMenu.
         * @return this builder, for chaining.
         */
        public T withParent(final StandardMenu parent) {
            this.parent = Validate.notNull(parent, "parent");
            return (T) this;
        }
    }
}
