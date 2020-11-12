package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 * The Class FloatArray.
 *
 * @author nand
 */
public class FloatArray extends AArray {

    /**
     * Instantiates a new float array.
     *
     * @param o the o
     */
    public FloatArray(final Object o) {
        super(o, true);
    }

    /**
     * Instantiates a new float array.
     *
     * @param o        the o
     * @param majority the majority
     */
    public FloatArray(final Object o, final boolean majority) {
        super(o, majority);
    }

    @Override
    public ByteBuffer buffer(final Class<?> cl, final int ignore) {

        if (cl != Float.TYPE) {
            throw new IllegalArgumentException("Only float targets supported was " + cl);
        }

        if (this.dimensions > 4) {
            throw new IllegalArgumentException("Rank > 4 not supported");
        }

        ByteBuffer buf = allocate(4);
        int[] _dim = this.attributeArray.getDimensions();
        FloatBuffer _buf = buf.asFloatBuffer();

        switch (this.dimensions) {
            case 1:
                float[] data1 = (float[]) this.data;
                _buf.put(data1);
                return buf;
            case 2:
                float[][] data2 = (float[][]) this.data;
                for (int i = 0; i < _dim[0]; i++) {
                    _buf.put(data2[i]);
                }
                return buf;
            case 3:
                float[][][] data3 = (float[][][]) this.data;
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
                float[][][][] data4 = (float[][][][]) this.data;
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
}
