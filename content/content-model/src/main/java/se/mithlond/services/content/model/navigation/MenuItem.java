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
 * <p>Specification for a bootstrap-type MenuItem, with the principal structure shown below:</p>
 * <pre>
 *     &lt;li role="[role]"&gt;
 *      &lt;a role="[anchorRole]" tabindex="[tabIndex]" href="[href]"&gt;
 *          &lt;i class="icon-fixed-width [iconIdentifier]"&gt;&lt;/i&gt; [text]&lt;/a&gt;
 *     &lt;/li&gt;
 * </pre>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface MenuItem extends PresentableLink {

    /**
     * @return {@code true} to indicate that this MenuItem should be enabled. Default return value is {@code true};
     * override for MenuItems which are disabled by default or can be disabled.
     */
    default boolean isEnabled() {
        return true;
    }

    /**
     * @return The role of the MenuItem list element within this MenuItem.
     * The WAI-ARIA specification requires that menu items which should be part
     * of the presentation set the attribute role to {@code presentation}.
     */
    default String getRole() {
        return "presentation";
    }

    /**
     * <p>Retrieves the role attribute for the anchor/link of this MenuItem. This r</p>
     * <pre>
     *     &lt;li role="[role]"&gt;
     *      &lt;a role="[anchorRole]" tabindex="[tabIndex]" href="[href]"&gt;
     *          &lt;i class="icon-fixed-width [iconIdentifier]"&gt;&lt;/i&gt; [text]&lt;/a&gt;
     *     &lt;/li&gt;
     * </pre>
     *
     * @return The role of the inner anchor element, such as {@code button}.
     * By default, no role is set - the specification method returns {@code null}.
     * A null value from this method implies that no 'role' attribute should be set in the anchor
     * Element of this MenuItem.
     */
    default String getAnchorRole() {
        return null;
    }

    /**
     * @return The tab index of this MenuItem. Default value: {@code -1}.
     */
    default int getTabIndex() {
        return -1;
    }

    /**
     * @return The identifier of the Icon used for this MenuItem, or {@code null} if none is assigned.
     */
    default String getIconIdentifier() {
        return null;
    }
}
