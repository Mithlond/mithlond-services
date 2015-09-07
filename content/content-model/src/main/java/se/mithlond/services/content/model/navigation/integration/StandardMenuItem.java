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
import se.mithlond.services.content.model.navigation.AbstractPresentableLink;
import se.mithlond.services.content.model.navigation.MenuItem;
import se.mithlond.services.organisation.model.membership.Group;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.SortedSet;

/**
 * <p>Default Entity implementation of a MenuItem, complying with the MenuItem specification of a
 * <a href="http://getbootstrap.com">Twitter Bootstrap</a>-style menu item. Refer to the
 * MenuItem for a markup model specification.</p>
 * <pre>
 *     &lt;li role="[role]"&gt;
 *      &lt;a role="[linkRole]" tabindex="[tabIndex]" href="[href]"&gt;
 *          &lt;i class="icon-fixed-width [iconIdentifier]"&gt;&lt;/i&gt; [text]&lt;/a&gt;
 *     &lt;/li&gt;
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {})
@XmlAccessorType(XmlAccessType.FIELD)
public class StandardMenuItem extends AbstractPresentableLink implements MenuItem {

    /**
     * JAXB/JPA-friendly constructor.
     */
    public StandardMenuItem() {
        super();
    }

    /**
     *
     * @param anchorHRef
     * @param text
     * @param requiredGroups
     */
    public StandardMenuItem(final String anchorHRef, final String text, final SortedSet<Group> requiredGroups) {
        super(anchorHRef, text, requiredGroups);
    }
}
