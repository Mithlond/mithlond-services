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
import se.mithlond.services.content.model.navigation.Menu;
import se.mithlond.services.content.model.navigation.MenuItem;
import se.mithlond.services.content.model.navigation.PresentableLink;
import se.mithlond.services.organisation.model.membership.GroupMembership;
import se.mithlond.services.organisation.model.membership.guild.GuildMembership;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;
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
@XmlType(namespace = Patterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public class StandardMenu extends NazgulEntity implements Menu {

    // Internal state
    @XmlElement(nillable = true, required = false)
    private String anchorHRef;

    @XmlElement(nillable = true, required = false)
    private String text;

    @XmlElementWrapper(required = true, nillable = false)
    @XmlElements(value = {
            @XmlElement(name = "subMenu", type = Menu.class),
            @XmlElement(name = "menuItem", type = MenuItem.class)
    })
    private List<PresentableLink> children;

    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends PresentableLink> List<T> getChildren() {
        return (List<T>) children;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAnchorHRef() {
        return anchorHRef;
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
    protected void validateEntityState() throws InternalStateValidationException {
    }
}
