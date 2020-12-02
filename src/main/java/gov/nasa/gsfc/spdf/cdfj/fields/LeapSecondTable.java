package gov.nasa.gsfc.spdf.cdfj.fields;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import java.util.stream.*;

import lombok.*;
import lombok.experimental.*;

/**
 * The Interface LeapSecondTable.
 */
public interface LeapSecondTable {

    static final String DEFAULT_CDF_LEAP_SECONDS_FILENAME = "CDFLeapSeconds.txt";

    static URL DEFAULT_LEAP_SECOND_TABLE_SYS_RESOURCE_URL = ClassLoader
            .getSystemResource(DEFAULT_CDF_LEAP_SECONDS_FILENAME);

    static URI DEFAULT_LEAP_SECOND_TABLE_SYS_RESOURCE_URI = Optional
            .ofNullable(DEFAULT_LEAP_SECOND_TABLE_SYS_RESOURCE_URL)
            .map(url -> {

                try {
                    return url.toURI();
                }
                catch (URISyntaxException e) {
                    throw new IllegalStateException(
                            "Default CDF Leap Seconds Table File URL, " + url + ", is not valid.", e);
                }

            })
            .orElseThrow(() -> new IllegalStateException(
                    "Default CDF Leap Seconds Table File, " + DEFAULT_CDF_LEAP_SECONDS_FILENAME + ", is not found"));

    static final LeapSecondTable DEFAULT_LEAP_SECOND_TABLE = from(DEFAULT_LEAP_SECOND_TABLE_SYS_RESOURCE_URI);

    /**
     * Creates a {@link LeapSecondTable} from a URI.
     *
     * @param cdfLeapSecondsFilePath the cdf leap seconds file path
     * 
     * @return the leap second table
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    static LeapSecondTable from(URI cdfLeapSecondsFileUri) {

        return from(Path.of(cdfLeapSecondsFileUri));

    }

    /**
     * Creates a {@link LeapSecondTable} from a Path.
     *
     * @param cdfLeapSecondsFilePath the cdf leap seconds file path
     * 
     * @return the leap second table
     * 
     * @throws IOException Signals that an I/O exception has occurred.
     */
    static LeapSecondTable from(Path cdfLeapSecondsFilePath) {

        try {
            return Files.lines(cdfLeapSecondsFilePath)
                    .map(Row.ROW_PATTERN::matcher)
                    .filter(Matcher::matches)
                    .map(Row::from)
                    .collect(LeapSecondTableImpl.COLLECTOR);
        }
        catch (IOException e) {
            throw new UncheckedIOException("Failed to read CDF Leap Seconds Table at path: " + cdfLeapSecondsFilePath,
                    e);
        }

    }

    /**
     * THe rows in the table
     *
     * @return the navigable set
     */
    NavigableSet<LeapSecondTable.Row> rows();

    @Value
    @Accessors(fluent = true)
    @SuperBuilder(toBuilder = true)
    static class LeapSecondTableImpl implements LeapSecondTable {

        static final Collector<LeapSecondTable.Row, LeapSecondTableImplBuilder<?, ?>, LeapSecondTable> COLLECTOR = Collector
                .of(LeapSecondTableImpl::builder, LeapSecondTableImplBuilder::row, (lhs, rhs) -> {
                    rhs.build()
                            .rows()
                            .forEach(lhs::row);
                    return lhs;

                }, LeapSecondTableImpl.LeapSecondTableImplBuilder::build);

        @Singular
        NavigableSet<LeapSecondTable.Row> rows;

        // FIXME: Temp Workaround for javadoc
        public static abstract class LeapSecondTableImplBuilder<C extends LeapSecondTableImpl, B extends LeapSecondTableImplBuilder<C, B>> {

        }
    }

    /**
     * The Interface Row.
     */
    public interface Row extends Comparable<LeapSecondTable.Row> {

        /** The Constant ROW_REGEX_STRING. */
        // @formatter:off
        public static final String ROW_REGEX_STRING = "^[^;]\\h+" 
                + "(?<year>\\d+)"
                + "\\h+"
                + "(?<month>1[0-2]|[1-9])"
                + "\\h+"
                + "(?<day>[1-9]|[12]\\d|3[01])" 
                + "\\h+"
                + "(?<leapSeconds>\\d+\\.?\\d*)"
                + "\\h+"
                + "(?<drift1>\\d+\\.?\\d*)"
                + "\\h+"
                + "(?<drift2>\\d+\\.?\\d*)"
                +"$";
        // @formatter:on

        /** The Constant ROW_PATTERN. */
        public static final Pattern ROW_PATTERN = Pattern.compile(ROW_REGEX_STRING);

        static Comparator<LeapSecondTable.Row> COMPARATOR = Comparator.comparing(Row::leapSecondDate);

        /**
         * From.
         *
         * @param matcher the matcher
         * 
         * @return the row
         */
        static LeapSecondTable.Row from(Matcher matcher) {

            String yearAsString = matcher.group("year");
            String monthAsString = matcher.group("month");
            String dayAsString = matcher.group("day");

            String leapSecondAsString = matcher.group("leapSeconds");
            double leapSecondsAsDouble = Double.valueOf(leapSecondAsString);
            long leapSecondsAsMicroseconds = DateTimeFields.doubleToMicroseconds(leapSecondsAsDouble);

            String drift1AsString = matcher.group("drift1");
            double drift1AsDouble = Double.valueOf(drift1AsString);
            long drift1AsMicroseconds = DateTimeFields.doubleToMicroseconds(drift1AsDouble);

            String drift2AsString = matcher.group("drift2");
            double drift2AsDouble = Double.valueOf(drift2AsString);
            long drift2AsMicroseconds = DateTimeFields.doubleToMicroseconds(drift2AsDouble);

            LocalDate leapSecondDate = LocalDate.of(Integer.valueOf(yearAsString), Integer.valueOf(monthAsString),
                    Integer.valueOf(dayAsString));

            return RowImpl.builder()
                    .leapSecondDate(leapSecondDate)
                    .leapSecondsAsDouble(leapSecondsAsDouble)
                    .leapSecondsAsMicros(leapSecondsAsMicroseconds)
                    .drift1AsDouble(drift1AsDouble)
                    .drift1AsMicros(drift1AsMicroseconds)
                    .drift2AsDouble(drift2AsDouble)
                    .drift2AsMicros(drift2AsMicroseconds)
                    .build();

        }

        /**
         * Leap second date.
         *
         * @return the local date
         */
        LocalDate leapSecondDate();

        /**
         * Year.
         *
         * @return the int
         */
        default int year() {
            return leapSecondDate().getYear();
        }

        /**
         * Month.
         *
         * @return the month
         */
        default Month month() {
            return leapSecondDate().getMonth();
        }

        /**
         * Month value.
         *
         * @return the int
         */
        default int monthValue() {
            return leapSecondDate().getMonthValue();
        }

        /**
         * Day of month.
         *
         * @return the int
         */
        default int dayOfMonth() {
            return leapSecondDate().getDayOfMonth();
        }

        /**
         * Leap seconds as double.
         *
         * @return the double
         */
        double leapSecondsAsDouble();

        /**
         * Leap seconds as micros.
         *
         * @return the long
         */
        long leapSecondsAsMicros();

        /**
         * Leap seconds as millis.
         *
         * @return the long
         */
        default long leapSecondsAsMillis() {
            return TimeUnit.MICROSECONDS.toMillis(leapSecondsAsMicros());
        }

        /**
         * Drift 1 as double.
         *
         * @return the double
         */
        double drift1AsDouble();

        /**
         * Drift 1 as micros.
         *
         * @return the long
         */
        long drift1AsMicros();

        /**
         * Drift 1 as millis.
         *
         * @return the long
         */
        default long drift1AsMillis() {
            return TimeUnit.MICROSECONDS.toMillis(drift1AsMicros());
        }

        /**
         * Drift 2 as double.
         *
         * @return the double
         */
        double drift2AsDouble();

        /**
         * Drift 2 as micros.
         *
         * @return the long
         */
        long drift2AsMicros();

        /**
         * Drift 2 as millis.
         *
         * @return the long
         */
        default long drift2AsMillis() {
            return TimeUnit.MICROSECONDS.toMillis(drift2AsMicros());
        }

        /**
         * Compare to.
         *
         * @param that the that
         * 
         * @return the int
         */
        @Override
        default int compareTo(LeapSecondTable.Row that) {
            return COMPARATOR.compare(this, that);
        }
    }

    /**
     * The Class RowImpl.
     */
    @Value
    @Accessors(fluent = true)
    @SuperBuilder(toBuilder = true)
    public static class RowImpl implements LeapSecondTable.Row {

        LocalDate leapSecondDate;

        double leapSecondsAsDouble;

        long leapSecondsAsMicros;

        double drift1AsDouble;

        long drift1AsMicros;

        double drift2AsDouble;

        long drift2AsMicros;
    }
}