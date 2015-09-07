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
import se.mithlond.services.shared.authorization.api.SemanticAuthorizationPathProducer;
import se.mithlond.services.shared.authorization.api.builder.AuthorizationPathBuilder;
import se.mithlond.services.shared.authorization.model.SemanticAuthorizationPath;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;
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
@XmlType(namespace = Patterns.NAMESPACE, propOrder = {"anchorHRef", "text", "requiredAuthorization"})
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

    /**
     * Any extra CSS classes added to this AbstractPresentableLink.
     */
    @XmlElementWrapper(nillable = true, required = false)
    @XmlElement(nillable = true, name = "cssClass")
    private List<String> cssClasses;

    /**
     * A concatenated set of AuthorizationPaths required to access this AbstractPresentableLink.
     */
    @XmlElement(nillable = true, required = false)
    private String requiredAuthorization;

    // Mutex lock.
    private final transient Object[] lock;

    /**
     * JAXB/JPA-friendly constructor.
     */
    public AbstractPresentableLink() {
        lock = new Object[0];
        cssClasses = new ArrayList<>();
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
        // Delegate
        this();

        // Assign internal state
        this.anchorHRef = anchorHRef;
        this.text = text;
        if (requiredGroups != null) {
            final Group[] groupArray = new Group[requiredGroups.size()];
            requiredGroups.toArray(groupArray);
            addRequiredAuthorization(groupArray);
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
     * {@inheritDoc}
     */
    @Override
    public List<String> getCssClasses() {
        final List<String> toReturn = new ArrayList<>();
    }

    /**
     * Adds the SemanticAuthorizationPath instances as acquired from the SemanticAuthorizationPathProducer.
     *
     * @param producers All SemanticAuthorizationPathProducers whose SemanticAuthorizationPaths should be
     *                  added as required authorization paths to this AbstractPresentableLink.
     */
    public void addRequiredAuthorization(final SemanticAuthorizationPathProducer... producers) {

        if (producers != null && producers.length > 0) {

            synchronized (lock) {
                final SortedSet<SemanticAuthorizationPath> auths = this.requiredAuthorization == null
                        ? new TreeSet<>()
                        : AuthorizationPathBuilder.parse(this.requiredAuthorization);

                // Add all required authorization from the supplied producers.
                for (SemanticAuthorizationPathProducer current : producers) {
                    auths.addAll(current.getPaths());
                }

                // Write back the changed
                final StringBuilder builder = new StringBuilder();
                for (SemanticAuthorizationPath current : auths) {
                    builder.append(SemanticAuthorizationPath.SEGMENT_SEPARATOR)
                            .append(current.getRealm())
                            .append(SemanticAuthorizationPath.SEGMENT_SEPARATOR)
                            .append(current.getGroup())
                            .append(SemanticAuthorizationPath.SEGMENT_SEPARATOR)
                            .append(current.getQualifier())
                            .append(SemanticAuthorizationPath.PATTERN_SEPARATOR);
                }

                this.requiredAuthorization = builder.substring(
                        0,
                        builder.length() - SemanticAuthorizationPath.PATTERN_SEPARATOR);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SortedSet<SemanticAuthorizationPath> getPaths() {
        return AuthorizationPathBuilder.parse(this.requiredAuthorization);
    }
}
