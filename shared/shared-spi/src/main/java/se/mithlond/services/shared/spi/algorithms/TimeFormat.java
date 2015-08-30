package se.mithlond.services.shared.spi.algorithms;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.temporal.Temporal;
import java.util.Locale;

/**
 * <p>Helper utility to manage formatting and parsing dates and times.</p>
 * <p>This fully uses the JDK 8 time API.</p>
 *
 * @author <a href="mailto:lj@jguru.se">Lennart J&ouml;relid</a>, jGuru Europe AB
 */
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
     * Swedish Locale, used for representing times and dates. This is the default Locale used
     * by internal DateTimeFormatter instances.
     */
    public static final Locale SWEDISH_LOCALE = new Locale("sv", "SE");

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
        return (ZonedDateTime) formatter.withLocale(effectiveLocale).withZone(effectiveZoneID).parse(toParse);
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
}
