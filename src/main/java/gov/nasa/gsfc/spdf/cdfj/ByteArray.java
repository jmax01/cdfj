package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;

/**
 * The Class ByteArray.
 */
public class ByteArray extends AArray {

    /**
     * Instantiates a new byte array.
     *
     * @param o the o
     */
    public ByteArray(final Object o) {
        super(o);
    }

    /**
     * Instantiates a new byte array.
     *
     * @param o           the o
     * @param rowMajority the row majority
     */
    public ByteArray(final Object o, final boolean rowMajority) {
        super(o, rowMajority);
    }

    @Override
    public ByteBuffer buffer(final Class<?> cl, final int ignore) {

        if (cl != Byte.TYPE) {
            throw new IllegalArgumentException("Only byte targets supported");
        }

        if (this.dimensions > 4) {
            throw new IllegalStateException("Rank > 4 not supported");
        }

        ByteBuffer buf = allocate(1);

        int[] _dim = this.attributeArray.getDimensions();

        switch (this.dimensions) {
            case 1:
                byte[] data1 = (byte[]) this.data;
                buf.put(data1);
                buf.flip();
                return buf;
            case 2:
                byte[][] data2 = (byte[][]) this.data;
                for (int i = 0; i < _dim[0]; i++) {
                    buf.put(data2[i]);
                }
                buf.flip();
                return buf;
            case 3:
                byte[][][] data3 = (byte[][][]) this.data;
                if (this.rowMajority) {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {
                            buf.put(data3[i][j]);
                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int k = 0; k < _dim[2]; k++) {

                            for (int j = 0; j < _dim[1]; j++) {
                                buf.put(data3[i][j][k]);
                            }

                        }

                    }

                }
                buf.flip();
                return buf;
            case 4:
                byte[][][][] data4 = (byte[][][][]) this.data;
                if (this.rowMajority) {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int k = 0; k < _dim[2]; k++) {
                                buf.put(data4[i][j][k]);
                            }

                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int l = 0; l < _dim[3]; l++) {

                            for (int k = 0; k < _dim[2]; k++) {

                                for (int j = 0; j < _dim[1]; j++) {
                                    buf.put(data4[i][j][k][l]);
                                }

                            }

                        }

                    }

                }
                buf.flip();
                return buf;
            default:
                throw new IllegalStateException("Rank > 4 not supported");
        }

    }
}
