/*
 * #%L
 * Nazgul Project: mithlond-services-content-api
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
package se.mithlond.services.content.api;

import se.mithlond.services.content.api.navigation.transport.MenuStructure;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.membership.Membership;

import javax.ejb.Local;
import java.util.List;

/**
 * Service specification for navigating resources or sites.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Local
public interface NavigationService {

    /**
     * Retrieves the MenuStructure available to a caller sporting the supplied Memberships.
     *
     * @param menuOwner          The organisation owning the site for which a MenuStructure should be retrieved.
     * @param callersMemberships The Memberships of the caller, used to determine which MenuStructure
     *                           items should be retrieved.
     * @return A fully set-up Menu structure.
     */
    MenuStructure getMenuStructure(final Organisation menuOwner, final List<Membership> callersMemberships);
}