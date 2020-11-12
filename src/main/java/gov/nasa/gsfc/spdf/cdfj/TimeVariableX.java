package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;

/**
 * Time Variable.
 */
public interface TimeVariableX extends TimeVariable {

    /**
     * Gets the precision.
     *
     * @return the precision
     */
    @Override
    TimePrecision getPrecision();

    /**
     * Gets the raw buffer.
     *
     * @return the raw buffer
     */
    ByteBuffer getRawBuffer();

    /**
     * Gets the record range.
     *
     * @param timeRange the time range
     *
     * @return the record range
     * @
     */
    int[] getRecordRange(double[] timeRange);

    /**
     * Gets the record range.
     *
     * @param startTime the start time
     * @param stopTime  the stop time
     * @param ts        the ts
     *
     * @return the record range
     * @
     */
    int[] getRecordRange(int[] startTime, int[] stopTime, TimeInstantModel ts);

    /**
     * Returns relative times for the specified time range using the given
     * {@link TimeInstantModel time instant model}.
     * <p>
     *
     * @param timeRange relative time range
     * @param tspec     the tspec
     *
     * @return the times
     */
    double[] getTimes(double[] timeRange, TimeInstantModel tspec);
}
