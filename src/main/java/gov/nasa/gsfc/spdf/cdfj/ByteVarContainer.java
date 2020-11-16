package gov.nasa.gsfc.spdf.cdfj;

// import gov.nasa.gsfc.spdf.common.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * The Class ByteVarContainer.
 *
 * @author nand
 */
public class ByteVarContainer extends BaseVarContainer implements VDataContainer.CByte {

    final byte[] bpad;

    /**
     * Instantiates a new byte var container.
     *
     * @param thisCDF the this CDF
     * @param var     the var
     * @param pt      the pt
     */
    public ByteVarContainer(final CDFImpl thisCDF, final Variable var, final int[] pt) {
        super(thisCDF, var, pt, true, ByteOrder.BIG_ENDIAN, Byte.TYPE);

        Object pad = this.thisCDF.getPadValue(var);

        if (DataTypes.isStringType(this.type)) {
            String[] sa = (String[]) pad;
            int count = 0;

            for (String sa1 : sa) {
                count += sa1.length();
            }

            this.bpad = new byte[count];
            count = 0;

            for (String sa1 : sa) {
                byte[] ba = sa1.getBytes();

                for (byte value : ba) {
                    this.bpad[count++] = value;
                }

            }

        } else {
            double[] dpad = (double[]) pad;
            this.bpad = new byte[dpad.length];

            for (int i = 0; i < dpad.length; i++) {
                this.bpad[i] = (byte) dpad[i];
            }

        }

    }

    /**
     * Checks if is compatible.
     *
     * @param type   the type
     * @param strict the strict
     *
     * @return true, if is compatible
     */
    public static boolean isCompatible(final int type, final boolean strict) {
        /*
         * boolean stringType = DataTypes.isStringType(type);
         * if (stringType) return false;
         */
        return isCompatible(type, strict, Byte.TYPE);
    }

    /**
     * As array.
     *
     * @return the object
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

        int words = buf.remaining();
        int records = -1;

        switch (rank) {
            case 0:
                if (this.singlePoint) {
                    return buf.get();
                }
                byte[] ba = new byte[words];
                buf.get(ba);
                return ba;
            case 1:
                int n = this.var.getDimensionElementCounts()
                        .get(0);
                records = words / n;
                byte[][] ba1 = new byte[records][n];
                for (int r = 0; r < records; r++) {
                    buf.get(ba1[r]);
                }
                return (this.singlePoint) ? ba1[0] : ba1;
            case 2:
                int n0 = this.var.getDimensionElementCounts()
                        .get(0);
                int n1 = this.var.getDimensionElementCounts()
                        .get(1);
                records = words / (n0 * n1);
                byte[][][] ba2 = new byte[records][n0][n1];
                for (int r = 0; r < records; r++) {

                    for (int e0 = 0; e0 < n0; e0++) {
                        buf.get(ba2[r][e0]);
                    }

                }
                return (this.singlePoint) ? ba2[0] : ba2;
            case 3:
                n0 = this.var.getDimensionElementCounts()
                        .get(0);
                n1 = this.var.getDimensionElementCounts()
                        .get(1);
                int n2 = ((this.var.getDimensionElementCounts()
                        .get(2)));
                records = words / (n0 * n1 * n2);
                byte[][][][] ba3 = new byte[records][n0][n1][n2];
                for (int r = 0; r < records; r++) {

                    for (int e0 = 0; e0 < n0; e0++) {

                        for (int e1 = 0; e1 < n1; e1++) {
                            buf.get(ba3[r][e0][e1]);
                        }

                    }

                }
                return (this.singlePoint) ? ba3[0] : ba3;
            case 4:
                n0 = this.var.getDimensionElementCounts()
                        .get(0);
                n1 = this.var.getDimensionElementCounts()
                        .get(1);
                n2 = ((this.var.getDimensionElementCounts()
                        .get(2)));
                int n3 = ((this.var.getDimensionElementCounts()
                        .get(3)));
                records = words / (n0 * n1 * n2 * n3);
                byte[][][][][] ba4 = new byte[records][n0][n1][n2][n3];
                for (int r = 0; r < records; r++) {

                    for (int e0 = 0; e0 < n0; e0++) {

                        for (int e1 = 0; e1 < n1; e1++) {

                            for (int e2 = 0; e2 < n2; e2++) {
                                buf.get(ba4[r][e0][e1][e2]);
                            }

                        }

                    }

                }
                return (this.singlePoint) ? ba4[0] : ba4;
            default:
                throw new IllegalStateException("Ranks of " + rank + " are not supported");
        }

    }

    @Override
    public Object allocateDataArray(final int size) {
        return null;
    }

    @Override
    public byte[] as1DArray() {
        return (byte[]) super.as1DArray();
    }

    @Override
    public AArray asArray() {
        return new ByteArray(_asArray());
    }

    @Override
    public byte[] asOneDArray() {
        return (byte[]) super.asOneDArray(true);
    }

    @Override
    public byte[] asOneDArray(final boolean cmtarget) {
        return (byte[]) super.asOneDArray(cmtarget);
    }

    /**
     * Fill array.
     *
     * @param array  the array
     * @param offset the offset
     * @param first  the first
     * @param last   the last
     */
    public void fillArray(final byte[] array, final int offset, final int first, final int last) {

        if (this.buffers.isEmpty()) {
            throw new IllegalStateException("buffer not available, size is zero.");
        }

        int words = ((last - first) + 1) * this.elements;
        ByteBuffer b = getBuffer();
        int pos = (first - getRecordRange()[0]) * this.elements * getLength();
        b.position(pos);
        b.get(array, offset, words);
    }

    @Override
    ByteBuffer allocateBuffer(final int words) {
        ByteBuffer _buf = ByteBuffer.allocateDirect(words);
        _buf.order(this.order);
        return _buf;
    }

    @Override
    void doData(final ByteBuffer bv, final int _type, final int _elements, final int toprocess, final ByteBuffer _buf,
            final Object _data) {
        ByteBuffer needed = bv.slice();
        needed.limit(this.itemSize * toprocess);
        _buf.put(needed);
    }

    @Override
    void doMissing(final int records, final ByteBuffer buf, final Object _data, final int rec) {

        byte[] repl = (rec < 0) ? this.bpad : this.var.asByteArray(new int[] { rec });

        byte[] ba = new byte[records * this.elements];

        int n = 0;

        for (int i = 0; i < records; i++) {

            ba[n++] = repl[0];

            for (int j = 1; j < repl.length; j++) {
                ba[n++] = repl[j];
            }

        }

        buf.put(ba, 0, records * this.elements);

    }
}
