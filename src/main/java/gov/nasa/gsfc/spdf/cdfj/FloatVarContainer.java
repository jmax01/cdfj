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
 * The Class FloatVarContainer.
 *
 * @author nand
 */
public final class FloatVarContainer extends BaseVarContainer implements VDataContainer.CFloat {

    final float[] fpad;

    /**
     * Instantiates a new float var container.
     *
     * @param thisCDF  the this CDF
     * @param var      the var
     * @param pt       the pt
     * @param preserve the preserve
     */
    public FloatVarContainer(final CDFImpl thisCDF, final Variable var, final int[] pt, final boolean preserve) {
        this(thisCDF, var, pt, preserve, ByteOrder.nativeOrder());
    }

    /**
     * Instantiates a new float var container.
     *
     * @param thisCDF  the this CDF
     * @param var      the var
     * @param pt       the pt
     * @param preserve the preserve
     * @param bo       the bo
     */
    public FloatVarContainer(final CDFImpl thisCDF, final Variable var, final int[] pt, final boolean preserve,
            final ByteOrder bo) {
        super(thisCDF, var, pt, preserve, bo, Float.TYPE);
        Object pad = this.thisCDF.getPadValue(var);

        if (DataTypes.typeCategory[this.type] == DataTypes.LONG) {
            long[] lpad = (long[]) pad;
            this.fpad = new float[lpad.length];

            for (int i = 0; i < lpad.length; i++) {
                this.fpad[i] = lpad[i];
            }

        } else {
            double[] dpad = (double[]) pad;
            this.fpad = new float[dpad.length];

            for (int i = 0; i < dpad.length; i++) {
                this.fpad[i] = (float) dpad[i];
            }

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
        return isCompatible(type, preserve, Float.TYPE);
    }

    /**
     * As array.
     *
     * @return the object
     */
    public Object _asArray() {

        int rank = this.var.getEffectiveRank();

        if (rank > 4) {
            throw new IllegalStateException("Ranks > 4 are not supported at this time.ran");
        }

        ByteBuffer buf = getBuffer();

        if (buf == null) {
            return null;
        }

        int words = (buf.remaining()) / 4;
        FloatBuffer _buf = buf.asFloatBuffer();
        int records = -1;

        switch (rank) {
            case 0:
                float[] _a0 = new float[words];
                _buf.get(_a0);
                return (this.singlePoint) ? Float.valueOf(_a0[0]) : _a0;
            case 1:
                int n = ((this.var.getDimensionElementCounts()
                        .get(0)));
                records = words / n;
                float[][] _a1 = new float[records][n];
                for (int r = 0; r < records; r++) {
                    _buf.get(_a1[r]);
                }
                return (this.singlePoint) ? _a1[0] : _a1;
            case 2:
                int n0 = ((this.var.getDimensionElementCounts()
                        .get(0)));
                int n1 = ((this.var.getDimensionElementCounts()
                        .get(1)));
                records = words / (n0 * n1);
                float[][][] _a2 = new float[records][n0][n1];
                if (this.var.rowMajority()) {

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
                n0 = ((this.var.getDimensionElementCounts()
                        .get(0)));
                n1 = ((this.var.getDimensionElementCounts()
                        .get(1)));
                int n2 = ((this.var.getDimensionElementCounts()
                        .get(2)));
                records = words / (n0 * n1 * n2);
                float[][][][] _a3 = new float[records][n0][n1][n2];
                if (this.var.rowMajority()) {

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
                n0 = ((this.var.getDimensionElementCounts()
                        .get(0)));
                n1 = ((this.var.getDimensionElementCounts()
                        .get(1)));
                n2 = ((this.var.getDimensionElementCounts()
                        .get(2)));
                int n3 = ((this.var.getDimensionElementCounts()
                        .get(3)));
                records = words / (n0 * n1 * n2 * n3);
                float[][][][][] _a4 = new float[records][n0][n1][n2][n3];
                if (this.var.rowMajority()) {

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
                throw new IllegalStateException("Ranks > 4 not yet supported .");
        }

    }

    @Override
    public Object allocateDataArray(final int size) {
        return new float[size];
    }

    @Override
    public float[] as1DArray() {
        return (float[]) super.as1DArray();
    }

    @Override
    public FloatArray asArray() {
        return new FloatArray(_asArray());
    }

    @Override
    public float[] asOneDArray() {
        return (float[]) super.asOneDArray(true);
    }

    @Override
    public float[] asOneDArray(final boolean cmtarget) {
        return (float[]) super.asOneDArray(cmtarget);
    }

    /**
     * Fill array.
     *
     * @param array  the array
     * @param offset the offset
     * @param first  the first
     * @param last   the last
     */
    public void fillArray(final float[] array, final int offset, final int first, final int last) {

        if (this.buffers.isEmpty()) {
            throw new IllegalStateException("buffer not available");
        }

        int words = ((last - first) + 1) * this.elements;
        ByteBuffer b = getBuffer();
        int pos = (first - getRecordRange()[0]) * this.elements * getLength();
        b.position(pos);
        b.asFloatBuffer()
                .get(array, offset, words);
    }

    @Override
    ByteBuffer allocateBuffer(final int words) {
        ByteBuffer _buf = ByteBuffer.allocateDirect(4 * words);
        _buf.order(this.order);
        return _buf;
    }

    @Override
    void doData(final ByteBuffer bv, final int type, final int elements, final int toprocess, final ByteBuffer _buf,
            final Object _data) {
        float[] data = (float[]) _data;
        int position = _buf.position();
        FloatBuffer fbuf = _buf.asFloatBuffer();
        Method method = null;

        try {

            int processed = 0;

            switch (DataTypes.typeCategory[type]) {
                case 0:
                    int ipos = bv.position();
                    FloatBuffer bvf = bv.asFloatBuffer();
                    while (processed < toprocess) {
                        int _num = (toprocess - processed) * elements;

                        if (_num > data.length) {
                            _num = data.length;
                        }

                        bvf.get(data, 0, _num);
                        ipos += 4 * _num;
                        fbuf.put(data, 0, _num);
                        position += 4 * _num;
                        processed += (_num / elements);
                    }
                    bv.position(ipos);
                    _buf.position(position);
                    break;
                case 1:
                    double[] td = new double[data.length];
                    ipos = bv.position();
                    DoubleBuffer bvd = bv.asDoubleBuffer();
                    while (processed < toprocess) {
                        int _num = (toprocess - processed) * elements;

                        if (_num > data.length) {
                            _num = data.length;
                        }

                        bvd.get(td, 0, _num);
                        ipos += 8 * _num;

                        for (int n = 0; n < _num; n++) {
                            data[n] = (float) td[n];
                        }

                        fbuf.put(data, 0, _num);
                        position += 4 * _num;
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
                            data[e] = num.floatValue();
                        }

                        fbuf.put(data, 0, _num);
                        position += 4 * _num;
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

                        fbuf.put(data, 0, _num);
                        position += 4 * _num;
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

                        fbuf.put(data, 0, _num);
                        position += 4 * _num;
                        processed += (_num / elements);
                    }
                    bv.position(ipos);
                    _buf.position(position);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized data type " + type);
            }

        } catch (IllegalAccessException | InvocationTargetException e) {

            throw new IllegalStateException("Attempt to execute method, " + method + "failed", e);
        }

    }

    @Override
    void doMissing(final int records, final ByteBuffer _buf, final Object _data, final int rec) {
        float[] data = (float[]) _data;
        float[] repl = null;

        try {
            repl = (rec < 0) ? this.fpad : this.var.asFloatArray(new int[] { rec });
        } catch (RuntimeException th) {
            th.printStackTrace();
            System.out.println("Should not see this.");
        }

        int position = _buf.position();
        FloatBuffer fbuf = _buf.asFloatBuffer();
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

            fbuf.put(data, 0, tofill * this.elements);
            position += 4 * tofill * this.elements;
            rem -= tofill;
        }

        _buf.position(position);
    }
}
