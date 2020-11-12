package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

/**
 * The Class DoubleArray.
 *
 * @author nand
 */
public class DoubleArray extends AArray {

    /**
     * Instantiates a new double array.
     *
     * @param o the o
     */
    public DoubleArray(final Object o) {
        super(o);
    }

    /**
     * Instantiates a new double array.
     *
     * @param o           the o
     * @param rowMajority the row majority
     */
    public DoubleArray(final Object o, final boolean rowMajority) {
        super(o, rowMajority);
    }

    /**
     * create a byte buffer of a compatible type.
     *
     * @param ignore
     */
    @Override
    public ByteBuffer buffer(final Class<?> cl, final int ignore) {

        if (((cl != Double.TYPE) && (cl != Float.TYPE))) {
            throw new IllegalArgumentException("Only float and double targets supported");
        }

        if (this.dimensions > 4) {
            throw new IllegalStateException("Rank > 4 not supported");
        }

        int elementSize = (cl == Float.TYPE) ? 4 : 8;
        ByteBuffer buf = allocate(elementSize);

        if (cl == Float.TYPE) {
            return doFloat(buf);
        }

        return doDouble(buf);
    }

    ByteBuffer doDouble(final ByteBuffer buf) {
        int[] _dim = this.attributeArray.getDimensions();
        DoubleBuffer _buf = buf.asDoubleBuffer();

        switch (this.dimensions) {
            case 1:
                double[] data1 = (double[]) this.data;
                _buf.put(data1);
                return buf;
            case 2:
                double[][] data2 = (double[][]) this.data;
                for (int i = 0; i < _dim[0]; i++) {
                    _buf.put(data2[i]);
                }
                return buf;
            case 3:
                double[][][] data3 = (double[][][]) this.data;
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
                double[][][][] data4 = (double[][][][]) this.data;
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

    ByteBuffer doFloat(final ByteBuffer buf) {
        int[] _dim = this.attributeArray.getDimensions();
        float[] temp = null;
        FloatBuffer _buf = buf.asFloatBuffer();

        switch (this.dimensions) {
            case 1:
                double[] data1 = (double[]) this.data;
                temp = new float[data1.length];
                for (int i = 0; i < data1.length; i++) {
                    temp[i] = (float) data1[i];
                }
                _buf.put(temp);
                return buf;
            case 2:
                double[][] data2 = (double[][]) this.data;
                temp = new float[_dim[1]];
                for (int i = 0; i < _dim[0]; i++) {
                    double[] di = data2[i];

                    for (int j = 0; j < _dim[1]; j++) {
                        temp[j] = (float) di[j];
                    }

                    _buf.put(temp);
                }
                return buf;
            case 3:
                double[][][] data3 = (double[][][]) this.data;
                if (this.rowMajority) {
                    temp = new float[_dim[2]];

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {
                            double[] di = data3[i][j];

                            for (int k = 0; k < _dim[2]; k++) {
                                temp[k] = (float) di[k];
                            }

                            _buf.put(temp);
                        }

                    }

                } else {

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int k = 0; k < _dim[2]; k++) {

                            for (int j = 0; j < _dim[1]; j++) {
                                _buf.put((float) data3[i][j][k]);
                            }

                        }

                    }

                }
                return buf;
            case 4:
                double[][][][] data4 = (double[][][][]) this.data;
                if (this.rowMajority) {
                    temp = new float[_dim[3]];

                    for (int i = 0; i < _dim[0]; i++) {

                        for (int j = 0; j < _dim[1]; j++) {

                            for (int k = 0; k < _dim[2]; k++) {
                                double[] di = data4[i][j][k];

                                for (int l = 0; l < _dim[3]; l++) {
                                    temp[l] = (float) di[l];
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
                                    _buf.put((float) data4[i][j][k][l]);
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
