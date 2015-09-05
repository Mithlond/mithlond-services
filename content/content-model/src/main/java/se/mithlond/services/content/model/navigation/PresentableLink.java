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

import se.mithlond.services.shared.spi.algorithms.authorization.SemanticAuthorizationPath;
import se.mithlond.services.shared.spi.algorithms.authorization.SemanticAuthorizationPathProducer;

import java.util.SortedSet;

/**
 * <h2>Basic specification</h2>
 * <p>Specification for a minimalistic bootstrap-type item which can be put in a Menu.</p>
 * <pre>
 *     &lt;li role="[role]"&gt;
 *      &lt;a role="[linkRole]" tabindex="[tabIndex]" href="[href]"&gt;
 *          &lt;i class="icon-fixed-width [iconIdentifier]"&gt;&lt;/i&gt; [text]&lt;/a&gt;
 *     &lt;/li&gt;
 * </pre>
 * <h2>AuthorizationPath</h2>
 * <p>Any PresentableLink can be defined as requiring at least one of a defined set of
 * authorization paths. Each AuthorizationPath defines a realm and an optional group to
 * which the active User must belong in order to view the full data within this PresentableLink.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface PresentableLink extends SemanticAuthorizationPathProducer {

    /**
     * @return The value in the href attribute of the MenuItem anchor/link.
     */
    String getAnchorHRef();

    /**
     * @return The human-readable text of this MenuItem.
     */
    String getText();

    /**
     * @return the altText (help-popup-text) of this PresentableLink. Default implementation retrieves {@code null}.
     */
    default String getAltText() {
        return null;
    }

    /**
     * <p>Retrieves an optional Set of AuthorizationPath instances. If the user possesses at least one of these
     * AuthorizationPaths, this PresentableLink should be shown to the user with all its detail.
     * If the user does <strong>not</strong> possess any of the AuthorizationPaths returned by this method,
     * only non-sensitive data from this PresentableLink should be made available to the user.</p>
     * <p>In this context, <strong>non-sensitive data</strong> might be the text and icon of the PresentableLink,
     * but not any HREFs.</p>
     *
     * @return A SortedSet containing AuthorizationPaths required to view the data of this PresentableLink.
     * A {@code null} return value from this method indicates that this PresentableLink does not have any protection,
     * and is hence viewable by all Users.
     */
    @Override
    default SortedSet<SemanticAuthorizationPath> getPaths() {
        return null;
    }
}
