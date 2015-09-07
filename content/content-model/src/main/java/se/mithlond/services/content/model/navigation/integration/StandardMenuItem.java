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

import se.jguru.nazgul.core.persistence.model.NazgulEntity;
import se.jguru.nazgul.tools.validation.api.exception.InternalStateValidationException;
import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.content.model.navigation.MenuItem;
import se.mithlond.services.shared.spi.algorithms.authorization.SemanticAuthorizationPath;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.SortedSet;

/**
 * <p>Default Entity implementation of a MenuItem, complying with the MenuItem specification of a
 * <a href="http://getbootstrap.com">Twitter Bootstrap</a>-style menu item. Refer to the
 * MenuItem for a markup model specification.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {})
@XmlAccessorType(XmlAccessType.FIELD)
public class StandardMenuItem extends NazgulEntity implements MenuItem {

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(nillable = false, required = true)
    private String text;

    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(nillable = true, required = false)
    private String altText;

    @Basic(optional = false)
    @Column(nullable = false)
    @XmlElement(nillable = false, required = true)
    private String href;

    @Basic(optional = true)
    @Column(nullable = true)
    @XmlElement(nillable = true, required = false)
    private String iconIdentifier;

    private String anchorRole;

    private String role;

    private int tabIndex;

    private boolean enabled;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public StandardMenuItem() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAnchorHRef() {
        return href;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getText() {
        return text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAltText() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<SemanticAuthorizationPath> getPaths() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getIconIdentifier() {
        return iconIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTabIndex() {
        return tabIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAnchorRole() {
        return anchorRole;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRole() {
        return role;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {
    }
}
