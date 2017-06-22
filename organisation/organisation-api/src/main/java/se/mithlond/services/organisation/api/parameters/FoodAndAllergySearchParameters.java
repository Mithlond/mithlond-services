package se.mithlond.services.organisation.api.parameters;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

/**
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
public class FoodAndAllergySearchParameters
        extends AbstractSearchParameters<FoodAndAllergySearchParameters.FoodAndAllergySearchParametersBuilder> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected void populateInternalStateMap(final SortedMap<String, String> toPopulate) {

    }

    /**
     * Retrieves a FoodAndAllergySearchParametersBuilder, to start populating a FoodAndAllergySearchParameters instance.
     *
     * @return a new FoodAndAllergySearchParametersBuilder used to populate with parameters for searches.
     */
    public static FoodAndAllergySearchParameters.FoodAndAllergySearchParametersBuilder builder() {
        return new FoodAndAllergySearchParameters.FoodAndAllergySearchParametersBuilder();
    }

    /**
     * Builder class for creating FoodAndAllergySearchParameters instances.
     */
    public static class FoodAndAllergySearchParametersBuilder
            extends AbstractParameterBuilder<FoodAndAllergySearchParameters.FoodAndAllergySearchParametersBuilder> {

        // Internal state
        private List<Long> groupIDs = new ArrayList<>();
        private List<Long> organisationIDs = new ArrayList<>();
        private List<Long> activityIDs = new ArrayList<>();

        /**
         * Adds the provided activity IDs parameter to be used by the FoodAndAllergySearchParametersBuilder instance.
         *
         * @param activityIDs the activityIDs parameter to be used by the FoodAndAllergySearchParameters instance.
         *                 Cannot be null or empty.
         * @return This FoodAndAllergySearchParametersBuilder instance.
         */
        public FoodAndAllergySearchParametersBuilder withActivityIDs(@NotNull final Long... activityIDs) {

            // Add the program to the programs List.
            return addValuesIfApplicable(this.activityIDs, "activityIDs", activityIDs);
        }

        /**
         * Adds the provided group IDs parameter to be used by the FoodAndAllergySearchParametersBuilder instance.
         *
         * @param groupIDs the groupIDs parameter to be used by the FoodAndAllergySearchParameters instance.
         *                 Cannot be null or empty.
         * @return This FoodAndAllergySearchParametersBuilder instance.
         */
        public FoodAndAllergySearchParametersBuilder withGroupIDs(@NotNull final Long... groupIDs) {

            // Add the program to the programs List.
            return addValuesIfApplicable(this.groupIDs, "groupIDs", groupIDs);
        }

        /**
         * Adds the provided organisation IDs parameter to be used by the FoodAndAllergySearchParametersBuilder instance.
         *
         * @param organisationIDs the organisationIDs parameter to be used by the FoodAndAllergySearchParameters instance.
         *                        Cannot be null or empty.
         * @return This FoodAndAllergySearchParametersBuilder instance.
         */
        public FoodAndAllergySearchParametersBuilder withOrganisationIDs(@NotNull final Long... organisationIDs) {

            // Add the program to the programs List.
            return addValuesIfApplicable(this.organisationIDs, "organisationIDs", organisationIDs);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public FoodAndAllergySearchParameters build() {
            return null;
        }
    }
}
