package gov.nasa.gsfc.spdf.cdfj;

/**
 * Time Variable.
 */
public interface TimeVariable {

    /**
     * Returns whether the given {@link TimePrecision precision} is available
     * for this variable.
     *
     * @param tp
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
     * @return
     */
    double getFirstMilliSecond();

    /**
     * Returns name of the variable.
     *
     * @return
     */
    String getName();

    /**
     * Returns {@link TimePrecision time precision} of the variable.
     *
     * @return
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
     * @return
     *
     * @throws java.lang.Throwable
     */
    int[] getRecordRange(int[] startTime, int[] stopTime) throws Throwable;
    // public int[] getRecordRange(double[] timeRange) throws Throwable;

    /**
     * Returns relative times using the default
     * {@link TimeInstantModel time instant model}.
     *
     * @return
     */
    double[] getTimes();

    /**
     * Returns relative times for the specified record range using the default
     * {@link TimeInstantModel time instant model}.
     *
     * @param recordRange
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    double[] getTimes(int[] recordRange) throws Throwable;

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
     * @return
     *
     * @throws java.lang.Throwable
     */
    double[] getTimes(int[] startTime, int[] stopTime) throws Throwable;

    /**
     * Returns relative times for the specified time range using the given
     * {@link TimeInstantModel time instant model}.
     * <p>
     *
     * @param startTime a 3 to 7 element int[], containing year, month,
     *                  day,hour, minute, second and millisecond.
     * @param stopTime  a 3 to 7 element int[], containing year, month,
     *                  day,hour, minute, second and millisecond.
     * @param tspec
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    double[] getTimes(int[] startTime, int[] stopTime, TimeInstantModel tspec) throws Throwable;

    /**
     * Returns relative times for the specified record range using the specified
     * {@link TimeInstantModel time instant model}.
     *
     * @param recordRange
     * @param tspec
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    double[] getTimes(int[] recordRange, TimeInstantModel tspec) throws Throwable;

    /**
     * Returns relative times using the specified
     * {@link TimeInstantModel time instant model}.
     *
     * @param tspec
     *
     * @return
     *
     * @throws java.lang.Throwable
     */
    double[] getTimes(TimeInstantModel tspec) throws Throwable;

    /**
     * Returns whether this is a TT2000 type variable
     *
     * @return
     */
    boolean isTT2000();
}
