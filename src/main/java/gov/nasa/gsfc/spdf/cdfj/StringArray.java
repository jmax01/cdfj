package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;

/**
 * The Class StringArray.
 */
public class StringArray extends AArray {

    /**
     * Instantiates a new string array.
     *
     * @param o the o
     */
    public StringArray(final Object o) {
        super(o);
    }

    /**
     * Instantiates a new string array.
     *
     * @param o        the o
     * @param majority the majority
     */
    public StringArray(final Object o, final boolean majority) {
        super(o, majority);
    }

    /**
     * create a byte buffer of a compatible type.
     */
    @Override
    public ByteBuffer buffer(final Class<?> cl, final int size) {

        if (cl != String.class) {
            throw new IllegalArgumentException(cl.getCanonicalName() + " is not type String");
        }

        if (this.dimensions > 4) {
            throw new IllegalStateException("Rank > 4 not supported, is " + this.dimensions);
        }

        ByteBuffer buf = allocate(size);

        int[] _dim = this.attributeArray.getDimensions();

        switch (this.dimensions) {
            case 1:
                String[] _s1 = (String[]) this.data;
                addString(buf, _s1, size);
                buf.flip();
                return buf;
            case 2:
                String[][] _s2 = (String[][]) this.data;
                for (int i = 0; i < _dim[0]; i++) {
                    addString(buf, _s2[i], size);
                }
                buf.flip();
                return buf;
            case 3:
                String[][][] _s3 = (String[][][]) this.data;
                if (this.rowMajority) {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {
                            addString(buf, _s3[i][j], size);
                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int k = 0; k < _dim[2]; k++) {

                            for (int j = 0; j < _dim[1]; j++) {
                                addString(buf, _s3[i][j][k], size);
                            }

                        }

                    }

                }
                buf.flip();
                return buf;
            case 4:
                String[][][][] _s4 = (String[][][][]) this.data;
                if (this.rowMajority) {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int k = 0; k < _dim[2]; k++) {
                                addString(buf, _s4[i][j][k], size);
                            }

                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int l = 0; l < _dim[3]; l++) {

                            for (int k = 0; k < _dim[2]; k++) {

                                for (int j = 0; j < _dim[1]; j++) {
                                    addString(buf, _s4[i][j][k][l], size);
                                }

                            }

                        }

                    }

                }
                buf.flip();
                return buf;
            default:
                throw new IllegalStateException("Rank > 4 not supported, is " + this.dimensions);
        }

    }

    /*
     * void addString(ByteBuffer buf, String[] sa, int max) {
     * for (int i = 0; i < sa.length; i++) {
     * int len = sa[i].length();
     * if (len > max) throw new Throwable("String " + sa[i] +
     * " is longer than the specified max " + max);
     * byte[] _bar = sa[i].getBytes();
     * buf.put(_bar);
     * for (int f = 0; f < (max - _bar.length); f++) {
     * buf.put((byte)0x20);
     * }
     * }
     * }
     */
    void addString(final ByteBuffer buf, final String s, final int max) {
        int len = s.length();

        if (len > max) {
            throw new IllegalArgumentException("String " + s + " is longer than the specified max " + max);
        }

        byte[] _bar = s.getBytes();
        buf.put(_bar);

        for (int f = 0; f < (max - _bar.length); f++) {
            buf.put((byte) 0x20);
        }

    }

    void addString(final ByteBuffer buf, final String[] sa, final int max) {

        for (String sa1 : sa) {
            addString(buf, sa1, max);
        }

    }
}
