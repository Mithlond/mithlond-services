/*
 * #%L
 * Nazgul Project: mithlond-services-content-impl-ejb
 * %%
 * Copyright (C) 2015 - 2016 Mithlond
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
package se.mithlond.services.content.impl.ejb;

import se.mithlond.services.organisation.model.Category;
import se.mithlond.services.organisation.model.Organisation;
import se.mithlond.services.organisation.model.OrganisationPatterns;
import se.mithlond.services.organisation.model.address.CategorizedAddress;
import se.mithlond.services.organisation.model.membership.Membership;
import se.mithlond.services.shared.spi.algorithms.TimeFormat;
import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.chrono.ChronoLocalDate;

/**
 * Utility class holding JPA-related shared methods.
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@SuppressWarnings("all")
public final class CommonPersistenceTasks {

    /**
     * Retrieves a single organisation with the given name.
     *
     * @param entityManager    The non-null and active {@link EntityManager}.
     * @param organisationName the non-empty organisationName.
     * @return The Organisation corresponding to the supplied organisationName.
     * @throws IllegalArgumentException if no single organisation with the given organisationName could be found.
     */
    public static Organisation getOrganisation(final EntityManager entityManager, final String organisationName)
            throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(entityManager, "entityManager");
        Validate.notEmpty(organisationName, "organisationName");

        // Fire the JPA query
        final Organisation organisation = entityManager.createNamedQuery(
                Organisation.NAMEDQ_GET_BY_NAME, Organisation.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisationName)
                .getSingleResult();

        // Handle insanity
        if (organisation == null) {
            throw new IllegalArgumentException("Hittade ingen organisation med namnet [" + organisationName + "]");
        }

        // All Done
        return organisation;
    }

    /**
     * Retrieves the Organisation with the supplied organisation JPA ID.
     *
     * @param entityManager  The non-null entity manager.
     * @param organisationID The JPA ID of the organisation to retrieve.
     * @return The Organisation entity - or null if no organisation could be retrieved.
     */
    public static Organisation getOrganisation(final EntityManager entityManager, final long organisationID) {

        try {
            return entityManager.find(Organisation.class, organisationID);
        } catch (Exception e) {
            throw new IllegalArgumentException("Hittade ingen organisation med ID [" + organisationID + "]");
        }
    }

    /**
     * Retrieves the Category with the classification {@link CategorizedAddress#ACTIVITY_CLASSIFICATION}
     * and the given ID.
     *
     * @param entityManager The non-null and active {@link EntityManager}.
     * @param categoryID    the non-empty categoryID.
     * @return The Category corresponding to the supplied categoryID and having the
     * classification {@link CategorizedAddress#ACTIVITY_CLASSIFICATION}
     * @throws IllegalArgumentException if no single organisation with the given organisationName could be found.
     */
    public static Category getAddressCategory(final EntityManager entityManager, final String categoryID)
            throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(entityManager, "entityManager");
        Validate.notEmpty(categoryID, "categoryID");

        // Fire
        final Category category = entityManager.createNamedQuery(
                Category.NAMEDQ_GET_BY_ID_CLASSIFICATION, Category.class)
                .setParameter(OrganisationPatterns.PARAM_CATEGORY_ID, categoryID)
                .setParameter(OrganisationPatterns.PARAM_CLASSIFICATION, CategorizedAddress.ACTIVITY_CLASSIFICATION)
                .getSingleResult();

        if (category == null) {
            throw new IllegalArgumentException("Hittade ingen kategori [" + categoryID + "]");
        }

        // All Done.
        return category;
    }

    /**
     * Retrieves a single Membership with the supplied alias and organisationName.
     *
     * @param entityManager    The active {@link EntityManager}.
     * @param alias            The non-empty alias.
     * @param organisationName The non-empty organisationName.
     * @return The Membership with the given alias and organisationName.
     * @throws IllegalArgumentException if no Membership could be found.
     */
    public static Membership getSingleMembership(final EntityManager entityManager,
            final String alias,
            final String organisationName) throws IllegalArgumentException {

        // Check sanity
        Validate.notNull(entityManager, "entityManager");
        Validate.notEmpty(alias, "alias");
        Validate.notEmpty(organisationName, "organisationName");

        // Fire
        final Membership toReturn = entityManager.createNamedQuery(
                Membership.NAMEDQ_GET_BY_ALIAS_ORGANISATION, Membership.class)
                .setParameter(OrganisationPatterns.PARAM_ORGANISATION_NAME, organisationName)
                .setParameter(OrganisationPatterns.PARAM_ALIAS, alias)
                .getSingleResult();

        if (toReturn == null) {
            throw new IllegalArgumentException("Hittade ingen medlem i organisationen ["
                    + organisationName + "] med alias [" + alias + "]");
        }

        // All Done.
        return toReturn;
    }

    /**
     * Validates the interval supplied by the startInterval and endInterval arguments.
     *
     * @param startInterval               The start of the interval.
     * @param nameOfStartIntervalArgument The name of the interval start, such as "activityVO.getStart()".
     * @param endInterval                 The end of the interval.
     * @param nameOfEndIntervalArgument   The name of the interval end, such as "activityVO.getEnd()".
     * @param <T>                         The type of ChronoLocalDate used.
     */
    public static <T extends ChronoLocalDate> void validateInterval(final T startInterval,
            final String nameOfStartIntervalArgument,
            final T endInterval,
            final String nameOfEndIntervalArgument) {

        // Check sanity
        final String startArg = nameOfStartIntervalArgument == null ? "startInterval" : nameOfStartIntervalArgument;
        final String endArg = nameOfEndIntervalArgument == null ? "endInterval" : nameOfEndIntervalArgument;
        Validate.notNull(startInterval, startArg);
        Validate.notNull(endInterval, endArg);

        if (startInterval.isAfter(endInterval)) {

            final String endTimeString = TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(startInterval);
            final String startTimeString = TimeFormat.YEAR_MONTH_DATE_HOURS_MINUTES.print(endInterval);
            throw new IllegalArgumentException("Intervallets sluttid [" + endTimeString
                    + "] måste vara efter starttiden [" + startTimeString + "]");
        }
    }

    /**
     * Finds the startTime originating from the supplied endTime and period.
     *
     * @param endTime The endTime to originate from.
     * @param period  The period size.
     * @return The start LocalDate.
     */
    public static LocalDate getStartTimeFrom(
            final LocalDate endTime,
            final Period period) {

        final LocalDate effectiveEnd = endTime == null
                ? LocalDate.now(TimeFormat.SWEDISH_TIMEZONE)
                : endTime;

        final Period effectivePeriod = period == null || period.isNegative()
                ? Period.ofMonths(3)
                : period;

        // All Done.
        return effectiveEnd.minus(effectivePeriod);
    }

    /**
     * Finds the startTime originating from the supplied endTime and period.
     *
     * @param endTime The endTime to originate from.
     * @param period  The period size.
     * @return The start LocalDateTime.
     */
    public static LocalDateTime getStartTimeFrom(
            final LocalDateTime endTime,
            final Period period) {

        final LocalDateTime effectiveEndTime = endTime == null
                ? LocalDateTime.now(TimeFormat.SWEDISH_TIMEZONE)
                : endTime;

        final Period effectivePeriod = period == null || period.isNegative()
                ? Period.ofMonths(3)
                : period;

        // All Done.
        return effectiveEndTime.minus(effectivePeriod);
    }

    /*
     * Hide the constructor for utility classes.
     */
    private CommonPersistenceTasks() {
        // Do nothing
    }
}
