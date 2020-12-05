package gov.nasa.gsfc.spdf.cdfj;

import static gov.nasa.gsfc.spdf.cdfj.fields.DateTimeFields.*;

import java.nio.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.*;

import gov.nasa.gsfc.spdf.cdfj.fields.*;
import lombok.extern.java.*;

/**
 * A factory for creating TimeVariable objects.
 *
 * @author nand
 */
@Log
public final class TimeVariableFactory {

    /** The Constant JANUARY_1_1970. */
    public static final double JANUARY_1_1970 = DateTimeFields.MILLIS_FROM_CDF_EPOCH_TO_JAVA_EPOCH;

    static final OffsetDateTime JANUARY_1_100_NOON_UTC = OffsetDateTime.of(2000, 1, 1, 12, 0, 0, 0, ZoneOffset.UTC);

    static final Instant JANUARY_1_100_NOON_UTC_AS_INSTANT = JANUARY_1_100_NOON_UTC.toInstant();

    static final long JANUARY_1_100_NOON_UTC_JAVA_EPOCH_MILLIS = JANUARY_1_100_NOON_UTC_AS_INSTANT.toEpochMilli();

    static final long MILLISECONDS_IN_A_DAY = TimeUnit.DAYS.toMillis(1);

    static final double MILLISECONDS_IN_A_DAY_AS_DOUBLE = MILLISECONDS_IN_A_DAY;

    static final long LONG_FILL = -9_223_372_036_854_775_807L;

    static final double DOUBLE_FILL = -1.0e31;

    static final TimeInstantModel defaultTimeInstantModel = new DefaultTimeInstantModelImpl();

    /** The Constant JANUARY_1_1970_LONG. */
    public static final long JANUARY_1_1970_LONG = DateTimeFields.MILLIS_FROM_CDF_EPOCH_TO_JAVA_EPOCH;

    public static final long DIFF_TIA_UTC_JANUARY_1_1972 = LeapSecondTable.JANUARY_1_1972_LEAP_SECOND
            .leapSecondsAsMillis() + DIFF_TIA_AND_J2000_MILLISECONDS;

    /** The Constant TT2000_DATE. */
    public static final long TT2000_DATE = (JANUARY_1_1970_LONG + JANUARY_1_100_NOON_UTC_JAVA_EPOCH_MILLIS)
            - DIFF_TIA_UTC_JANUARY_1_1972;

    private TimeVariableFactory() {}

    /**
     * Gets the default time instant model.
     *
     * @return the default time instant model
     */
    public static TimeInstantModel getDefaultTimeInstantModel() {

        return defaultTimeInstantModel.clone();

    }

    /**
     * Gets the default time instant model.
     *
     * @param msec the msec
     *
     * @return the default time instant model
     */
    public static TimeInstantModel getDefaultTimeInstantModel(final double msec) {
        TimeInstantModel tspec = getDefaultTimeInstantModel();
        ((DefaultTimeInstantModelImpl) tspec).setBaseTime(msec);
        return tspec;
    }

    /**
     * Gets the time variable.
     *
     * @param rdr   the rdr
     * @param vname the vname
     *
     * @return the time variable
     */
    public static CDFTimeVariable getTimeVariable(final MetaData rdr, final String vname) {
        CDFImpl cdf = rdr.thisCDF;
        Variable variable = cdf.getVariable(vname);
        int recordCount = variable.getNumberOfValues();
        CDFTimeVariable tv;

        String tname = rdr.getTimeVariableName(vname);

        List<String> depend0VariableNames = (List<String>) cdf.getAttribute(variable.getName(), "DEPEND_0");

        if (!depend0VariableNames.isEmpty()) {
            tname = depend0VariableNames.get(0);
        }

        if (tname == null) {

            if (!"Epoch".equals(vname)) {

                if (cdf.getVariable("Epoch") != null) {
                    tname = "Epoch";
                    LOGGER.fine("Variable " + vname + " has no DEPEND_0 attribute. Variable named Epoch "
                            + "assumed to be the right time variable");
                } else {
                    throw new IllegalArgumentException("Time variable not found for " + vname);
                }

            } else {
                throw new IllegalStateException("Variable named Epoch has no DEPEND_0 attribute.");
            }

        }

        Variable tvar = cdf.getVariable(tname);

        if (tvar == null) {
            throw new IllegalArgumentException("Time variable not found for " + vname);
        }

        boolean themisLike = false;

        if (tvar.getNumberOfValues() == 0) { // themis like

            List<String> dependTimeVariableNames = (List<String>) cdf.getAttribute(variable.getName(), "DEPEND_TIME");

            if (!dependTimeVariableNames.isEmpty()) {
                tname = dependTimeVariableNames.get(0);
                tvar = cdf.getVariable(tname);
                themisLike = true;
            } else {
                throw new IllegalArgumentException("Expected unix time variable not found for " + variable.getName());
            }

        }

        if (tvar.getNumberOfValues() == 0) {
            throw new IllegalArgumentException("Empty time variable for " + variable.getName());
        }

        ByteBuffer buf = null;

        if (tvar.getType() == DataTypes.CDF_TIME_TT2000) {
            LongVarContainer lbuf = new LongVarContainer(cdf, tvar, null);
            lbuf.run();
            buf = lbuf.getBuffer();
        } else {
            DoubleVarContainer dbuf = new DoubleVarContainer(cdf, tvar, null, true);
            dbuf.run();
            buf = dbuf.getBuffer();
        }

        if (tvar.getType() == DataTypes.EPOCH16) {
            tv = new CDFEpoch16Variable(cdf, tname, buf);
        } else {

            if (tvar.getType() == DataTypes.CDF_TIME_TT2000) {
                tv = new CDFTT2000Variable(cdf, tname, buf);
            } else {

                if (themisLike) {
                    tv = new UnixTimeVariable(cdf, tname, buf);
                } else {
                    tv = new CDFEpochVariable(cdf, tname, buf);
                }

            }

        }

        tv.setRecordCount(recordCount);
        return tv;
    }

    /**
     * The Class CDFEpoch16Variable.
     */
    public static class CDFEpoch16Variable extends CDFTimeVariable {

        DoubleBuffer _dbuf;

        CDFEpoch16Variable(final CDFImpl cdf, final String name, final ByteBuffer obuf) {
            super(cdf, name, obuf);
            this.precision = TimePrecision.PICOSECOND;
            this._dbuf = this.tbuf.asDoubleBuffer();
        }

        @Override
        public boolean canSupportPrecision(final TimePrecision tp) {
            return true;
        }

        @Override
        public double[] getTimes(final int first, final int last, final TimeInstantModel ts) {
            TimePrecision offsetUnits = TimePrecision.MILLISECOND;
            long base = JANUARY_1_1970_LONG;

            if (ts != null) {
                base = (long) ts.getBaseTime();
                offsetUnits = ts.getOffsetUnits();
            }

            int count = (last - first) + 1;
            double[] da = new double[count];
            ByteBuffer bbuf = this.tbuf.duplicate();
            bbuf.order(this.tbuf.order());
            DoubleBuffer dbuf = bbuf.asDoubleBuffer();
            double d;
            double _d;
            long mul;

            if (offsetUnits == TimePrecision.MILLISECOND) {
                mul = 1_000;

                for (int i = first; i <= last; i++) {
                    _d = dbuf.get(2 * i);

                    if (_d == DOUBLE_FILL) {
                        da[i - first] = Double.NaN;
                        continue;
                    }

                    d = ((long) (dbuf.get(2 * i)) * mul) - base;
                    da[i - first] = d + (dbuf.get((2 * i) + 1) / 1.0e9);
                }

            } else {

                if (offsetUnits == TimePrecision.MICROSECOND) {
                    this.offset = 1_000 * base;
                    mul = 1_000_000;

                    for (int i = first; i <= last; i++) {
                        _d = dbuf.get(2 * i);

                        if (_d == DOUBLE_FILL) {
                            da[i - first] = Double.NaN;
                            continue;
                        }

                        d = ((long) (dbuf.get(2 * i)) * mul) - this.offset;
                        da[i - first] = d + (dbuf.get((2 * i) + 1) / 1.0e6);
                    }

                } else {

                    if (offsetUnits == TimePrecision.NANOSECOND) {
                        this.offset = 1_000_000 * base;
                        mul = 1_000_000_000;

                        for (int i = first; i <= last; i++) {
                            _d = dbuf.get(2 * i);

                            if (_d == DOUBLE_FILL) {
                                da[i - first] = Double.NaN;
                                continue;
                            }

                            d = ((long) (dbuf.get(2 * i)) * mul) - this.offset;
                            da[i - first] = d + (dbuf.get((2 * i) + 1) / 1.0e3);
                        }

                    } else { // pico

                        for (int i = first; i <= last; i++) {
                            _d = dbuf.get(2 * i);

                            if (_d == DOUBLE_FILL) {
                                da[i - first] = Double.NaN;
                                continue;
                            }

                            d = (dbuf.get(2 * i) * 1.0e3) - base; // millisec
                            da[i - first] = (d * 1.0e9) + dbuf.get((2 * i) + 1);
                        }

                    }

                }

            }

            return da;
        }

        @Override
        public boolean isTT2000() {
            return false;
        }

        @Override
        void reset() {
            this._dbuf.position(0);
        }
    }

    /**
     * The Class CDFEpochVariable.
     */
    public static class CDFEpochVariable extends CDFTimeVariable {

        TimePrecision offsetUnits = TimePrecision.MILLISECOND;

        DoubleBuffer _dbuf;

        CDFEpochVariable(final CDFImpl cdf, final String name, final ByteBuffer obuf) {
            super(cdf, name, obuf);
            this.precision = TimePrecision.MILLISECOND;
            this._dbuf = this.tbuf.asDoubleBuffer();
        }

        @Override
        public boolean canSupportPrecision(final TimePrecision tp) {
            return tp == TimePrecision.MILLISECOND;
        }

        @Override
        public double[] getTimes(final int first, final int last, final TimeInstantModel ts) {
            double base = JANUARY_1_1970_LONG;

            if (ts != null) {

                if (ts.getOffsetUnits() != TimePrecision.MILLISECOND) {
                    throw new IllegalArgumentException("Unsupported offset units: "
                            + "Only millisecond offset units are supported for this variable.");
                }

                base = ts.getBaseTime();
            }

            int count = (last - first) + 1;
            double[] da = new double[count];
            ByteBuffer bbuf = this.tbuf.duplicate();
            bbuf.order(this.tbuf.order());
            DoubleBuffer dbuf = bbuf.asDoubleBuffer();
            dbuf.position(first);
            dbuf.get(da);

            for (int i = 0; i < count; i++) {

                if (da[i] == DOUBLE_FILL) {
                    da[i] = Double.NaN;
                    continue;
                }

                da[i] -= base;
            }

            return da;
        }

        @Override
        public boolean isTT2000() {
            return false;
        }

        @Override
        void reset() {
            this._dbuf.position(0);
        }
    }

    /**
     * The Class CDFTimeVariable.
     */
    @Log
    public abstract static class CDFTimeVariable implements TimeVariableX {

        CDFImpl cdf;

        String name;

        TimePrecision precision;

        final ByteBuffer tbuf;

        long offset;

        int recordCount;

        CDFTimeVariable(final CDFImpl cdf, final String name, final ByteBuffer obuf) {
            this.name = name;
            this.cdf = cdf;
            this.tbuf = obuf;
        }

        @Override
        public double getFirstMilliSecond() {
            TimeInstantModel tspec = getDefaultTimeInstantModel();
            ((DefaultTimeInstantModelImpl) tspec).setBaseTime(0);
            tspec.setOffsetUnits(TimePrecision.MILLISECOND);

            try {
                double d = Double.NaN;
                int n = 0;

                while (n < this.recordCount) {
                    d = getTimes(n, n, tspec)[0];

                    if (!Double.isNaN(d)) {
                        return d;
                    }

                    n++;
                }

                return d;
            }
            catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, e, () -> "getFirstMilliSecond");
                return Double.NaN;
            }

        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public TimePrecision getPrecision() {
            return this.precision;
        }

        @Override
        public ByteBuffer getRawBuffer() {
            return this.tbuf;
        }

        @Override
        public int[] getRecordRange(final double[] timeRange) {
            return getRecordRange(timeRange, null);
        }

        /**
         * Gets the record range.
         *
         * @param timeRange the time range
         * @param ts        the ts
         *
         * @return the record range
         *
         */
        public int[] getRecordRange(final double[] timeRange, final TimeInstantModel ts) {
            double[] temp = getTimes(0, this.recordCount - 1, ts);
            double start = timeRange[0]; // offset in millis since 1970
            double stop = timeRange[1];

            if (((ts != null) && (!Objects.equals(ts, defaultTimeInstantModel)))) {
                start = (start - ts.getBaseTime()) + JANUARY_1_1970_LONG;
                stop = (stop - ts.getBaseTime()) + JANUARY_1_1970_LONG;

                if (ts.getOffsetUnits() == TimePrecision.MICROSECOND) {
                    start *= 1.0e3;
                    stop *= 1.0e3;
                } else {

                    if (ts.getOffsetUnits() == TimePrecision.NANOSECOND) {
                        start *= 1.0e6;
                        stop *= 1.0e6;
                    }

                }

            }

            int i = 0;

            for (; i < temp.length; i++) {

                if (Double.isNaN(temp[i])) {
                    continue;
                }

                if (start > temp[i]) {
                    continue;
                }

                break;
            }

            if (i == temp.length) {
                return null;
            }

            int low = i;
            int last = i;

            for (; i < temp.length; i++) {

                if (Double.isNaN(temp[i])) {
                    continue;
                }

                last = i;

                if (stop < temp[i]) {
                    break;
                }

                if (stop == temp[i]) {
                    last = i - 1;
                    break;
                }

            }

            return new int[] { low, last };
        }

        @Override
        public int[] getRecordRange(final int[] startTime, final int[] stopTime) {
            return getRecordRange(startTime, stopTime, null);
        }

        /**
         * Gets the record range.
         *
         * @param startTime the start time
         * @param stopTime  the stop time
         * @param ts        the ts
         *
         * @return the record range
         */
        @Override
        public int[] getRecordRange(final int[] startTime, final int[] stopTime, final TimeInstantModel ts) {

            if (startTime.length < 3) {
                throw new IllegalArgumentException(
                        "incomplete start time definition," + startTime.length + " less than 3");
            }

            if (stopTime.length < 3) {
                throw new IllegalArgumentException(
                        "incomplete stop time definition," + stopTime.length + " less than 3");
            }

            long start = TSExtractor.getTime(startTime);
            long stop = TSExtractor.getTime(stopTime);

            if (isTT2000()) {
                start = (long) TimeUtil.milliSecondSince1970(start);
                stop = (long) TimeUtil.milliSecondSince1970(stop);
            }

            return getRecordRange(new double[] { start, stop }, ts);
        }

        @Override
        public double[] getTimes() {

            try {
                return getTimes(0, this.recordCount - 1, null);
            }
            catch (RuntimeException e) {
                LOGGER.log(Level.SEVERE, "getTimes failed, returning null", e);
                return null;
            }

        }

        /**
         * Gets the times.
         *
         * @param timeRange the time range
         *
         * @return the times
         */
        public double[] getTimes(final double[] timeRange) {

            try {
                return getTimes(timeRange, null);
            }
            catch (RuntimeException e) {
                LOGGER.log(Level.SEVERE,
                        "getTimes failed for timeRange," + Arrays.toString(timeRange) + ", returning null", e);
                return null;
            }

        }

        @Override
        public double[] getTimes(final double[] timeRange, final TimeInstantModel ts) {

            if (timeRange == null) {
                return getTimes(0, this.recordCount - 1, ts);
            }

            int[] rr = getRecordRange(timeRange, ts);

            if (rr == null) {
                return null;
            }

            return getTimes(rr[0], rr[1], ts);
        }

        @Override
        public double[] getTimes(final int[] recordRange) {

            try {
                return getTimes(recordRange, defaultTimeInstantModel);
            }
            catch (RuntimeException e) {
                LOGGER.log(Level.SEVERE,
                        "getTimes failed for recordRange," + Arrays.toString(recordRange) + ", returning null", e);
                return null;
            }

        }

        @Override
        public double[] getTimes(final int[] startTime, final int[] stopTime) {
            return getTimes(startTime, stopTime, null);
        }

        @Override
        public double[] getTimes(final int[] startTime, final int[] stopTime, final TimeInstantModel ts) {

            if (startTime == null) {
                throw new IllegalArgumentException("start time is required");
            }

            if (stopTime == null) {
                throw new IllegalArgumentException("stop time is required");
            }

            if (startTime.length < 3) {
                throw new IllegalArgumentException("incomplete start time definition.");
            }

            long start = TSExtractor.getTime(startTime);

            if (stopTime.length < 3) {
                throw new IllegalArgumentException("incomplete stop time definition.");
            }

            long stop = TSExtractor.getTime(stopTime);

            if (isTT2000()) {
                start = (long) TimeUtil.milliSecondSince1970(start);
                stop = (long) TimeUtil.milliSecondSince1970(stop);
            }

            return getTimes(new double[] { start, stop }, ts);
        }

        @Override
        public double[] getTimes(final int[] recordRange, final TimeInstantModel ts) {
            return getTimes(recordRange[0], recordRange[1], ts);
        }

        @Override
        public double[] getTimes(final TimeInstantModel ts) {
            return getTimes(0, this.recordCount - 1, ts);
        }

        @Override
        public abstract boolean isTT2000();

        /**
         * Sets the record count.
         *
         * @param count the new record count
         */
        protected void setRecordCount(final int count) {
            this.recordCount = count;
        }

        abstract double[] getTimes(int first, int last, TimeInstantModel ts);

        abstract void reset();
    }

    /**
     * The Class CDFTT2000Variable.
     */
    public static class CDFTT2000Variable extends CDFTimeVariable {

        LongBuffer _lbuf;

        CDFTT2000Variable(final CDFImpl cdf, final String name, final ByteBuffer obuf) {
            super(cdf, name, obuf);
            this.precision = TimePrecision.NANOSECOND;
            this._lbuf = this.tbuf.asLongBuffer();
        }

        @Override
        public boolean canSupportPrecision(final TimePrecision tp) {
            return tp != TimePrecision.PICOSECOND;
        }

        @Override
        public double[] getTimes(final int first, final int last, final TimeInstantModel ts) {
            TimePrecision offsetUnits = TimePrecision.MILLISECOND;
            long base = JANUARY_1_1970_LONG;

            if (ts != null) {
                base = (long) ts.getBaseTime();
                offsetUnits = ts.getOffsetUnits();
            }

            int count = (last - first) + 1;
            double[] da = new double[count];
            ByteBuffer bbuf = this.tbuf.duplicate();
            bbuf.order(this.tbuf.order());
            LongBuffer lbuf = bbuf.asLongBuffer();

            if (offsetUnits == TimePrecision.MILLISECOND) {
                this.offset = base - TT2000_DATE;

                for (int i = first; i <= last; i++) {
                    long nano = lbuf.get(i);

                    if (nano == LONG_FILL) {
                        da[i - first] = Double.NaN;
                        continue;
                    }

                    long milli = (nano / 1_000_000) - this.offset;
                    double rem = (nano % 1_000_000) / 1.0e6;
                    da[i - first] = (milli) + rem;
                }

            } else {

                if (offsetUnits == TimePrecision.MICROSECOND) {
                    this.offset = 1_000 * (base - TT2000_DATE);

                    for (int i = first; i <= last; i++) {
                        long nano = lbuf.get(i);

                        if (nano == LONG_FILL) {
                            da[i - first] = Double.NaN;
                            continue;
                        }

                        long micro = (nano / 1_000) - this.offset;
                        double rem = (nano % 1_000) / 1.0e3;
                        da[i - first] = (micro) + rem;
                    }

                } else {

                    if (offsetUnits != TimePrecision.NANOSECOND) {
                        throw new IllegalArgumentException(
                                "You may request only millisecond, microsecond or nanosecond offset "
                                        + "for a variable whose time variable is TT2000 type.");
                    }

                    this.offset = 1_000_000 * (base - TT2000_DATE);

                    for (int i = first; i <= last; i++) {
                        long nano = lbuf.get(i);

                        if (nano == LONG_FILL) {
                            da[i - first] = Double.NaN;
                            continue;
                        }

                        da[i - first] = nano - this.offset;
                    }

                }

            }

            return da;
        }

        @Override
        public boolean isTT2000() {
            return true;
        }

        @Override
        void reset() {
            this._lbuf.position(0);
        }
    }

    /**
     * The Class UnixTimeVariable.
     */
    public static class UnixTimeVariable extends CDFTimeVariable {

        DoubleBuffer _dbuf;

        UnixTimeVariable(final CDFImpl cdf, final String name, final ByteBuffer obuf) {
            super(cdf, name, obuf);
            this.precision = TimePrecision.MICROSECOND;
            this._dbuf = this.tbuf.asDoubleBuffer();
        }

        @Override
        public boolean canSupportPrecision(final TimePrecision tp) {
            return (tp == TimePrecision.MICROSECOND) || (tp == TimePrecision.MILLISECOND);
        }

        @Override
        public double[] getTimes(final int first, final int last, final TimeInstantModel ts) {
            TimePrecision offsetUnits = TimePrecision.MILLISECOND;
            long base = JANUARY_1_1970_LONG;

            if (ts != null) {
                base = (long) ts.getBaseTime();
                offsetUnits = ts.getOffsetUnits();
            }

            int count = (last - first) + 1;
            double[] da = new double[count];
            ByteBuffer bbuf = this.tbuf.duplicate();
            bbuf.order(this.tbuf.order());
            DoubleBuffer dbuf = bbuf.asDoubleBuffer();
            dbuf.position(first);
            dbuf.get(da);

            if (offsetUnits == TimePrecision.MILLISECOND) {

                if (base == JANUARY_1_1970_LONG) {

                    for (int i = 0; i < count; i++) {

                        if (da[i] == DOUBLE_FILL) {
                            da[i] = Double.NaN;
                            continue;
                        }

                        da[i] *= 1.0e3;
                    }

                } else {
                    this.offset = base - JANUARY_1_1970_LONG;

                    for (int i = 0; i < count; i++) {

                        if (da[i] == DOUBLE_FILL) {
                            da[i] = Double.NaN;
                            continue;
                        }

                        long milli = (long) (da[i] * 1_000) - this.offset;
                        da[i] = (milli);
                    }

                }

            } else { // it must be micro second

                if (offsetUnits != TimePrecision.MICROSECOND) {
                    throw new IllegalArgumentException(
                            "Desired precision exceeds highest available precision -- microsecond");
                }

                if (base == JANUARY_1_1970) {

                    for (int i = 0; i < count; i++) {

                        if (da[i] == DOUBLE_FILL) {
                            da[i] = Double.NaN;
                            continue;
                        }

                        da[i] *= 1.0e6;
                    }

                } else {
                    this.offset = 1_000 * (base - JANUARY_1_1970_LONG);

                    for (int i = 0; i < count; i++) {

                        if (da[i] == DOUBLE_FILL) {
                            da[i] = Double.NaN;
                            continue;
                        }

                        long micro = (long) (da[i] * 1_000_000) - this.offset;
                        da[i] = (micro);
                    }

                }

            }

            return da;
        }

        @Override
        public boolean isTT2000() {
            return false;
        }

        @Override
        void reset() {
            this._dbuf.position(0);
        }
    }

    static class DefaultTimeInstantModelImpl implements TimeInstantModel {

        double baseTime = JANUARY_1_1970;

        TimePrecision baseTimeUnits = TimePrecision.MILLISECOND;

        TimePrecision offsetUnits = TimePrecision.MILLISECOND;

        DefaultTimeInstantModelImpl(final double baseTime, final TimePrecision baseTimeUnits,
                final TimePrecision offsetUnits) {
            this.baseTime = baseTime;
            this.baseTimeUnits = baseTimeUnits;
            this.offsetUnits = offsetUnits;
        }

        DefaultTimeInstantModelImpl() {
            this(JANUARY_1_1970, TimePrecision.MILLISECOND, TimePrecision.MILLISECOND);
        }

        @Override
        public DefaultTimeInstantModelImpl clone() {

            return new DefaultTimeInstantModelImpl(this.baseTime, this.baseTimeUnits, this.offsetUnits);
        }

        @Override
        public double getBaseTime() {
            return this.baseTime;
        }

        @Override
        public TimePrecision getBaseTimeUnits() {
            return this.baseTimeUnits;
        }

        @Override
        public TimePrecision getOffsetUnits() {
            return this.offsetUnits;
        }

        @Override
        public void setOffsetUnits(final TimePrecision offsetUnits) {
            this.offsetUnits = offsetUnits;
        }

        void setBaseTime(final double msec) {
            this.baseTime = msec;
        }
    }
}
