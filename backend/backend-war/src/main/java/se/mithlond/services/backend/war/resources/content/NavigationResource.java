/*
 * #%L
 * Nazgul Project: mithlond-services-backend-war
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
package se.mithlond.services.backend.war.resources.content;

import se.mithlond.services.backend.war.resources.AbstractResource;
import se.mithlond.services.backend.war.resources.Parameters;
import se.mithlond.services.content.api.NavigationService;
import se.mithlond.services.content.model.navigation.integration.MenuStructure;
import se.mithlond.services.shared.authorization.api.SemanticAuthorizationPathProducer;

import javax.ejb.EJB;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.ArrayList;
import java.util.List;

/**
 * Navigation-related resources, to retrieve MenuStructures for callers.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Path("/navigation")
public class NavigationResource extends AbstractResource {

    // Internal state
    @EJB
    private NavigationService navigationService;

    /**
     * Retrieves the MenuStructure available from the supplied organisation, for the
     *
     * @param organisationName The name of the organisation for which the MenuStructure should be retrieved.
     * @return The fully populated MenuStructure for the supplied organisationName and the active Membership.
     */
    @Path("/{" + Parameters.ORGANISATION_NAME + "}")
    public MenuStructure getMenuStructure(@PathParam(Parameters.ORGANISATION_NAME) final String organisationName) {

        // Populate the SemanticAuthorizationPathProducer List with the active Membership.
        final List<SemanticAuthorizationPathProducer> sapp = new ArrayList<>();
        if (getDisconnectedActiveMembership().isPresent()) {
            sapp.add(getDisconnectedActiveMembership().get());
        }

        // All done.
        return navigationService.getMenuStructure(organisationName, sapp);
    }
}
