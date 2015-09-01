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

import java.util.List;

/**
 * <p>A Bootstrap-type Menu (button) which provides a dropdown containing MenuItems.</p>
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
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public interface Menu extends PresentableLink {

    /**
     * Retrieves an ordered List of all PresentableLink children stashed within this Menu.
     *
     * @param <T> The type of data retrieved.
     * @return A List containing all PresentableLinks contained within this Menu.
     */
    <T extends PresentableLink> List<T> getChildren();
}
