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
 * <p>Specification for a minimalistic bootstrap-type item which can be put in a Menu.</p>
 * <pre>
 *     &lt;li role="[role]"&gt;
 *      &lt;a role="[linkRole]" tabindex="[tabIndex]" href="[href]"&gt;
 *          &lt;i class="icon-fixed-width [iconIdentifier]"&gt;&lt;/i&gt; [text]&lt;/a&gt;
 *     &lt;/li&gt;
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface PresentableLink {

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
}
