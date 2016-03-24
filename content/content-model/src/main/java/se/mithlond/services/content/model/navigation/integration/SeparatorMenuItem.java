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
import se.mithlond.services.content.model.ContentPatterns;
import se.mithlond.services.content.model.navigation.AbstractAuthorizedNavItem;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

/**
 * <p>Implementation of a menu item Separator, on the following form:</p>
 * <pre>
 *     &lt;li role="separator" class="divider"&gt;&lt;/li&gt;
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Entity
@DiscriminatorValue("separator")
@XmlType(namespace = ContentPatterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class SeparatorMenuItem extends AbstractAuthorizedNavItem {

    /**
     * Defines the standard CSS class used by this SeparatorMenuItem.
     */
    @SuppressWarnings("all")
    public static final String CSS_DIVIDER = "divider";

    /**
     * JPA/JAXB-friendly constructor.
     */
    public SeparatorMenuItem() {
        super();
    }

    /**
     * @return The constant value {@code separator}
     */
    @Override
    public String getRoleAttribute() {
        return "separator";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        final SeparatorMenuItem that = (SeparatorMenuItem) o;

        final String thisCssClasses = getCssClasses() == null ? "" : getCssClasses().stream()
                .sorted()
                .reduce((left, right) -> left + "," + right)
                .get();
        final String thatCssClasses = that.getCssClasses() == null ? "" : that.getCssClasses().stream()
                .sorted()
                .reduce((left, right) -> left + "," + right)
                .get();

        return Objects.equals(getIndex(), that.getIndex())
                && Objects.equals(getIdAttribute(), that.getIdAttribute())
                && Objects.equals(getParent(), that.getParent())
                && Objects.equals(thisCssClasses, thatCssClasses)
                && Objects.equals(isEnabled(), that.isEnabled())
                && Objects.equals(getRoleAttribute(), that.getRoleAttribute());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        final String cssClasses = getCssClasses() == null
                ? ""
                : getCssClasses().stream().sorted().reduce((left, right) -> left + "," + right).get();

        return Objects.hash(getRoleAttribute(),
                getIdAttribute(),
                getTabIndexAttribute(),
                cssClasses,
                isEnabled(),
                getRoleAttribute());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validateEntityState() throws InternalStateValidationException {
    }
}
