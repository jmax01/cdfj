package gov.nasa.gsfc.spdf.cdfj;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
// import gov.nasa.gsfc.spdf.common.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.LongBuffer;

/**
 * The Class DoubleVarContainer.
 *
 * @author nand
 */
public final class DoubleVarContainer extends BaseVarContainer implements VDataContainer.CDouble {

    final double[] dpad;

    /**
     * Instantiates a new double var container.
     *
     * @param thisCDF  the this CDF
     * @param variable the variable
     * @param pt       the pt
     * @param preserve the preserve
     */
    public DoubleVarContainer(final CDFImpl thisCDF, final Variable variable, final int[] pt, final boolean preserve) {
        this(thisCDF, variable, pt, preserve, ByteOrder.nativeOrder());
    }

    /**
     * Instantiates a new double var container.
     *
     * @param thisCDF  the this CDF
     * @param variable the var
     * @param pt       the pt
     * @param preserve the preserve
     * @param bo       the bo
     */
    public DoubleVarContainer(final CDFImpl thisCDF, final Variable variable, final int[] pt, final boolean preserve,
            final ByteOrder bo) {

        super(thisCDF, variable, pt, preserve, bo, Double.TYPE);

        Object pad = this.thisCDF.getPadValue(variable);

        if (DataTypes.typeCategory[this.type] == DataTypes.LONG) {
            long[] lpad = (long[]) pad;
            this.dpad = new double[lpad.length];

            for (int i = 0; i < lpad.length; i++) {
                this.dpad[i] = lpad[i];
            }

        } else {
            this.dpad = (double[]) pad;
        }

    }

    /**
     * Checks if is compatible.
     *
     * @param type     the type
     * @param preserve the preserve
     *
     * @return true, if is compatible
     */
    public static boolean isCompatible(final int type, final boolean preserve) {
        return isCompatible(type, preserve, Double.TYPE);
    }

    /**
     * As array.
     *
     * @return the object
     */
    public Object _asArray() {
        int rank = this.variable.getEffectiveRank();

        if (rank > 4) {
            throw new IllegalStateException("Ranks > 4 are not supported at this time.");
        }

        ByteBuffer buf = getBuffer();

        if (buf == null) {
            return null;
        }

        int words = (buf.remaining()) / 8;
        DoubleBuffer _buf = buf.asDoubleBuffer();
        int records = -1;

        switch (rank) {
            case 0:
                double[] _a0 = new double[words];
                _buf.get(_a0);
                return (this.singlePoint) ? Double.valueOf(_a0[0]) : _a0;
            case 1:
                int n = this.variable.getDimensionElementCounts()
                        .get(0);
                records = words / n;
                double[][] _a1 = new double[records][n];
                for (int r = 0; r < records; r++) {
                    _buf.get(_a1[r]);
                }
                return (this.singlePoint) ? _a1[0] : _a1;
            case 2:
                int n0 = this.variable.getDimensionElementCounts()
                        .get(0);
                int n1 = this.variable.getDimensionElementCounts()
                        .get(1);
                records = words / (n0 * n1);
                double[][][] _a2 = new double[records][n0][n1];
                if (this.variable.rowMajority()) {

                    for (int r = 0; r < records; r++) {

                        for (int e = 0; e < n0; e++) {
                            _buf.get(_a2[r][e]);
                        }

                    }

                } else {

                    for (int r = 0; r < records; r++) {

                        for (int e0 = 0; e0 < n1; e0++) {

                            for (int e1 = 0; e1 < n0; e1++) {
                                _a2[r][e1][e0] = _buf.get();
                            }

                        }

                    }

                }
                return (this.singlePoint) ? _a2[0] : _a2;
            case 3:
                n0 = this.variable.getDimensionElementCounts()
                        .get(0);
                n1 = this.variable.getDimensionElementCounts()
                        .get(1);
                int n2 = ((this.variable.getDimensionElementCounts()
                        .get(2)));
                records = words / (n0 * n1 * n2);
                double[][][][] _a3 = new double[records][n0][n1][n2];
                if (this.variable.rowMajority()) {

                    for (int r = 0; r < records; r++) {

                        for (int e0 = 0; e0 < n0; e0++) {

                            for (int e1 = 0; e1 < n1; e1++) {
                                _buf.get(_a3[r][e0][e1]);
                            }

                        }

                    }

                } else {

                    for (int r = 0; r < records; r++) {

                        for (int e0 = 0; e0 < n2; e0++) {

                            for (int e1 = 0; e1 < n1; e1++) {

                                for (int e2 = 0; e2 < n0; e2++) {
                                    _a3[r][e2][e1][e0] = _buf.get();
                                }

                            }

                        }

                    }

                }
                return (this.singlePoint) ? _a3[0] : _a3;
            case 4:
                n0 = this.variable.getDimensionElementCounts()
                        .get(0);
                n1 = this.variable.getDimensionElementCounts()
                        .get(1);
                n2 = ((this.variable.getDimensionElementCounts()
                        .get(2)));
                int n3 = ((this.variable.getDimensionElementCounts()
                        .get(3)));
                records = words / (n0 * n1 * n2 * n3);
                double[][][][][] _a4 = new double[records][n0][n1][n2][n3];
                if (this.variable.rowMajority()) {

                    for (int r = 0; r < records; r++) {

                        for (int e0 = 0; e0 < n0; e0++) {

                            for (int e1 = 0; e1 < n1; e1++) {

                                for (int e2 = 0; e2 < n2; e2++) {
                                    _buf.get(_a4[r][e0][e1][e2]);
                                }

                            }

                        }

                    }

                } else {

                    for (int r = 0; r < records; r++) {

                        for (int e0 = 0; e0 < n3; e0++) {

                            for (int e1 = 0; e1 < n2; e1++) {

                                for (int e2 = 0; e2 < n1; e2++) {

                                    for (int e3 = 0; e3 < n0; e3++) {
                                        _a4[r][e3][e2][e1][e0] = _buf.get();
                                    }

                                }

                            }

                        }

                    }

                }
                return (this.singlePoint) ? _a4[0] : _a4;
            default:
                // XXX: Not reachable
                throw new IllegalStateException("Rank > 4 not supported yet.");
        }

    }

    @Override
    public double[] as1DArray() {
        return (double[]) super.as1DArray();
    }

    @Override
    public DoubleArray asArray() {
        return new DoubleArray(_asArray());
    }

    /**
     * As array element.
     *
     * @param index0 the index 0
     * @param index1 the index 1
     *
     * @return the double[]
     */
    public double[] asArrayElement(final int index0, final int index1) {
        int rank = this.variable.getEffectiveRank();

        if (rank != 2) {
            throw new IllegalStateException("Rank other than 2 not supported, was " + rank);
        }

        int n0 = this.variable.getDimensionElementCounts()
                .get(0);

        if ((index0 < 0) || (index0 >= n0)) {
            throw new IllegalArgumentException("Invalid first index " + index0);
        }

        int n1 = this.variable.getDimensionElementCounts()
                .get(1);

        if ((index1 < 0) || (index1 >= n1)) {
            throw new IllegalArgumentException("Invalid second index " + index1);
        }

        int pointSize = n0 * n1;
        ByteBuffer buf = getBuffer();

        if (buf == null) {
            return null;
        }

        int words = (buf.remaining()) / 8;
        DoubleBuffer _buf = buf.asDoubleBuffer();
        int records = words / pointSize;
        double[] _a1 = new double[records];
        int loc = (this.variable.rowMajority()) ? ((n1 * index0) + index1) : ((n0 * index1) + index0);
        int pos = 0;

        for (int r = 0; r < records; r++) {
            _a1[r] = _buf.get(pos + loc);
            pos += pointSize;
        }

        return _a1;
    }
    /*
     * public double[] asSampledArray(int[] stride) {
     * int[] range = getRecordRange();
     * Stride strideObject = new Stride(stride);
     * int numberOfValues = range[1] - range[0] + 1;
     * int _stride = strideObject.getStride(numberOfValues);
     * if (_stride > 1) {
     * int n = (numberOfValues/_stride);
     * if ((numberOfValues % _stride) != 0) n++;
     * numberOfValues = n;
     * }
     * ByteBuffer buf = getBuffer();
     * if (buf == null) return null;
     * DoubleBuffer _buf = buf.asDoubleBuffer();
     * int words = elements*numberOfValues;
     * double[] sampled = new double[words];
     * int advance = _stride*elements;
     * int pos = 0;
     * int off = 0;
     * for (int i = 0; i < numberOfValues; i++) {
     * for (int w = 0; w < elements; w++) {
     * sampled[off++] = _buf.get(pos + w);
     * }
     * pos += advance;
     * }
     * return sampled;
     * }
     */

    /**
     * As array element.
     *
     * @param elements the elements
     *
     * @return the object
     */
    public Object asArrayElement(final int[] elements) {

        int rank = this.variable.getEffectiveRank();

        if (rank != 1) {
            throw new IllegalStateException("Rank > 1 not supported.");
        }

        if (!validElement(this.variable, elements)) {
            return null;
        }

        ByteBuffer buf = getBuffer();

        if (buf == null) {
            return null;
        }

        int words = (buf.remaining()) / 8;
        DoubleBuffer _buf = buf.asDoubleBuffer();
        int n = this.variable.getDimensionElementCounts()
                .get(0);
        int records = words / n;

        if (elements.length == 1) {
            int element = elements[0];
            double[] _a1 = new double[records];
            int pos = element;

            for (int r = 0; r < records; r++) {
                _a1[r] = _buf.get(pos);
                pos += n;
            }

            return _a1;
        }

        int ne = elements.length;
        double[][] data = new double[records][ne];
        int pos = 0;

        for (int r = 0; r < records; r++) {

            for (int i = 0; i < ne; i++) {
                data[r][i] = _buf.get(pos + elements[i]);
            }

            pos += n;
        }

        return data;
    }

    @Override
    public double[] asOneDArray() {
        return (double[]) super.asOneDArray(true);
    }

    @Override
    public double[] asOneDArray(final boolean cmtarget) {
        return (double[]) super.asOneDArray(cmtarget);
    }

    /**
     * Fill array.
     *
     * @param array  the array
     * @param offset the offset
     * @param first  the first
     * @param last   the last
     */
    public void fillArray(final double[] array, final int offset, final int first, final int last) {

        if (this.buffers.isEmpty()) {
            throw new IllegalStateException("buffer not available");
        }

        int words = ((last - first) + 1) * this.elements;
        ByteBuffer b = getBuffer();
        int pos = (first - getRecordRange()[0]) * this.elements * getLength();
        b.position(pos);
        b.asDoubleBuffer()
                .get(array, offset, words);
    }

    @Override
    ByteBuffer allocateBuffer(final int words) {
        ByteBuffer _buf = ByteBuffer.allocateDirect(8 * words);
        _buf.order(this.order);
        return _buf;
    }

    @Override
    Object allocateDataArray(final int size) {
        return new double[size];
    }

    @Override
    void doData(final ByteBuffer bv, final int type, final int elements, final int toprocess, final ByteBuffer _buf,
            final Object _data) {

        Method method = null;

        try {
            double[] data = (double[]) _data;

            int position = _buf.position();

            DoubleBuffer dbuf = _buf.asDoubleBuffer();

            int processed = 0;

            switch (DataTypes.typeCategory[type]) {
                case 0:
                    float[] tf = new float[data.length];
                    int ipos = bv.position();
                    FloatBuffer bvf = bv.asFloatBuffer();
                    while (processed < toprocess) {
                        int _num = (toprocess - processed) * elements;

                        if (_num > data.length) {
                            _num = data.length;
                        }

                        bvf.get(tf, 0, _num);
                        ipos += 4 * _num;

                        for (int n = 0; n < _num; n++) {
                            data[n] = tf[n];
                        }

                        dbuf.put(data, 0, _num);
                        position += 8 * _num;
                        processed += (_num / elements);
                    }
                    bv.position(ipos);
                    _buf.position(position);
                    break;
                case 1:
                    ipos = bv.position();
                    DoubleBuffer bvd = bv.asDoubleBuffer();
                    while (processed < toprocess) {
                        int _num = (toprocess - processed) * elements;

                        if (_num > data.length) {
                            _num = data.length;
                        }

                        bvd.get(data, 0, _num);
                        ipos += 8 * _num;
                        dbuf.put(data, 0, _num);
                        position += 8 * _num;
                        processed += (_num / elements);
                    }
                    bv.position(ipos);
                    _buf.position(position);
                    break;
                case 2:
                    method = DataTypes.method[type];
                    while (processed < toprocess) {
                        int _num = (toprocess - processed) * elements;

                        if (_num > data.length) {
                            _num = data.length;
                        }

                        for (int e = 0; e < _num; e++) {
                            Number num = (Number) method.invoke(bv);
                            data[e] = num.doubleValue();
                        }

                        dbuf.put(data, 0, _num);
                        position += 8 * _num;
                        processed += (_num / elements);
                    }
                    _buf.position(position);
                    break;
                case 3:
                    method = DataTypes.method[type];
                    long longInt = DataTypes.longInt[type];
                    while (processed < toprocess) {
                        int _num = (toprocess - processed) * elements;

                        if (_num > data.length) {
                            _num = data.length;
                        }

                        for (int e = 0; e < _num; e++) {
                            Number num = (Number) method.invoke(bv);
                            int x = num.intValue();
                            data[e] = (x >= 0) ? x : (longInt + x);
                        }

                        dbuf.put(data, 0, _num);
                        position += 8 * _num;
                        processed += (_num / elements);
                    }
                    _buf.position(position);
                    break;
                case 5:
                    ipos = bv.position();
                    LongBuffer bvl = bv.asLongBuffer();
                    long[] tl = new long[data.length];
                    while (processed < toprocess) {
                        int _num = (toprocess - processed) * elements;

                        if (_num > data.length) {
                            _num = data.length;
                        }

                        bvl.get(tl, 0, _num);
                        ipos += 8 * _num;

                        for (int n = 0; n < _num; n++) {
                            data[n] = tl[n];
                        }

                        dbuf.put(data, 0, _num);
                        position += 8 * _num;
                        processed += (_num / elements);
                    }
                    bv.position(ipos);
                    _buf.position(position);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized data type " + type);
            }

        }
        catch (IllegalAccessException | InvocationTargetException e) {

            throw new IllegalStateException("Failed to invoke method" + method, e);
        }

    }

    @Override
    void doMissing(final int records, final ByteBuffer _buf, final Object _data, final int rec) {

        double[] data = (double[]) _data;

        double[] repl = (rec < 0) ? this.dpad : this.variable.asDoubleArray(new int[] { rec });

        int position = _buf.position();
        DoubleBuffer dbuf = _buf.asDoubleBuffer();
        int rem = records;

        while (rem > 0) {
            int tofill = rem;

            if ((tofill * this.elements) > data.length) {
                tofill = data.length / this.elements;
            }

            int index = 0;

            for (int i = 0; i < tofill; i++) {

                for (int e = 0; e < this.elements; e++) {
                    data[index++] = repl[e];
                }

            }

            dbuf.put(data, 0, tofill * this.elements);
            position += 8 * tofill * this.elements;
            rem -= tofill;
        }

        _buf.position(position);
    }
}
