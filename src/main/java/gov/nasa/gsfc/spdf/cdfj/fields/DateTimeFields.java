/**
 * Self.
 *
 * @return the b
 */
package gov.nasa.gsfc.spdf.cdfj.fields;

import java.math.*;
import java.nio.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.stream.*;

import lombok.experimental.*;

/**
 * The Class DateTimeFields.
 */
@UtilityClass
public class DateTimeFields {

    private static final OffsetDateTime OFFSET_DATE_TIME_JAVA_EPOCH_UTC = Instant.EPOCH.atOffset(ZoneOffset.UTC);

    /** The Constant JAN_1_0000. */
    public static final OffsetDateTime JAN_1_0000 = OffsetDateTime.of(0, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);

    /** The Constant J2000. */
    public static final OffsetDateTime J2000 = OffsetDateTime.of(2000, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

    /** The Constant JAN_1_0000_AS_INSTANT. */
    public static final Instant JAN_1_0000_AS_INSTANT = JAN_1_0000.toInstant();

    /** The Constant MILLIS_FROM_CDF_EPOCH_TO_JAVA_EPOCH. */
    public static final long MILLIS_FROM_CDF_EPOCH_TO_JAVA_EPOCH = JAN_1_0000.until(OFFSET_DATE_TIME_JAVA_EPOCH_UTC,
            ChronoUnit.MILLIS);

    /** The Constant MILLIS_FROM_CDF_TIME_TT2000_TO_JAVA_EPOCH. */
    public static final long MILLIS_FROM_CDF_TIME_TT2000_TO_JAVA_EPOCH = JAN_1_0000
            .until(OFFSET_DATE_TIME_JAVA_EPOCH_UTC, ChronoUnit.MILLIS);

    public static final double DIFF_TIA_AND_J2000_SECONDS_AS_DOUBLE = 32.1840;

    public static final long DIFF_TIA_AND_J2000_MILLISECONDS = doubleToMilliseconds(
            DIFF_TIA_AND_J2000_SECONDS_AS_DOUBLE);

    /**
     * Double to microseconds.
     *
     * @param leapSecondsAsDouble the leap seconds as double
     * 
     * @return the long
     */
    public static long doubleToMicroseconds(double leapSecondsAsDouble) {

        return doubleToFactionalSeconds(leapSecondsAsDouble, 1_000_000);

    }

    /**
     * Double to milliseconds.
     *
     * @param leapSecondsAsDouble the leap seconds as double
     * 
     * @return the long
     */
    public static long doubleToMilliseconds(double leapSecondsAsDouble) {

        return doubleToFactionalSeconds(leapSecondsAsDouble, 1_000);

    }

    static long doubleToFactionalSeconds(double leapSecondsAsDouble, int numberInASecond) {

        return BigDecimal.valueOf(leapSecondsAsDouble)
                .multiply(BigDecimal.valueOf(numberInASecond))
                .longValue();

    }

    /**
     * As instant.
     *
     * @param cdfEpoch the cdf epoch
     * 
     * @return the instant
     */
    public static Instant asInstant(double cdfEpoch) {
        return asInstant((long) cdfEpoch);
    }

    /**
     * As instant.
     *
     * @param cdfEpoch the cdf epoch
     * 
     * @return the instant
     */
    public static Instant asInstant(long cdfEpoch) {
        return Instant.ofEpochMilli(cdfEpoch - MILLIS_FROM_CDF_EPOCH_TO_JAVA_EPOCH);
    }

    /**
     * To double array.
     *
     * @param bytes            the bytes
     * @param numberOfElements the number of elements
     * 
     * @return the double[]
     */
    public static List<Instant> toInstantCDFEpoch(byte[] bytes, int numberOfElements) {

        return CDFDataTypes.toDoubleStream(bytes, numberOfElements)
                .mapToObj(BigDecimal::valueOf)
                .filter(d -> !CDFDataTypes.DOUBLE_FILL_AS_BIG_DECIMAL.equals(d))
                .map(BigDecimal::doubleValue)
                .map(DateTimeFields::asInstant)
                .collect(Collectors.toList());

    }

    /**
     * To double array.
     *
     * @param bytes            the bytes
     * @param numberOfElements the number of elements
     * 
     * @return the double[]
     */
    // FIXME: Complete
    public static List<Instant> toInstantFromCDFEpoch16(byte[] bytes, int numberOfElements) {

        ByteBuffer wrap = ByteBuffer.wrap(bytes);
        List<Instant> instants = new ArrayList<>();

        while (wrap.hasRemaining()) {
            long first = (long) wrap.getDouble();
            long second = (long) wrap.getDouble();

        }

        return instants;

    }
}
