package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;

/**
 * The Class StringVarContainer.
 *
 * @author nand
 */
public final class StringVarContainer extends ByteVarContainer implements VDataContainer.CString {

    /**
     * Instantiates a new string var container.
     *
     * @param thisCDF the this CDF
     * @param var     the var
     * @param pt      the pt
     */
    public StringVarContainer(final CDFImpl thisCDF, final Variable var, final int[] pt) {
        super(thisCDF, var, pt);
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

        if (isCompatible(type, preserve, Byte.TYPE)) {
            boolean stringType = DataTypes.isStringType(type);
            return stringType;
        }

        return false;
    }

    @Override
    public Object _asArray() {
        int rank = this.var.getEffectiveRank();

        if (rank > 1) {
            throw new IllegalStateException("Rank > 1 not supported for strings.");
        }

        ByteBuffer buf = getBuffer();

        if (buf == null) {
            return null;
        }

        int words = buf.remaining();
        int records = -1;
        int len = this.var.getNumberOfElements();
        byte[] ba = new byte[len];

        switch (rank) {
            case 0:
                records = words / len;
                String[] sa = new String[records];
                for (int r = 0; r < records; r++) {
                    buf.get(ba);
                    sa[r] = new String(ba);
                }
                return sa;
            case 1:
                int n0 = this.var.getDimensionElementCounts()
                        .get(0);
                records = words / (n0 * len);
                String[][] sa1 = new String[records][n0];
                for (int r = 0; r < records; r++) {

                    for (int e = 0; e < n0; e++) {
                        buf.get(ba);
                        sa1[r][e] = new String(ba);
                    }

                }
                return sa1;
            default:
                throw new IllegalStateException("Rank > 1 not supported for strings.");
        }

    }

    @Override
    public byte[] as1DArray() {
        return super.as1DArray();
    }

    @Override
    public AArray asArray() {
        return new StringArray(_asArray());
    }

    @Override
    public byte[] asOneDArray() {
        return super.asOneDArray();
    }

    @Override
    public byte[] asOneDArray(final boolean cmtarget) {
        return super.asOneDArray(cmtarget);
    }
}
