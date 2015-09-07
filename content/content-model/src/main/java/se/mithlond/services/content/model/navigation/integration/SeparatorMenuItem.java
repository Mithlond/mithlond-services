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
import se.mithlond.services.content.model.navigation.MenuItem;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;

import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

/**
 * <p>Implementation of a menu item Separator, on the following form:</p>
 * <pre>
 *     &lt;li role="separator" class="divider"&gt;&lt;/li&gt;
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class SeparatorMenuItem extends NazgulEntity implements MenuItem {

    /**
     * Defines the standard list of CSS classes used by this SeparatorMenuItem.
     */
    @SuppressWarnings("all")
    public static final List<String> CSS_CLASSES = Arrays.asList("divider");

    /**
     * JPA/JAXB-friendly constructor.
     */
    public SeparatorMenuItem() {
        super();
    }

    /**
     * @return Always returns {@code null}.
     */
    @Override
    public String getText() {
        return null;
    }

    /**
     * @return Always returns {@code null}.
     */
    @Override
    public String getAnchorHRef() {
        return null;
    }

    /**
     * @return the role "separator".
     */
    @Override
    public String getRole() {
        return "separator";
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
     * @return The standard List of CSS classes for this SeparatorMenuItem.
     * @see #CSS_CLASSES
     */
    @Override
    public List<String> getCssClasses() {
        return CSS_CLASSES;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {
    }
}
