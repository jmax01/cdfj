package gov.nasa.gsfc.spdf.cdfj;

/**
 * Time Variable.
 */
public interface TimeVariable {

    /**
     * Returns whether the given {@link TimePrecision precision} is available
     * for this variable.
     *
     * @param tp the tp
     *
     * @return false if the required precision is finer than this variable's
     *         resolution.
     *         Thus, for a variable of type EPOCH, this method will return false for
     *         microsecond, or finer resolution.
     */
    boolean canSupportPrecision(TimePrecision tp);

    /**
     * Returns the millisecond offset of the first record using Epoch 0
     * as the base time
     * <p>
     * This number may be useful as a base time for a time instant model
     * when looking at high time resolution data.
     *
     * @return the first milli second
     */
    double getFirstMilliSecond();

    /**
     * Returns name of the variable.
     *
     * @return the name
     */
    String getName();

    /**
     * Returns {@link TimePrecision time precision} of the variable.
     *
     * @return the precision
     */
    TimePrecision getPrecision();

    /**
     * Returns range of records which fall within the specified range
     * of times relative to the base of the given
     * {@link TimeInstantModel time instant model}.
     *
     * @param startTime a 3 to 7 element int[], containing year, month,
     *                  day,hour, minute, second and millisecond.
     * @param stopTime  a 3 to 7 element int[], containing year, month,
     *                  day,hour, minute, second and millisecond.
     *
     * @return the record range
     */
    int[] getRecordRange(int[] startTime, int[] stopTime);
    // public int[] getRecordRange(double[] timeRange) ;

    /**
     * Returns relative times using the default
     * {@link TimeInstantModel time instant model}.
     *
     * @return the times
     */
    double[] getTimes();

    /**
     * Returns relative times for the specified record range using the default
     * {@link TimeInstantModel time instant model}.
     *
     * @param recordRange the record range
     *
     * @return the times
     */
    double[] getTimes(int[] recordRange);

    /**
     * Returns relative times for the specified time range using the default
     * {@link TimeInstantModel time instant model}.
     * <p>
     *
     * @param startTime a 3 to 7 element int[], containing year, month,
     *                  day,hour, minute, second and millisecond.
     * @param stopTime  a 3 to 7 element int[], containing year, month,
     *                  day,hour, minute, second and millisecond.
     *
     * @return the times
     */
    double[] getTimes(int[] startTime, int[] stopTime);

    /**
     * Returns relative times for the specified time range using the given
     * {@link TimeInstantModel time instant model}.
     * <p>
     *
     * @param startTime a 3 to 7 element int[], containing year, month,
     *                  day,hour, minute, second and millisecond.
     * @param stopTime  a 3 to 7 element int[], containing year, month,
     *                  day,hour, minute, second and millisecond.
     * @param tspec     the tspec
     *
     * @return the times
     */
    double[] getTimes(int[] startTime, int[] stopTime, TimeInstantModel tspec);

    /**
     * Returns relative times for the specified record range using the specified
     * {@link TimeInstantModel time instant model}.
     *
     * @param recordRange the record range
     * @param tspec       the tspec
     *
     * @return the times
     */
    double[] getTimes(int[] recordRange, TimeInstantModel tspec);

    /**
     * Returns relative times using the specified
     * {@link TimeInstantModel time instant model}.
     *
     * @param tspec the tspec
     *
     * @return the times
     */
    double[] getTimes(TimeInstantModel tspec);

    /**
     * Returns whether this is a TT2000 type variable.
     *
     * @return true, if is tt2000
     */
    boolean isTT2000();
}
