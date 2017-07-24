/*-
 * #%L
 * Nazgul Project: mithlond-services-backend-war
 * %%
 * Copyright (C) 2015 - 2017 Mithlond
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
package se.mithlond.services.backend.war.resources.food;

import se.mithlond.services.backend.war.resources.AbstractResource;
import se.mithlond.services.backend.war.resources.RestfulParameters;
import se.mithlond.services.organisation.api.FoodAndAllergyService;
import se.mithlond.services.organisation.model.transport.food.Foods;

import javax.ejb.EJB;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import java.util.Locale;

/**
 * Service getting and emitting information about Food, Allergies etc.
 * 
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@Path("/food")
public class FoodResource extends AbstractResource {

    @EJB
    private FoodAndAllergyService foodAndAllergyService;

    /**
     * Retrieves a Foods container holding all known Food substances, including all Categorization.
     *
     * @param getAllDetails if {@code true}, includes a detailed representation of all food
     *                      stuffs - and otherwise a shallow one.
     * @return a Foods transport container holding all Food known to this organisation.
     */
    @GET
    public Foods getAllFoods(@QueryParam(RestfulParameters.DETAILS)
                             @DefaultValue("false") final boolean getAllDetails) {

        // Use the same Locale as the Organisation of the ActiveMembership.
        final Locale locale = getActiveMembership().getOrganisation().getLocale();
        final Foods toReturn = new Foods(!getAllDetails, locale);

        // Find and add all Foods.
        foodAndAllergyService.getAllFoods().forEach(f -> toReturn.add(!getAllDetails, f));

        // All Done.
        return toReturn;
    }
}
