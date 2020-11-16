package gov.nasa.gsfc.spdf.cdfj;

// import gov.nasa.gsfc.spdf.common.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * The Class IntVarContainer.
 *
 * @author nand
 */
public final class IntVarContainer extends BaseVarContainer implements VDataContainer.CInt {

    final int[] ipad;

    /**
     * Instantiates a new int var container.
     *
     * @param thisCDF  the this CDF
     * @param var      the var
     * @param pt       the pt
     * @param preserve the preserve
     * @
     */
    public IntVarContainer(final CDFImpl thisCDF, final Variable var, final int[] pt, final boolean preserve) {
        this(thisCDF, var, pt, preserve, ByteOrder.nativeOrder());
    }

    /**
     * Instantiates a new int var container.
     *
     * @param thisCDF  the this CDF
     * @param var      the var
     * @param pt       the pt
     * @param preserve the preserve
     * @param bo       the bo
     * @
     */
    public IntVarContainer(final CDFImpl thisCDF, final Variable var, final int[] pt, final boolean preserve,
            final ByteOrder bo) {
        super(thisCDF, var, pt, preserve, bo, Integer.TYPE);
        Object pad = this.thisCDF.getPadValue(var);
        double[] dpad = (double[]) pad;
        this.ipad = new int[dpad.length];

        for (int i = 0; i < dpad.length; i++) {
            this.ipad[i] = (int) dpad[i];
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
        return isCompatible(type, preserve, Integer.TYPE);
    }

    /**
     * As array.
     *
     * @return the object
     * @
     */
    public Object _asArray() {
        int rank = this.var.getEffectiveRank();

        if (rank > 4) {
            throw new IllegalStateException("Ranks > 4 are not supported at this time.");
        }

        ByteBuffer buf = getBuffer();

        if (buf == null) {
            return null;
        }

        int words = (buf.remaining()) / 4;
        IntBuffer _buf = buf.asIntBuffer();
        int records = -1;

        switch (rank) {
            case 0:
                int[] _a0 = new int[words];
                _buf.get(_a0);
                return (this.singlePoint) ? Integer.valueOf(_a0[0]) : _a0;
            case 1:
                int n = this.var.getDimensionElementCounts()
                        .get(0);
                records = words / n;
                int[][] _a1 = new int[records][n];
                for (int r = 0; r < records; r++) {
                    _buf.get(_a1[r]);
                }
                return (this.singlePoint) ? _a1[0] : _a1;
            case 2:
                int n0 = this.var.getDimensionElementCounts()
                        .get(0);
                int n1 = this.var.getDimensionElementCounts()
                        .get(1);
                records = words / (n0 * n1);
                int[][][] _a2 = new int[records][n0][n1];
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
                n0 = this.var.getDimensionElementCounts()
                        .get(0);
                n1 = this.var.getDimensionElementCounts()
                        .get(1);
                int n2 = this.var.getDimensionElementCounts()
                        .get(2);
                records = words / (n0 * n1 * n2);
                int[][][][] _a3 = new int[records][n0][n1][n2];
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
                n0 = this.var.getDimensionElementCounts()
                        .get(0);
                n1 = this.var.getDimensionElementCounts()
                        .get(1);
                n2 = this.var.getDimensionElementCounts()
                        .get(2);
                int n3 = this.var.getDimensionElementCounts()
                        .get(3);
                records = words / (n0 * n1 * n2 * n3);
                int[][][][][] _a4 = new int[records][n0][n1][n2][n3];
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
        return new int[size];
    }

    @Override
    public int[] as1DArray() {
        return (int[]) super.as1DArray();
    }

    @Override
    public IntArray asArray() {
        return new IntArray(_asArray());
    }

    @Override
    public int[] asOneDArray() {
        return (int[]) super.asOneDArray(true);
    }

    @Override
    public int[] asOneDArray(final boolean cmtarget) {
        return (int[]) super.asOneDArray(cmtarget);
    }

    /**
     * Fill array.
     *
     * @param array  the array
     * @param offset the offset
     * @param first  the first
     * @param last   the last
     * @
     */
    public void fillArray(final int[] array, final int offset, final int first, final int last) {

        if (this.buffers.isEmpty()) {
            throw new IllegalStateException("buffer not available");
        }

        int words = ((last - first) + 1) * this.elements;
        ByteBuffer b = getBuffer();
        int pos = (first - getRecordRange()[0]) * this.elements * getLength();
        b.position(pos);
        b.asIntBuffer()
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
        int[] data = (int[]) _data;
        int position = _buf.position();
        IntBuffer ibuf = _buf.asIntBuffer();
        int processed = 0;
        int ipos;

        switch (DataTypes.typeCategory[type]) {
            case 2:
                if ((type == 1) || (type == 41)) {

                    while (processed < toprocess) {
                        int _num = (toprocess - processed) * elements;

                        if (_num > data.length) {
                            _num = data.length;
                        }

                        for (int e = 0; e < _num; e++) {
                            data[e] = bv.get();
                        }

                        ibuf.put(data, 0, _num);
                        position += 4 * _num;
                        processed += (_num / elements);
                    }

                    _buf.position(position);
                    return;
                }
                if (type == 2) {
                    ipos = bv.position();
                    ShortBuffer bvs = bv.asShortBuffer();

                    while (processed < toprocess) {
                        int _num = (toprocess - processed) * elements;

                        if (_num > data.length) {
                            _num = data.length;
                        }

                        for (int e = 0; e < _num; e++) {
                            data[e] = bvs.get();
                        }

                        ipos += 2 * _num;
                        ibuf.put(data, 0, _num);
                        position += 4 * _num;
                        processed += (_num / elements);
                    }

                    bv.position(ipos);
                    _buf.position(position);
                    return;
                }
                if (type == 4) {
                    ipos = bv.position();
                    IntBuffer bvi = bv.asIntBuffer();

                    while (processed < toprocess) {
                        int _num = (toprocess - processed) * elements;

                        if (_num > data.length) {
                            _num = data.length;
                        }

                        bvi.get(data, 0, _num);
                        ipos += 4 * _num;
                        ibuf.put(data, 0, _num);
                        position += 4 * _num;
                        processed += (_num / elements);
                    }

                    bv.position(ipos);
                    _buf.position(position);
                    return;
                }
            case 3:
                if (type == 11) {

                    while (processed < toprocess) {
                        int _num = (toprocess - processed) * elements;

                        if (_num > data.length) {
                            _num = data.length;
                        }

                        for (int e = 0; e < _num; e++) {
                            int x = bv.get();
                            data[e] = (x < 0) ? (x + 256) : x;
                        }

                        ibuf.put(data, 0, _num);
                        position += 4 * _num;
                        processed += (_num / elements);
                    }

                    _buf.position(position);
                    return;
                }
                if (type == 12) {
                    ipos = bv.position();
                    ShortBuffer bvs = bv.asShortBuffer();

                    while (processed < toprocess) {
                        int _num = (toprocess - processed) * elements;

                        if (_num > data.length) {
                            _num = data.length;
                        }

                        for (int e = 0; e < _num; e++) {
                            int x = bvs.get();
                            data[e] = (x < 0) ? (x + (1 << 16)) : x;
                        }

                        ipos += 2 * _num;
                        ibuf.put(data, 0, _num);
                        position += 4 * _num;
                        processed += (_num / elements);
                    }

                    bv.position(ipos);
                    _buf.position(position);
                    return;
                }
                if (type == 14) {
                    ipos = bv.position();
                    IntBuffer bvi = bv.asIntBuffer();

                    while (processed < toprocess) {
                        int _num = (toprocess - processed) * elements;

                        if (_num > data.length) {
                            _num = data.length;
                        }

                        bvi.get(data, 0, _num);
                        ipos += 4 * _num;
                        ibuf.put(data, 0, _num);
                        position += 4 * _num;
                        processed += (_num / elements);
                    }

                    bv.position(ipos);
                    _buf.position(position);
                    return;
                }
            default:
                throw new IllegalArgumentException("Unrecognized type " + type);
        }

    }

    @Override
    void doMissing(final int records, final ByteBuffer _buf, final Object _data, final int rec) {

        int[] data = (int[]) _data;

        int[] repl = (rec < 0) ? this.ipad : this.var.asIntArray(new int[] { rec });

        int position = _buf.position();
        IntBuffer ibuf = _buf.asIntBuffer();
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

            ibuf.put(data, 0, tofill * this.elements);
            position += 4 * tofill * this.elements;
            rem -= tofill;
        }

        _buf.position(position);
    }
}
