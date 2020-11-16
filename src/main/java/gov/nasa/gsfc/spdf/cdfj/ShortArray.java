package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * The Class ShortArray.
 *
 * @author nand
 */
public class ShortArray extends AArray {

    /**
     * Instantiates a new short array.
     *
     * @param o the o
     */
    public ShortArray(final Object o) {
        super(o);
    }

    /**
     * Instantiates a new short array.
     *
     * @param o        the o
     * @param majority the majority
     */
    public ShortArray(final Object o, final boolean majority) {
        super(o, majority);
    }

    /**
     * Buffer.
     *
     * @param cl     the cl
     * @param ignore the ignore
     *
     * @return the byte buffer
     */
    @Override
    public ByteBuffer buffer(final Class<?> cl, final int ignore) {

        if (((cl != Short.TYPE) && (cl != Byte.TYPE))) {
            throw new IllegalArgumentException("Only byte and short targets supported");
        }

        if (this.dimensions > 4) {
            throw new IllegalStateException("Rank > 4 not supported");
        }

        int elementSize = (cl == Byte.TYPE) ? 1 : 2;

        ByteBuffer buf = allocate(elementSize);

        if (cl == Byte.TYPE) {
            return doByte(buf);
        }

        return doShort(buf);
    }

    ByteBuffer doByte(final ByteBuffer buf) {

        int[] _dim = this.attributeArray.getDimensions();

        byte[] temp = null;

        switch (this.dimensions) {
            case 1:
                short[] data = (short[]) this.data;
                temp = new byte[data.length];
                for (int i = 0; i < data.length; i++) {
                    temp[i] = (byte) data[i];
                }
                buf.put(temp);
                buf.flip();
                return buf;
            case 2:
                short[][] data2 = (short[][]) this.data;
                temp = new byte[_dim[1]];
                for (int i = 0; i < _dim[0]; i++) {
                    short[] di = data2[i];

                    for (int j = 0; j < _dim[1]; j++) {
                        temp[j] = (byte) di[j];
                    }

                    buf.put(temp);
                }
                buf.flip();
                return buf;
            case 3:
                short[][][] data3 = (short[][][]) this.data;
                if (this.rowMajority) {
                    temp = new byte[_dim[2]];

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {
                            short[] di = data3[i][j];

                            for (int k = 0; k < _dim[2]; k++) {
                                temp[k] = (byte) di[k];
                            }

                            buf.put(temp);
                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int k = 0; k < _dim[2]; k++) {

                            for (int j = 0; j < _dim[1]; j++) {
                                buf.put((byte) data3[i][j][k]);
                            }

                        }

                    }

                }
                buf.flip();
                return buf;
            case 4:
                short[][][][] data4 = (short[][][][]) this.data;
                if (this.rowMajority) {
                    temp = new byte[_dim[3]];

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int k = 0; k < _dim[2]; k++) {

                                short[] di = data4[i][j][k];

                                for (int l = 0; l < _dim[3]; l++) {
                                    temp[l] = (byte) di[l];
                                }

                                buf.put(temp);
                            }

                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int l = 0; l < _dim[3]; l++) {

                            for (int k = 0; k < _dim[2]; k++) {

                                for (int j = 0; j < _dim[1]; j++) {
                                    buf.put((byte) data4[i][j][k][l]);
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

    ByteBuffer doShort(final ByteBuffer buf) {

        int[] _dim = this.attributeArray.getDimensions();

        ShortBuffer _buf = buf.asShortBuffer();

        switch (this.dimensions) {

            case 1:
                short[] data1 = (short[]) this.data;
                _buf.put(data1);
                return buf;

            case 2:
                short[][] data2 = (short[][]) this.data;
                for (int i = 0; i < _dim[0]; i++) {
                    _buf.put(data2[i]);
                }
                return buf;
            case 3:
                short[][][] data3 = (short[][][]) this.data;
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
                short[][][][] data4 = (short[][][][]) this.data;

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
