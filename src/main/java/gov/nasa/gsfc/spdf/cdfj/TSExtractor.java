package gov.nasa.gsfc.spdf.cdfj;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author nand
 */
public class TSExtractor extends Extractor {

    static {

        try {
            Class variableClass = Class.forName("gov.nasa.gsfc.spdf.cdfj.Variable");
            Class rdrClass = Class.forName("gov.nasa.gsfc.spdf.cdfj.MetaData");
            Class cl = Class.forName("gov.nasa.gsfc.spdf.cdfj.TSExtractor");
            Class timeSpecClass = Class.forName("gov.nasa.gsfc.spdf.cdfj.TimeInstantModel");
            double[] da = new double[0];
            int[] ia = new int[0];
            Class[][] arglist;
            arglist = new Class[][] { new Class[] { rdrClass, variableClass, Boolean.class, da.getClass() },
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
            ex.printStackTrace();
        }

    }

    static Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));

    /**
     *
     * @param times
     * @param vdata
     * @param fill
     * @param first
     *
     * @return
     */
    public static double[][] filterFill(double[] times, double[] vdata, double fill, int first) {
        double[][] series;
        int count = 0;

        for (double vdatum : vdata) {

            if (vdatum != fill) {
                count++;
            }

        }

        series = new double[2][count];
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
     *
     * @param times
     * @param o
     * @param fillValue
     *
     * @return
     */
    public static double[][] filterFill(double[] times, Object o, Number fillValue) {
        double[][] series;
        int count = 0;

        if (o.getClass().getComponentType() == Long.TYPE) {
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

            if (o.getClass().getComponentType() != Double.TYPE) {
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
     *
     * @param var
     * @param name
     * @param rank
     *
     * @return
     *
     * @throws Throwable
     */
    public static Method getMethod(Variable var, String name, int rank) throws Throwable {
        return getMethod(var, name, rank, false);
    }

    /**
     *
     * @param rdr
     * @param trange
     * @param varName
     * @param startTime
     * @param stopTime
     *
     * @return
     *
     * @throws Throwable
     */
    public static double[] getOverlap(MetaData rdr, double[] trange, String varName, int[] startTime, int[] stopTime)
            throws Throwable {
        double[] overlap = new double[] { Double.MIN_VALUE, Double.MAX_VALUE };

        if (startTime != null) {

            if (startTime.length < 3) {
                throw new Throwable("incomplete start" + " time " + "definition.");
            }

            double _start = getTime(rdr, varName, startTime);

            if (_start > trange[1]) {
                throw new Throwable("Start time is " + "beyond end of data");
            }

            overlap[0] = Math.max(_start, trange[0]);
        } else {
            overlap[0] = trange[0];
        }

        if (stopTime != null) {

            if (stopTime.length < 3) {
                throw new Throwable("incomplete stop" + " time " + "definition.");
            }

            double _stop = getTime(rdr, varName, stopTime);

            if (_stop < trange[0]) {
                throw new Throwable("Stop time is " + "before start of data");
            }

            if (_stop < overlap[0]) {
                throw new Throwable("Stop time is " + "before start time");
            }

            overlap[1] = /* (_stop > trange[1])?trange[1]: */_stop;
        } else {
            overlap[1] = trange[1];
        }

        return overlap;
    }

    /**
     *
     * @param rdr
     * @param var
     * @param which
     * @param ignoreFill
     * @param timeRange
     * @param stride
     *
     * @return
     *
     * @throws Throwable
     */
    public static double[][] getSampledTimeSeries(MetaData rdr, Variable var, Integer which, Boolean ignoreFill,
            double[] timeRange, int[] stride) throws Throwable {

        if (var.getNumberOfValues() == 0) {
            return null;
        }

        boolean ignore = ignoreFill;
        double[] vdata;
        int[] recordRange = null;
        TimeVariable tv = TimeVariableFactory.getTimeVariable(rdr, var.getName());
        double[] times = tv.getTimes();

        if (times == null) {
            return null;
        }

        double[] stimes;
        Stride strideObject = new Stride(stride);

        if (timeRange == null) {
            vdata = (double[]) (which == null ? getSeries0(rdr.thisCDF, var, strideObject)
                    : getElement1(rdr.thisCDF, var, which, strideObject));
        } else {
            recordRange = getRecordRange(rdr, var, timeRange);

            if (recordRange == null) {
                return null;
            }

            if (which == null) {
                vdata = (double[]) getRange0(rdr.thisCDF, var, recordRange[0], recordRange[1], strideObject);
            } else {
                vdata = (double[]) getRangeForElement1(rdr.thisCDF, var, recordRange[0], recordRange[1], which,
                        strideObject);
            }

        }

        int _stride = strideObject.getStride();
        double[] fill = (double[]) getFillValue(rdr.thisCDF, var);

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
     *
     * @param rdr
     * @param var
     * @param ignoreFill
     * @param timeRange
     * @param stride
     *
     * @return
     *
     * @throws Throwable
     */
    public static double[][] getSampledTimeSeries0(MetaData rdr, Variable var, Boolean ignoreFill, double[] timeRange,
            int[] stride) throws Throwable {
        return getSampledTimeSeries(rdr, var, null, ignoreFill, timeRange, stride);
    }

    /**
     *
     * @param rdr
     * @param var
     * @param which
     * @param ignoreFill
     * @param timeRange
     * @param stride
     *
     * @return
     *
     * @throws Throwable
     */
    public static double[][] getSampledTimeSeries1(MetaData rdr, Variable var, Integer which, Boolean ignoreFill,
            double[] timeRange, int[] stride) throws Throwable {
        return getSampledTimeSeries(rdr, var, which, ignoreFill, timeRange, stride);
    }

    /**
     *
     * @param time
     *
     * @return
     */
    public static long getTime(int[] time) {
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

        cal.clear();
        cal.set(t[0], t[1], t[2], t[3], t[4], t[5]);
        cal.set(Calendar.MILLISECOND, (n > 6) ? time[6] : 0);
        return cal.getTimeInMillis();
    }

    /**
     *
     * @param rdr
     * @param vname
     * @param time
     *
     * @return
     *
     * @throws Throwable
     */
    public static double getTime(MetaData rdr, String vname, int[] time) throws Throwable {
        boolean isTT2000 = TimeVariableFactory.getTimeVariable(rdr, vname).isTT2000();
        long t = getTime(time);
        return (isTT2000) ? TimeUtil.milliSecondSince1970(t) : t;
    }

    /**
     *
     * @param rdr
     * @param var
     * @param which
     * @param ignoreFill
     * @param timeRange
     *
     * @return
     *
     * @throws Throwable
     */
    public static double[][] getTimeSeries(MetaData rdr, Variable var, Integer which, Boolean ignoreFill,
            double[] timeRange) throws Throwable {

        if (var.getNumberOfValues() == 0) {
            return null;
        }

        boolean ignore = ignoreFill;
        double[] vdata;
        int[] recordRange = null;
        TimeVariable tv = TimeVariableFactory.getTimeVariable(rdr, var.getName());
        double[] times = tv.getTimes();

        if (times == null) {
            return null;
        }

        boolean longType = false;
        int type = var.getType();
        int element = (which == null) ? 0 : which;
        Number pad;

        if (DataTypes.typeCategory[type] == DataTypes.LONG) {
            longType = true;
            pad = ((long[]) getPadValue(rdr.thisCDF, var))[element];
        } else {
            pad = ((double[]) getPadValue(rdr.thisCDF, var))[element];
        }

        double[] stimes;
        Object o = null;
        Object[] oa = null;

        if (timeRange == null) {
            o = (which == null) ? Extractor.getSeries0(rdr.thisCDF, var)
                    : Extractor.getElement1(rdr.thisCDF, var, which);

            if (var.isMissingRecords()) {
                long[][] locations = var.getLocator().getLocations();
                oa = filterPad(o, times, pad, locations, 0);
            } else {
                oa = new Object[] { times, o };
            }

        } else {
            recordRange = getRecordRange(rdr, var, timeRange);

            if (recordRange == null) {
                return null;
            }

            if (which == null) {
                o = getRange0(rdr.thisCDF, var, recordRange[0], recordRange[1]);
            } else {
                o = getRangeForElement1(rdr.thisCDF, var, recordRange[0], recordRange[1], which);
            }

            stimes = new double[Array.getLength(o)];
            int index = recordRange[0];

            for (int i = 0; i < stimes.length; i++) {
                stimes[i] = times[index++];
            }

            if (var.isMissingRecords()) {
                long[][] locations = var.getLocator().getLocations();
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
        Object fill = Extractor.getFillValue(rdr.thisCDF, var);
        boolean fillDefined = true;
        Number fillValue = null;

        if (fill.getClass().getComponentType() == Double.TYPE) {
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
     *
     * @param rdr
     * @param var
     * @param ignoreFill
     * @param timeRange
     *
     * @return
     *
     * @throws Throwable
     */
    public static double[][] getTimeSeries0(MetaData rdr, Variable var, Boolean ignoreFill, double[] timeRange)
            throws Throwable {
        return getTimeSeries(rdr, var, null, ignoreFill, timeRange);
    }

    /**
     *
     * @param rdr
     * @param var
     * @param which
     * @param ignoreFill
     * @param timeRange
     *
     * @return
     *
     * @throws Throwable
     */
    public static double[][] getTimeSeries1(MetaData rdr, Variable var, Integer which, Boolean ignoreFill,
            double[] timeRange) throws Throwable {
        return getTimeSeries(rdr, var, which, ignoreFill, timeRange);
    }

    /**
     *
     * @param rdr
     * @param var
     * @param ignoreFill
     * @param timeRange
     * @param ts
     *
     * @return
     *
     * @throws Throwable
     */
    public static TimeSeries getTimeSeriesObject0(MetaData rdr, Variable var, Boolean ignoreFill, double[] timeRange,
            TimeInstantModel ts) throws Throwable {
        return new GeneralTimeSeries(rdr, var, null, ignoreFill, timeRange, ts);
    }

    /**
     *
     * @param rdr
     * @param var
     * @param which
     * @param ignoreFill
     * @param timeRange
     * @param ts
     *
     * @return
     *
     * @throws Throwable
     */
    public static TimeSeries getTimeSeriesObject1(MetaData rdr, Variable var, Integer which, Boolean ignoreFill,
            double[] timeRange, TimeInstantModel ts) throws Throwable {
        return new GeneralTimeSeries(rdr, var, which, ignoreFill, timeRange, ts);
    }

    /**
     *
     * @return
     */
    public static String identifier() {
        return "TSExtractor";
    }

    static Object[] filterPad(Object o, double[] times, Number pad, long[][] locations, int first) {
        RecordSensor sensor = new RecordSensor(locations);

        if (o.getClass().getComponentType() == Double.TYPE) {
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

        if (o.getClass().getComponentType() != Long.TYPE) {
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

    static Method getMethod(Variable var, String name, int rank, boolean checkMissing) throws Throwable {

        if (var == null) {
            throw new Throwable("Internal error. Null variable " + "encountered in call to TSExtractor.getMethod()");
        }

        int _rank = var.getEffectiveRank();

        if (_rank != rank) {
            throw new Throwable("Called method is not appropriate for variables of " + "effective rank " + _rank);
        }

        if (checkMissing && (var.isMissingRecords())) {
            System.out.println("Variable " + var.getName() + " has gaps."
                    + " Sampled time series code is being tested. Feature is not "
                    + " currently available if the variable has gaps.");
            return null;
        }

        Method method = getMethod(var, name);

        if (method == null) {
            throw new Throwable("get" + name + " not " + "implemented for " + var.getName());
        }

        return method;
    }

    static int[] getRecordRange(MetaData rdr, Variable var, double[] timeRange) {
        return getRecordRange(rdr, var, timeRange, null);
    }

    static int[] getRecordRange(MetaData rdr, VariableMetaData var, double[] timeRange, TimeInstantModel ts) {

        try {
            TimeVariableX tvx = TimeVariableFactory.getTimeVariable(rdr, var.getName());
            return tvx.getRecordRange(timeRange);
        } catch (Throwable t) {
        }

        return null;
    }

    /**
     * Loss of precision may occur if type of var is LONG
     * times obtained are millisecond since 1970 regardless of the
     * precision of time variable corresponding to variable var
     */
    public static class GeneralTimeSeries implements TimeSeries {

        double[] vdata;

        double[] times;

        TimeInstantModel tspec;

        double[][] filtered = null;

        /**
         *
         * @param rdr
         * @param var
         * @param which
         * @param ignoreFill
         * @param timeRange
         * @param ts
         *
         * @throws Throwable
         */
        public GeneralTimeSeries(MetaData rdr, Variable var, Integer which, Boolean ignoreFill, double[] timeRange,
                TimeInstantModel ts) throws Throwable {
            boolean ignore = ignoreFill;
            int[] recordRange = null;

            if (ts != null) {

                synchronized (ts) {
                    this.tspec = (TimeInstantModel) ts.clone();
                }

            }

            TimeVariable tv = TimeVariableFactory.getTimeVariable(rdr, var.getName());
            this.times = tv.getTimes(this.tspec);

            if (this.times == null) {
                throw new Throwable("times not available for " + var.getName());
            }

            double[] stimes;
            boolean longType = false;
            int type = var.getType();

            if (DataTypes.typeCategory[type] == DataTypes.LONG) {
                longType = true;
            }

            Object o = null;

            if (timeRange == null) {
                o = (which == null) ? getSeries0(rdr.thisCDF, var) : getElement1(rdr.thisCDF, var, which);
            } else {
                recordRange = getRecordRange(rdr, var, timeRange, ts);

                if (recordRange == null) {
                    throw new Throwable("no record range");
                }

                if (which == null) {
                    o = getRange0(rdr.thisCDF, var, recordRange[0], recordRange[1]);
                } else {
                    o = getRangeForElement1(rdr.thisCDF, var, recordRange[0], recordRange[1], which);
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
                double[] fill = (double[]) getFillValue(rdr.thisCDF, var);
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
     *
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
         *
         * @param rdr
         * @param var
         * @param ignoreFill
         * @param timeRange
         * @param ts
         * @param oned
         * @param columnMajor
         *
         * @throws Throwable
         */
        public GeneralTimeSeriesX(MetaData rdr, VariableMetaData var, Boolean ignoreFill, final double[] timeRange,
                TimeInstantModel ts, boolean oned, boolean columnMajor) throws Throwable {

            if (ts != null) {

                synchronized (ts) {
                    this.tspec = (TimeInstantModel) ts.clone();
                }

            } else {
                this.tspec = null;
            }

            this.vname = var.getName();
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
            } catch (Throwable th) {
                throw new CDFException.ReaderError(th.getMessage());
            }

        }

        @Override
        public Object getValues() throws CDFException.ReaderError {

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
            } catch (Throwable th) {
                throw new CDFException.ReaderError(th.getMessage());
            }

        }

        /**
         *
         * @return
         */
        @Override
        public boolean isColumnMajor() {
            return this.columnMajor;
        }

        /**
         *
         * @return
         */
        @Override
        public boolean isOneD() {
            return this.oned;
        }
    }

    static class RecordSensor {

        long[][] locations;

        int last = 0;

        RecordSensor(long[][] locations) {
            this.locations = locations;
        }

        boolean hasRecord(int number) {

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
