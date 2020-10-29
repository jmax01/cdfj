package gov.nasa.gsfc.spdf.cdfj;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

/**
 *
 * @author nand
 */
public class FloatArray extends AArray {

    /**
     *
     * @param o
     *
     * @throws Throwable
     */
    public FloatArray(Object o) throws Throwable {
        super(o, true);
    }

    /**
     *
     * @param o
     * @param bln
     *
     * @throws Throwable
     */
    public FloatArray(Object o, boolean majority) throws Throwable {
        super(o, majority);
    }

    /**
     *
     * @return
     */
    @Override
    public Object array() {

        switch (this.dim) {
            case 1:
                return this.o;
            case 2:
                return this.o;
            case 3:
                return this.o;
            case 4:
                return this.o;
        }

        return null;
    }

    /**
     *
     * @param cl
     * @param ignore
     *
     * @return
     *
     * @throws Throwable
     */
    @Override
    public ByteBuffer buffer(Class<?> cl, int ignore) throws Throwable {

        if (cl != Float.TYPE) {
            throw new Throwable("Only float targets supported");
        }

        if (this.dim > 4) {
            throw new Throwable("Rank > 4 not supported");
        }

        ByteBuffer buf = allocate(4);
        int[] _dim = this.aa.getDimensions();
        FloatBuffer _buf = buf.asFloatBuffer();

        switch (this.dim) {
            case 1:
                float[] data = (float[]) this.o;
                _buf.put(data);
                return buf;
            case 2:
                float[][] data2 = (float[][]) this.o;
                for (int i = 0; i < _dim[0]; i++) {
                    _buf.put(data2[i]);
                }
                return buf;
            case 3:
                float[][][] data3 = (float[][][]) this.o;
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
                float[][][][] data4 = (float[][][][]) this.o;
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
        }

        return null;
    }
}
