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
import se.mithlond.services.content.model.Patterns;
import se.mithlond.services.organisation.model.membership.Group;
import se.mithlond.services.shared.spi.algorithms.authorization.SemanticAuthorizationPath;

import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlType;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * <p>Abstract implementation for a minimalistic bootstrap-type item which can be put in a Menu.</p>
 * <pre>
 *     &lt;li role="[role]"&gt;
 *      &lt;a role="[linkRole]" tabindex="[tabIndex]" href="[href]"&gt;
 *          &lt;i class="icon-fixed-width [iconIdentifier]"&gt;&lt;/i&gt; [text]&lt;/a&gt;
 *     &lt;/li&gt;
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@MappedSuperclass
@XmlType(namespace = Patterns.NAMESPACE)
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractPresentableLink extends NazgulEntity implements PresentableLink {

    /**
     * The anchor HREf of this AbstractPresentableLink, to which the user is directed when clicking it.
     * Used as the value in the href attribute of the MenuItem anchor/link.
     */
    @XmlElement(nillable = true, required = false)
    private String anchorHRef;

    /**
     * The human-readable text of this Menu or MenuItem.
     */
    @XmlElement(nillable = true, required = false)
    private String text;

    @ElementCollection()
    @XmlElementWrapper(required = true, nillable = false)
    @XmlElement(nillable = true, required = false, name = "authorizationPath")
    @XmlIDREF
    private SortedSet<String> requiredAuthorization;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public AbstractPresentableLink() {
        requiredAuthorization = new TreeSet<>();
    }

    /**
     * Compound constructor, creating an AbstractPresentableLink wrapping the supplied data.
     *
     * @param anchorHRef The value in the href attribute of the AbstractPresentableLink.
     * @param text       The human-readable text of this AbstractPresentableLink.
     */
    protected AbstractPresentableLink(final String anchorHRef,
                                      final String text,
                                      final SortedSet<Group> requiredGroups) {
        this();

        // Assign internal state
        this.anchorHRef = anchorHRef;
        this.text = text;
        if (requiredGroups != null) {
            requiredGroups.forEach(current -> requiredAuthorization.add(current.createPath()));
        }
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
     *
     * {@inheritDoc}
     */
    @Override
    public SortedSet<SemanticAuthorizationPath> getPaths() {
        return requiredAuthorization;
    }
}
