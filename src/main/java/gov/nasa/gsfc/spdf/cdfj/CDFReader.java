package gov.nasa.gsfc.spdf.cdfj;

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

import gov.nasa.gsfc.spdf.cdfj.CDFException.ReaderError;
import lombok.extern.java.Log;

/**
 * CDFReader extends GenericReader with access methods for time series
 * variables. Time series methods of this class do not require a detailed
 * knowledge of the internal structure of CDF.
 */
@Log
public class CDFReader extends GenericReader implements Closeable {

    Scalar scalar;

    final CDFVec cdfVec = new CDFVec();

    /**
     * Constructs a reader for the given CDF file.
     *
     * @param cdfFile the cdf file
     *
     * @throws ReaderError the reader error
     */
    public CDFReader(final String cdfFile) throws CDFException.ReaderError {
        super(cdfFile);
        this.scalar = new Scalar();
        this.scalar.rdr = this;
        this.cdfVec.rdr = this;
    }

    /**
     * Constructs a reader for the given URL for CDF file.
     *
     * @param url the url
     *
     * @throws ReaderError the reader error
     */
    public CDFReader(final URL url) throws CDFException.ReaderError {
        super(url);
        this.scalar = new Scalar();
        this.scalar.rdr = this;
        this.cdfVec.rdr = this;
    }

    /**
     * Instantiates a new CDF reader.
     *
     * @param cdfImpl the cdf impl
     */
    public CDFReader(final CDFImpl cdfImpl) {
        super(cdfImpl);
    }

    /**
     * Returns default {@link TimeInstantModel time instant model}.
     * <p>
     * The base of default TimeInstantModel is January 1,1970 0:0:0
     *
     * @return the time instant model
     */
    public static TimeInstantModel timeModelInstance() {
        return TimeVariableFactory.getDefaultTimeInstantModel();
    }

    /**
     * Returns {@link TimeInstantModel time instant model} that uses
     * given units for the offset..
     * <p>
     * The base of default TimeInstantModel is January 1,1970 0:0:0
     *
     * @param offsetUnits the offset units
     *
     * @return the time instant model
     */
    public static TimeInstantModel timeModelInstance(final String offsetUnits) {
        TimeInstantModel tim = TimeVariableFactory.getDefaultTimeInstantModel();
        tim.setOffsetUnits(TimePrecision.getPrecision(offsetUnits));
        return tim;
    }

    /**
     * Returns first available time for a variable.Returned time has millisecond
     * precision.
     *
     * @param variableName the variable name
     *
     * @return int[7] containing year, month, day, hour, minute, second and
     *         millisecond, or null.
     *
     * @throws ReaderError the reader error
     */
    public int[] firstAvailableTime(final String variableName) throws CDFException.ReaderError {
        return firstAvailableTime(variableName, null);
    }

    /**
     * Returns first available time which is not before the given time for a
     * variable.Returned time has millisecond precision.
     *
     * @param variableName the variable name
     * @param start        a 3 to 7 element int[], containing year,
     *                     month (January is 1), day, hour, minute, second and
     *                     millisecond.
     *
     * @return int[7] containing year, month, day, hour, minute, second and
     *         millisecond, or null.
     *
     * @throws ReaderError the reader error
     */
    public int[] firstAvailableTime(final String variableName, final int[] start) throws CDFException.ReaderError {

        try {
            TimeVariable tv = TimeVariableFactory.getTimeVariable(this, variableName);
            double[] times = tv.getTimes();
            double[] trange = { times[0], times[times.length - 1] };
            double[] tr;

            try {
                tr = TSExtractor.getOverlap(this, trange, variableName, start, null);
            } catch (RuntimeException ex) {
                LOGGER.log(Level.SEVERE, ex,
                        () -> String.format("firstAvailableTime failed for variableName: %s start[]: %s", variableName,
                                Arrays.toString(start)));
                return null;
            }

            if (tr[0] != Double.MIN_VALUE) {
                Calendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                c.setTimeInMillis((long) tr[0]);

                if (tv.isTT2000()) {
                    long l0 = c.getTime()
                            .getTime();
                    long l = (long) TimeUtil.getOffset(l0);
                    c.setTimeInMillis(((long) tr[0] - l) + l0);
                }

                return GMT(c);
            }

            return null;
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError(th);
        }

    }

    /**
     * Returns available time range using default
     * {@link TimeInstantModel time instant model}.
     *
     * @param variableName variable name
     *
     * @return double[2] 0th element is the first available offset time;
     *         1st element is the last available offset time;
     *
     */
    public double[] getAvailableTimeRange(final String variableName) {

        TimeVariable tv = TimeVariableFactory.getTimeVariable(this, variableName);
        double[] times = tv.getTimes();
        return new double[] { times[0], times[times.length - 1] };

    }

    /**
     * Returns the time series of the specified component of a 1 dimensional
     * variable, ignoring points whose value equals fill value.
     * <p>
     * A double[2][] array is returned. The 0th element is the
     * array containing times, and the 1st element is the array containing
     * corresponding values. If a fill value has been specified for this
     * variable via the FILLVAL attribute, then points where the value is
     * equal to fill value are excluded.
     * </p>
     *
     * @param variableName the variable name
     * @param component    the component
     *
     * @return the copy on write array list time series
     *
     * @throws ReaderError the reader error
     */
    public double[][] getVectorTimeSeries(final String variableName, final int component)
            throws CDFException.ReaderError {

        try {
            return this.cdfVec.getTimeSeries(variableName, component);
        } catch (RuntimeException e) {
            throw new CDFException.ReaderError("getVectorTimeSeries failed", e);
        }

    }

    /**
     * Returns the time series of the specified component of a 1 dimensional
     * variable, optionally ignoring points whose value equals fill value.
     * <p>
     * A double[2][] array is returned. The 0th element is the
     * array containing times, and the 1st element is the array containing
     * corresponding values. If a fill value has been specified for this
     * variable via the FILLVAL attribute, then points where the value is
     * equal to fill value are excluded if ignoreFill = true.
     * </p>
     *
     * @param variableName the variable name
     * @param component    the component
     * @param ignoreFill   the ignore fill
     *
     * @return the copy on write array list time series
     *
     * @throws ReaderError the reader error
     */
    public double[][] getVectorTimeSeries(final String variableName, final int component, final boolean ignoreFill)
            throws CDFException.ReaderError {
        return this.cdfVec.getTimeSeries(variableName, component, ignoreFill);
    }

    /**
     * Returns the time series of the specified component of 1 dimensional
     * variable in the specified time range, optionally ignoring points whose
     * value equals fill value.
     * <p>
     * A double[2][] array is returned. The 0th element is the
     * array containing times, and the 1st element is the array containing
     * corresponding values. If a fill value has been specified for this
     * variable via the FILLVAL attribute, then points where the value is
     * equal to fill value are excluded if ignoreFill = true.
     * </p>
     *
     * @param variableName the variable name
     * @param component    the component
     * @param ignoreFill   the ignore fill
     * @param startTime    a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond.
     * @param stopTime     a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the stop time is assumed to be later than the last
     *                     available time.
     *
     * @return the copy on write array list time series
     *
     * @throws ReaderError the reader error
     */
    public double[][] getVectorTimeSeries(final String variableName, final int component, final boolean ignoreFill,
            final int[] startTime, final int[] stopTime) throws CDFException.ReaderError {

        try {
            return this.cdfVec.getTimeSeries(variableName, component, ignoreFill, startTime, stopTime);
        } catch (RuntimeException e) {
            throw new CDFException.ReaderError("getVectorTimeSeries failed for " + variableName, e);
        }

    }

    /**
     * Returns the {@link TimeSeries time series} of the specified component
     * of 1 dimensional variable in the specified time range using the given
     * {@link TimeInstantModel time instant model}, optionally ignoring points
     * whose
     * value
     * equals fill value.
     * <p>
     * If a fill value has been specified for this variable via the FILLVAL
     * attribute, then if ignoreFill has the value true, points where the
     * value is equal to fill value are excluded if ignoreFill = true.
     * </p>
     *
     * @param variableName variable name
     * @param component    the component
     * @param ignoreFill   the ignore fill
     * @param startTime    a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the first available time is used.
     * @param stopTime     a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the stop time is assumed to be later than the last
     *                     available time.
     * @param tspec        {@link TimeInstantModel time instant model}, May be
     *                     null, in which case the default model is used.
     *
     * @return the copy on write array list time series
     *
     * @throws ReaderError the reader error
     */
    public TimeSeries getVectorTimeSeries(final String variableName, final int component, final boolean ignoreFill,
            final int[] startTime, final int[] stopTime, final TimeInstantModel tspec) throws CDFException.ReaderError {

        try {
            return this.cdfVec.getTimeSeries(variableName, component, ignoreFill, startTime, stopTime, tspec);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError("getVectorTimeSeries failed for " + variableName, th);
        }

    }

    /**
     * Returns the time series of the specified component of 1 dimensional
     * variable in the specified time range, ignoring points whose
     * value equals fill value.
     * <p>
     * A double[2][] array is returned. The 0th element is the
     * array containing times, and the 1st element is the array containing
     * corresponding values. If a fill value has been specified for this
     * variable via the FILLVAL attribute, then points where the value is
     * equal to fill value are excluded.
     * </p>
     *
     * @param variableName the variable name
     * @param component    the component
     * @param startTime    a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the first available time is used.
     * @param stopTime     a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the stop time is assumed to be later than the last
     *                     available
     *                     time.
     *
     * @return the copy on write array list time series
     *
     * @throws ReaderError the reader error
     */
    public double[][] getVectorTimeSeries(final String variableName, final int component, final int[] startTime,
            final int[] stopTime) throws CDFException.ReaderError {

        try {
            return this.cdfVec.getTimeSeries(variableName, component, startTime, stopTime);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError("getVectorTimeSeries failed for " + variableName, th);
        }

    }

    /**
     * Returns the time series as a {@link TimeSeries TimeSeries}, of the
     * specified component of a CopyOnWriteArrayList variable in the specified time
     * range
     * using the given {@link TimeInstantModel time instant model}, ignoring
     * points
     * whose value
     * equals fill value.
     * <p>
     * If a fill value has been specified for this variable via the FILLVAL
     * attribute, then points where the value is equal to fill value are
     * excluded.
     * </p>
     *
     * @param variableName variable name
     * @param component    index of the CopyOnWriteArrayList component
     * @param startTime    a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the first available time is used.
     * @param stopTime     a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the stop time is assumed to be later than the last
     *                     available
     *                     time.
     * @param tspec        {@link TimeInstantModel time instant model}, May be
     *                     null, in which case the default model is used.
     *
     * @return the copy on write array list time series
     *
     * @throws ReaderError the reader error
     */
    public TimeSeries getVectorTimeSeries(final String variableName, final int component, final int[] startTime,
            final int[] stopTime, final TimeInstantModel tspec) throws CDFException.ReaderError {

        try {
            return this.cdfVec.getTimeSeries(variableName, component, startTime, stopTime, tspec);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError("getVectorTimeSeries failed for " + variableName, th);
        }

    }

    /**
     * Returns names of variables that the specified variable depends on.
     *
     * @param variableName the variable name
     *
     * @return String[]
     */
    public String[] getDependent(final String variableName) {

        String[] variableAttributeNames = this.thisCDF.variableAttributeNames(variableName);

        if (variableAttributeNames == null) {
            return new String[0];
        }

        return Arrays.stream(variableAttributeNames)
                .filter(variableAttributeName -> variableAttributeName.startsWith("DEPEND_"))
                .map(variableAttributeName -> this.thisCDF.getAttribute(variableName, variableAttributeName))
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .filter(l -> !l.isEmpty())
                .map(attributes -> attributes.get(0))
                .filter(String.class::isInstance)
                .map(String.class::cast)
                .toArray(String[]::new);

    }

    /**
     * Returns the name of the specified index of a multi-dimensional
     * variable.
     *
     * @param variableName variable name
     * @param index        index whose name is required
     *
     * @return String
     *
     * @throws ReaderError the reader error
     */
    public String getIndexName(final String variableName, final int index) throws CDFException.ReaderError {

        int[] dim = getDimensions(variableName);

        if (dim.length == 0) {
            return null;
        }

        if (index >= dim.length) {
            return null;
        }

        @SuppressWarnings("unchecked")
        List<String> attr = (List<String>) getAttribute(variableName, "DEPEND_" + (1 + index));

        return attr.get(0);

    }

    /**
     * Returns the time series of the specified scalar variable, ignoring
     * points whose value equals fill value.
     * <p>
     * A double[2][] array is returned. The 0th element is the
     * array containing times, and the 1st element is the array containing
     * corresponding values. If a fill value has been specified for this
     * variable via the FILLVAL attribute, then points where the value is
     * equal to fill value are excluded.
     * </p>
     *
     * @param variableName the variable name
     *
     * @return the scalar time series
     *
     * @throws ReaderError the reader error
     */
    public double[][] getScalarTimeSeries(final String variableName) throws CDFException.ReaderError {

        try {
            return this.scalar.getTimeSeries(variableName);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError(th);
        }

    }

    /**
     * Returns the time series of the specified scalar variable, optionally
     * ignoring points whose value equals fill value.
     * <p>
     * A double[2][] array is returned. The 0th element is the
     * array containing times, and the 1st element is the array containing
     * corresponding values. If a fill value has been specified for this
     * variable via the FILLVAL attribute, then points where the value is
     * equal to fill value are excluded if ignoreFill = true.
     * </p>
     *
     * @param variableName the variable name
     * @param ignoreFill   the ignore fill
     *
     * @return the scalar time series
     *
     * @throws ReaderError the reader error
     */
    public double[][] getScalarTimeSeries(final String variableName, final boolean ignoreFill)
            throws CDFException.ReaderError {

        try {
            return this.scalar.getTimeSeries(variableName, ignoreFill);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError("getScalarTimeSeries failed for " + variableName, th);
        }

    }

    /**
     * Returns the time series of the specified scalar variable in the
     * specified time range, optionally ignoring points whose value equals
     * fill value.
     * <p>
     * A double[2][] array is returned. The 0th element is the
     * array containing times, and the 1st element is the array containing
     * corresponding values.
     * If a fill value has been specified for this variable via the FILLVAL
     * attribute, then if ignoreFill has the value true, points where the
     * value is equal to fill value are excluded if ignoreFill = true.
     * </p>
     *
     * @param variableName the variable name
     * @param ignoreFill   the ignore fill
     * @param startTime    a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the first available time is used.
     * @param stopTime     a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the stop time is assumed to be later than the last
     *                     available time.
     *
     * @return the scalar time series
     *
     * @throws ReaderError the reader error
     */
    public double[][] getScalarTimeSeries(final String variableName, final boolean ignoreFill, final int[] startTime,
            final int[] stopTime) throws CDFException.ReaderError {

        try {
            return this.scalar.getTimeSeries(variableName, ignoreFill, startTime, stopTime);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError("getScalarTimeSeries failed for " + variableName, th);
        }

    }

    /**
     * Returns the time series as a {@link TimeSeries TimeSeries} of the
     * specified scalar variable in the specified time range using the given
     * {@link TimeInstantModel time instant model}, optionally ignoring points
     * whose
     * value equals fill value.
     * <p>
     * If a fill value has been specified for this variable via the FILLVAL
     * attribute, then if ignoreFill has the value true, points where the
     * value is equal to fill value are excluded if ignoreFill = true.
     * </p>
     *
     * @param variableName the variable name
     * @param ignoreFill   the ignore fill
     * @param startTime    a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the first available time is used.
     * @param stopTime     a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the stop time is assumed to be later than the last
     *                     available time.
     * @param tspec        {@link TimeInstantModel time instant model}, May be
     *                     null, in which case the default model is used.
     *
     * @return the scalar time series
     *
     * @throws ReaderError the reader error
     */
    public TimeSeries getScalarTimeSeries(final String variableName, final boolean ignoreFill, final int[] startTime,
            final int[] stopTime, final TimeInstantModel tspec) throws CDFException.ReaderError {

        try {
            return this.scalar.getTimeSeries(variableName, ignoreFill, startTime, stopTime, tspec);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError("getScalarTimeSeries failed", th);
        }

    }

    /**
     * Returns the time series of the specified scalar variable in the
     * specified time range, ignoring points whose value equals
     * fill value.
     * <p>
     * A double[2][] array is returned. The 0th element is the
     * array containing times, and the 1st element is the array containing
     * corresponding values.
     * If a fill value has been specified for this variable via the FILLVAL
     * attribute, then points where the
     * value is equal to fill value are excluded.
     * </p>
     * For numeric variables of dimension other than 0, and for
     * character string variables an exception is thrown.
     *
     * @param variableName the variable name
     * @param startTime    a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the first available time is used.
     * @param stopTime     a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the stop time is assumed to be later than the last
     *                     available
     *                     time.
     *
     * @return the scalar time series
     *
     * @throws ReaderError the reader error
     */
    public double[][] getScalarTimeSeries(final String variableName, final int[] startTime, final int[] stopTime)
            throws CDFException.ReaderError {

        try {
            return this.scalar.getTimeSeries(variableName, startTime, stopTime);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError("getScalarTimeSeries failed for " + variableName, th);
        }

    }

    /**
     * Returns the time series as a {@link TimeSeries TimeSeries}, of the
     * specified scalar variable in the specified time range, using the given
     * {@link TimeInstantModel time instant model}, ignoring points whose
     * value equals fill value.
     * <p>
     * If a fill value has been specified for this variable via the FILLVAL
     * attribute, then points where the value is equal to fill value are
     * excluded.
     * </p>
     *
     * @param variableName the variable name
     * @param startTime    a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the first available time is used.
     * @param stopTime     a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the stop time is assumed to be later than the last
     *                     available
     *                     time.
     * @param tspec        {@link TimeInstantModel time instant model}, May be
     *                     null, in which case the default model is used.
     *
     * @return the scalar time series
     *
     * @throws ReaderError the reader error
     */
    public TimeSeries getScalarTimeSeries(final String variableName, final int[] startTime, final int[] stopTime,
            final TimeInstantModel tspec) throws CDFException.ReaderError {

        try {
            return this.scalar.getTimeSeries(variableName, startTime, stopTime, tspec);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError("getScalarTimeSeries failed for " + variableName, th);
        }

    }

    /**
     * Returns {@link TimeSeries TimeSeries} of the specified variable
     * using the default {@link TimeInstantModel time instant model}.
     *
     * @param variableName the variable name
     *
     * @return the time series
     *
     * @throws ReaderError the reader error
     */
    public TimeSeries getTimeSeries(final String variableName) throws CDFException.ReaderError {
        return getTimeSeries(variableName, null, timeModelInstance());
    }

    /**
     * Returns {@link TimeSeries TimeSeries} of the specified variable
     * in the specified time range using the default
     * {@link TimeInstantModel time instant model}.
     *
     * @param variableName variable name
     * @param startTime    a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the first available time is used.
     * @param stopTime     a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the stop time is assumed to be later than the last
     *                     available
     *                     time.
     *
     * @return the time series
     *
     * @throws ReaderError the reader error
     */
    public TimeSeries getTimeSeries(final String variableName, final int[] startTime, final int[] stopTime)
            throws CDFException.ReaderError {
        return getTimeSeries(variableName, startTime, stopTime, null);
    }

    /**
     * Returns {@link TimeSeries TimeSeries} of the specified variable in
     * the specified time range using the given
     * {@link TimeInstantModel time instant model}.
     *
     * @param variableName variable name
     * @param startTime    a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the first available time is used.
     * @param stopTime     a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the stop time is assumed to be later than the last
     *                     available
     *                     time.
     * @param tspec        {@link TimeInstantModel time instant model}, May be
     *                     null, in which case the default model is used.
     *
     * @return {@link TimeSeries time series}
     *
     * @throws ReaderError the reader error
     */
    public TimeSeries getTimeSeries(final String variableName, final int[] startTime, final int[] stopTime,
            final TimeInstantModel tspec) throws CDFException.ReaderError {

        try {
            TimeVariable tv = TimeVariableFactory.getTimeVariable(this, variableName);
            TimeInstantModel _tspec = tspec;

            if (_tspec == null) {
                _tspec = timeModelInstance();
            }

            if (!tv.canSupportPrecision(_tspec.getOffsetUnits())) {
                throw new CDFException.ReaderError(variableName + " has lower time precision than requested.");
            }

            double[] trange = getAvailableTimeRange(variableName);
            double[] tr = TSExtractor.getOverlap(this, trange, variableName, startTime, stopTime);
            return getTimeSeries(variableName, tr, tspec);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError("getTimeSeries failed for " + variableName, th);
        }

    }

    /**
     * Returns {@link TimeSeries TimeSeries} of the specified variable
     * using the specified {@link TimeInstantModel time instant model}.
     *
     * @param variableName variable name
     * @param tspec        {@link TimeInstantModel time instant model}, May be
     *                     null, in which case the default model is used.
     *
     * @return the time series
     *
     * @throws ReaderError the reader error
     */
    public TimeSeries getTimeSeries(final String variableName, final TimeInstantModel tspec)
            throws CDFException.ReaderError {
        TimeInstantModel _tspec = (tspec == null) ? timeModelInstance() : tspec;
        return getTimeSeries(variableName, null, _tspec);
    }

    /**
     * Returns {@link TimeSeriesOneD time series} of the specified variable
     * in the specified time range using the given
     * {@link TimeInstantModel time instant model}.
     *
     * @param variableName variable name
     * @param startTime    a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the first available time is used.
     * @param stopTime     a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond. May be null, in
     *                     which case
     *                     the stop time is assumed to be later than the last
     *                     available time.
     * @param tspec        {@link TimeInstantModel time instant model}, May be
     *                     null, in which case the default model is used.
     * @param columnMajor  specifies whether the first index of the
     *                     variable dimension varies the fastest, i.e. IDL like.
     *
     * @return {@link TimeSeriesOneD time series}
     *
     * @throws ReaderError the reader error
     */
    public TimeSeriesOneD getTimeSeriesOneD(final String variableName, final int[] startTime, final int[] stopTime,
            final TimeInstantModel tspec, final boolean columnMajor) throws CDFException.ReaderError {

        try {
            TimeVariable tv = TimeVariableFactory.getTimeVariable(this, variableName);
            TimeInstantModel _tspec = tspec;

            if (_tspec == null) {
                _tspec = timeModelInstance();
            }

            if (!tv.canSupportPrecision(_tspec.getOffsetUnits())) {

                throw new CDFException.ReaderError(variableName + " has lower time precision than requested.");
            }

            double[] trange = getAvailableTimeRange(variableName);

            double[] tr = TSExtractor.getOverlap(this, trange, variableName, startTime, stopTime);

            return getTimeSeries(variableName, tr, _tspec, columnMajor);

        } catch (IllegalArgumentException e) {

            throw new CDFException.ReaderError("getTimeSeriesOneD failed", e);
        }

    }

    /**
     * Returns last available time for a variable.Returned time has millisecond
     * precision.
     *
     * @param variableName the variable name
     *
     * @return int[7] containing year, month, day, hour, minute, second and
     *         millisecond, or null.
     *
     * @throws ReaderError the reader error
     */
    public int[] lastAvailableTime(final String variableName) throws CDFException.ReaderError {
        return lastAvailableTime(variableName, null);
    }

    /**
     * Returns last available time which is not later than the given time
     * for a variable.Returned time has millisecond precision.
     *
     * @param variableName the variable name
     * @param stop         a 3 to 7 element int[], containing year,
     *                     month (January is 1), day, hour, minute, second and
     *                     millisecond.
     *
     * @return int[7] containing year, month, day, hour, minute, second and
     *         millisecond, or null.
     *
     * @throws ReaderError the reader error
     */
    public int[] lastAvailableTime(final String variableName, final int[] stop) throws CDFException.ReaderError {

        try {
            TimeVariable tv = TimeVariableFactory.getTimeVariable(this, variableName);
            double[] times = tv.getTimes();
            double[] trange = { times[0], times[times.length - 1] };
            double[] tr = TSExtractor.getOverlap(this, trange, variableName, null, stop);

            if (tr[1] != Double.MAX_VALUE) {
                Calendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
                c.setTimeInMillis((long) tr[1]);

                if (tv.isTT2000()) {
                    long l0 = c.getTime()
                            .getTime();
                    long l = (long) TimeUtil.getOffset(l0);
                    c.setTimeInMillis(((long) tr[1] - l) + l0);
                }

                return GMT(c);
            }

            return null;
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError("lastAvailableTime failed for " + variableName, th);
        }

    }

    /**
     * Returns {@link TimeInstantModel time instant model} with specified base
     * time and default offset units (millisecond) for a variable.
     *
     * @param variableName variable name
     * @param baseTime     a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond.
     *
     * @return the time instant model
     *
     * @throws ReaderError the reader error
     */
    public TimeInstantModel timeModelInstance(final String variableName, final int[] baseTime)
            throws CDFException.ReaderError {

        if (baseTime.length < 3) {
            throw new CDFException.ReaderError("incomplete base time definition.");
        }

        try {
            boolean isTT2000 = TimeVariableFactory.getTimeVariable(this, variableName)
                    .isTT2000();
            long l = TSExtractor.getTime(baseTime);
            double msec = (isTT2000) ? TimeUtil.milliSecondSince1970(l) : l;
            msec += TimeVariableFactory.JANUARY_1_1970_LONG;
            return getTimeInstantModel(msec);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError("timeModelInstance failed for " + variableName, th);
        }

    }

    /**
     * Returns {@link TimeInstantModel time instant model} with specified base
     * time and specified offset units (millisecond) for a variable.
     *
     * @param variableName variable name
     * @param baseTime     a 3 to 7 element int[], containing year,
     *                     month (January is 1),
     *                     day,hour, minute, second and millisecond.
     * @param offsetUnits  the offset units
     *
     * @return the time instant model
     *
     * @throws ReaderError the reader error
     */
    public TimeInstantModel timeModelInstance(final String variableName, final int[] baseTime,
            final TimePrecision offsetUnits) throws CDFException.ReaderError {
        TimeInstantModel model = timeModelInstance(variableName, baseTime);
        model.setOffsetUnits(offsetUnits);
        return model;
    }

    int[] GMT(final Calendar c) {
        return new int[] { c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), c.get(Calendar.SECOND),
                c.get(Calendar.MILLISECOND) };
    }

    boolean overlaps(final double[] t) {
        return (t[0] != Double.MIN_VALUE) && (t[0] != Double.MAX_VALUE);
    }

    private TimeInstantModel getTimeInstantModel(final double msec) {
        return TimeVariableFactory.getDefaultTimeInstantModel(msec);
    }

    private TimeSeries getTimeSeries(final String variableName, final double[] timeRange, final TimeInstantModel tspec)
            throws CDFException.ReaderError {
        Variable variable = this.thisCDF.getVariable(variableName);

        try {
            TimeSeriesX ts = new TSExtractor.GeneralTimeSeriesX(this, variable, false, timeRange, tspec, false, true);
            return new TimeSeriesImpl(ts);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError("getTimeSeries failed for " + variableName, th);
        }

    }

    private TimeSeriesOneD getTimeSeries(final String variableName, final double[] timeRange,
            final TimeInstantModel tspec, final boolean columnMajor) throws CDFException.ReaderError {
        Variable variable = this.thisCDF.getVariable(variableName);

        try {
            TimeSeriesX ts = new TSExtractor.GeneralTimeSeriesX(this, variable, false, timeRange, tspec, true,
                    columnMajor);
            return new TimeSeriesOneDImpl(ts);
        } catch (RuntimeException th) {
            throw new CDFException.ReaderError("getTimeSeries failed for " + variableName, th);
        }

    }

    class CDFVec {

        MetaData rdr;

        public double[][] getTimeSeries(final String variableName, final int component) throws ReaderError {
            Variable variable = CDFReader.this.thisCDF.getVariable(variableName);

            if (variable.getEffectiveRank() != 1) {
                throw new IllegalArgumentException(variableName + " is not a CopyOnWriteArrayList.");
            }

            int dim = variable.getEffectiveDimensions()[0];

            if ((component < 0) || (component > dim)) {
                throw new IllegalArgumentException("component exceeds dimension of " + variableName + " (" + dim + ')');
            }

            return _getTimeSeries(variableName, component, true, null);
        }

        public double[][] getTimeSeries(final String variableName, final int component, final boolean ignoreFill)
                throws ReaderError {

            if (CDFReader.this.thisCDF.getVariable(variableName)
                    .getEffectiveRank() != 1) {
                throw new IllegalArgumentException(variableName + " is not a CopyOnWriteArrayList.");
            }

            Integer dim = (CDFReader.this.thisCDF.getVariable(variableName)
                    .getDimensionElementCounts()
                    .get(0));

            if ((component < 0) || (component > dim)) {
                throw new IllegalArgumentException("Invalid component " + component + " for " + variableName);
            }

            return _getTimeSeries(variableName, component, ignoreFill, null);
        }

        public double[][] getTimeSeries(final String variableName, final int component, final boolean ignoreFill,
                final int[] startTime, final int[] stopTime) throws ReaderError {

            if (CDFReader.this.thisCDF.getVariable(variableName)
                    .getEffectiveRank() != 1) {
                throw new IllegalArgumentException(variableName + " is not a CopyOnWriteArrayList.");
            }

            Integer dim = (CDFReader.this.thisCDF.getVariable(variableName)
                    .getDimensionElementCounts()
                    .get(0));

            if ((component < 0) || (component > dim)) {
                throw new IllegalArgumentException("Invalid component " + component + " for " + variableName);
            }

            double[] trange = getAvailableTimeRange(variableName);
            double[] tr = TSExtractor.getOverlap(this.rdr, trange, variableName, startTime, stopTime);
            return _getTimeSeries(variableName, component, ignoreFill, tr);
        }

        public TimeSeries getTimeSeries(final String variableName, final int component, final boolean ignoreFill,
                final int[] startTime, final int[] stopTime, final TimeInstantModel tspec) throws ReaderError {

            if (CDFReader.this.thisCDF.getVariable(variableName)
                    .getEffectiveRank() != 1) {
                throw new IllegalArgumentException(variableName + " is not a CopyOnWriteArrayList.");
            }

            Integer dim = (CDFReader.this.thisCDF.getVariable(variableName)
                    .getDimensionElementCounts()
                    .get(0));

            if ((component < 0) || (component > dim)) {
                throw new IllegalArgumentException("Invalid component " + component + " for " + variableName);
            }

            double[] trange = getAvailableTimeRange(variableName);
            double[] tr = TSExtractor.getOverlap(this.rdr, trange, variableName, startTime, stopTime);
            return _getTimeSeries(variableName, component, ignoreFill, tr, tspec);
        }

        public double[][] getTimeSeries(final String variableName, final int component, final int[] startTime,
                final int[] stopTime) throws ReaderError {

            if (CDFReader.this.thisCDF.getVariable(variableName)
                    .getEffectiveRank() != 1) {
                throw new IllegalArgumentException(variableName + " is not a CopyOnWriteArrayList.");
            }

            Integer dim = (CDFReader.this.thisCDF.getVariable(variableName)
                    .getDimensionElementCounts()
                    .get(0));

            if ((component < 0) || (component > dim)) {
                throw new IllegalArgumentException("Invalid component " + component + " for " + variableName);
            }

            double[] trange = getAvailableTimeRange(variableName);
            double[] tr = TSExtractor.getOverlap(this.rdr, trange, variableName, startTime, stopTime);
            return _getTimeSeries(variableName, component, true, tr);
        }

        public TimeSeries getTimeSeries(final String variableName, final int component, final int[] startTime,
                final int[] stopTime, final TimeInstantModel tspec) throws ReaderError {

            if (CDFReader.this.thisCDF.getVariable(variableName)
                    .getEffectiveRank() != 1) {
                throw new IllegalArgumentException(variableName + " is not a CopyOnWriteArrayList.");
            }

            Integer dim = (CDFReader.this.thisCDF.getVariable(variableName)
                    .getDimensionElementCounts()
                    .get(0));

            if ((component < 0) || (component > dim)) {
                throw new IllegalArgumentException("Invalid component " + component + " for " + variableName);
            }

            double[] trange = getAvailableTimeRange(variableName);
            double[] tr = TSExtractor.getOverlap(this.rdr, trange, variableName, startTime, stopTime);
            return _getTimeSeries(variableName, component, true, tr, tspec);
        }

        private double[][] _getTimeSeries(final String variableName, final int component, final boolean ignoreFill,
                final double[] timeRange) throws ReaderError {

            checkType(variableName);

            Variable variable = CDFReader.this.thisCDF.getVariable(variableName);

            Method method = TSExtractor.getMethod(variable, "TimeSeries", 1);

            try {
                return (double[][]) method.invoke(null, this.rdr, variable, component, ignoreFill, timeRange);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new ReaderError("getTimeSeries failed", e);
            }

        }

        private TimeSeries _getTimeSeries(final String variableName, final int component, final boolean ignoreFill,
                final double[] timeRange, final TimeInstantModel tspec) throws ReaderError {

            checkType(variableName);

            Variable variable = CDFReader.this.thisCDF.getVariable(variableName);

            Method method = TSExtractor.getMethod(variable, "TimeSeriesObject", 1);

            try {
                return new TimeSeriesImpl(
                        (TimeSeries) method.invoke(null, this.rdr, variable, component, ignoreFill, timeRange, tspec));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new ReaderError("getTimeSeries failed", e);
            }

        }
    }

    /*
     * void checkType(String variableName) {
     * Variable var = thisCDF.getVariable(variableName);
     * int type = var.getType();
     * if (DataTypes.typeCategory[type] == DataTypes.LONG) {
     * throw new Throwable("This method cannot be used for " +
     * "variables of type long. Use the get methods for the " +
     * "variable and the associated time variable. ");
     * }
     * }
     */
    class Scalar {

        MetaData rdr;

        public double[][] getTimeSeries(final String variableName) throws ReaderError {
            Variable variable = CDFReader.this.thisCDF.getVariable(variableName);

            if (variable.getEffectiveRank() != 0) {
                throw new IllegalArgumentException(variableName + " is not a scalar.");
            }

            return _getTimeSeries(variableName, true, null);
        }

        public double[][] getTimeSeries(final String variableName, final boolean ignoreFill) throws ReaderError {

            if (CDFReader.this.thisCDF.getVariable(variableName)
                    .getEffectiveRank() != 0) {
                throw new IllegalArgumentException(variableName + " is not a scalar.");
            }

            return _getTimeSeries(variableName, ignoreFill, null);
        }

        public double[][] getTimeSeries(final String variableName, final boolean ignoreFill, final int[] startTime,
                final int[] stopTime) throws ReaderError {

            if (CDFReader.this.thisCDF.getVariable(variableName)
                    .getEffectiveRank() != 0) {
                throw new IllegalArgumentException(variableName + " is not a scalar.");
            }

            double[] trange = getAvailableTimeRange(variableName);
            double[] tr = TSExtractor.getOverlap(this.rdr, trange, variableName, startTime, stopTime);
            return _getTimeSeries(variableName, ignoreFill, tr);
        }

        public TimeSeries getTimeSeries(final String variableName, final boolean ignoreFill, final int[] startTime,
                final int[] stopTime, final TimeInstantModel tspec) throws ReaderError {

            if (CDFReader.this.thisCDF.getVariable(variableName)
                    .getEffectiveRank() != 0) {
                throw new IllegalArgumentException(variableName + " is not a scalar.");
            }

            double[] trange = getAvailableTimeRange(variableName);
            double[] tr = TSExtractor.getOverlap(this.rdr, trange, variableName, startTime, stopTime);
            return _getTimeSeries(variableName, ignoreFill, tr, tspec);
        }

        public double[][] getTimeSeries(final String variableName, final int[] startTime, final int[] stopTime)
                throws ReaderError {

            if (CDFReader.this.thisCDF.getVariable(variableName)
                    .getEffectiveRank() != 0) {
                throw new IllegalArgumentException(variableName + " is not a scalar.");
            }

            double[] trange = getAvailableTimeRange(variableName);
            double[] tr = TSExtractor.getOverlap(this.rdr, trange, variableName, startTime, stopTime);
            return _getTimeSeries(variableName, true, tr);
        }

        public TimeSeries getTimeSeries(final String variableName, final int[] startTime, final int[] stopTime,
                final TimeInstantModel tspec) throws ReaderError {

            if (CDFReader.this.thisCDF.getVariable(variableName)
                    .getEffectiveRank() != 0) {
                throw new IllegalArgumentException(variableName + " is not a scalar.");
            }

            double[] trange = getAvailableTimeRange(variableName);
            double[] tr = TSExtractor.getOverlap(this.rdr, trange, variableName, startTime, stopTime);
            return _getTimeSeries(variableName, true, tr, tspec);
        }

        double[][] _getTimeSeries(final String variableName, final boolean ignoreFill, final double[] timeRange)
                throws ReaderError {
            checkType(variableName);
            Variable variable = CDFReader.this.thisCDF.getVariable(variableName);
            Method method = TSExtractor.getMethod(variable, "TimeSeries", 0);

            try {
                return (double[][]) method.invoke(null, this.rdr, variable, ignoreFill, timeRange);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new ReaderError("getTimeSeries failed", e);
            }

        }

        TimeSeries _getTimeSeries(final String variableName, final boolean ignoreFill, final double[] timeRange,
                final TimeInstantModel tspec) throws ReaderError {
            checkType(variableName);
            Variable variable = CDFReader.this.thisCDF.getVariable(variableName);
            Method method = TSExtractor.getMethod(variable, "TimeSeriesObject", 0);

            try {
                return new TimeSeriesImpl(
                        (TimeSeries) method.invoke(null, this.rdr, variable, ignoreFill, timeRange, tspec));
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new ReaderError("getTimeSeries failed", e);
            }

        }
    }

    static class TimeSeriesImpl implements TimeSeries {

        double[] times;

        Object values;

        TimeInstantModel tspec;

        TimeSeriesImpl(final TimeSeries ts) throws CDFException.ReaderError {
            this.times = ts.getTimes();
            this.values = ts.getValues();
            this.tspec = ts.getTimeInstantModel();
        }

        @Override
        public TimeInstantModel getTimeInstantModel() {
            return this.tspec;
        }

        @Override
        public double[] getTimes() throws CDFException.ReaderError {
            return this.times;
        }

        @Override
        public Object getValues() {
            return this.values;
        }
    }

    static class TimeSeriesOneDImpl extends TimeSeriesImpl implements TimeSeriesOneD {

        boolean columnMajor;

        TimeSeriesOneDImpl(final TimeSeriesX ts) throws CDFException.ReaderError {
            super(ts);

            if (!ts.isOneD()) {
                throw new CDFException.ReaderError("Not 1D timeseries.");
            }

            this.columnMajor = ts.isColumnMajor();
        }

        @Override
        public double[] getValues() {
            return (double[]) this.values;
        }

        public double[] getValuesOneD() {
            return (double[]) this.values;
        }

        @Override
        public boolean isColumnMajor() {
            return this.columnMajor;
        }
    }

    @Override
    public void close() throws IOException {
        this.thisCDF.close();

    }
}
