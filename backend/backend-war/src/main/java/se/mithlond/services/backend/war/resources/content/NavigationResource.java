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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mithlond.services.backend.war.resources.AbstractResource;
import se.mithlond.services.backend.war.resources.RestfulParameters;
import se.mithlond.services.content.api.NavigationService;
import se.mithlond.services.content.model.navigation.integration.MenuStructure;
import se.mithlond.services.content.model.navigation.integration.SeparatorMenuItem;
import se.mithlond.services.content.model.navigation.integration.StandardMenu;
import se.mithlond.services.content.model.navigation.integration.StandardMenuItem;
import se.mithlond.services.shared.authorization.api.SemanticAuthorizationPathProducer;

import javax.ejb.EJB;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Navigation-related resources, to retrieve MenuStructures for callers.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Path("/navigation")
public class NavigationResource extends AbstractResource {

    // Our Logger
    private static final Logger log = LoggerFactory.getLogger(NavigationResource.class);

    // Internal state
    @EJB
    private NavigationService navigationService;
    private static final String JSON_CONTENT_TYPE = "application/json";
    private static final String ECLIPSELINK_MEDIA_TYPE = "eclipselink.media-type";

    /**
     * Retrieves the MenuStructure available from the supplied organisation, for the
     *
     * @param organisationName The name of the organisation for which the MenuStructure should be retrieved.
     * @return The fully populated MenuStructure for the supplied organisationName and the active Membership.
     */
    @Path("/{" + RestfulParameters.ORGANISATION_NAME + "}")
    @GET
    public MenuStructure getMenuStructure(@PathParam(RestfulParameters.ORGANISATION_NAME) final String organisationName) {

        // Populate the SemanticAuthorizationPathProducer List with the active Membership.
        final List<SemanticAuthorizationPathProducer> sapp = new ArrayList<>();
        if (getDisconnectedActiveMembership().isPresent()) {
            sapp.add(getDisconnectedActiveMembership().get());
        }

        // All done.
        return navigationService.getMenuStructure(organisationName, sapp);
    }

    /**
     * Creates or updates the MenuStructure of the supplied organisation.
     *
     * @param organisationName The name of the organisation for which the MenuStructure should be retrieved.
     * @param isXml            true to indicate that the newMenuStructure is given in XML form, and
     *                         false to indicate that it was given in JSON form.
     * @param newMenuStructure The new MenuStructure in its marshalled String form.
     * @return The fully populated MenuStructure for the supplied organisationName and the active Membership.
     */
    @Path("/{" + RestfulParameters.ORGANISATION_NAME + "}/update")
    @POST
    public MenuStructure updateMenuStructure(
            @PathParam(RestfulParameters.ORGANISATION_NAME) final String organisationName,
            @HeaderParam(RestfulParameters.MENUSTRUCTURE_DATA) final String newMenuStructure,
            @QueryParam(RestfulParameters.ISXML) @DefaultValue("true") final boolean isXml) {

        if(log.isDebugEnabled()) {
            log.debug("Starting " + (isXml ? "XML" : "JSON")
                    + " update of MenuStructure for [" + organisationName + "]");
        }

        // Populate the SemanticAuthorizationPathProducer List with the active Membership.
        final List<SemanticAuthorizationPathProducer> sapp = new ArrayList<>();
        if (getDisconnectedActiveMembership().isPresent()) {
            sapp.add(getDisconnectedActiveMembership().get());
        }

        // Get the existing MenuStructure, if applicable.
        final MenuStructure inboundMenuStructure = unmarshalMenuStructure(!isXml, newMenuStructure);

        // All Done.
        return navigationService.createOrUpdate(inboundMenuStructure, sapp);
    }

    //
    // Private helpers
    //

    private MenuStructure unmarshalMenuStructure(final boolean expectJSON, final String newStructure) {

        if(log.isDebugEnabled()) {
            log.debug("Unmarshalling MenuStructure in " + (expectJSON ? "JSON" : "XML") + " form:\n " + newStructure);
        }

        try {
            final JAXBContext context = JAXBContext.newInstance(
                    MenuStructure.class,
                    StandardMenu.class,
                    StandardMenuItem.class,
                    SeparatorMenuItem.class);

            final Unmarshaller unmarshaller = context.createUnmarshaller();

            if (expectJSON) {
                unmarshaller.setProperty(ECLIPSELINK_MEDIA_TYPE, JSON_CONTENT_TYPE);
            }

            final JAXBElement<MenuStructure> unmarshalled = unmarshaller
                    .unmarshal(new StreamSource(new StringReader(newStructure)), MenuStructure.class);
            if (unmarshalled != null) {
                return unmarshalled.getValue();
            }

            // Complain.
            throw new IllegalArgumentException("Could not unmarshal newStructure '" + newStructure + "'");

        } catch (JAXBException e) {

            // Complain more.
            throw new IllegalStateException("Could not create a JAXB Unmarshaller.", e);
        }
    }
}
