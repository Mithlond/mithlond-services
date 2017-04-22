/*
 * #%L
 * Nazgul Project: mithlond-services-shared-spi-algorithms
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
package se.mithlond.services.shared.spi.algorithms;

import se.jguru.nazgul.core.algorithms.api.Validate;

import javax.xml.bind.annotation.XmlTransient;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <p>Helper utility to manage formatting and parsing dates and times.</p>
 * <p>This fully uses the JDK 8 time API.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
@XmlTransient
public enum TimeFormat {

    /**
     * A format similar to "Onsdag 2014-02-13".
     */
    DAY_OF_WEEK_AND_DATE("EEEE yyyy-MM-dd"),

    /**
     * A format yielding "2014-02-13" or equivalent.
     */
    YEAR_MONTH_DATE("yyyy-MM-dd"),

    /**
     * A format yielding "20140213" or equivalent.
     */
    COMPACT_LOCALDATE("yyyyMMdd"),

    /**
     * A format yielding "2014-02-13 14:23" or equivalent.
     */
    YEAR_MONTH_DATE_HOURS_MINUTES("yyyy-MM-dd HH:mm"),

    /**
     * A format yielding "20140213132500" or equivalent.
     */
    COMPACT_LOCALDATETIME("yyyyMMddHHmmss"),

    /**
     * A format retrieving hours and minutes only ("12:45").
     */
    HOURS_MINUTES("HH:mm"),

    /**
     * A Standard XML DateTime transport format.
     *
     * @see DateTimeFormatter#ISO_LOCAL_DATE_TIME
     */
    XML_TRANSPORT("yyyy-MM-dd'T'HH:mm:ssZ");

    /**
     * Swedish TimeZone, used to manage times and dates.
     */
    public static final ZoneId SWEDISH_TIMEZONE = java.time.ZoneId.of("Europe/Stockholm");

    /**
     * The language code for Swedish.
     */
    public static final String SWEDISH_LANGUAGE_CODE = "sv";

    /**
     * Swedish Locale, used for representing times and dates. This is the default Locale used
     * by internal DateTimeFormatter instances.
     */
    public static final Locale SWEDISH_LOCALE = new Locale(SWEDISH_LANGUAGE_CODE, "SE");

    // Internal state
    private Locale defaultLocale;
    private ZoneId defaultZoneId;
    private DateTimeFormatter formatter;

    /**
     * Creates a new TimeFormat instance with a DateTimeFormatter wrapping the supplied DateFormat pattern.
     *
     * @param pattern The DateTimeFormatter patter to use.
     */
    TimeFormat(final String pattern) {

        // This is required since enums can't access local-type constants within their constructor.
        defaultLocale = new Locale("sv", "SE");
        defaultZoneId = ZoneId.of("Europe/Stockholm");

        formatter = DateTimeFormatter.ofPattern(pattern).withLocale(defaultLocale).withZone(defaultZoneId);
    }

    /**
     * Prints the supplied Temporal object to a String, using the optionalLocale if it is supplied
     * and {@code SWEDISH_LOCALE} otherwise.
     *
     * @param aTemporal      The temporal to print to a String. Cannot be null.
     * @param optionalLocale An optional Locale, used to format the supplied Temporal with the internal
     *                       DateTimeFormatter. If optionalLocale is {@code null}, {@code SWEDISH_LOCALE} is used.
     * @param optionalZoneId An optional ZoneID, used to format the supplied Temporal with the internal
     *                       DateTimeFormatter. If optionalZoneId is {@code null}, {@code SWEDISH_TIMEZONE} is used.
     * @return A String representation of the supplied aTemporal.
     */
    public String print(final Temporal aTemporal,
            final Locale optionalLocale,
            final ZoneId optionalZoneId) {

        // Check sanity
        Validate.notNull(aTemporal, "aTemporal");

        // Be simplistic ...
        final Locale effectiveLocale = optionalLocale == null ? defaultLocale : optionalLocale;
        final ZoneId effectiveZoneID = optionalZoneId == null ? defaultZoneId : optionalZoneId;

        // All done.
        return formatter.withLocale(effectiveLocale).withZone(effectiveZoneID).format(aTemporal);
    }

    /**
     * Convenience method to print the supplied Temporal using default Locale and ZoneID.
     *
     * @param aTemporal The temporal to print to a String. Cannot be null.
     * @return A String representation of the supplied aTemporal.
     */
    public String print(final Temporal aTemporal) {
        return print(aTemporal, null, null);
    }

    /**
     * Parses the supplied toParse string into a ZonedDateTime, using the provided optionalLocale and optionalZoneId
     * if they are not null and the default versions otherwise.
     *
     * @param toParse        The string to parse into a ZonedDateTime.
     * @param optionalLocale An optional Locale, used to parse the supplied String into a ZonedDateTime using the
     *                       internal DateTimeFormatter. If optionalLocale is {@code null},
     *                       {@code SWEDISH_LOCALE} is used.
     * @param optionalZoneId An optional ZoneID, used to parse the supplied String into a ZonedDateTime using the
     *                       internal DateTimeFormatter. If optionalZoneId is {@code null},
     *                       {@code SWEDISH_TIMEZONE} is used.
     * @return A ZonedDateTime parsed from the supplied String.
     */
    public ZonedDateTime parse(final String toParse, final Locale optionalLocale, final ZoneId optionalZoneId) {

        // Check sanity
        Validate.notEmpty(toParse, "toParse");

        // Be simplistic ...
        final Locale effectiveLocale = optionalLocale == null ? defaultLocale : optionalLocale;
        final ZoneId effectiveZoneID = optionalZoneId == null ? defaultZoneId : optionalZoneId;

        // All done.
        return ZonedDateTime.parse(toParse, formatter.withLocale(effectiveLocale).withZone(effectiveZoneID));
    }

    /**
     * Convenience method to parse the supplied String using default Locale and ZoneID.
     *
     * @param toParse The string to parse into a ZonedDateTime.
     * @return A ZonedDateTime parsed from the supplied String.
     */
    public ZonedDateTime parse(final String toParse) {
        return parse(toParse, null, null);
    }

    /**
     * Converts a LocalDate to a Calendar
     * @param aLocalDate
     * @return
     */
    @SuppressWarnings("all")
    public Calendar convert(final LocalDate aLocalDate) {
        if(aLocalDate == null) {
            return null;
        }

        final Instant instant = aLocalDate.atStartOfDay(TimeFormat.SWEDISH_TIMEZONE).toInstant();
        final Calendar toReturn = Calendar.getInstance(TimeZone.getTimeZone(TimeFormat.SWEDISH_TIMEZONE));
        toReturn.setTime(Date.from(instant));

        // All done.
        return toReturn;
    }
}
