package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/**
 * The Class IntArray.
 *
 * @author nand
 */
public class IntArray extends AArray {

    /**
     * Instantiates a new int array.
     *
     * @param o the o
     */
    public IntArray(final Object o) {
        super(o);
    }

    /**
     * Instantiates a new int array.
     *
     * @param o        the o
     * @param majority the majority
     */
    public IntArray(final Object o, final boolean majority) {
        super(o, majority);
    }

    /**
     * create a byte buffer of a compatible type.
     *
     * @param ignore
     */
    @Override
    public ByteBuffer buffer(final Class<?> cl, final int ignore) {

        if (((cl != Integer.TYPE) && (cl != Short.TYPE))) {
            throw new IllegalArgumentException("Only int and short targets supported");
        }

        if (this.dimensions > 4) {
            throw new IllegalArgumentException("Rank > 4 not supported");
        }

        int elementSize = (cl == Short.TYPE) ? 2 : 4;
        ByteBuffer buf = allocate(elementSize);

        if (cl == Short.TYPE) {
            return doShort(buf);
        }

        return doInt(buf);
    }

    ByteBuffer doInt(final ByteBuffer buf) {

        int[] _dim = this.attributeArray.getDimensions();

        IntBuffer _buf = buf.asIntBuffer();

        switch (this.dimensions) {
            case 1:
                int[] data1 = (int[]) this.data;
                _buf.put(data1);
                return buf;
            case 2:
                int[][] data2 = (int[][]) this.data;
                for (int i = 0; i < _dim[0]; i++) {
                    _buf.put(data2[i]);
                }
                return buf;
            case 3:
                int[][][] data3 = (int[][][]) this.data;
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
                int[][][][] data4 = (int[][][][]) this.data;
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
                throw new IllegalArgumentException("Rank > 4 not supported");
        }

    }

    ByteBuffer doShort(final ByteBuffer buf) {

        int[] _dim = this.attributeArray.getDimensions();

        short[] temp = null;

        ShortBuffer _buf = buf.asShortBuffer();

        switch (this.dimensions) {
            case 1:
                int[] data1 = (int[]) this.data;
                temp = new short[data1.length];
                for (int i = 0; i < data1.length; i++) {
                    temp[i] = (short) data1[i];
                }
                _buf.put(temp);
                return buf;
            case 2:
                int[][] data2 = (int[][]) this.data;
                temp = new short[_dim[1]];
                for (int i = 0; i < _dim[0]; i++) {
                    int[] di = data2[i];

                    for (int j = 0; j < _dim[1]; j++) {
                        temp[j] = (short) di[j];
                    }

                    _buf.put(temp);
                }
                return buf;
            case 3:
                int[][][] data3 = (int[][][]) this.data;
                if (this.rowMajority) {
                    temp = new short[_dim[2]];

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {
                            int[] di = data3[i][j];

                            for (int k = 0; k < _dim[2]; k++) {
                                temp[k] = (short) di[k];
                            }

                            _buf.put(temp);
                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int k = 0; k < _dim[2]; k++) {

                            for (int j = 0; j < _dim[1]; j++) {
                                _buf.put((short) data3[i][j][k]);
                            }

                        }

                    }

                }
                return buf;
            case 4:
                int[][][][] data4 = (int[][][][]) this.data;
                if (this.rowMajority) {
                    temp = new short[_dim[3]];

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int k = 0; k < _dim[2]; k++) {
                                int[] di = data4[i][j][k];

                                for (int l = 0; l < _dim[3]; l++) {
                                    temp[l] = (short) di[l];
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
                                    _buf.put((short) data4[i][j][k][l]);
                                }

                            }

                        }

                    }

                }
                return buf;
            default:
                throw new IllegalArgumentException("Rank > 4 not supported");

        }

    }
}
