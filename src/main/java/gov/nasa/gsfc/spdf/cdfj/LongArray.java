package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

/**
 * The Class LongArray.
 *
 * @author nand
 */
public class LongArray extends AArray {

    /**
     * Instantiates a new long array.
     *
     * @param o the o
     * @
     */
    public LongArray(final Object o) {
        super(o);
    }

    /**
     * Instantiates a new long array.
     *
     * @param o        the o
     * @param majority the majority
     * @
     */
    public LongArray(final Object o, final boolean majority) {
        super(o, majority);
    }

    @Override
    public ByteBuffer buffer(final Class<?> cl, final int ignore) {

        if (((cl != Long.TYPE) && (cl != Integer.TYPE))) {
            throw new IllegalArgumentException("Only int and long targets supported");
        }

        if (this.dimensions > 4) {
            throw new IllegalStateException("Rank > 4 not supported");
        }

        int elementSize = (cl == Integer.TYPE) ? 4 : 8;
        ByteBuffer buf = allocate(elementSize);

        if (cl == Integer.TYPE) {
            return doInt(buf);
        }

        return doLong(buf);
    }

    ByteBuffer doInt(final ByteBuffer buf) {
        int[] _dim = this.attributeArray.getDimensions();
        int[] temp = null;
        IntBuffer _buf = buf.asIntBuffer();

        switch (this.dimensions) {
            case 1:
                long[] data = (long[]) this.data;
                temp = new int[data.length];
                for (int i = 0; i < data.length; i++) {
                    temp[i] = (int) data[i];
                }
                _buf.put(temp);
                return buf;
            case 2:
                long[][] data2 = (long[][]) this.data;
                temp = new int[_dim[1]];
                for (int i = 0; i < _dim[0]; i++) {
                    long[] di = data2[i];

                    for (int j = 0; j < _dim[1]; j++) {
                        temp[j] = (int) di[j];
                    }

                    _buf.put(temp);
                }
                return buf;
            case 3:
                long[][][] data3 = (long[][][]) this.data;
                if (this.rowMajority) {
                    temp = new int[_dim[2]];

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {
                            long[] di = data3[i][j];

                            for (int k = 0; k < _dim[2]; k++) {
                                temp[k] = (int) di[k];
                            }

                            _buf.put(temp);
                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int k = 0; k < _dim[2]; k++) {

                            for (int j = 0; j < _dim[1]; j++) {
                                _buf.put((int) data3[i][j][k]);
                            }

                        }

                    }

                }
                return buf;
            case 4:
                long[][][][] data4 = (long[][][][]) this.data;
                if (this.rowMajority) {
                    temp = new int[_dim[3]];

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int k = 0; k < _dim[2]; k++) {
                                long[] di = data4[i][j][k];

                                for (int l = 0; l < _dim[3]; l++) {
                                    temp[l] = (int) di[l];
                                }

                                _buf.put(temp);
                            }

                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int l = 0; l < _dim[3]; l++) {

                            for (int k = 0; k < _dim[2]; k++) {

                                for (int j = 0; j < _dim[1]; j++) {
                                    _buf.put((int) data4[i][j][k][l]);
                                }

                            }

                        }

                    }

                }
                return buf;
            default:
                throw new IllegalStateException("Rank > 4 not supported");
        }

    }

    ByteBuffer doLong(final ByteBuffer buf) {

        int[] _dim = this.attributeArray.getDimensions();

        LongBuffer _buf = buf.asLongBuffer();

        switch (this.dimensions) {
            case 1:
                long[] data = (long[]) this.data;
                _buf.put(data);
                return buf;
            case 2:
                long[][] data2 = (long[][]) this.data;
                for (int i = 0; i < _dim[0]; i++) {
                    _buf.put(data2[i]);
                }
                return buf;
            case 3:
                long[][][] data3 = (long[][][]) this.data;
                if (this.rowMajority) {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {
                            _buf.put(data3[i][j]);
                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int k = 0; k < _dim[2]; k++) {

                            for (int j = 0; j < _dim[1]; j++) {
                                _buf.put(data3[i][j][k]);
                            }

                        }

                    }

                }
                return buf;
            case 4:
                long[][][][] data4 = (long[][][][]) this.data;
                if (this.rowMajority) {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int k = 0; k < _dim[2]; k++) {
                                _buf.put(data4[i][j][k]);
                            }

                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int l = 0; l < _dim[3]; l++) {

                            for (int k = 0; k < _dim[2]; k++) {

                                for (int j = 0; j < _dim[1]; j++) {
                                    _buf.put(data4[i][j][k][l]);
                                }

                            }

                        }

                    }

                }
                return buf;

            default:
                throw new IllegalStateException("Rank > 4 not supported");
        }

    }
}
