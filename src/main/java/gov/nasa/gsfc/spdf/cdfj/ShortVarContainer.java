package gov.nasa.gsfc.spdf.cdfj;

// import gov.nasa.gsfc.spdf.common.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

/**
 * The Class ShortVarContainer.
 *
 * @author nand
 */
public final class ShortVarContainer extends BaseVarContainer implements VDataContainer.CShort {

    final short[] spad;

    /**
     * Instantiates a new short var container.
     *
     * @param thisCDF  the this CDF
     * @param variable the var
     * @param pt       the pt
     * @param preserve the preserve
     */
    public ShortVarContainer(final CDFImpl thisCDF, final Variable variable, final int[] pt, final boolean preserve) {
        this(thisCDF, variable, pt, preserve, ByteOrder.nativeOrder());
    }

    /**
     * Instantiates a new short var container.
     *
     * @param thisCDF  the this CDF
     * @param variable the var
     * @param pt       the pt
     * @param preserve the preserve
     * @param bo       the bo
     *
     */
    public ShortVarContainer(final CDFImpl thisCDF, final Variable variable, final int[] pt, final boolean preserve,
            final ByteOrder bo) {
        super(thisCDF, variable, pt, preserve, bo, Short.TYPE);
        Object pad = this.thisCDF.getPadValue(variable);
        double[] dpad = (double[]) pad;
        this.spad = new short[dpad.length];

        for (int i = 0; i < dpad.length; i++) {
            this.spad[i] = (short) dpad[i];
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
        return isCompatible(type, preserve, Short.TYPE);
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

        int words = (buf.remaining()) / 2;
        ShortBuffer _buf = buf.asShortBuffer();
        int records = -1;

        switch (rank) {
            case 0:
                short[] _a0 = new short[words];
                _buf.get(_a0);
                return (this.singlePoint) ? Short.valueOf(_a0[0]) : _a0;
            case 1:
                int n = ((this.variable.getDimensionElementCounts()
                        .get(0)));
                records = words / n;
                short[][] _a1 = new short[records][n];
                for (int r = 0; r < records; r++) {
                    _buf.get(_a1[r]);
                }
                return (this.singlePoint) ? _a1[0] : _a1;
            case 2:
                int n0 = ((this.variable.getDimensionElementCounts()
                        .get(0)));
                int n1 = ((this.variable.getDimensionElementCounts()
                        .get(1)));
                records = words / (n0 * n1);
                short[][][] _a2 = new short[records][n0][n1];
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
                n0 = ((this.variable.getDimensionElementCounts()
                        .get(0)));
                n1 = ((this.variable.getDimensionElementCounts()
                        .get(1)));
                int n2 = ((this.variable.getDimensionElementCounts()
                        .get(2)));
                records = words / (n0 * n1 * n2);
                short[][][][] _a3 = new short[records][n0][n1][n2];
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
                n0 = ((this.variable.getDimensionElementCounts()
                        .get(0)));
                n1 = ((this.variable.getDimensionElementCounts()
                        .get(1)));
                n2 = ((this.variable.getDimensionElementCounts()
                        .get(2)));
                int n3 = ((this.variable.getDimensionElementCounts()
                        .get(3)));
                records = words / (n0 * n1 * n2 * n3);
                short[][][][][] _a4 = new short[records][n0][n1][n2][n3];
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
                throw new IllegalStateException("Rank > 4 not supported yet.");
        }

    }

    @Override
    public Object allocateDataArray(final int size) {
        return new short[size];
    }

    @Override
    public short[] as1DArray() {
        return (short[]) super.as1DArray();
    }

    @Override
    public ShortArray asArray() {
        return new ShortArray(_asArray());
    }

    @Override
    public short[] asOneDArray() {
        return (short[]) super.asOneDArray(true);
    }

    @Override
    public short[] asOneDArray(final boolean cmtarget) {
        return (short[]) super.asOneDArray(cmtarget);
    }

    /**
     * Fill array.
     *
     * @param array  the array
     * @param offset the offset
     * @param first  the first
     * @param last   the last
     */
    public void fillArray(final short[] array, final int offset, final int first, final int last) {

        if (this.buffers.isEmpty()) {
            throw new IllegalStateException("buffer not available");
        }

        int words = ((last - first) + 1) * this.elements;
        ByteBuffer b = getBuffer();
        int pos = (first - getRecordRange()[0]) * this.elements * getLength();
        b.position(pos);
        b.asShortBuffer()
                .get(array, offset, words);
    }

    @Override
    ByteBuffer allocateBuffer(final int words) {
        ByteBuffer _buf = ByteBuffer.allocateDirect(2 * words);
        _buf.order(this.order);
        return _buf;
    }

    @Override
    void doData(final ByteBuffer bv, final int type, final int elements, final int toprocess, final ByteBuffer _buf,
            final Object _data) {
        short[] data = (short[]) _data;
        int position = _buf.position();
        ShortBuffer sbuf = _buf.asShortBuffer();
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

                        sbuf.put(data, 0, _num);
                        position += 2 * _num;
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

                        bvs.get(data, 0, _num);
                        ipos += 2 * _num;
                        sbuf.put(data, 0, _num);
                        position += 2 * _num;
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
                            data[e] = (short) ((x < 0) ? (x + 256) : x);
                        }

                        sbuf.put(data, 0, _num);
                        position += 2 * _num;
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

                        bvs.get(data, 0, _num);
                        ipos += 2 * _num;
                        sbuf.put(data, 0, _num);
                        position += 2 * _num;
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
        short[] data = (short[]) _data;
        short[] repl = (rec < 0) ? this.spad : this.variable.asShortArray(new int[] { rec });

        int position = _buf.position();
        ShortBuffer sbuf = _buf.asShortBuffer();
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

            sbuf.put(data, 0, tofill * this.elements);
            position += 2 * tofill * this.elements;
            rem -= tofill;
        }

        _buf.position(position);
    }
}
