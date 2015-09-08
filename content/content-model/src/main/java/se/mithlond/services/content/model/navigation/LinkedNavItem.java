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

/**
 * <p>Specification for AuthorizedNavItems which contain Anchors/Hyperlinks.
 * The standard structure to render for a Bootstrap-esque MenuItem corresponds
 * to the following:</p>
 * <pre>
 *     &lt;li&gt;
 *          &lt;a tabindex="[tabIndex]" href="[href]"&gt;
 *              &lt;i class="icon-fixed-width [iconIdentifier]"&gt;&lt;/i&gt; [text]&lt;/a&gt;
 *     &lt;/li&gt;
 * </pre>
 * <p>In this specification, the </p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface LinkedNavItem extends AuthorizedNavItem {

    /**
     * @return The value of the {@code href} attribute of this LinkedNavItem.
     */
    String getHrefAttribute();

    /**
     * Retrieves an Icon identifier string, possibly augmented by the service implementation to yield
     * the actual CSS class which retrieves the Icon as desired.
     *
     * @return An Icon identifier string. If {@code null} is returned, the client-side rendering engine is instructed
     * not to render an icon for this LinkedNavItem.
     */
    String getIconIdentifier();
}
