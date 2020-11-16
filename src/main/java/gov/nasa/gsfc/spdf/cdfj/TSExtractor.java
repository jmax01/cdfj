package gov.nasa.gsfc.spdf.cdfj;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.logging.Level;

import gov.nasa.gsfc.spdf.cdfj.CDFException.ReaderError;
import lombok.extern.java.Log;

/**
 * The Class TSExtractor.
 *
 * @author nand
 */
@Log
public class TSExtractor extends Extractor {

    static {

        try {
            Class<?> variableClass = Class.forName("gov.nasa.gsfc.spdf.cdfj.Variable");
            Class<?> rdrClass = Class.forName("gov.nasa.gsfc.spdf.cdfj.MetaData");
            Class<?> cl = Class.forName("gov.nasa.gsfc.spdf.cdfj.TSExtractor");
            Class<?> timeSpecClass = Class.forName("gov.nasa.gsfc.spdf.cdfj.TimeInstantModel");
            double[] da = new double[0];
            int[] ia = new int[0];
            Class<?>[][] arglist = new Class[][] {
                    new Class[] { rdrClass, variableClass, Boolean.class, da.getClass() },
                    new Class[] { rdrClass, variableClass, Integer.class, Boolean.class, da.getClass() }, null, null };
            addFunction("TimeSeries", cl, arglist);
            arglist = new Class[][] {
                    new Class[] { rdrClass, variableClass, Boolean.class, da.getClass(), ia.getClass() },
                    new Class[] { rdrClass, variableClass, Integer.class, Boolean.class, da.getClass(), ia.getClass() },
                    null, null };
            addFunction("SampledTimeSeries", cl, arglist);

            arglist = new Class[][] {
                    new Class[] { rdrClass, variableClass, Boolean.class, da.getClass(), timeSpecClass },
                    new Class[] { rdrClass, variableClass, Integer.class, Boolean.class, da.getClass(), timeSpecClass },
                    null, null };
            addFunction("TimeSeriesObject", cl, arglist);

        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Could not find classes", ex);
        }

    }

    /**
     * Filter fill.
     *
     * @param times the times
     * @param vdata the vdata
     * @param fill  the fill
     * @param first the first
     *
     * @return the double[][]
     */
    public static double[][] filterFill(final double[] times, final double[] vdata, final double fill,
            final int first) {
        int count = 0;

        for (double vdatum : vdata) {

            if (vdatum != fill) {
                count++;
            }

        }

        double[][] series = new double[2][count];
        int n = 0;

        for (int i = 0; i < vdata.length; i++) {

            if (vdata[i] == fill) {
                continue;
            }

            series[0][n] = times[i + first];
            series[1][n] = vdata[i];
            n++;
        }

        return series;
    }

    /**
     * Filter fill.
     *
     * @param times     the times
     * @param o         the o
     * @param fillValue the fill value
     *
     * @return the double[][]
     */
    public static double[][] filterFill(final double[] times, final Object o, final Number fillValue) {
        double[][] series;
        int count = 0;

        if (o.getClass()
                .getComponentType() == Long.TYPE) {
            long fill = fillValue.longValue();
            long[] ldata = (long[]) o;

            for (long ldatum : ldata) {

                if (ldatum != fill) {
                    count++;
                }

            }

            series = new double[2][count];
            int n = 0;

            for (int i = 0; i < ldata.length; i++) {

                if (ldata[i] == fill) {
                    continue;
                }

                series[0][n] = times[i];
                series[1][n] = ldata[i];
                n++;
            }

        } else {

            if (o.getClass()
                    .getComponentType() != Double.TYPE) {
                return null;
            }

            double fill = fillValue.doubleValue();
            double[] data = (double[]) o;

            for (double datum : data) {

                if (datum != fill) {
                    count++;
                }

            }

            series = new double[2][count];
            int n = 0;

            for (int i = 0; i < data.length; i++) {

                if (data[i] == fill) {
                    continue;
                }

                series[0][n] = times[i];
                series[1][n] = data[i];
                n++;
            }

        }

        return series;
    }

    /**
     * Gets the method.
     *
     * @param variable the variable
     * @param name     the name
     * @param rank     the rank
     *
     * @return the method
     * @
     */
    public static Method getMethod(final Variable variable, final String name, final int rank) {
        return getMethod(variable, name, rank, false);
    }

    /**
     * Gets the overlap.
     *
     * @param rdr          the rdr
     * @param trange       the trange
     * @param variableName the var name
     * @param startTime    the start time
     * @param stopTime     the stop time
     *
     * @return the overlap
     * @
     */
    public static double[] getOverlap(final MetaData rdr, final double[] trange, final String variableName,
            final int[] startTime, final int[] stopTime) {

        double[] overlap = { Double.MIN_VALUE, Double.MAX_VALUE };

        if (startTime != null) {

            if (startTime.length < 3) {
                throw new IllegalArgumentException(
                        "incomplete start time " + "definition, startTime length must be >=3, was " + startTime.length);
            }

            double _start = getTime(rdr, variableName, startTime);

            if (_start > trange[1]) {
                throw new IllegalArgumentException("Start time is beyond end of data");
            }

            overlap[0] = Math.max(_start, trange[0]);
        } else {
            overlap[0] = trange[0];
        }

        if (stopTime != null) {

            if (stopTime.length < 3) {
                throw new IllegalArgumentException("incomplete stop time definition.");
            }

            double _stop = getTime(rdr, variableName, stopTime);

            if (_stop < trange[0]) {
                throw new IllegalArgumentException("Stop time is before start of data");
            }

            if (_stop < overlap[0]) {
                throw new IllegalArgumentException("Stop time is before start time");
            }

            overlap[1] = /* (_stop > trange[1])?trange[1]: */_stop;
        } else {
            overlap[1] = trange[1];
        }

        return overlap;
    }

    /**
     * Gets the sampled time series.
     *
     * @param rdr        the rdr
     * @param variable   the variable
     * @param which      the which
     * @param ignoreFill the ignore fill
     * @param timeRange  the time range
     * @param stride     the stride
     *
     * @return the sampled time series
     * @
     */
    public static double[][] getSampledTimeSeries(final MetaData rdr, final Variable variable, final Integer which,
            final Boolean ignoreFill, final double[] timeRange, final int[] stride) {

        if (variable.getNumberOfValues() == 0) {
            return null;
        }

        boolean ignore = ignoreFill;
        double[] vdata;
        int[] recordRange = null;
        TimeVariable tv = TimeVariableFactory.getTimeVariable(rdr, variable.getName());
        double[] times = tv.getTimes();

        if (times == null) {
            return null;
        }

        double[] stimes;
        Stride strideObject = new Stride(stride);

        if (timeRange == null) {
            vdata = (double[]) ((which == null) ? getSeries0(rdr.thisCDF, variable, strideObject)
                    : getElement1(rdr.thisCDF, variable, which, strideObject));
        } else {
            recordRange = getRecordRange(rdr, variable, timeRange);

            if (recordRange == null) {
                return null;
            }

            if (which == null) {
                vdata = (double[]) getRange0(rdr.thisCDF, variable, recordRange[0], recordRange[1], strideObject);
            } else {
                vdata = (double[]) getRangeForElement1(rdr.thisCDF, variable, recordRange[0], recordRange[1], which,
                        strideObject);
            }

        }

        int _stride = strideObject.getStride();
        double[] fill = (double[]) getFillValue(rdr.thisCDF, variable);

        if ((!ignore) || (fill[0] != 0)) {

            if (timeRange == null) {

                if (_stride == 1) {
                    return new double[][] { times, vdata };
                }

                stimes = new double[vdata.length];

                for (int i = 0; i < vdata.length; i++) {
                    stimes[i] = times[i * _stride];
                }

                return new double[][] { stimes, vdata };
            }

            stimes = new double[vdata.length];

            if (_stride == 1) {
                System.arraycopy(times, recordRange[0], stimes, 0, vdata.length);
            } else {
                int srec = recordRange[0];

                for (int i = 0; i < vdata.length; i++) {
                    stimes[i] = times[srec + (i * _stride)];
                }

            }

            return new double[][] { stimes, vdata };
        }

        // fill values need to be filtered
        if (timeRange == null) {

            if (_stride == 1) {
                return filterFill(times, vdata, fill[1], 0);
            }

            stimes = new double[vdata.length];

            for (int i = 0; i < vdata.length; i++) {
                stimes[i] = times[i * _stride];
            }

            return filterFill(stimes, vdata, fill[1], 0);
        }

        stimes = new double[vdata.length];

        if (_stride == 1) {
            System.arraycopy(times, recordRange[0], stimes, 0, vdata.length);
        } else {
            int srec = recordRange[0];

            for (int i = 0; i < vdata.length; i++) {
                stimes[i] = times[srec + (i * _stride)];
            }

        }

        return filterFill(stimes, vdata, fill[1], 0);
    }

    /**
     * Gets the sampled time series 0.
     *
     * @param rdr        the rdr
     * @param variable   the variable
     * @param ignoreFill the ignore fill
     * @param timeRange  the time range
     * @param stride     the stride
     *
     * @return the sampled time series 0
     * @
     */
    public static double[][] getSampledTimeSeries0(final MetaData rdr, final Variable variable,
            final Boolean ignoreFill, final double[] timeRange, final int[] stride) {
        return getSampledTimeSeries(rdr, variable, null, ignoreFill, timeRange, stride);
    }

    /**
     * Gets the sampled time series 1.
     *
     * @param rdr        the rdr
     * @param variable   the variable
     * @param which      the which
     * @param ignoreFill the ignore fill
     * @param timeRange  the time range
     * @param stride     the stride
     *
     * @return the sampled time series 1
     * @
     */
    public static double[][] getSampledTimeSeries1(final MetaData rdr, final Variable variable, final Integer which,
            final Boolean ignoreFill, final double[] timeRange, final int[] stride) {
        return getSampledTimeSeries(rdr, variable, which, ignoreFill, timeRange, stride);
    }

    /**
     * Gets the time.
     *
     * @param time the time
     *
     * @return the time
     */
    public static long getTime(final int[] time) {
        int[] t = new int[6];
        System.arraycopy(time, 0, t, 0, 3);
        t[1]--;

        for (int i = 3; i < 6; i++) {
            t[i] = 0;
        }

        int n = time.length;

        if (n >= 4) {
            t[3] = time[3];

            if (n >= 5) {
                t[4] = time[4];

                if (n >= 6) {
                    t[5] = time[5];
                }

            }

        }

        Calendar cal = TimeUtil.newGMTCalendarInstance();
        cal.clear();
        cal.set(t[0], t[1], t[2], t[3], t[4], t[5]);
        cal.set(Calendar.MILLISECOND, (n > 6) ? time[6] : 0);
        return cal.getTimeInMillis();
    }

    /**
     * Gets the time.
     *
     * @param rdr   the rdr
     * @param vname the vname
     * @param time  the time
     *
     * @return the time
     * @
     */
    public static double getTime(final MetaData rdr, final String vname, final int[] time) {
        boolean isTT2000 = TimeVariableFactory.getTimeVariable(rdr, vname)
                .isTT2000();
        long t = getTime(time);
        return (isTT2000) ? TimeUtil.milliSecondSince1970(t) : t;
    }

    /**
     * Gets the time series.
     *
     * @param rdr        the rdr
     * @param variable   the variable
     * @param which      the which
     * @param ignoreFill the ignore fill
     * @param timeRange  the time range
     *
     * @return the time series
     * @
     */
    public static double[][] getTimeSeries(final MetaData rdr, final Variable variable, final Integer which,
            final Boolean ignoreFill, final double[] timeRange) {

        if (variable.getNumberOfValues() == 0) {
            return null;
        }

        boolean ignore = ignoreFill;
        double[] vdata;
        TimeVariable tv = TimeVariableFactory.getTimeVariable(rdr, variable.getName());
        double[] times = tv.getTimes();

        if (times == null) {
            return null;
        }

        boolean longType = false;
        int type = variable.getType();
        int element = (which == null) ? 0 : which;
        Number pad;

        if (DataTypes.typeCategory[type] == DataTypes.LONG) {
            longType = true;
            pad = ((long[]) getPadValue(rdr.thisCDF, variable))[element];
        } else {
            pad = ((double[]) getPadValue(rdr.thisCDF, variable))[element];
        }

        double[] stimes;
        Object o = null;
        Object[] oa = null;

        if (timeRange == null) {
            o = (which == null) ? Extractor.getSeries0(rdr.thisCDF, variable)
                    : Extractor.getElement1(rdr.thisCDF, variable, which);

            if (variable.isMissingRecords()) {
                long[][] locations = variable.getLocator()
                        .getLocations();
                oa = filterPad(o, times, pad, locations, 0);
            } else {
                oa = new Object[] { times, o };
            }

        } else {
            int[] recordRange = getRecordRange(rdr, variable, timeRange);

            if (recordRange == null) {
                return null;
            }

            if (which == null) {
                o = getRange0(rdr.thisCDF, variable, recordRange[0], recordRange[1]);
            } else {
                o = getRangeForElement1(rdr.thisCDF, variable, recordRange[0], recordRange[1], which);
            }

            stimes = new double[Array.getLength(o)];
            int index = recordRange[0];

            for (int i = 0; i < stimes.length; i++) {
                stimes[i] = times[index++];
            }

            if (variable.isMissingRecords()) {
                long[][] locations = variable.getLocator()
                        .getLocations();
                oa = filterPad(o, stimes, pad, locations, recordRange[0]);
            } else {
                oa = new Object[] { stimes, o };
            }

        }

        stimes = (double[]) oa[0];

        if (!ignore) {
            vdata = castToDouble(oa[1], longType);
            return new double[][] { stimes, vdata };
        }

        // fill values need to be filtered
        Object fill = Extractor.getFillValue(rdr.thisCDF, variable);
        boolean fillDefined = true;
        Number fillValue = null;

        if (fill.getClass()
                .getComponentType() == Double.TYPE) {
            fillDefined = (((double[]) fill)[0] == 0);

            if (fillDefined) {
                fillValue = ((double[]) fill)[1];
            }

        } else {
            fillDefined = (((long[]) fill)[0] == 0);

            if (fillDefined) {
                fillValue = ((long[]) fill)[1];
            }

        }

        if (!fillDefined) {
            vdata = castToDouble(oa[1], longType);
            return new double[][] { stimes, vdata };
        }

        return filterFill(stimes, oa[1], fillValue);
    }

    /**
     * Gets the time series 0.
     *
     * @param rdr        the rdr
     * @param variable   the variable
     * @param ignoreFill the ignore fill
     * @param timeRange  the time range
     *
     * @return the time series 0
     * @
     */
    public static double[][] getTimeSeries0(final MetaData rdr, final Variable variable, final Boolean ignoreFill,
            final double[] timeRange) {
        return getTimeSeries(rdr, variable, null, ignoreFill, timeRange);
    }

    /**
     * Gets the time series 1.
     *
     * @param rdr        the rdr
     * @param variable   the variable
     * @param which      the which
     * @param ignoreFill the ignore fill
     * @param timeRange  the time range
     *
     * @return the time series 1
     * @
     */
    public static double[][] getTimeSeries1(final MetaData rdr, final Variable variable, final Integer which,
            final Boolean ignoreFill, final double[] timeRange) {
        return getTimeSeries(rdr, variable, which, ignoreFill, timeRange);
    }

    /**
     * Gets the time series object 0.
     *
     * @param rdr        the rdr
     * @param variable   the variable
     * @param ignoreFill the ignore fill
     * @param timeRange  the time range
     * @param ts         the ts
     *
     * @return the time series object 0
     * @
     */
    public static TimeSeries getTimeSeriesObject0(final MetaData rdr, final Variable variable, final Boolean ignoreFill,
            final double[] timeRange, final TimeInstantModel ts) {
        return new GeneralTimeSeries(rdr, variable, null, ignoreFill, timeRange, ts);
    }

    /**
     * Gets the time series object 1.
     *
     * @param rdr        the rdr
     * @param variable   the variable
     * @param which      the which
     * @param ignoreFill the ignore fill
     * @param timeRange  the time range
     * @param ts         the ts
     *
     * @return the time series object 1
     * @
     */
    public static TimeSeries getTimeSeriesObject1(final MetaData rdr, final Variable variable, final Integer which,
            final Boolean ignoreFill, final double[] timeRange, final TimeInstantModel ts) {
        return new GeneralTimeSeries(rdr, variable, which, ignoreFill, timeRange, ts);
    }

    /**
     * Identifier.
     *
     * @return the string
     */
    public static String identifier() {
        return "TSExtractor";
    }

    static Object[] filterPad(final Object o, final double[] times, final Number pad, final long[][] locations,
            final int first) {
        RecordSensor sensor = new RecordSensor(locations);

        if (o.getClass()
                .getComponentType() == Double.TYPE) {
            double dpad = pad.doubleValue();
            double[] vdata = (double[]) o;
            int npad = 0;

            for (int i = 0; i < vdata.length; i++) {

                if (sensor.hasRecord(first + i)) {
                    continue;
                }

                if (vdata[i] == dpad) {
                    npad++;
                }

            }

            if (npad == 0) {
                return new Object[] { times, vdata };
            }

            double[] _data = new double[vdata.length - npad];
            double[] _times = new double[vdata.length - npad];
            int index = 0;

            for (int i = 0; i < vdata.length; i++) {

                if (sensor.hasRecord(first + i) || (vdata[i] != dpad)) {
                    _data[index] = vdata[i];
                    _times[index] = times[i];
                    index++;
                }

            }

            return new Object[] { _times, _data };
        }

        if (o.getClass()
                .getComponentType() != Long.TYPE) {
            return null;
        }

        long lpad = pad.longValue();
        long[] ldata = (long[]) o;
        int npad = 0;

        for (int i = 0; i < ldata.length; i++) {

            if (sensor.hasRecord(first + i)) {
                continue;
            }

            if (ldata[i] == lpad) {
                npad++;
            }

        }

        if (npad == 0) {
            return new Object[] { times, ldata };
        }

        long[] _data = new long[ldata.length - npad];
        double[] _times = new double[ldata.length - npad];
        int index = 0;

        for (int i = 0; i < ldata.length; i++) {

            if (sensor.hasRecord(first + i) || (ldata[i] != lpad)) {
                _data[index] = ldata[i];
                _times[index] = times[i];
                index++;
            }

        }

        return new Object[] { _times, _data };
    }

    static Method getMethod(final Variable variable, final String name, final int rank, final boolean checkMissing) {

        if (variable == null) {
            throw new IllegalArgumentException(
                    "Internal error. Null variable encountered in call to TSExtractor.getMethod()");
        }

        int _rank = variable.getEffectiveRank();

        if (_rank != rank) {
            throw new IllegalArgumentException(
                    "Called method is not appropriate for variables of effective rank " + _rank);
        }

        if (checkMissing && (variable.isMissingRecords())) {

            LOGGER.log(Level.WARNING,
                    "Variable " + variable.getName() + " has gaps."
                            + " Sampled time series code is being tested. Feature is not "
                            + " currently available if the variable has gaps.");
            return null;
        }

        Method method = getMethod(variable, name);

        if (method == null) {
            throw new IllegalArgumentException("get" + name + " not implemented for " + variable.getName());
        }

        return method;
    }

    static int[] getRecordRange(final MetaData rdr, final Variable variable, final double[] timeRange) {
        return getRecordRange(rdr, variable, timeRange, null);
    }

    static int[] getRecordRange(final MetaData rdr, final VariableMetaData variable, final double[] timeRange,
            final TimeInstantModel ts) {

        TimeVariableX tvx = TimeVariableFactory.getTimeVariable(rdr, variable.getName());

        return tvx.getRecordRange(timeRange);

    }

    /**
     * Loss of precision may occur if type of var is LONG
     * times obtained are millisecond since 1970 regardless of the
     * precision of time variable corresponding to variable variable.
     */
    public static class GeneralTimeSeries implements TimeSeries {

        double[] vdata;

        double[] times;

        TimeInstantModel tspec;

        double[][] filtered = null;

        /**
         * Instantiates a new general time series.
         *
         * @param rdr        the rdr
         * @param variable   the variable
         * @param which      the which
         * @param ignoreFill the ignore fill
         * @param timeRange  the time range
         * @param ts         the ts
         * @
         */
        public GeneralTimeSeries(final MetaData rdr, final Variable variable, final Integer which,
                final Boolean ignoreFill, final double[] timeRange, final TimeInstantModel ts) {
            boolean ignore = ignoreFill;
            int[] recordRange = null;

            if (ts != null) {

                synchronized (ts) {
                    this.tspec = ts.clone();
                }

            }

            TimeVariable tv = TimeVariableFactory.getTimeVariable(rdr, variable.getName());
            this.times = tv.getTimes(this.tspec);

            if (this.times == null) {
                throw new IllegalArgumentException("times not available for " + variable.getName());
            }

            double[] stimes;
            boolean longType = false;
            int type = variable.getType();

            if (DataTypes.typeCategory[type] == DataTypes.LONG) {
                longType = true;
            }

            Object o = null;

            if (timeRange == null) {
                o = (which == null) ? getSeries0(rdr.thisCDF, variable) : getElement1(rdr.thisCDF, variable, which);
            } else {
                recordRange = getRecordRange(rdr, variable, timeRange, ts);

                if (recordRange == null) {
                    throw new IllegalStateException("no record range");
                }

                if (which == null) {
                    o = getRange0(rdr.thisCDF, variable, recordRange[0], recordRange[1]);
                } else {
                    o = getRangeForElement1(rdr.thisCDF, variable, recordRange[0], recordRange[1], which);
                }

            }

            this.vdata = castToDouble(o, longType);

            if (!ignore) {

                if (timeRange != null) {
                    stimes = new double[this.vdata.length];
                    System.arraycopy(this.times, recordRange[0], stimes, 0, this.vdata.length);
                    this.times = stimes;
                }

            } else {
                // fill values need to be filtered
                double[] fill = (double[]) getFillValue(rdr.thisCDF, variable);
                int first = (timeRange != null) ? recordRange[0] : 0;

                if (fill[0] != 0) { // there is no fill value
                    stimes = new double[this.vdata.length];
                    System.arraycopy(this.times, first, stimes, 0, this.vdata.length);
                    this.times = stimes;
                } else {
                    this.filtered = filterFill(this.times, this.vdata, fill[1], first);
                }

            }

        }

        @Override
        public TimeInstantModel getTimeInstantModel() {
            return this.tspec;
        }

        @Override
        public double[] getTimes() {
            return (this.filtered != null) ? this.filtered[0] : this.times;
        }

        @Override
        public double[] getValues() {
            return (this.filtered != null) ? this.filtered[1] : this.vdata;
        }
    }

    /**
     * The Class GeneralTimeSeriesX.
     */
    public static class GeneralTimeSeriesX implements TimeSeriesX {

        final TimeInstantModel tspec;

        final TimeVariableX tv;

        final String vname;

        final CDFImpl thisCDF;

        final double[] timeRange;

        final boolean oned;

        final boolean columnMajor;

        /**
         * Instantiates a new general time series X.
         *
         * @param rdr         the rdr
         * @param variable    the variable
         * @param ignoreFill  the ignore fill
         * @param timeRange   the time range
         * @param ts          the ts
         * @param oned        the oned
         * @param columnMajor the column major
         * @
         */
        public GeneralTimeSeriesX(final MetaData rdr, final VariableMetaData variable, final Boolean ignoreFill,
                final double[] timeRange, final TimeInstantModel ts, final boolean oned, final boolean columnMajor) {

            if (ts != null) {

                synchronized (ts) {
                    this.tspec = ts.clone();
                }

            } else {
                this.tspec = null;
            }

            this.vname = variable.getName();
            this.tv = TimeVariableFactory.getTimeVariable(rdr, this.vname);
            this.thisCDF = rdr.thisCDF;
            this.timeRange = timeRange;
            this.oned = oned;
            this.columnMajor = columnMajor;
        }

        @Override
        public TimeInstantModel getTimeInstantModel() {
            return this.tspec;
        }

        @Override
        public double[] getTimes() throws CDFException.ReaderError {

            try {

                if (this.timeRange == null) {
                    return this.tv.getTimes(this.tspec);
                }

                return this.tv.getTimes(this.timeRange, this.tspec);
            } catch (RuntimeException th) {
                throw new CDFException.ReaderError(th);
            }

        }

        @Override
        public Object getValues() throws ReaderError {

            try {

                if (this.timeRange == null) {
                    return (this.oned) ? this.thisCDF.getOneD(this.vname, this.columnMajor)
                            : this.thisCDF.get(this.vname);
                }

                int[] recordRange = this.tv.getRecordRange(this.timeRange);

                if (recordRange == null) {
                    throw new CDFException.ReaderError("no data");
                }

                if (!this.oned) {
                    return this.thisCDF.getRange(this.vname, recordRange[0], recordRange[1]);
                }

                return this.thisCDF.getRangeOneD(this.vname, recordRange[0], recordRange[1], this.columnMajor);
            } catch (RuntimeException th) {
                throw new CDFException.ReaderError(th);
            }

        }

        @Override
        public boolean isColumnMajor() {
            return this.columnMajor;
        }

        @Override
        public boolean isOneD() {
            return this.oned;
        }
    }

    static class RecordSensor {

        long[][] locations;

        int last = 0;

        RecordSensor(final long[][] locations) {
            this.locations = locations;
        }

        boolean hasRecord(final int number) {

            for (int i = this.last; i < this.locations.length; i++) {

                if ((number >= this.locations[i][0]) && (number <= this.locations[i][1])) {
                    this.last = i;
                    return true;
                }

            }

            return false;
        }
    }
}
