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

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.shared.authorization.api.AuthorizationPattern;
import se.mithlond.services.shared.authorization.model.AuthorizationPath;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Abstract AuthorizedNavItem implementation, sporting standard integration
 * with persistence frameworks and XML binding.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(discriminatorType = DiscriminatorType.STRING, name = "nav_item_type")
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"role", "domId", "tabIndex", "transportCssClasses",
        "enabled", "transportAuthorizationPatterns"})
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractAuthorizedNavItem extends NazgulEntity implements AuthorizedNavItem {

    /**
     * Standard twitter bootstrap class for disabled Nav items.
     */
    public static final String DISABLED_CSS_CLASS = "disabled";

    /**
     * Retrieves the value of the "role" attribute for a Menu Item (typically a "li" element).
     * In a standard Twitter Bootstrap menu, this attribute typically has a value such as:
     * separator, search, button.
     */
    @XmlAttribute
    @Column(length = 32)
    private String role;

    /**
     * The DOM ID of this AbstractAuthorizedNavItem. Null values implies no DOM ID should be rendered.
     */
    @XmlAttribute
    @Column(length = 32)
    private String domId;

    /**
     * The tabindex of this AbstractAuthorizedNavItem.
     */
    @XmlAttribute
    private Integer tabIndex;

    @Column(nullable = true, length = 512)
    @XmlTransient
    private String cssClasses;

    /**
     * A sequence of CSS classes to be used/rendered on this AbstractAuthorizedNavItem.
     */
    @Transient
    @XmlElementWrapper(name = "cssClasses")
    @XmlElement(name = "cssClass")
    private List<String> transportCssClasses;

    @Column(length = 1024)
    @XmlTransient
    private String authorizationPatterns;

    /**
     * A sequence of AuthorizationPatterns to be used when checking permissions on this AbstractAuthorizedNavItem.
     */
    @Transient
    @XmlElementWrapper(name = "authPatterns")
    @XmlElement(name = "pattern")
    private SortedSet<String> transportAuthorizationPatterns;

    /**
     * A boolean flag indicating if this AbstractAuthorizedNavItem is enabled (default) or disabled.
     */
    @XmlAttribute
    private Boolean enabled;

    /**
     * JAXB/JPA-friendly constructor. Actually delegates to the compound constructor, using a default
     * value of true for the enabled indicator.
     */
    public AbstractAuthorizedNavItem() {
        this(null, null, null, null, null, true);
    }

    /**
     * Compound constructor creating an AbstractAuthorizedNavItem wrapping the supplied data.
     * Ay argument may be {@code null} to indicate that the corresponding attribute/value should not
     * be used on the rendered AbstractAuthorizedNavItem subclass.
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
     */
    @SuppressWarnings("PMD")
    public AbstractAuthorizedNavItem(final String role,
                                     final String domId,
                                     final Integer tabIndex,
                                     final String cssClasses,
                                     final String authorizationPatterns,
                                     final boolean enabled) {
        this.role = role;
        this.domId = domId;
        this.tabIndex = tabIndex;
        this.cssClasses = cssClasses;
        this.authorizationPatterns = authorizationPatterns;
        this.enabled = enabled;

        // Create derived state
        if (cssClasses != null) {
            transportCssClasses = getCssClasses();
        }
        if (authorizationPatterns != null) {
            transportAuthorizationPatterns = new TreeSet<>();

            final StringTokenizer tokenizer = new StringTokenizer(
                    authorizationPatterns, AuthorizationPath.PATTERN_SEPARATOR_STRING, false);
            while(tokenizer.hasMoreTokens()) {
                transportAuthorizationPatterns.add(tokenizer.nextToken());
            }
        }

        // Handle disabled state
        if (!enabled) {
            addCssClass(DISABLED_CSS_CLASS);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRoleAttribute() {
        return role;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIdAttribute() {
        return domId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getTabIndexAttribute() {
        return tabIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getCssClasses() {

        // No CSS classes to render?
        if (cssClasses == null) {
            return null;
        }

        final List<String> toReturn = new ArrayList<>();
        final StringTokenizer tok = new StringTokenizer(
                cssClasses,
                SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING,
                false);

        while (tok.hasMoreTokens()) {
            toReturn.add(tok.nextToken());
        }

        // All done.
        return toReturn;
    }

    /**
     * Adds the supplied CSS class to the list of CSS classes used by this AbstractAuthorizedNavItem.
     *
     * @param cssClass A non-null cssClass.
     */
    public void addCssClass(final String cssClass) {

        if (cssClass != null) {
            if (cssClasses == null) {
                this.cssClasses = cssClass;
            } else if (!cssClasses.contains(cssClass)) {
                this.cssClasses = this.cssClasses + SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING + cssClass;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Assigns the enabled flag.
     *
     * @param enabled The enabled value.
     */
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<AuthorizationPattern> getRequiredAuthorizationPatterns() {
        return authorizationPatterns == null ? null : AuthorizationPattern.parse(authorizationPatterns);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {
        // Do nothing
    }

    //
    // Private helpers
    //

    /**
     * Standard JAXB class-wide listener method, automagically invoked
     * after it has created an instance of this Class.
     *
     * @param marshaller The active Marshaller.
     */
    @SuppressWarnings("PMD")
    private void beforeMarshal(final Marshaller marshaller) {

        // Only pass the 'enabled' flag if this AbstractAuthorizedNavItem is disabled.
        if (enabled) {
            enabled = null;
        }

        // Populate the transport state
        if (cssClasses != null) {
            transportCssClasses = getCssClasses();
        } else {
            transportCssClasses = null;
        }

        if (authorizationPatterns != null) {
            transportAuthorizationPatterns = new TreeSet<>();

            final StringTokenizer tokenizer = new StringTokenizer(
                    authorizationPatterns, AuthorizationPath.PATTERN_SEPARATOR_STRING, false);
            while(tokenizer.hasMoreTokens()) {
                transportAuthorizationPatterns.add(tokenizer.nextToken());
            }
        }
    }

    /**
     * This method is called after all the properties (except IDREF) are unmarshalled for this object,
     * but before this object is set to the parent object.
     */
    @SuppressWarnings("PMD")
    private void afterUnmarshal(final Unmarshaller unmarshaller, final Object parent) {

        // Populate the JPA state
        if (transportAuthorizationPatterns != null) {
            StringBuilder builder = new StringBuilder();
            transportAuthorizationPatterns.forEach(current -> {
                builder.append(current).append(SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING);
            });
            authorizationPatterns = builder.toString().substring(
                    0,
                    builder.length() - SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING.length());
        }

        if (transportCssClasses != null) {
            StringBuilder builder = new StringBuilder();
            transportCssClasses.forEach(current -> {
                builder.append(current).append(SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING);
            });
            cssClasses = builder.toString().substring(
                    0,
                    builder.length() - SemanticAuthorizationPath.PATTERN_SEPARATOR_STRING.length());
        }

        // The 'enabled' flag is only present if it is false
        if (enabled == null) {
            enabled = true;
        }
    }
}
